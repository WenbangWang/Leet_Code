https://www.hellointerview.com/learn/system-design/problem-breakdowns/top-k

# Clarification questions (ordered FR → NFR)

*(I’ll state them as things you’d ask the interviewer — helps scope the system)*

1. Functional (FR) — essential:

    1. Do you need Top-K **globally** or Top-K **per key/partition** (e.g., per user, per region, per category)?
    2. Is the stream **append-only events** (each event is one count for an item) or do events carry increments/decrements?
    3. Do we need Top-K for **time windows** (sliding / tumbling) or just cumulative counts? If windows, what window sizes and slide intervals?
    4. Are queries allowed to return **approximate** results (with error bounds) or must be **exact**?
    5. Expected QPS for queries (read) and events (write)? (If unknown, I’ll assume high throughput).

2. Non-functional (NFR):

    1. Acceptable staleness for results (strict real-time vs seconds/minutes delay)?
    2. Error budget for approximate algorithms (e.g., ±1%, or probabilistic guarantees)?
    3. SLA for query latency (e.g., <50ms) and event ingestion latency (e.g., <1s)?
    4. Cost sensitivity — are we allowed to use large clusters / managed services?
    5. Durability / audit requirements (must keep raw stream for replay or not)?

---

# Functional Requirements (FR)

* Ingest a high-rate stream of events (each event refers to an item id and a delta, typically +1).
* Support API to query top `k` items for:

    * Global time range (e.g., last 1 hour) — sliding/tumbling windows.
    * Optional partitioned top-k (e.g., top k per country).
* Provide results with either exact or configurable approximate guarantees.
* Support frequent queries with low latency (real-time or near real-time).

# Non-Functional Requirements (NFR)

* Low query latency (target: <100ms for cached results).
* High ingestion throughput (target example: 1M events/s — adjustable).
* Scalability: horizontally scalable for both ingestion and query serving.
* Fault tolerance: no single point of failure, ability to recover from node loss with minimal data loss.
* Bounded memory: algorithms must be memory-efficient (for large distinct item sets).
* Optional: support exactly-once semantics for counts (if events can be duplicated).

---

# Back-of-the-Envelope (B.o.E.) calculation

(performing step-by-step arithmetic)

**Assumptions (example)**

* Ingest rate: 1,000,000 events/sec (1e6/s).
* Each event is a single increment (+1).
* Distinct items: 100 million (1e8) possible ids (sparsity).
* We want Top-K where k = 100.
* Window: last 1 hour (3600s).
* Retain raw events for 24 hours in cold storage.

**Raw event storage rate**

* Assume each event stored as 32 bytes (item id, timestamp, metadata).
* Events/sec = 1e6 → bytes/sec = 1e6 \* 32 = 32,000,000 B/s = 32 MB/s.
* Per hour: 32 MB/s \* 3600 s = 115,200 MB ≈ 112.5 GB/hr.
* For 24 hrs: 112.5 GB/hr \* 24 = 2700 GB ≈ 2.7 TB.
  (Arithmetic checked digit-by-digit.)

**Memory for exact counts (not feasible globally)**

* If we store a count per distinct key: assume 16 bytes per key (8-byte id + 8-byte counter) → for 1e8 keys: 1e8 \* 16 = 1.6e9 bytes = 1.6 GB? Wait: compute precisely:

    * 1e8 \* 16 = 1.6e9 bytes = 1,600,000,000 bytes ≈ 1.49 GiB — that's surprisingly small, but in practice hash map overhead multiplies this by 3–4x.
    * With overhead factor 4 → \~6.4 GB. So still possible across shards but single machine unlikely.
* However heavy pointer/hashmap overhead and GC (on JVM) blow this up; realistic exact counting across 100M keys needs distributed storage.

**Memory for approximate algorithm (Space-Saving)**

* Keep top M counters per shard. If we choose M = 1e6 counters (1,000,000), with 16 bytes/counter → 16 MB? Wait compute:

    * 1,000,000 \* 16 = 16,000,000 bytes = 16 MB. That seems too small; in practice overhead larger, but the idea: space-saving allows tiny memory footprint relative to distinct items.

**Network & merge cost**

* If we partition stream into 100 shards: ingest per shard = 1e6 / 100 = 10,000 events/s. That’s low and trivial for a shard.

These numbers show feasibility choices: exact distributed counters are possible but require significant coordination; approximate algorithms are far more memory- and compute-efficient.

---

# Core Data Entities

* **Event**: {event\_id, item\_id, delta, timestamp, partition\_key}
* **PartialCounter**: for each shard/window: {item\_id, count\_estimate, error\_bound} (for approximate methods can include error).
* **TopKSnapshot**: {window\_id, timestamp, items:\[(item\_id, count)]} — precomputed serving result.
* **Config**: {k, window\_size, slide\_interval, error\_tolerance}
* **Audit Log / Raw Stream Storage**: retained raw events in object store for reprocessing.

---

# System Interfaces (APIs)

1. `POST /events` — ingest single event(s) (batched). Body: list of events. Response: ack.
2. `GET /topk?k=100&window=1h&agg=global` — return top-k for specified window/partition.
3. `GET /topk/{partition}?k=10&window=15m` — return partitioned top-k.
4. `GET /stats` — system health / metrics.
5. Admin: `PUT /config` — change k, window, slide.

API-level SLAs: ingestion API should accept bursts and return quickly (async ingestion recommended).

---

# Simple Design (single-node, exact) — when traffic is small or single-machine acceptable

**Idea:** Single process holds an in-memory hashmap of counts and a min-heap size k for top-k. Periodically flush snapshots to disk.

ASCII diagram:

```
CLIENTS
  |
  v
[API Server]
  |
  v
[In-memory hashmap: item -> count]   <-- updates per event
  |
  v
[min-heap (size k)]  <-- maintained from hashmap
  |
  v
[Snapshot Writer] ---> [Disk/Object Store]
```

**Flow**

* On event arrival: increment hashmap count for `item` (O(1)).
* Also maintain min-heap of size k: if item count > heap.min, replace (O(log k)).
* Serve `GET /topk` from min-heap (constant time to read top-k).
* For windowed behavior: use a sliding-window technique — maintain ring buffer of per-second counts or use decrement on expiry (costly for large distinct keys).

**Pros**

* Simple, exact, easy to reason about.
  **Cons**
* Not scalable for high throughput or many distinct items; limited fault tolerance.

---

# Enriched Design (distributed, streaming, approximate + exact hybrid)

Goal: scalable real-time Top-K with windowing and low-latency queries.

Key components:

* Ingestion layer: load balancer → API gateway → Kafka (durable log).
* Stream processors: per-partition stream processors (Flink / Spark Streaming / custom) implement **Space-Saving** or **Count-Min Sketch** + heavy-hitter extraction.
* Shard-level TopKs: each stream processor maintains local Top-M (M >> k) using Space-Saving.
* Aggregator / Merger: periodically (or on query) merge per-shard top lists to produce global top-k. This merge can be run in stateful aggregator jobs.
* Serving layer: results (TopKSnapshot) stored in Redis for low-latency reads and in Cassandra for history.
* Replay: raw events in Kafka + S3/Blob allow recomputing.

ASCII diagram:

```
                 +------------------+
                 |   Clients (QPS)  |
                 +--------+---------+
                          |
                          v
                     [Load Balancer]
                          |
                          v
                     [API Gateway]
                          |
                          v
                       [Kafka Topic]
                    (partitioned by item)
                 /     |     |     \    <-- many partitions
                v      v     v      v
       +--------------+  +--------------+
       | Stream Proc  |  | Stream Proc  |   (Space-Saving / CMS)
       |  (Shard A)   |  |  (Shard B)   |
       +--------------+  +--------------+
           |   \              |   \
(Per-shard TopM)  \          |    \
                   v         v     v
               [Aggregator / Merger (periodic or on-demand)]
                          |
                          v
                   [TopKSnapshot Cache (Redis)]
                          |
            +-------------+------------+
            |                          |
   [Query API / Dashboard]      [Historical Store (Cassandra/S3)]
```

**Detailed components & responsibilities**

* **API Gateway**: accepts events in batches, writes to Kafka for durability.
* **Kafka**: durable, partitioned log. Partition by `hash(item_id)` so same item always goes to same partition → simplifies per-item aggregation.
* **Stream Processors** (stateless or stateful):

    * Consume Kafka partitions.
    * Maintain Space-Saving algorithm with capacity M (M tunable).
    * Emit `local top M` periodically (e.g., every 1s) to Aggregator topic.
    * Maintain windowed counts via keyed state (for exact per-key window counts if needed).
* **Aggregator**:

    * Consumes local top M streams from all shards, merges them to compute global top k (merging complexity \~ (#shards \* M) log k).
    * Writes TopKSnapshot to Redis for fast serving. Also writes to long-term store.
* **Serving/API**:

    * Query `GET /topk` reads from Redis. If miss, triggers on-demand merge from recent local tops (can be async).
* **Storage**:

    * S3 for raw events retention.
    * Cassandra/HBase for historical TopK snapshots and offline analytics.

**Algorithm choices**

* **Space-Saving (SS)**: maintains M counters, good for heavy hitters with bounded error; directly produces top-k candidates.
* **Count-Min Sketch (CMS) + Heap**: low memory, gives estimates with probabilistic error; to get top-k we typically maintain candidate set plus CMS for counts.
* **Exact**: maintained only where feasible — e.g., for small partitions or for offline recompute.

**Consistency and windowing**

* Use event-time windowing with watermarking (supported by Flink/Spark) to manage late events.
* For sliding windows: maintain per-window SS states or use time-buckets and merge.

**Fault tolerance**

* Kafka ensures durability; stream processors use checkpoints (Flink checkpointing) to resume state.
* Aggregator is idempotent — merging top lists is associative/commutative → simplifies retries.

**Serving latency**

* Redis provides sub-10ms reads for top-k snapshots. Aggregation latency depends on merge interval (e.g., 1s–5s).

---

# Merge algorithm (how shards combine)

* Each shard emits top-M list: \[(item, count\_est, error)].
* Aggregator merges by summing counts for same item across shards (note: if using SS, counts are *estimates* with error; merging preserves bounded error).
* Use a min-heap of size k during merge: iterate over candidates (shards \* M), add/update counts, keep top-k.

Complexity: O(S \* M \* log k) where S = #shards. Choose M such that S\*M is manageable (e.g., S=100, M=2k → 20k candidates).

---

# Possible Deep Dives (and expanded explanations)

1. **Algorithms for frequency estimation**

    * **Space-Saving (SS)**:

        * Maintains M counters. On arrival of item:

            * If item exists, increment its counter.
            * Else if free slot exists, add with count 1.
            * Else replace min-counter `m` with new item and set count = m.count + 1, and record error = m.count.
        * Guarantees: for any reported count `c`, true count ≥ `c - error`. Good for heavy hitters and top-k.
        * Memory: O(M). Choose M = α \* k (α between 2–10) depending on skew.
    * **Count-Min Sketch (CMS) + Heap**:

        * CMS stores compact frequency estimates with probabilistic bounds (width, depth -> error, confidence).
        * To get top-k, maintain a candidate heap of potential heavy hitters (e.g., items seen above threshold).
        * CMS alone doesn't return the top-k set; needs candidate list.
    * **Lossy Counting**:

        * Buckets of size `1/ε`. Keeps approximate frequencies with error ≤ ε\*N. Works for streams.
    * **Tradeoffs**:

        * SS directly gives top-k candidates → often preferred.
        * CMS is memory-cheap but needs candidate tracking.

2. **Data partitioning & keying**

    * Partition by `hash(item_id)` so same item always routed to same Kafka partition and stream processor.
    * If partitioned by user/region, merging strategy must know which dimension(s) to aggregate by.
    * Hot keys: skewed items (super popular) will overload one partition. Mitigation:

        * Use *denser partitioning* for hot keys (virtual nodes), replicate hot-key processing across multiple workers with partial aggregation and consistent split keys (e.g., item\_id\_shard = hash(item\_id) % Nshards but use second-level partitioning for hot items).
        * Use asymmetric processing: hot keys processed specially with dedicated resources.

3. **Windowing (sliding vs tumbling)**

    * **Tumbling**: disjoint fixed windows. Simple to compute; snapshots at window boundaries.
    * **Sliding**: overlapping windows (e.g., 5m window sliding every 1m). More expensive; can implement via incremental updates using per-bucket counts (e.g., maintain per-second buckets and sum appropriate buckets).
    * Implementation choices:

        * Keep small time buckets (e.g., 1s granularity) per item in state — too heavy for large cardinality.
        * Use *time-decayed* counters (exponential decay) for approximate recency without strict windows.
        * For SS-based windows: maintain M counters per bucket and merge required buckets to produce top-k; tradeoff memory vs latency.

4. **Exactly-once and duplicates**

    * If events can be duplicated, counts will be inflated. To handle this:

        * Use idempotent writes at ingestion (dedupe by event\_id) — expensive at scale.
        * Use Kafka with exactly-once semantics + Flink checkpointing to achieve end-to-end exactly-once processing.
        * If approximate counts are acceptable, accept possible small duplication and set error budget.

5. **Merging partial top-k (accuracy considerations)**

    * Merging top lists from shards requires that per-shard top M contains all global top-k items. Choose M large enough (often M = factor \* k). Risk: if shards spread an item’s counts across many shards, it might not appear in per-shard top M but still be globally top-k. Mitigation:

        * Use hashing by item so item always in single shard (best).
        * If using multiple partitions per item for throughput, need higher M or a second pass (rare items aggregated).

6. **Serving & low-latency queries**

    * Materialize precomputed top-k in Redis for each window and partition (TTL = slide interval).
    * For ad-hoc queries (different k/window), either compute on-the-fly by merging per-shard cached small tops or fallback to offline compute (slower).
    * Consider using in-memory vector/compact list for quick transport.

7. **Hot keys and load skew**

    * Hotkey detection: monitor per-shard throughput; if a single item dominates, route it to a dedicated processor.
    * Use *consistent hashing with replication* or *split keys* (hash(item\_id) + salt) then aggregate partials.

8. **Monitoring & alerting**

    * Track ingestion lag, Kafka consumer lag, processing latency, error rates, memory usage, merge latency.
    * Alert if number of unique keys spikes (possible attack) or if merge misses items.

9. **Testing & correctness**

    * Unit test algorithms with synthetic streams (Zipfian distributions to simulate skew).
    * Load test with replay from production traces.
    * Property-based tests: merging invariants, error bounds.

10. **Cost vs accuracy tradeoffs**

    * Larger M (more memory) → better accuracy & lower false negatives; higher cost.
    * Shorter merge intervals → fresher data but more CPU and network overhead.

11. **Security and privacy**

    * If items are sensitive (e.g., user ids), apply hashing/aggregation or differential privacy for public results.

12. **Operational concerns**

    * Rolling upgrades: use Kafka snapshot/offsets and stream processor checkpointing to resume.
    * Backpressure: if downstream aggregator is slow, allow local buffering; throttle ingestion or drop low-priority events if needed.

---

# Example interview talk-track (how to present)

1. Clarify: ask about exact vs approximate, windows, scale. State assumptions.
2. Offer simple single-node exact solution for small scale (hashmap + min-heap).
3. Explain scaling problems and present distributed streaming design (Kafka + Flink + Space-Saving + aggregator + Redis).
4. Discuss algorithm choice and give reasons for Space-Saving (memory efficient and directly produces top-k).
5. Walk through merge logic, correctness, error bounds.
6. Cover NFRs: latency, fault-tolerance, monitoring, hotkeys, replay.
7. Finish with tradeoffs & alternatives (CMS if memory extremely tight, exact if strong guarantees required).

---

# Short checklist & configuration knobs you’ll mention in interview

* Partitioning key (important): hash(item\_id) to Kafka partition.
* Shard top M (choose M = 5–10 \* k as starting point).
* Merge frequency: 1s or 5s depending on freshness need.
* Storage: Redis for serving, S3 for raw events, Cassandra for snapshots.
* Algorithm: Space-Saving for production; CMS as alternative for strict memory limits.
* Windowing strategy: tumbling for simplicity; sliding with small buckets for accuracy.

