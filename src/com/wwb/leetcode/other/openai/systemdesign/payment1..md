
---

# A — Simple design (minimal components)

Goal: implement a correct, easy-to-explain payment flow: client → authorize/hold → store → daily batch → acquirer.

## Simple components

* API Gateway / Load Balancer
* Auth Service (sync call to gateway)
* Idempotency check (simple DB or KV)
* Transaction DB (single relational DB + ledger table)
* Batch Job (daily cron)
* Batch Submitter → Acquirer
* Reconciler (basic)

### ASCII diagram (simple)

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

---

## Normal workflow (happy path)

1. Client `POST /payments` with `idempotency_key`, payment token or card info (tokenized preferably).
2. API Gateway routes to Auth Service.
3. Auth Service performs idempotency check:

    * If key exists, return stored response (no duplicate).
    * If key not found, insert idempotency record and continue.
4. Auth Service calls external Payment Gateway (authorization API).
5. Gateway returns `approved` with `external_auth_id`.
6. Auth Service writes a `PaymentRequest` + `AuthHold` + `LedgerEntry` in the Transaction DB (single atomic DB transaction for application-level correctness).
7. Response to client: `authorized` + `payment_id`.
8. During day, holds are kept (or auto-captured). End of day, Batch Job queries DB for `authorized` holds, builds a Batch file, and Batch Submitter sends it to Acquirer.
9. Reconciler ingests acquirer response, updates `LedgerEntry` to `settled` or flags exceptions.

**Notes:** In simple design idempotency ensures duplicate client retries don’t charge twice. Batching groups and reduces fees / conforms to acquirer flows (industry norm). ([Stripe][1])

---

## Error handling — simple design (component-by-component)

I’ll cover common error classes and how the simple design reacts.

### 1) Client retries / duplicate requests

* **Symptom:** Client times out and retries `POST /payments`.
* **Handling:** Idempotency store returns stored response. No second authorization call. If first request still in progress, the second waits or returns an in-progress status depending on API contract. Use unique constraint on idempotency key in DB to guard against race insert anomalies. Preferred simple implementation: DB `INSERT ... ON CONFLICT DO NOTHING` to atomically detect duplicates. ([Reddit][2])

### 2) External gateway times out / transient failure during auth

* **Symptom:** Gateway doesn’t respond or returns HTTP 5xx.
* **Handling (simple):**

    * Auth Service retries a small number of times (exponential backoff, circuit breaker).
    * If unsuccessful, mark `PaymentRequest` as `pending_gateway` (or `failed`) and return error to client (or return “pending” if business accepts delayed result).
    * Record the attempted authorization in DB and an audit log for retriable post-processing.
    * Reconciliation / ops team may manually retry later.
* **Trade-offs:** Synchronous retries increase client latency; too few retries increase manual work. Keep small retry window for simple designs.

### 3) DB write fails after gateway returns approved (partial failure)

* **Symptom:** Gateway said `approved` but DB write crashed (network partition, DB down).
* **Handling:**

    * If idempotency record was inserted before calling gateway, rely on that to detect retry later. Otherwise you may have an outstanding approved auth with no local record (danger).
    * Recommended simple approach: do idempotency insert *first* (so you can always return prior decision), then call gateway, then write DB using a retryable worker if write fails (but track the external auth id).
    * If you cannot guarantee DB persist synchronously, log the external auth to durable append-only log (file or queue) for re-ingest.
* **Risk:** If external auth is approved and you never persist it, money could be held or double-captured. Simple design accepts some operational manual work to reconcile.

### 4) Batch submit fails or partially accepted

* **Symptom:** Acquirer rejects the batch file or returns partial acceptance.
* **Handling:**

    * Mark per-transaction settlement statuses based on acquirer response.
    * For transient acquirer errors, retry whole-batch submit (if acquirer idempotency supports it) or re-submit only non-accepted transactions.
    * Create reconciliation tickets for items rejected with permanent errors (expired card, chargeback risk).
* **Note:** Acquirers often return per-item statuses; reconciler must update ledger accordingly. ([Stripe][1])

### 5) Reconciler discrepancies

* **Symptom:** Acquirer says transaction settled but system shows failed (or vice versa).
* **Handling:** Escalate to ops with full audit log and raw acquirer response. Admin tools to re-run matching and apply manual adjustments.

**Simple design summary:** straightforward, limited automation for failures; relies on idempotency + basic retries + human ops for complex reconciliation.

---

# B — Enriched design (production-grade): detailed workflow, error handling, and deep dives

Enriched design adds streaming, multi-acquirer routing, token vault, robust idempotency, replayable event log, scalable ledger, automated reconciliation, and ops tooling.

## Enriched components (overview)

* API Gateway / LB (with auth, rate limiting)
* Ingest Queue / Event Log (Kafka)
* Frontend Auth Service (stateless)
* Idempotency Store (fast KV + durable DB)
* Token Vault / Tokenization Service (or gateway-managed tokens)
* Authorization Workers (consumers of Kafka)
* Transaction DB (sharded, ACID ledger; e.g., Postgres/CockroachDB)
* Materialized views / read replicas for queries
* Batch Builder (stream processing or periodic job)
* Batch Submitter (multi-acquirer adapter)
* Reconciler (streaming reconciler and exception processing)
* Retry & Dead-Letter queues
* Observability (tracing, metrics), Ops UI for exceptions
* Access controls / KMS / HSM for keys

### ASCII diagram (enriched)

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

**Key differences vs simple design:**

* Decoupling via Kafka (durable ingest) isolates spikes and enables replayability. Kafka + stream processing is common for scalable payment platforms. ([Kai Waehner][3])
* Idempotency implemented as hybrid: fast KV for low-latency checks, durable DB for ground truth (prevents data loss).
* Token Vault isolates PAN and reduces PCI scope (or delegate to gateway tokenization). ([cybersource.com][4])
* Multi-acquirer adapter allows routing per-merchant/region/fee optimization.

---

## Enriched workflow (happy path) — numbered steps through components

1. **Client request arrives** at API GW with `Idempotency-Key` and a `card_token` (or raw PAN if using client-side tokenization).
2. **Idempotency fast check**:

    * API tries to insert idempotency key into KV (Redis) with a small TTL and optimistic lock.
    * If a conflict exists, return stored response (fast).
3. **Publish to ingest topic** (`payments-ingest`) in Kafka (synchronously from API or via async handoff). The message includes idempotency_key and client payload.

    * This gives durable, ordered, replayable input for processing. Kafka is used to handle spikes and enable replay. ([Instaclustr][5])
4. **Auth Worker consumer** reads from `payments-ingest` (partitioned by merchant or payment_id).

    * Worker retrieves token from Token Vault if needed (or uses token provided).
    * Worker calls external Payment Gateway(s), following routing rules (single or multi-acquirer).
    * Worker writes the result to Transaction DB inside a DB transaction and emits an event to `payments-events` (approved/declined).
    * Worker updates the idempotency DB durable record with the canonical response (so later reads return definitive result).
5. **Materialized reads**: read replicas / materialized views serve `GET /payments/{id}` requests without hitting primary ledger.
6. **Batch formation:** Stream processing job (or scheduled job) reads `payments-events` and accumulates authorized holds per merchant into `Settlement Batches` based on business rules (cutoff time, currency, merchant preferences).
7. **Batch submitter** formats batches for each acquirer and sends them. It records batch submission metadata and persists submission attempts (idempotency at batch level).
8. **Acquirer response** (immediately or later) is consumed by reconciler that updates per-transaction settlement status and applies cash movements to ledger; exceptions go to DLQ and Ops UI.
9. **Automated retries** handle transient failures; persistent errors create tickets or manual workflows.

---

## Enriched error handling — what happens when things fail (detailed)

I’ll walk component-by-component and show automated and manual resolution strategies.

### 1) API GW / Idempotency KV failures

* **Failure:** Redis cluster (idempotency KV) unavailable.
* **Response:** API can fall back to synchronous DB idempotency insert (slower) or reject writes if strict. Preferred: fallback to DB `INSERT` with unique idempotency key (`ON CONFLICT DO NOTHING`) to preserve correctness albeit with higher latency. Always prefer correctness over speed for payments. ([Reddit][2])

### 2) Kafka publish failure

* **Failure:** Broker unavailability or partition leader failure.
* **Handling:**

    * API should use synchronous publish with retries and a local persistent retry queue (durable disk-backed queue) if Kafka is down.
    * If synchronous publish cannot be guaranteed, the system should *not* return success to client; instead either return transient error or accept and queue locally for later (but that complicates client expectations). Prefer to fail fast (return error).
* **Rationale:** Indistinguishable loss of ingest messages is unacceptable for financial transactions; durable append log is required.

### 3) Auth worker—gateway call times out or gateway returns 5xx

* **Transient:** Retry with exponential backoff up to N times (configurable). Use a Circuit Breaker to stop thrashing a failing gateway and route to fallback acquirer if available.
* **Persistent:** Mark payment as `pending_gateway` and emit an event to `payments-events` with `status=pending`. The reconciler and an automated retry worker will retry later. If multiple gateways exist, failover routing can attempt a second gateway (see multi-acquirer deep dive).
* **Note:** Because we published the original request to Kafka, the worker can safely retry without client involvement.

### 4) Auth succeeded at gateway but DB transaction fails (partial success)

* **Problem:** money is held by issuer but the local ledger didn’t record it.
* **Solution pattern:**

    * Worker should write to DB in same processing context as emitting `payments-events`. If DB write fails, worker should produce a compensating event to a `DLQ` or to a special `orphaned-auths` topic that triggers a reconciliation process.
    * Reconciler consumes issuer reports or calls gateway `/v2/transactions/{id}` to recover external state and reconcile.
* **Preferred approach:** make DB writes durable *before* acknowledging the gateway result to the API (write idempotency + placeholder record first), or use a two-phase commit pattern across the outbound gateway call and DB (but 2PC with external gateway is impractical). The resilient approach is durable ingest + reconciliation.

### 5) Partition hot-spot / Kafka consumer lag

* **Symptom:** All payments for one mega-merchant map to a single partition and consumer can’t keep up.
* **Handling:**

    * Partitioning strategy: shard by `merchant_id` + `hash(payment_id)` or use consistent hashing to spread load.
    * For heavy merchants, create dedicated partitions/consumer groups or route high-volume merchants to a separate ingestion cluster.
* **Trade-off:** Finer sharding increases complexity; prefer hashing strategy and ensure idempotency key includes partitioning key to keep ordering where necessary.

### 6) Batch submitter failure or acquirer rejects

* **Partial accept:** Acquirer returns per-item statuses. Reconciler marks each item as `settled`/`rejected`. For rejections with recoverable reasons (temporary network, insufficient funds), scheduler retries. For permanent failures (card expired), generate return/merchant notification or refund as per policy.
* **Entire batch rejected:** Re-submit with corrected format or route to backup acquirer; create an alert. Store original batch file and acquirer response for audit.
* **Idempotency for batch submission:** use a batch-level idempotency token so re-submission does not cause duplicates at acquirer-level.

### 7) Reconciler discrepancies and disputes

* **Automated match:** reconcile ledger totals with acquirer file using key fields (external_auth_id, amount, currency, merchant_id). If matched, mark settled.
* **Exceptions:** Put into workflow queue for ops. Provide UI that shows full event stream for forensic tracing (Kafka offsets, DB transaction ids).
* **Chargebacks/disputes:** Create dispute object, block funds if necessary, start dispute resolution process and notify merchant.

**Observability & tooling:** Enriched design needs traces that correlate `idempotency_key` → Kafka offset → DB tx id → batch id → acquirer response, so ops can find any transaction quickly.

---

## Deep dives (enriched design): options, trade-offs, and preferred approaches

I’ll expand the important areas you’ll be asked about in an interview: **idempotency**, **ledger architecture**, **batch formation & multi-acquirer routing**, **retry & exactly-once semantics**, **tokenization/PCI**, and **scalability via Kafka**.

---

### 1) Idempotency — deep dive

**Goal:** prevent duplicate charges on retries and ensure deterministic outcomes.

**Options**

* Fast KV (Redis) with TTL for immediate dedupe; fallback to DB for durability.
* Single durable DB table with unique `idempotency_key` (use `INSERT ... ON CONFLICT`).
* Use Kafka offset + transactional writes to ensure at-least-once + dedupe downstream.

**Trade-offs**

* KV only: very low latency but risk if KV loses data.
* DB-only: higher latency, but durable; can be single source of truth.
* Kafka-centric: complex but offers replay, ordering guarantees; dedupe must still be in DB.

**Preferred**

* **Hybrid:** insert idempotency record in DB (unique constraint) or write to a durable ingress log (Kafka) and keep fast KV for low latency reads. For correctness, ensure DB unique constraint as ground truth. When API needs low latency, consult KV; if KV down, fall back to DB atomic insert. This pattern matches Stripe guidance. ([docs.stripe.com][6])

**Implementation notes**

* Store `{idempotency_key, request_hash, response_body, status, created_at}`.
* TTL: keep keys for at least as long as the capture window + some safety (e.g., 72 hours).
* For partial failures, ensure idempotency record includes external_auth_id if any.

---

### 2) Ledger & transactional correctness — deep dive

**Goal:** monetary correctness, auditability, and ability to reconcile.

**Options**

* Single ACID relational DB (Postgres/CockroachDB) with append-only ledger table and balance computed by aggregation or maintained in materialized balance table.
* Event-sourcing (append-only event log + materialized projection to compute balances).
* Hybrid: event log (Kafka) + relational DB for final authoritative ledger.

**Trade-offs**

* ACID DB: simpler to reason about for money movement; need sharding/partitioning for scale.
* Event-sourcing: ideal for replay & audit; more complex to guarantee real-time balances; needs careful design for double-spend protection.
* Hybrid: gives replay and durability; use DB as materialized projection for balance queries.

**Preferred**

* **Hybrid:** Keep ACID DB as the authoritative ledger for final monetary entries; use Kafka event log for ingest and replay/audit. This allows strong correctness for debits/credits while preserving replayability. Use unique constraints, strong isolation (serializable or optimistic locking patterns) for critical updates.

**Implementation details**

* LedgerEntry: `{id, payment_id, event_type, amount, currency, effective_date, balance_delta, running_balance, external_ref, metadata}`.
* Use DB transactions for write sets that affect more than one row (e.g., merchant account balance + platform fees).
* Backups and immutable append logs for audit.

---

### 3) Batch formation & multi-acquirer routing — deep dive

**Goal:** create efficient settlement batches, support routing for redundancy/fees/perf.

**Options**

* Single fixed acquirer and simple batch at EOD.
* Multi-acquirer routing: choose acquirer per-merchant, per-currency, or per-transaction.
* Real-time settlement (skip batch) where supported (more expensive / different rails).

**Trade-offs**

* Single acquirer: simpler; less development and ops work.
* Multi-acquirer: reduces single point of failure and costs; increases complexity (mapping rules, format transformations).
* Real-time settlement: reduces deferred risk, but not always possible / more expensive.

**Preferred**

* Start with single acquirer; implement abstraction layer for acquirer adapters. When scaling, add routing logic:

    * Maintain routing table keyed by merchant_id, currency, region, and fallback priority.
    * Batch-level idempotency: include `batch_id` that acquirer can dedupe on re-submits.

**Formatting & compliance**

* Build adapters that transform internal format into acquirer-specific file (CSV, XML, ISO 20022 type, or API payload). Keep raw batch file storage for audit.

**References:** industry batch processing approaches and best practices. ([Stripe][1])

---

### 4) Exactly-once vs at-least-once & retries — deep dive

**Problem:** distributed systems are usually at-least-once; payments require effectively exactly-once semantics (no double charging).

**Options**

* Idempotency keys and unique DB constraints for application-level exactly-once.
* Use Kafka transactions and consumer transactions to apply exactly-once processing semantics where possible.
* Sagas/compensating transactions for long-running flows.

**Trade-offs**

* True distributed exactly-once across external gateway is impossible; must rely on idempotency + reconciliation.
* Kafka transactions help for internal state but don’t extend to external systems.

**Preferred**

* Use idempotency + single-source-of-truth DB unique constraints + reconciler to close gaps. Design compensating workflows for orphaned external auths. Keep retries idempotent and bounded.

---

### 5) Tokenization & PCI (security) — deep dive

**Options**

* Use gateway-managed tokenization (outsourced).
* Build in-house token vault (needs PCI DSS scope and heavy controls).
* Client-side tokenization using payment SDKs (so PAN never touches servers).

**Trade-offs**

* Outsourced tokenization: low ops/PCI burden, less control.
* In-house vault: full control, higher ops & compliance cost.
* Client tokens: minimal scope but requires client integration.

**Preferred**

* Use *gateway tokenization* or client-side tokenization for most use cases. If business needs dictate, a hardened in-house vault with HSMs and strict PCI controls is possible but only when scale/ROI justify it. Tokenization significantly reduces PCI scope. ([cybersource.com][4])

---

### 6) Scaling architecture (Kafka, partitioning, consumer model) — deep dive

**Goal:** handle 10k TPS (or more) and provide resilience to bursts.

**Pattern**

* Use Kafka (or similar durable log) for ingest with many partitions. Partition key = hashed(merchant_id || payment_id) to retain ordering where needed and distribute load. Kafka enables replay and backpressure handling. ([Talent500][7])
* Stateless auth workers scale horizontally; use consumer groups to increase throughput.
* Ensure idempotency and DB uniqueness eliminate duplicate effects when processing is retried.

**Hotspot mitigation**

* If a merchant causes a hotspot, route that merchant to dedicated partitions/consumer groups or apply rate limiting/queue throttling at ingress.

**Operational considerations**

* Monitor consumer lag, partition skew, and set topic retention appropriately for replay windows. Use stream processors (Flink/ksql) for continuous batch building and reconciliation.

---

## Example enriched error scenarios (end-to-end)

### Scenario 1: Gateway outages during a peak

* Ingest still accepts messages to Kafka.
* Auth workers detect gateway failures and flip circuit breaker; route eligible payments to secondary gateway if routing rules allow.
* Payments that cannot be routed are left as `pending_gateway` in DB and retried by retry workers.
* Alerts fire if pending queue size exceeds threshold. Ops UI surfaces top merchants and error rates.

### Scenario 2: DB partition failure during batch window

* Transaction DB primary partition becomes unavailable for subset of merchants.
* Batch builder reads events and skips transactions that can’t be read, or builds partial batches per merchant shard.
* Reconciler later merges results when DB recovers.
* Orphaned authorizations (authorized but not in ledger) are found by a periodic reconciliation job which queries external gateway for recent authorizations and matches them back to ingest Kafka offsets — then creates ledger entries to reattach them.

### Scenario 3: Duplicate external notifications / webhooks

* Webhooks include provider’s unique `event_id`. System dedupes using a webhook idempotency store and ignores duplicate events.
* If duplicate webhook triggered double settlement risk, ledger unique constraints prevent double apply (and reconcile flags anomaly).

---

## Operational & SRE practices (quick checklist)

* End-to-end tracing (idempotency_key as trace header).
* Alerts on: consumer lag > threshold, reconciliation exceptions rate, batch failure rate.
* Chaos-testing: simulate acquirer downtime, Kafka broker failover, DB partitioning.
* Runbooks for common ops: re-submit batch, reconcile orphaned auths, recover idempotency DB.

---

