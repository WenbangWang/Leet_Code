# Monitoring System Design (Inspired by Meta's Gorilla Philosophy)

**Goal:** Build a monitoring system that can observe service metrics, alert on anomalies, and provide real-time dashboards for a large-scale infrastructure (think tens of thousands of services).

---

## 1. Clarification Questions (Ordered by FR → NFR)

**Functional Requirements (FR):**

1. What types of metrics do we need to monitor?

    * System-level (CPU, memory, disk)
    * Application-level (requests/sec, error rates, latency)
    * Business metrics (user signup, purchases)
2. Do we need real-time alerts or periodic reporting?
3. How long do we need to store metrics?
4. How many services / nodes are expected to be monitored?
5. Do we need anomaly detection or just threshold-based alerts?
6. Should we support dashboards for visualizing metrics?

**Non-Functional Requirements (NFR):**

1. **Scalability:** Must handle millions of metrics per second.
2. **Availability:** Monitoring system must be highly available; loss of monitoring data can be tolerated temporarily, but alerting should be reliable.
3. **Consistency:** Eventual consistency for metrics is acceptable, but alerting requires near-real-time guarantees.
4. **Latency:** Dashboards should update within seconds. Alerts should be triggered within seconds/minutes.
5. **Durability:** Metrics retention policies should balance storage cost vs historical analysis.

---

## 2. Back-of-Envelope Calculation

Assume we monitor **100,000 services**, each emitting **50 metrics per second**.

* Total metrics per second:
  `100,000 * 50 = 5,000,000 metrics/sec`
* Each metric \~100 bytes → 5,000,000 × 100B = **500 MB/sec**
* Storage for 1 day:
  `500 MB/sec * 86,400 sec/day ≈ 43 TB/day`

We need distributed storage and sharding to scale.

---

## 3. Core Data Entities

| Entity       | Description                                                     |
| ------------ | --------------------------------------------------------------- |
| Metric       | `{service_id, metric_name, value, timestamp}`                   |
| AlertRule    | `{service_id, metric_name, threshold, severity}`                |
| Alert        | `{alert_id, service_id, metric_name, value, timestamp, status}` |
| Dashboard    | Visual representation of metrics & alerts                       |
| AnomalyEvent | `{service_id, metric_name, anomaly_type, score, timestamp}`     |

---

## 4. System Interfaces (REST / RPC)

```text
POST /metrics           -> submit metric
GET /metrics            -> query metrics for dashboard
POST /alerts/rule       -> create/update alert rule
GET /alerts             -> query active alerts
POST /alerts/ack        -> acknowledge alert
GET /dashboard          -> retrieve dashboard metrics
POST /anomaly/detect    -> optional anomaly detection trigger
```

---

## 5. Start from Simple Design (ASCII diagram)

```
+-----------+       +-------------+       +--------------+
| Services  | --->  | Metric      | --->  | Storage      |
| & Agents  |       | Aggregator  |       | (Timeseries) |
+-----------+       +-------------+       +--------------+
                          |
                          v
                     +---------+
                     | Alert   |
                     | Engine  |
                     +---------+
                          |
                          v
                     +---------+
                     | Dashboard|
                     +---------+
```

**Explanation:**

* Agents push metrics to a central aggregator.
* Aggregator stores metrics in a time-series DB.
* Alert engine evaluates metrics and triggers alerts.
* Dashboard queries metrics and alerts for visualization.

---

## 6. Enrich the Design (ASCII diagram)

```
+-----------+       +-------------+       +----------------+       +--------------+
| Services  | --->  | Aggregator  | --->  | Distributed     | --->  | Long-term    |
| & Agents  |       | (Shardable) |       | TS Storage      |       | Storage      |
+-----------+       +-------------+       +----------------+       +--------------+
                          |
                          v
                   +---------------+
                   | Alert Engine  |
                   | (Threshold +  |
                   | Anomaly)      |
                   +---------------+
                          |
          +---------------+---------------+
          |                               |
          v                               v
   +-------------+                  +------------+
   | Notification|                  | Dashboard  |
   | System      |                  | UI/Graph   |
   +-------------+                  +------------+
```

**Enhancements in enriched design:**

1. **Sharding aggregator** to handle high metric throughput.
2. **Long-term storage** for historical analysis.
3. **Notification system** (Slack, email, PagerDuty) connected to alerts.
4. **Anomaly detection engine** for dynamic thresholds.

---

## 7. Possible Deep Dives

### 7.1 Data Ingestion & Aggregation

**Problem:** High throughput of metrics (millions/sec).

**Solutions:**

* Use a **sharded aggregator**: partition metrics by service\_id or metric\_name.
* Batch metrics before writing to storage to reduce I/O.
* Use append-only logs (like Kafka or Meta's FOQS) for durability.

**Trade-offs:**

* Batching reduces write overhead but increases latency slightly.
* Sharding increases complexity but scales linearly.

### 7.2 Storage & Compression

**Problem:** Storing high-volume metrics efficiently.

**Disk Block Storage Layout:**

* **Time-series are split into blocks**, e.g., each block contains **1,000 to 10,000 samples**.
* Each block stores **timestamps and values separately (columnar)**.
* Blocks are **immutable** → simplifies concurrency and crash recovery.

```
+----------------------------+
| Metric Block (1k samples)  |
+----------------------------+
| Timestamp Column           |
|  t0, t1, t2, ...          |
+----------------------------+
| Value Column               |
|  v0, v1, v2, ...          |
+----------------------------+
| Metadata                   |
|  metric_id, min/max, sum   |
+----------------------------+
```

* **Blocks are serialized and compressed**, then written sequentially.
* **Segment files** can be memory-mapped.
* **Index files** map `(metric_id, block_start_time) → file_offset`.

**Gorilla-style Compression:**

* **Timestamp Compression (Delta-of-Deltas)**

    * Store differences of timestamps, then differences of differences.
    * Example: `t = [1000,1005,1010,1015]` → delta-of-delta: `[0,0]`.
    * Encoded with variable-length bit encoding.

* **Value Compression**

    * XOR consecutive values to store only changed bits.
    * Example: `v0=10110010, v1=10110011` → XOR=00000001 → store minimal bits.

* **Benefits:**

    * Smooth time-series → delta-of-delta mostly 0.
    * Small value changes → XOR compression highly efficient.
    * Achieves \~10–20x compression over naive storage.

### 7.3 Alerting Engine

**Problem:** Need real-time or near real-time alerting and scale to large volumes of alerts.

**Solutions for Large-Scale Alerts:**

1. **Sharding Alerts**: Partition alert rules by metric type, service, or cluster to distribute load across multiple alert engines.
2. **Hierarchical Alerting**: Aggregate low-level alerts into high-level summaries to prevent alert storms.
3. **Deduplication and Rate-Limiting**: Merge duplicate alerts and throttle repeated alerts per target system to avoid flooding.
4. **Async Notification Pipeline**: Use queues to decouple alert evaluation from delivery (email, Slack, PagerDuty).
5. **Alert Prioritization**: Assign severity levels to route critical alerts first and defer low-priority notifications.
6. **Multi-Tenancy Consideration**: Support independent alerting configurations per team or service without centralized bottlenecks.

**Trade-offs:**

* Sharding and async pipelines add complexity but allow horizontal scalability.
* Deduplication and aggregation reduce noise but may delay some alert visibility.

### 7.4 Dashboard & Query Optimization

**Problem:** Dashboards must be real-time.

**Solutions:**

* Pre-aggregate metrics at multiple granularities.
* Cache frequently accessed queries.
* Async pipelines for heavy computations.

### 7.5 High Availability & Fault Tolerance

**Problem:** Monitoring system itself cannot fail.

**Solutions:**

* Replicate aggregators and storage nodes.
* Leader-election for critical components.
* Durable queues (FOQS/Kafka) to avoid data loss.

### 7.6 Why Use an Agent Instead of Naive Push/Poll

**Naive Polling:** Central server queries each service → bottleneck, fan-out issues.
**Naive Push:** Service directly writes metrics → tight coupling, no batching, risk of data loss.

**Agent Design:**

* Deployed on each host/service node.
* Collects metrics locally, batches and compresses (Gorilla-style).
* Pushes periodically to aggregator.
* Can retry on network failures.

**Benefits:**

1. **Network efficiency** (batching).
2. **Reliability** (buffered metrics).
3. **Decoupling** from storage layer.
4. **Local pre-processing** (delta computation, sampling).
5. **Scalability** (aggregators handle streams from agents, no central bottleneck).
