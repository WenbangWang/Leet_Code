https://pyemma.github.io/How-to-Design-Webhook/

I. **Clarification questions (ordered FR → NFR)**
*(I’ll make reasonable assumptions below; if any are wrong, tell me and I’ll adapt.)*

Functional (FR)

1. Which delivery guarantee do we need for the system owner → consumer? (At-least-once by default; exactly-once is rare / costly.)
2. Do consumers need event filtering/server-side (i.e., subscribe to subsets of events) or do they always receive every event?
3. Do we support custom retry policies per consumer (e.g., TTL, backoff) or a global policy?
4. Must the webhook provider store/retain payloads for replay (e.g., 30 days)?
5. Do we need a UI for customers to register/update endpoints and view delivery logs/errors?
6. Do we need event ordering guarantees? (FIFO per endpoint, or can events be delivered out-of-order?)
7. How do we validate webhook URLs to prevent SSRF attacks? (Reject private IPs, localhost, cloud metadata endpoints?)
8. Do customers need reconciliation/audit APIs to verify all events were delivered?

Non-functional (NFR)

1. Expected peak QPS and acceptable p95/p99 latency for accepting an event and acknowleging enqueue.
2. SLO/SLA for delivery success rate (e.g., 99.9% within 24h).
3. Security constraints: must we support secret rotation, IP allowlists, or mutual TLS?
4. Durability: how long to keep events / DLQ / audit logs?
5. Multi-region / geo-redundancy requirements?

---

II. **Functional requirements (FR)** — condensed

* Register/manage webhook endpoints: create/update/delete, event filters, metadata (secret, retry policy).
* Accept events (ingest API) from internal producers / services.
* Fan-out event deliveries to registered endpoints (HTTP POST).
* Fast acknowledgement to producers (queue ingestion path).
* Retry logic with exponential backoff and DLQ on exhaust.
* Idempotency: detect duplicate deliveries and allow safe reprocessing.
* Security: HTTPS-only, HMAC signatures, timestamp windows, optional mTLS, secret rotation.
* URL validation: prevent SSRF attacks by validating webhook URLs (reject private IPs, localhost, cloud metadata endpoints).
* Event ordering: optionally guarantee FIFO delivery per endpoint or per ordering key (e.g., user_id).
* Reconciliation API: allow customers to query all events and delivery status for audit/compliance.
* Observability & admin UI: delivery logs, retry counts, metrics, manual replay.
* Access control & audit logs for endpoint changes.

(References: PyEmma design notes; GitHub/Stripe best-practices on verification and short ack-windows.) ([Coding Monkey][1])

---

III. **Non-functional requirements (NFR)**

* Throughput: handle bursty traffic (e.g., 10k events/minute per customer, aggregate bursts to 100k+/min).
* Latency: ack producer within <100–500ms (fast enqueue); delivery to endpoints is asynchronous. GitHub recommends responding within ~10s for immediate processing. ([GitHub Docs][2])
* Durability: persist events until successfully delivered or TTL expiry (configurable; e.g., 7–30 days).
* Availability: 99.9% ingestion availability; cross-AZ/region failover.
* Scalability: horizontal scaling of workers and queues.
* Security: TLS, HMAC SHA-256 signing, timestamp check, replay protection. (Most providers use HMAC SHA256.) ([ngrok.com][3])
* **SLO/SLA Definitions:**
    * **Primary SLO**: 99.9% of events delivered within 5 minutes to healthy endpoints (error budget: 43 min/month).
    * **Ingestion SLO**: p95 ack latency < 500ms.
    * **Queue Health SLO**: retry queue depth < 10k per endpoint (alert at 10k, page at 50k).
    * **DLQ Growth SLO**: DLQ growth rate < 1% of total events (warn at 1%, alert at 5%).

---

IV. **Back-of-Envelope (BoE) calculations**
Assumptions (adjustable): 1M active users; 5% produce events per minute → 50k events/minute = ~833 events/sec. Peak burst: 10× baseline = 8,330 EPS.

Producer side:

* If each event payload ~5 KB, baseline traffic = 833 * 5KB ≈ 4.2 MB/s. Peak ~42 MB/s.

Queueing:

* If average endpoint count per event = fan-out to 5 endpoints → effective delivery attempts/sec = 833 * 5 = 4,165 attempts/sec baseline, peak ~41,650 attempts/sec.
* If retry policy attempts up to 6 times with exponential backoff, worst-case inflight attempts can multiply; ensure queue and DLQ capacity for multiplicative retries.

Storage:

* **Event payloads (S3)**: Retain raw payloads for 7 days. Daily raw bytes baseline = 833 events/sec * 5KB * 86400 ≈ 360 GB/day. *Important: fan-out does not require duplicating stored payloads; store once and reference ID for delivery attempts.*
* **DeliveryAttempt table (current state)**: One row per event-endpoint pair = 4,165 new rows/sec. Each row ~500 bytes. Daily: 4,165 * 500B * 86400 ≈ 180 GB/day. But only keeps current state (one row per delivery), so steady-state size depends on delivery completion rate. If p95 delivery time is 5 min, ~1.25M rows in table at any time ≈ 625 MB (manageable).
* **DeliveryAttemptHistory table (audit trail)**: This is the hot write path. Assumptions:
    * 95% of deliveries succeed on first attempt (1 history row)
    * 4% require 2-3 attempts (avg 2.5 rows)
    * 1% require 4-6 attempts (avg 5 rows)
    * Average history rows per delivery = 0.95 * 1 + 0.04 * 2.5 + 0.01 * 5 = 1.1 rows/delivery
    * History writes/sec = 4,165 deliveries/sec * 1.1 ≈ 4,582 writes/sec baseline, peak ~45,820 writes/sec
    * Each history row ~1 KB (response_code, error_message, response_body_excerpt, timestamps). Daily: 4,582 * 1KB * 86400 ≈ 396 GB/day
    * Retention: 30 days → ~11.8 TB total. Archive to S3 and drop old partitions. Compressed archive ~2-3 TB.
* **Database write load**: 4,165 current-state UPDATEs/sec + 4,582 history INSERTs/sec ≈ 8,747 writes/sec baseline. Partition both tables by time to distribute load. Use write-optimized DB (e.g., Postgres with partitioning, or Cassandra).

Worker sizing:

* If a single delivery worker can do ~50 HTTP deliveries/sec (including TLS handshake + network), to handle 4,165 attempts/sec we need ~84 workers baseline; for peak 41,650 attempts/sec → ~833 workers. Use autoscaling groups / serverless workers (e.g., Fargate, lambdas with concurrency, or Kubernetes) with per-worker throughput assumptions.

Monitoring:

* Track delivery queue depth, attempts/sec, p95/p99 delivery latency, % 2xx success.
* Track DB write throughput: current-state UPDATEs/sec, history INSERTs/sec, partition lag.
* Alert if history table write lag > 5 seconds (indicates DB bottleneck).

(These numbers are illustrative; adapt based on real expected customer behavior.)

**Key insight for interview**: The DeliveryAttemptHistory table is write-heavy (~4.6k writes/sec baseline, ~46k peak) but append-only, making it perfect for time-based partitioning. Old data can be archived cheaply to S3, keeping hot dataset small. The current-state table stays small because successful deliveries are effectively "deleted" from the hot query path.

---

V. **Core Data Entities**

1. **WebhookEndpoint**

    * id (UUID)
    * owner_id (customer)
    * url (HTTPS)
    * secret (rotatable)
    * events_subscribed (list or filter expression)
    * retry_policy (initial_delay, max_attempts, backoff_factor, jitter)
    * active (bool)
    * created_at, updated_at
    * **Indexes**: (owner_id, active), (owner_id, created_at), UNIQUE(url) optional

2. **Event**

    * id (UUID)
    * type (string)
    * payload_location (object storage ref) OR payload (nullable)
    * created_at, source_service
    * **Indexes**: (created_at), (type, created_at), (source_service, created_at)

3. **DeliveryAttempt** (HOT TABLE - partition by created_at monthly)

    * id (UUID)
    * event_id
    * endpoint_id
    * current_attempt_number
    * status (pending/sent/success/failure/dlq)
    * next_retry_at
    * created_at, updated_at
    * **Primary Key**: (id, created_at) for partition support
    * **Indexes**: 
        * (endpoint_id, status, next_retry_at) — scheduler query
        * (event_id, endpoint_id) — lookup by event
        * (status, next_retry_at) WHERE status='pending' — partial index for ready-to-retry
        * (endpoint_id, created_at DESC) — customer UI queries
    * **Note**: This is the "current state" table (SCD Type 2 main table)

4. **DeliveryAttemptHistory** (SCD Type 2 history table - partition by attempt_timestamp monthly)

    * id (UUID) — history record ID
    * delivery_attempt_id — FK to DeliveryAttempt
    * attempt_number
    * status (sent/success/failure/timeout)
    * response_code (HTTP status)
    * response_body_excerpt (first 500 chars)
    * error_message
    * attempt_timestamp
    * attempt_duration_ms
    * **Primary Key**: (id, attempt_timestamp) for partition support
    * **Indexes**:
        * (delivery_attempt_id, attempt_number) — query all attempts for a delivery
        * (delivery_attempt_id, attempt_timestamp DESC) — latest attempts first
    * **Retention**: Keep for 30 days, then archive to cold storage (S3)
    * **Pattern**: Insert-only (no updates), one row per attempt

5. **DLQEntry**

    * delivery_attempt_id, event_id, endpoint_id, failure_reason, stored_payload_ref, moved_to_dlq_at
6. **IdempotencyStore / ProcessedEvents**

    * provider_event_id or delivery_signature, endpoint_id → processed_at
7. **AuditLog**

    * user, action, object, timestamp, before/after
8. **Metrics & Traces** (time-series)

---

VI. **System Interfaces (APIs)**

Producer-facing:

* `POST /v1/events` — accept event(s). Body: {type, payload, metadata}. Returns event_id(s). (Fast ack after persist to queue.)
* `GET /v1/events/{id}` — fetch stored event (admin/internal).

Admin / customer:

* `POST /v1/endpoints` — create endpoint (url, secret, filters, retry policy).
* `GET /v1/endpoints/{id}` — view endpoint, test-send.
* `PUT /v1/endpoints/{id}` — update (rotate secret).
* `POST /v1/endpoints/{id}/test` — send test payload.
* `POST /v1/endpoints/{id}/validate` — validate URL (check DNS, reachability, no SSRF).
* `POST /v1/endpoints/{id}/pause` — pause webhook without deleting.
* `POST /v1/endpoints/{id}/resume` — resume paused webhook.
* `GET /v1/deliveries?endpoint_id=&status=&from=&to=` — list deliveries / errors (returns current state).
* `GET /v1/deliveries/{delivery_id}/attempts` — list all attempts with history (from DeliveryAttemptHistory table).
* `POST /v1/deliveries/{delivery_id}/replay` — manual replay.

Reconciliation / Audit:

* `GET /v1/events?from=timestamp&to=timestamp&type=event_type` — query events with delivery status per endpoint.
* `POST /v1/reconciliation/reports` — generate batch reconciliation report (async, returns report_id).
* `GET /v1/reconciliation/reports/{report_id}` — get report status and download URL (CSV).
* `GET /v1/events/stream?endpoint_id=&from_seq=N` — Server-Sent Events stream for real-time reconciliation.

Internal:

* `enqueueDelivery(event_id, endpoint_id)` — internal queue producer.
* `deliverNow(delivery_attempt_id)` — worker call to perform HTTP POST.
* `markDeliveryResult(delivery_attempt_id, result)` — record result, schedule retry/ DLQ.

---

VII. **Start from Simple Design (ASCII diagram)**

Simple: single producer → webhook service (store + queue) → single delivery worker → endpoint.

```
Producer                Webhook Service                  Consumer (endpoint)
   |                         |                                   |
   |---POST /v1/events------>|                                   |
   |                         |--persist event (DB/object)-------->|
   |                         |--enqueue delivery jobs------------>|
   |                         |                                   |
   |                         |---worker pulls job--------------->|
   |                         |---HTTP POST --> endpoint --------->|
   |                         |<--2xx / 5xx------------------------|
```

Key idea: ACK producer quickly after persist + enqueue; delivery is asynchronous.

(Based on simple flow practiced by providers; see PyEmma and GitHub recommendations.) ([Coding Monkey][1])

---

VIII. **Enrich the Design (scale / reliability / security) — ASCII diagram**

Add components: API gateway, auth, event store (object store), fanout queue, delivery workers (stateless), rate limiter per endpoint, backoff scheduler, DLQ, retry controller, dashboard, HSM/secret service.

```
                           +-------------------------+
                           |   API Gateway / LB     |
                           +-----------+-------------+
                                       |
                           +-----------v-------------+
                           |  Ingest Service (stateless) -- validates & signs --> Auth
                           +-----------+-------------+
                                       |
           +---------------------------+---------------------------+
           |                           |                           |
+----------v---------+      +----------v---------+       +---------v--------+
| Metadata DB (SQL)  |      |  Event Store (S3)  |       |  Fanout Queue     |
| (endpoints, config)|      |  (payload objects)  |       | (e.g., Kafka/SQS) |
+----------+---------+      +----------+---------+       +---------+--------+
           |                             |                           |
           |                             +------(ref)--------------- |
           |                                                     |
           +-----------------------+-----------------------------+
                                   |
                            +------v-------+
                            |  Scheduler / |
                            |  Backoff     |  <-- schedules retry times, moves to DLQ
                            +------+-------+
                                   |
                           +-------v--------+
                           | Delivery Workers|
                           | (stateless, autoscale)
                           +------+-----------+
                                  |  |  |
          +-----------------------+  |  +----------------------+
          |                          |                         |
+---------v------+          +--------v-------+         +-------v-------+
| Rate limiter   |          | Idempotency    |         | DLQ / Replay  |
| per endpoint   |          | store (Redis/C)|         | UI            |
+----------------+          +----------------+         +---------------+
                                  |
                             Observability
                        (metrics, tracing, logs)
```

Key details:

* Ingest Service validates schema & enqueues job quickly (fast ACK).
* Event payload stored once in durable object store; fan-out uses payload reference.
* Fanout Queue holds per-endpoint delivery attempts; scheduler drives next attempt time.
* Delivery Workers handle HTTP POST, verify response, update DeliveryAttempt state.
* Idempotency store keyed by (event_id, endpoint_id) prevents duplicate side-effects.
* DLQ for exhausted attempts. Manual replay from UI.

(Scaling patterns and DLQ described in industry writeups.) ([Medium][4])

---

IX. **Operations and Reliability patterns (practical details)**

1. **Idempotency**

    * Use unique delivery IDs included in headers (e.g., `X-Delivery-ID`) so consumers can deduplicate. On provider side, keep processed delivery keys in a small TTL store (Redis/DynamoDB) to avoid duplicate processing. Stripe/GitHub patterns recommend using delivery IDs. ([docs.stripe.com][5])

2. **Signing & security**

    * Sign each delivery body with HMAC SHA-256 using per-endpoint secret; include timestamp header and signature header. Reject signatures outside a narrow time window to mitigate replay attacks. Support secret rotation. (Most modern providers follow this.) ([ngrok.com][3])

3. **Retries / backoff**

    * Use exponential backoff + jitter for retries. After N attempts (configurable), move to DLQ and surface to UI for manual replay. Provide per-endpoint override. (Common practice.) ([Medium][4])

4. **Throughput & burst handling**

    * Decouple ingestion & delivery with durable queue(s). Use partitioning (by endpoint id hash) to limit head-of-line blocking. Autoscale workers on queue depth and processing rates.

5. **Per-endpoint rate limiting / circuit-breakers**

    * Track health of endpoints; if an endpoint is failing consistently, pause automatic retries and notify customer (circuit open). Optionally exponential backoff of entire endpoint to avoid hammering.

6. **Observability & replay**

    * Store delivery logs (response codes, durations) cheaply for 30 days and full payloads for configured retention. Provide metrics: delivered %, p95 latency, retry counts, DLQ size.

7. **Data model: SCD Type 2 for delivery tracking**

    * **Pattern**: Use two tables to separate hot operational data from cold historical data
    * **Workflow example** (retry scenario):
        1. Initial delivery attempt: INSERT into `DeliveryAttempt` (status=pending), no history yet
        2. Worker picks up delivery: UPDATE status to 'sent', INSERT into `DeliveryAttemptHistory` (attempt 1, status=sent, timestamp=T1)
        3. Endpoint returns 500: UPDATE DeliveryAttempt (status=pending, next_retry_at=T1+60s, attempt_number=1), UPDATE history record (status=failure, response_code=500)
        4. Scheduler triggers retry: UPDATE status to 'sent', INSERT new history row (attempt 2, status=sent, timestamp=T2)
        5. Success: UPDATE DeliveryAttempt (status=success), UPDATE last history row (status=success, response_code=200)
    * **Why this works**: DeliveryAttempt table stays small (one row per event-endpoint pair), scheduler queries are fast (only scan pending rows), history table captures full audit trail for debugging
    * **Cleanup**: Archive DeliveryAttemptHistory older than 30 days to S3, drop old partitions

8. **Testing & local dev**

    * Provide test endpoints and `test-send` API; allow customers to see sample payloads and signing.

9. **Circuit breaker detailed state machine**

    * **States**: Closed (normal), Open (failing), Half-Open (testing recovery)
    * **Closed → Open**: After 5 consecutive failures OR success rate < 50% over last 20 attempts
    * **Open → Half-Open**: Wait 60 seconds (exponential backoff: 60s, 120s, 240s, cap at 600s)
    * **Half-Open → Closed**: If test request succeeds
    * **Half-Open → Open**: If test request fails, double the wait time
    * **Metrics per endpoint**: track consecutive failures, success rate (sliding window), last success timestamp
    * **Notification**: alert customer when circuit opens; auto-resume when closed

10. **SSRF protection and URL validation**

    * **Registration time validation**:
        * Parse URL, enforce HTTPS only (reject HTTP)
        * Resolve DNS, check resolved IP against blocklist (RFC1918 private IPs: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)
        * Reject localhost (127.0.0.0/8, ::1), link-local (169.254.0.0/16), cloud metadata (169.254.169.254)
        * Send test delivery, follow redirects (max 2), re-validate IPs after each redirect
    * **Runtime protection**:
        * Delivery workers run in isolated network segment with egress firewall
        * All deliveries go through egress proxy with IP filtering
        * Connection timeout: 5s, read timeout: 30s, total timeout: 60s
        * Reject self-signed certificates (configurable per endpoint)
    * **Audit**: log all URL validation failures and blocked attempts

11. **Event ordering strategies**

    * **Option A - No ordering guarantee** (default): maximum throughput, events delivered ASAP, include timestamp + sequence number in payload for client-side reordering
    * **Option B - Per-endpoint FIFO**: partition queue by endpoint_id, single worker per partition, guarantees order but may cause head-of-line blocking
    * **Option C - Ordering groups** (recommended): events tagged with `ordering_key` (e.g., user_id), only events with same key ordered relative to each other
    * **Implementation**: use Kafka partitions keyed by ordering_key or SQS FIFO queues with MessageGroupId = ordering_key
    * **Trade-off**: strong ordering reduces throughput; choose based on customer needs

12. **SLO/SLA monitoring and alerting**

    * **Dashboard hierarchy**:
        * Page 1 (Health Overview): delivery success rate (24h rolling), events ingested/hour, p50/p95/p99 time-to-delivery, active endpoints count
        * Page 2 (Queue Health): pending queue depth, retry queue by attempt number, DLQ size and growth rate, worker utilization %
        * Page 3 (Per-Endpoint): top 10 failing endpoints, top 10 slowest endpoints, circuit breaker state distribution, delivery latency histogram
    * **Alerting levels**:
        * **Page** (immediate): SLO violation (delivery success < 99.9% for 10 min), primary queue depth > 100k, all workers down
        * **Urgent** (15 min): queue depth > 50k, DLQ growth > 5%, p95 latency > 2x baseline
        * **Warn** (next day): DLQ growth > 1%, circuit breakers open > 10% of endpoints
        * **Info** (dashboard): trending toward capacity limits
    * **SLI calculations**:
        * Delivery success rate = (events_delivered_success / total_events_to_healthy_endpoints) over 5-min windows
        * Time-to-delivery = event_created_at to delivery_success_at (p50/p95/p99)
        * Queue health = current_depth / capacity_limit

---

X. **Possible Deep Dives (and expanded notes)**

1. **Exactly-once vs At-least-once delivery**

    * *What to discuss:* Tradeoffs: exactly-once requires distributed transactions / two-phase commit or dedupe at consumer with idempotency tokens + reconciliation; most providers choose at-least-once and require idempotency. Show approaches: de-dup store, event nonces, idempotent upserts. ([Amazon Web Services, Inc.][6])

2. **Fan-out architecture choices**

    * *What to discuss:* Push (HTTP delivery) vs. event streaming (pub/sub). Fan-out using a durable queue such as Kafka or SQS with reference pointers to payload is typical. Partitioning strategy: partition by endpoint or by event type; each has pros/cons (ordering vs throughput). Show design for order-sensitive endpoints vs independent endpoints. ([Medium][4])

3. **Retries, backoff, & scheduling**

    * *What to discuss:* Scheduler design: use a delayed queue (e.g., SQS delay or Kafka + scheduling) vs DB-based scheduler with periodic polling. Demonstrate exponential backoff formula, jitter, cap, and DLQ threshold. Discuss efficiency: avoid re-enqueueing for every failed attempt — use a central scheduler worker to move items into ready queue at `next_retry_at`.

4. **Idempotency & dedupe**

    * *What to discuss:* Key design: what key to use? (event_id + endpoint_id or signature/hash). TTL for dedupe store — how long? (Depends on retention & business semantics; Stripe suggestions). Discuss storage choices: Redis for low-latency, DynamoDB for durability. Cite GitHub/Stripe usage. ([GitHub Docs][2])

5. **Security deep dive**

    * *What to discuss:* HMAC signing, defenses vs replay, secret management & rotation, optional mutual TLS, IP allowlist, rate limiting, auditing. Discuss HSM/KMS for secret storage. Reference hookdeck/ngrok security checklist. ([hookdeck.com][7])

6. **Observability, analytics & SLOs**

    * *What to discuss:* Metrics to track (delivery success rate, retry rate, queue depth, p95/p99 latencies), alerting thresholds, tracing (correlate event_id across pipeline), dashboards, and root-cause steps for partial outages.

7. **Cost & storage optimization**

    * *What to discuss:* Storing payloads in object store once (S3) and referencing them; compress payloads; TTL-based deletion; sampling logs for very high throughput. Show BoE tradeoffs from earlier.

8. **Multi-region & disaster recovery**

    * *What to discuss:* Active-active vs active-passive; cross-region replication of event store & metadata; handling duplicates across regions; failover of delivery workers.

9. **Consumer contracts & schema evolution**

    * *What to discuss:* Versioned payloads, backwards compatibility, schema validation at ingest or at consumer, and migration strategies.

10. **Database indexing and query optimization**

    * *What to discuss:* DeliveryAttempt is the hot table with highest write/read volume. Options: (A) Single table with heavy indexing, (B) Time-based partitioning with SCD Type 2 history table (recommended), (C) Separate tables by status. **Preferred: SCD Type 2 approach (Option B)** — split into two tables:
        * **DeliveryAttempt (current state)**: holds latest status per delivery, partitioned by created_at (monthly), optimized for scheduler queries ("find all pending retries ready now")
        * **DeliveryAttemptHistory (audit trail)**: insert-only, one row per attempt, partitioned by attempt_timestamp (monthly), auto-drop old partitions after 30 days
    * **Benefits**: (1) Current state table stays small and fast (scheduler hot path), (2) History table grows linearly but old data can be archived, (3) Partial indexes on current state: `CREATE INDEX ON delivery_attempts (next_retry_at, status) WHERE status = 'pending'`, (4) Debugging: join history to see all attempts: `SELECT * FROM delivery_attempt_history WHERE delivery_attempt_id = X ORDER BY attempt_number`
    * **Write pattern & load**: On each delivery attempt, INSERT into history table (~4.6k writes/sec baseline, ~46k peak), UPDATE current state table (~4.2k writes/sec). Total: ~8.7k writes/sec baseline. History table is append-only (no UPDATE contention), making it ideal for write scaling.
    * **Query optimization**: Read replicas for customer UI queries, primary for scheduler writes. Consider Postgres with native partitioning or Cassandra for write-heavy workloads.
    * **Cost consideration**: At 396 GB/day for history, 30-day retention = ~12 TB. With compression + S3 archival, reduces to ~3 TB stored cost (~$70/month S3). Hot 7-day working set in DB = ~2.8 TB uncompressed.

11. **Reconciliation and audit trail design**

    * *What to discuss:* Three approaches: (A) Real-time query API with pagination (simple but expensive at scale), (B) Async batch reports generated on-demand and stored in S3 (scalable for large time ranges), (C) Event stream with sequence numbers for continuous reconciliation (real-time but requires smart client). Trade-offs: latency vs scalability vs client complexity. Preferred: **Hybrid approach** — batch reports for compliance/audit (weekly/monthly), query API for ad-hoc debugging (last 7 days), optional event stream for enterprise customers. Include delivery_id and event_id in all records for cross-referencing.

12. **Multi-region architecture and data residency**

    * *What to discuss:* Options: (A) Active-passive (simple DR, wastes capacity), (B) Active-active with geo-routing (low latency, risk of duplicates on failover), (C) Data residency model (endpoints tagged with allowed_regions, events forwarded to correct region), (D) Hybrid (regional isolation + global fallback). Trade-offs: latency vs compliance vs complexity. Preferred: **Hybrid regional isolation** — each region fully independent (own DB, queue, workers), endpoint metadata replicated async across regions, events delivered from ingestion region, automatic cross-region failover with idempotency preventing duplicates. For GDPR compliance: allow EU endpoints to specify "EU-only delivery" flag enforcing data residency.

13. **HTTP connection pooling and timeout strategy**

    * *What to discuss:* Worker capacity calculation depends on connection model. Options: (A) New connection per delivery (simple, ~100ms TLS overhead, ~10 deliveries/sec/worker), (B) Connection pool with keep-alive (reuse, ~50ms per delivery, ~20 deliveries/sec/worker), (C) HTTP/2 with multiplexing (multiple deliveries over one connection, ~30ms per delivery, ~33 deliveries/sec/worker). Trade-offs: simplicity vs efficiency vs endpoint compatibility. Preferred: **Connection pool with HTTP/1.1 keep-alive (Option B)** as baseline, upgrade to HTTP/2 if endpoint supports. Timeouts: connection=5s, read=30s, total=60s. Show BoE impact: at 4,165 attempts/sec, need ~208 workers (Option B) vs ~416 workers (Option A).

14. **Webhook endpoint lifecycle management**

    * *What to discuss:* Edge cases in endpoint lifecycle: (1) Creation — validate URL synchronously before accepting, send test ping, (2) Update — if URL changes mid-flight, should queued deliveries use old or new URL? Options: complete in-flight with old URL vs cancel and re-enqueue with new URL, (3) Deletion — if endpoint deleted while deliveries pending, move to DLQ or drop? Options: configurable grace period vs immediate purge, (4) Pause/Resume — customer temporarily pauses webhook (e.g., during maintenance), hold deliveries without failing, (5) Secret rotation — support both old and new secret for transition period (24h), include key_id in signature header. Preferred: validate on creation, complete in-flight on update, grace period on deletion, native pause/resume support.

15. **Exactly-once delivery implementation deep dive**

    * *What to discuss:* Options for exactly-once: (A) Two-phase commit with ack callback (true exactly-once but 2x network cost + customer must implement ack endpoint), (B) Idempotent delivery with unique delivery_id (at-least-once with client dedup, industry standard), (C) Transactional outbox (write to customer's DB directly, complex integration), (D) Immutable audit ledger with reconciliation (detectable duplicates not preventable). Trade-offs: guarantee strength vs complexity vs cost. Preferred: **Idempotent delivery (Option B) as default** — generate delivery_id = hash(event_id, endpoint_id, attempt_num), include in X-Delivery-ID header, customer dedupes with TTL store (7 days recommended). For premium tier: add **audit ledger (Option D)** allowing customers to reconcile and detect any missed events. Show example header: `X-Delivery-ID: dlv_abc123, X-Event-ID: evt_xyz789, X-Attempt: 1, X-Timestamp: 1234567890`.

---

XI. **Interview talking points (how to present)**

* Start by stating assumptions (delivery semantics, expected scale) and negotiate them with interviewer.
* Draw the simple diagram first, then add queues, workers, DLQ, idempotency store. Use the ASCII diagrams above.
* Discuss tradeoffs explicitly: e.g., "we pick at-least-once because exactly-once requires heavy coordination; we'll provide idempotency helpers instead." Cite industry examples (Stripe/GitHub). ([docs.stripe.com][5])
* Be prepared to deep dive into: how to schedule retries efficiently, how to avoid head-of-line blocking in fanout, and how to guarantee security (signing + replay window + secret rotation).

---

XII. **Quick checklist / best practices (one-liner reminders)**

* Always use HTTPS + verify certs. ([GitHub Docs][2])
* Sign payloads (HMAC SHA-256) and include timestamp. ([docs.stripe.com][5])
* Ack producers fast (persist & enqueue). Use async deliveries. ([Coding Monkey][1])
* Decouple with queues, partition fanout to avoid H-O-L blocking, and autoscale workers. ([Medium][4])
* Implement idempotency and DLQ with replay UI. ([hookdeck.com][8])

---

XIII. **Interview Presentation Guide (Time Budget & Prioritization)**

### For 45-minute interview:

**Phase 1: Clarifications (5 min)**
* Must-ask: delivery guarantees (at-least-once vs exactly-once), ordering requirements, scale (QPS/events per day)
* Nice-to-have: SSRF concerns, reconciliation needs, multi-region
* **Tip**: State assumptions clearly if interviewer doesn't specify

**Phase 2: Requirements & BoE (5 min)**
* Must-cover: FR (register endpoints, fan-out, retry, idempotency), NFR (throughput, latency SLO, durability)
* BoE: focus on worker sizing and queue capacity calculations
* **Skip if time-constrained**: detailed storage calculations (can mention briefly)

**Phase 3: High-level Design (10 min)**
* Must-draw: simple design first (Section VII diagram), then enriched design (Section VIII diagram)
* Key components to call out: API gateway, event store, fanout queue, delivery workers, scheduler, DLQ, idempotency store
* **Tip**: Draw simple first, then say "let me add components for scale/reliability"

**Phase 4: Deep Dive (15 min) — Pick 2-3 based on interviewer interest**
* **Most likely to be asked**:
    1. Retry logic and circuit breakers (how to avoid hammering failing endpoints)
    2. Idempotency and exactly-once semantics (how to prevent duplicate processing)
    3. SSRF protection and URL validation (security focus)
* **Also common**:
    4. Event ordering guarantees (FIFO vs unordered)
    5. Database indexing and query optimization (for scale)
    6. Multi-region architecture (for availability)
* **Strategy**: Go deep on 1-2 topics, show breadth on others ("I can also discuss X, Y, Z if you're interested")

**Phase 5: Tradeoffs & Wrap-up (5 min)**
* Discuss key tradeoffs made: at-least-once (simplicity) vs exactly-once (complexity), ordering (throughput) vs FIFO (latency)
* Mention alternative approaches briefly
* Be ready for follow-up questions

**Phase 6: Q&A (5 min)**
* Common follow-ups to prepare:
    * "What if endpoint returns 200 but doesn't actually process?" → Need ack mechanism or rely on idempotency
    * "How do you prevent one customer from affecting others?" → Per-customer rate limits, separate queues, multi-tenancy isolation
    * "What if you need to deliver to 100M endpoints?" → Partitioning strategy, distributed workers, fanout optimizations

### Sections to skip/abbreviate if time-constrained:
* Section IV BoE: mention key numbers but don't derive all calculations
* Section IX items 7-8, 10-12: mention briefly, don't deep dive unless asked
* Section X items 7, 9: skip unless specific question
* Section V database schema: show entities but skip detailed indexes unless performance question

### Sections that are MUST-COVER:
* Section I: Clarifications (at least 3-4 key questions)
* Section II/III: FR/NFR summary
* Section VII: Simple design diagram
* Section VIII: Enriched design diagram
* Section IX items 1-6: Idempotency, signing, retries, burst handling, circuit breakers, SCD data model
* Section X items 1-2: Exactly-once vs at-least-once, fan-out architecture

### Pro tips:
* **Signal what you're doing**: "Let me start with a simple design, then we'll scale it up"
* **Check in with interviewer**: "Should I go deeper on X, or move to Y?"
* **Manage time**: If 30 min in and haven't drawn enriched design, speed up
* **Show tradeoffs**: Don't just present one solution, explain why you chose it over alternatives
* **Cite industry examples**: "Stripe does X, GitHub does Y, we'll follow similar approach because..."

