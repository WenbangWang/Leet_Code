# Design: Payment System (process auth → hold → daily batch settlement)

---

## 1) Clarification questions (ordered FR → NFR)

**Functional (FR)**

1. Do we support card networks only (Visa/MC/Amex) or also ACH/wire/other rails?
2. Is the external payment service (gateway/acquirer) single provider or multiple with failover?
3. Which operations must be synchronous vs asynchronous? (e.g., authorization needs sync; settlement can be daily async batch)
4. What failure semantics do we promise to merchants/users (exactly-once vs at-least-once)?
5. Are refunds/captures supported in the same daily batch or separately?

**Non-functional (NFR)**

1. Target throughput: 10,000 TPS sustained (from prompt).
2. Max acceptable latency for authorization endpoints (SLO) — e.g., 200–500 ms p95?
3. Durability / consistency needs: financial correctness (no double-charges).
4. Security / compliance: must be PCI-DSS compliant; minimize card data scope (tokenization).
5. Availability: e.g., 99.99% for critical endpoints; degrade gracefully for downstream outages.

---

## 2) Functional requirements (FR) — condensed

* Accept and validate payment requests (card/ACH/token).
* Forward auth requests to external gateway/acquirer and return approve/decline.
* Hold approved amounts (auth hold) for a configurable period.
* Store transaction ledger and durable state for settlement.
* Generate daily settlement batches (group approved holds), submit batch to acquirer, reconcile results.
* Expose APIs for capture, refund, void, and dispute handling.
* Ensure idempotency for payment creation to prevent duplicates.

## 3) Non-functional requirements (NFR)

* Scale: 10k TPS ingestion with horizontal scaling.
* Latency: authorization < 500 ms p95 (example).
* Durability: transaction records must be ACID/durable.
* Consistency: monetary correctness; exactly-once net effect for settlements.
* Security: PCI DSS scope minimized via tokenization; encryption in transit & at rest.
* Observability: end-to-end tracing, metrics, and alerts for failed batches and reconciliation issues.

*(Relevant background: batch settlement is the standard approach: authorizations happen during the day and merchants/processors send batched transactions to the acquirer at end-of-day; see Stripe/PayPal primers on batching.)* ([Stripe][1])

---

## 4) Back-of-envelope calculation (B.o.E.)

Assume 10,000 TPS sustained for peak window (e.g., 1 hour bursts).

* TPS → per-second writes/reads to API and ledger. If each transaction needs:

    * 1 write to an ingest log,
    * 1 read to check idempotency,
    * 1 write to transaction DB,
    * plus async submission to gateway.

**Storage / DB throughput**

* 10k writes/sec → pick database(s) that can handle that (partitioning/sharding, e.g., multiple write leaders or write log + worker pattern).
* If each tx record ≈ 1 KB, then per day: 10k * 3600 * 24 ≈ 864M tx/day → ~864 GB/day raw (so plan for compression/retention strategies).

**Compute**

* Frontend API fleet sized to handle 10k TPS with load balancers. Each instance handling ~500 RPS requires ~20 instances (plus headroom). Use autoscaling and multiple availability zones for HA.

These are starting numbers — tune with real latency/CPU/memory measurements in a real environment.

---

## 5) Core data entities

* **PaymentRequest**: idempotency_key, merchant_id, amount, currency, method (card_token/ACH), timestamp, metadata, status (pending/authorized/held/settled/failed).
* **CardToken**: token_id, masked_pan, expiry, last4, token_provider_id, created_at (tokenization reduces PCI scope). ([PCI Security Standards Council][2])
* **AuthHold**: hold_id, payment_request_id, amount, expiry, external_auth_id, status(held/released/expired/captured).
* **SettlementBatch**: batch_id, merchant_id, date, list of tx refs, total_amount, status (prepared/submitted/reconciled).
* **LedgerEntry**: id, payment_id, event_type (auth, capture, settle, refund), amount, timestamp, balance_delta.
* **ReconciliationRecord**: batch_id, external_response, matched_flag, exceptions.

---

## 6) System interfaces (APIs)

* `POST /payments` — create payment (idempotent, returns status and payment_id). Must accept idempotency key header.
* `GET /payments/{id}` — fetch payment status / history.
* `POST /payments/{id}/capture` — capture held funds.
* `POST /batches/{date}/submit` — (internal) generate & submit batch.
* `GET /batches/{id}` — batch status & reconciliation results.
* Webhook endpoints for gateway notifications (auth, capture, settlement notices).
* Internal admin RPCs for reconciliation, retry, manual intervention.

**Idempotency pattern**: clients send a UUID idempotency key with create requests. Server stores key → response mapping (or a short-lived reservation) so retries return same result. (Stripe & other providers implement this.) ([Stripe 文档][3])

---

## 7) Start from simple design (ASCII diagram)

Goal: authorize and hold → daily batch submit.

```
[Client]
   |
   v
[API Gateway]
   |
   v
[Auth Service] <--> [Payment Gateway (external)]
   |
   v
[Idempotency Store]  <---> [Transaction DB (ledger)]
   |
   v
[Batch Job (daily)] --> [Batch Submitter] --> [Acquirer]
   |
   v
[Reconciler]
```

Notes: simple design stores a PaymentRequest in Transaction DB synchronously after auth response, and uses a scheduled Batch Builder to collect all approved holds for settlement.

---

## 8) Enrich the design (scale, HA, reliability) — ASCII diagram

Add streaming, partitioning, multiple gateway providers, retries, offline reconciliation, and token vault.

```
[Client]
   |
   v
[API GW] --> [AuthZ / RateLimit]
   |
   v
[Idempotency KV (fast read/write)]
   |
   v
[Ingest Queue / Kafka topic <<payments-ingest>>]
   |
   v
+-----------------------+-----------------------+
|                       |                       |
|                 [Auth Workers]               |
|                       |                       |
|    (call gateway)     v                       |
|                 [Token Vault]                 |
|                       |                       |
+-----------------------+-----------------------+
           |                        |
           v                        v
   [Transaction DB (ledger)]    [Event topic <<payments-events>>]
           |                        |
           v                        v
   [Batch Builder (stream)]   [Reconciler Consumers]
           |
           v
   [Batch Submitter] -> [Acquirer(s)]
           |
           v
   [Settlement Response] -> [Reconciler / DLQ / Ops UI]
```

Key enrichments:

* **Ingress queue (Kafka/SQS)** decouples spike bursts from downstream storage/processing.
* **Workers** process authorizations, update ledger, emit events to ledger DB and event stream for eventual consistency.
* **Token vault** stores sensitive card data (or delegate tokenization to gateway) — reduces PCI DSS scope. ([PCI Security Standards Council][4])
* **Multiple gateway support** for failover and routing (lowest fees, highest success rate).

---

## 9) Important design patterns & components (explain briefly)

1. **Idempotency store (KV)** — required for safe retries and dedup; store key → response & TTL. Preferred: strong consistency within a single region; use Redis with persistence or DB-backed KV with conditional writes. ([Stripe 文档][3])
2. **Event-sourcing / append-only ingest** — use a durable log (Kafka) to scale writes and replay for reconciliation. Helps for replaying failed batch assembly.
3. **Write model (transaction DB)** — ledger entries must be durable and auditable (append-only), ACID updates for balances. Consider a relational DB (Postgres, CockroachDB) or ledger-specialized DB.
4. **Batch builder** — groups authorizations by merchant/settlement window; applies business rules (e.g., partial reversals, expired holds).
5. **Reconciliation engine** — compares acquirer response to ledger; flags exceptions for human ops.
6. **Tokenization** — reduce PCI burden by never storing PAN in clear; use network tokenization or gateway tokenization. ([PCI Security Standards Council][2])

---

## 10) Possible deep dives — expand on each (options, trade-offs, preferred)

### A. Idempotency implementation

**Options**

* *Simple KV store* (Redis with persistence): store idempotency_key → full response + status; TTL 24–72 hours.
* *DB-backed idempotency table*: strong durability, easy cross-region replication.
  **Trade-offs**
* KV is very fast but must persist & survive failover (Redis-AOF/RDB or clustered Redis with persistence).
* DB-backed is slower but consistent and easier for audits.
  **Preferred**
* Use a *hybrid* approach: KV for fast checks + DB as ground truth for durability. Use conditional DB insert (unique constraint on idempotency key) to enforce exactly-once for critical ops. Cite Stripe pattern. ([Stripe 文档][3])

### B. Ledger & transactional correctness

**Options**

* *Relational DB (Postgres/CockroachDB)* with ACID transactions and unique constraints.
* *Event-sourced ledger (Kafka + append-only store)* and derive balances via materialized views.
  **Trade-offs**
* RDB: familiar, ACID, easier to query for reconciliation, but scaling writes needs sharding or distributed SQL (CockroachDB).
* Event-sourcing: excellent for replay and audit; harder to do strong consistency on balances without careful design.
  **Preferred**
* Use *relational ledger* for money movement + append-only event log for audit/replay. Keep money updates in ACID DB to guarantee correctness.

### C. Batch formation & submission

**Options**

* Build per-merchant batches at fixed window (e.g., daily close) and submit to a single acquirer.
* Build dynamic batches with sharding across multiple acquirers for load & fee optimization.
  **Trade-offs**
* Fixed single acquirer simpler; dynamic multi-acquirer improves success rates & cost but increases complexity (routing, format differences).
  **Preferred**
* Start single acquirer; add multi-acquirer routing layer later with abstraction for batch format mapping. Batching specifics (format, file type) depend on acquirer network. (Batching is industry standard; see Stripe/PayPal explanations.) ([Stripe][1])

### D. Handling retries & exactly-once semantics

**Options**

* Rely on idempotency keys & unique DB constraints.
* Rely on deduping at batch submit/reconcile level.
  **Trade-offs**
* Idempotency at API avoids duplicate charges early.
* Reconciliation-level dedupe handles duplicates that slip through but exposes more manual work.
  **Preferred**
* Enforce idempotency at ingest + unique constraints downstream + reconciliation to catch any anomalies.

### E. Tokenization and PCI scope reduction

**Options**

* Use a third-party gateway for tokenization (delegate all PAN handling).
* Run own token vault (requires significant compliance effort).
  **Trade-offs**
* Third-party tokenization rapidly reduces PCI scope and operational cost.
* Self-hosted vault offers more control but large compliance/ops burden.
  **Preferred**
* Delegate tokenization to payment gateway or use a certified tokenization provider; if scale & control justify it, implement a hardened token vault under PCI controls. ([PCI Security Standards Council][2])

### F. Scaling to 10k TPS

**Options**

* Horizontal API autoscaling + ingress queue + partitioned DB writes.
* Use a write-optimized log (Kafka) for ingestion with multiple partitions keyed by merchant/payment ID.
  **Trade-offs**
* Direct DB writes at 10k TPS require sharding/partitioning and significantly more ops work.
* Kafka ingestion decouples spikes and enables replay/reprocessing.
  **Preferred**
* Use *ingest log (Kafka)* + horizontally scaled worker pool + partitioned DB. Key partitions by merchant or hash to avoid hotspots.

### G. Reconciliation & exceptions

**Design**

* After batch submission, acquirer returns success/fail per tx. Reconciler marks transactions settled or creates exception tickets. Create automatic retries for known transient failures, and ops workflows for manual resolution for disputes/fraud.

---

## 11) Reliability & edge cases (short list)

* **Downstream gateway outage**: accept auths but mark as pending; have circuit breaker and degrade to offline-authorization mode if allowed by business rules.
* **Stale holds**: hold expiry logic must void holds and update ledger before batch formation.
* **Duplicate external notifications**: handle webhook retries idempotently.
* **Partial batch failures**: support per-item reconciliation and re-submit only failed items.

---

## 12) Operational concerns & observability

* Tracing from client request → gateway call → batch entry → settlement result (OpenTelemetry).
* Metrics: auth TPS, p95 latency, batch success rate, reconciliation exceptions.
* Alerts: batch failure, >X reconciliation exceptions/hour, downstream unavailability.
* Audit logs: append-only ledger + immutable event log for forensic purposes.

---

## 13) Security & Compliance (PCI)

* Tokenize PANs; never store PANs in application logs. Use TLS everywhere and KMS for key management. Use PCI DSS guidance for tokenization and scoping. Consider network tokenization and use of gateway-managed tokens to reduce PCI DSS scope. ([PCI Security Standards Council][4])


