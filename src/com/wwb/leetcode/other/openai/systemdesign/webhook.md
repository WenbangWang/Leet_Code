https://pyemma.github.io/How-to-Design-Webhook/

I. **Clarification questions (ordered FR → NFR)**
*(I’ll make reasonable assumptions below; if any are wrong, tell me and I’ll adapt.)*

Functional (FR)

1. Which delivery guarantee do we need for the system owner → consumer? (At-least-once by default; exactly-once is rare / costly.)
2. Do consumers need event filtering/server-side (i.e., subscribe to subsets of events) or do they always receive every event?
3. Do we support custom retry policies per consumer (e.g., TTL, backoff) or a global policy?
4. Must the webhook provider store/retain payloads for replay (e.g., 30 days)?
5. Do we need a UI for customers to register/update endpoints and view delivery logs/errors?

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
* Observability & admin UI: delivery logs, retry counts, metrics, manual replay.
* Access control & audit logs for endpoint changes.

(References: PyEmma design notes; GitHub/Stripe best-practices on verification and short ack-windows.) ([Coding Monkey][1])

---

III. **Non-functional requirements (NFR)**

* Throughput: handle bursty traffic (e.g., 10k events/minute per customer, aggregate bursts to 100k+/min).
* Latency: ack producer within <100–500ms (fast enqueue); delivery to endpoints is asynchronous. GitHub recommends responding within ~10s for immediate processing. ([GitHub Docs][2])
* Durability: persist events until successfully delivered or TTL expiry (configurable; e.g., 7–30 days).
* Availability: 99.9% ingestion availability; cross-AZ/region failover.
* Scalability
* Security: TLS, HMAC SHA-256 signing, timestamp check, replay protection. (Most providers use HMAC SHA256.) ([ngrok.com][3])

---

IV. **Back-of-Envelope (BoE) calculations**
Assumptions (adjustable): 1M active users; 5% produce events per minute → 50k events/minute = ~833 events/sec. Peak burst: 10× baseline = 8,330 EPS.

Producer side:

* If each event payload ~5 KB, baseline traffic = 833 * 5KB ≈ 4.2 MB/s. Peak ~42 MB/s.

Queueing:

* If average endpoint count per event = fan-out to 5 endpoints → effective delivery attempts/sec = 833 * 5 = 4,165 attempts/sec baseline, peak ~41,650 attempts/sec.
* If retry policy attempts up to 6 times with exponential backoff, worst-case inflight attempts can multiply; ensure queue and DLQ capacity for multiplicative retries.

Storage:

* Retain raw payloads for 7 days: daily raw bytes baseline = 833 * 5KB * 86400 ≈ 360 GB/day (this is a lot — consider compressing or storing references). *Important: fan-out does not require duplicating stored payloads; store once and reference ID for delivery attempts.*

Worker sizing:

* If a single delivery worker can do ~50 HTTP deliveries/sec (including TLS handshake + network), to handle 4,165 attempts/sec we need ~84 workers baseline; for peak 41,650 attempts/sec → ~833 workers. Use autoscaling groups / serverless workers (e.g., Fargate, lambdas with concurrency, or Kubernetes) with per-worker throughput assumptions.

Monitoring:

* Track delivery queue depth, attempts/sec, p95/p99 delivery latency, % 2xx success.

(These numbers are illustrative; adapt based on real expected customer behavior.)

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
2. **Event**

    * id (UUID)
    * type (string)
    * payload_location (object storage ref) OR payload (nullable)
    * created_at, source_service
3. **DeliveryAttempt**

    * id (UUID)
    * event_id
    * endpoint_id
    * attempt_number
    * status (pending/sent/success/failure/dlq)
    * response_code
    * response_body_excerpt
    * next_retry_at
    * last_error
    * created_at, updated_at
4. **DLQEntry**

    * delivery_attempt_id, event_id, endpoint_id, failure_reason, stored_payload_ref
5. **IdempotencyStore / ProcessedEvents**

    * provider_event_id or delivery_signature, endpoint_id → processed_at
6. **AuditLog**

    * user, action, object, timestamp, before/after
7. **Metrics & Traces** (time-series)

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
* `GET /v1/deliveries?endpoint_id=&status=&from=&to=` — list deliveries / errors.
* `POST /v1/deliveries/{delivery_id}/replay` — manual replay.

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

7. **Testing & local dev**

    * Provide test endpoints and `test-send` API; allow customers to see sample payloads and signing.

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

