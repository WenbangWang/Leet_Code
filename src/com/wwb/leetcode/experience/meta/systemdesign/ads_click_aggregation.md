https://www.hellointerview.com/learn/system-design/problem-breakdowns/ad-click-aggregator


# Ads Click Aggregator System Design

### Clarification Questions (Ordered by FR → NFR)

#### Functional Requirements (FR)

1. Should the system support both web and mobile SDKs from day one?
2. Do we need to support real-time deduplication at ingestion, or can we allow some duplicates and clean them up later in batch?
3. What dimensions of aggregation are required initially (e.g., ad_id, campaign_id, publisher_id, geo, device)?
4. What latency is acceptable for showing aggregates on dashboards (seconds, minutes)?
5. Do we need to support replay of all historic raw clicks or just within the retention window?
6. Should the system expose aggregates via pull APIs, push notifications, or both?
7. Do we need to support custom aggregation queries (ad-hoc) or only predefined aggregations?
8. What is the required retention period for raw click events vs aggregated data?
9. Should we enforce exactly-once delivery to downstream consumers, or is eventual reconciliation sufficient?
10. Are there requirements for fraud detection or filtering of invalid clicks at ingestion time?
11. Do we need to support multi-tenancy (isolation across advertisers/publishers)?
12. Should dashboards support drill-down from aggregate to raw events?

#### Non-Functional Requirements (NFR)

1. What is the expected QPS at launch, and what peak traffic multipliers should we design for?
2. What availability SLO/SLAs are required for ingestion and query APIs?
3. What durability guarantees are required — is “at least once” ingestion acceptable if we reconcile with batch, or must it be strictly “exactly once”?
4. What is the maximum acceptable cost for raw storage and aggregation infrastructure?
5. Do we need multi-region active-active availability, or is active-passive failover sufficient?
6. What security requirements exist (encryption in transit, encryption at rest, token-based auth)?
7. Are there regulatory requirements for data privacy (e.g., GDPR, CCPA) that impact click storage?
8. How much backfill/replay lag is acceptable when reprocessing raw data?
9. What is the maximum acceptable query latency for dashboards and APIs?
10. Should the system support elasticity (auto-scaling) during traffic spikes?
11. Do we need observability standards like structured logs, metrics, and distributed tracing out-of-the-box?
12. Should the system tolerate partial degradation (graceful fallback) under extreme load, e.g., drop raw storage but keep aggregates available?


## FR (Functional Requirements)

1. **Collect** every ad click event from ad-serving clients (web/mobile/SDKs).
2. **Ingest** clicks reliably (no data loss) and acknowledge quickly to clients.
3. **Aggregate** clicks by various dimensions (ad\_id, campaign\_id, publisher\_id, geo, time window) in near-real-time and in batch.
4. **Query/Expose** aggregated metrics (e.g., last-1m, last-1h, day) via APIs for dashboards and billing.
5. **Reprocess/replay** historic raw clicks to recompute aggregates after bug fixes or schema changes.
6. **Deduplicate** clicks (duplicate client retries, network retries).
7. **Retention**: keep raw events for configurable retention (e.g., 30–90 days) and aggregated metrics longer.

## NFR (Non-Functional Requirements)

* **Durability**: No-loss ingestion (SLO: >= 99.999% durability of accepted clicks).
* **Availability**: Ingest APIs 99.99% available.
* **Latency**: Near-real-time aggregates visible within 5–30s for streaming paths; dashboards can tolerate slower (minutes) for heavy aggregation.
* **Throughput & Scalability**: System must scale linearly across producers/partitions.
* **Consistency**: Aggregates should be **eventually consistent**; streaming path aims for low-latency approximate counts with eventual exactness after batch reconciliation.
* **Cost**: Keep storage & compute cost-efficient (use TTLs, tier raw events to cheaper blob storage after window).
* **Security & Privacy**: access control on query APIs; PII redaction if present.
* **Observability**: metrics for ingestion rate, lag, consumer offsets, failed events, processing latency.

## Back-of-the-Envelope (BOTE) calculation

* Clicks/day = **100,000,000**
* Seconds/day = 86,400
* Average QPS ≈ 1,157; Peak QPS ≈ 3,471
* Event size ≈ 200B → 20GB/day → 600GB for 30-day retention
* Kafka replication factor 3 → throughput ≈ 60GB/day; average ≈ 0.22 MB/s, peak ≈ 0.66 MB/s
* At 1B clicks/day, multiply all numbers ×10

## Core Data Entities

1. **ClickEvent (raw)**: event\_id, ad\_id, campaign\_id, publisher\_id, user\_id, timestamp, device/geo, dedupe\_token, metadata.
2. **AggregatedMetric**: agg\_id, count, uniques, window\_start\_ts, window\_end\_ts, last\_updated\_ts.
3. **IngestionCheckpoint**: topic, partition, offset, processed\_ts.
4. **Metadata / SchemaRegistry**: schema versions, transformations.

## System Interfaces

* **/ingest/v1/click** — Accepts ClickEvent; returns ack.
* **Producer SDK** — batching, retry with dedupe\_token.
* **Aggregation Query API** — query aggregates by ad\_id + window.
* **Raw Event Query / Replay API** — fetch/reprocess raw clicks.
* **Admin APIs** — retention, schema updates.

## Simple Design (ASCII Diagram)

```
Clients (SDKs)
     |
     v
+-------------+
|  Ingest API |
+-------------+
     |
     v
+-------------+
|   Kafka     |  <-- durable log
+-------------+
   |       |
   v       v
Batch   Raw Store (S3/GCS)
Job     (for replay/reprocess)
   |
   v
+----------------+
| Aggregates DB  |
+----------------+
     |
     v
+----------------+
|   Query API    |
+----------------+
```

## Enriched Design (ASCII Diagram)

```
Clients (SDKs)
     |
     v
+-------------+
|  Ingest API |
+-------------+
     |
     v
+-------------+
|   Kafka     |  <-- partitioned by ad_id
+-------------+
   |       |         
   |       |         
   |   +-----------------------+
   |   | Stream Processor      |
   |   | (Flink/Beam)          |
   |   | - windowed agg        |
   |   | - dedup via token     |
   |   +-----------------------+
   |              |
   |              v
   |     +-----------------+
   |     | Realtime Store  | <-- OLAP/NoSQL
   |     +-----------------+
   |              |
   |              v
   |        +-------------+
   |        |  Query API  |
   |        +-------------+
   |
   v
+-------------------+
|  Raw Store (S3)   |
|  (immutable logs) |
+-------------------+
   |
   v
+-------------------+
| Batch Job (Spark) |
+-------------------+
   |
   v
+-------------------+
| Truth Store (OLAP)|
+-------------------+
   |
   v
+-------------------+
| Reconciliation    |
| (fix diffs)       |
+-------------------+
```

## Expanded Possible Deep Dives

### 1. Partitioning Strategy

* Distribute load evenly, avoid hot spots.
* Primary partition key: `ad_id` hashed to Kafka partitions.
* Hot keys: `(ad_id, sub-shard)` hashing.
* Trade-offs: too few partitions → hot spots; too many → overhead.

### 2. Exactly-once Semantics

* Avoid double-counting.
* Techniques: Kafka transactions + idempotent producers, Flink checkpoints, idempotent writes to sink.
* Trade-offs: complex, adds latency; eventual consistency + reconciliation often sufficient.

### 3. Deduplication

* Remove duplicate clicks.
* Use client dedupe token, server dedupe index (Redis/RocksDB), fallback heuristics.
* Bloom filters for approximate dedupe.
* Trade-offs: memory usage, false positives.

### 4. Event-time vs Processing-time

* Use event timestamps and watermarks.
* Allow lateness windows (e.g., 2 min). Late events → side outputs or batch reconciliation.
* Trade-offs: longer window → higher state/memory; shorter → undercount risk.

### 5. Storage Schema for Aggregates

* Wide-row per ad: fast sliding window queries, sparse data waste.
* Time-series row `(ad_id, window_start)`: simple, horizontally scalable.
* Implementation: OLAP (ClickHouse, Druid) or NoSQL (Cassandra/HBase).

### 6. Cost Optimization

* Tiered raw storage: hot (recent) / cold (archive).
* Compression: gzip/snappy/parquet.
* Pre-aggregate metrics to reduce query cost.
* Trade-offs: cheaper storage → higher retrieval latency for old events.

### 7. Reconciliation Algorithm

* Correct streaming aggregates using batch truth.
* Steps: batch aggregates → compare → compensating updates to Realtime Store.
* Idempotent updates required.
* Frequency: hourly/daily.

### 8. Query API & Caching

* API: aggregates by ad, campaign, geo, time window.
* Cache recent aggregates in Redis/CDN; precompute frequent queries.
* Trade-offs: aggressive caching → stale data unless invalidated by reconciliation.

### 9. Fraud / Bot Detection

* Detect abnormal click rates, click-farms.
* Filter events or annotate in raw store.
* Trade-offs: aggressive filtering → false negatives; delayed detection → temporary metric inflation.

### 10. Multi-region / Disaster Recovery

* Cross-region Kafka replication (active-passive or active-active).
* Reconciliation using global sequence numbers or timestamps.
* Trade-offs: latency, conflict resolution complexity.

### 11. Testing & Chaos Engineering

* Unit tests: schema validation, dedupe logic.
* Integration tests: ingestion → aggregation → query.
* Chaos testing: consumer failures, rebalances, network failures.
* Replay tests: rebuild aggregates from raw events.
