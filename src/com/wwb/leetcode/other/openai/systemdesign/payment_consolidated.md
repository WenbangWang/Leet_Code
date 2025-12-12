# Design: Payment System (Authorization → Hold → Daily Batch Settlement)

---

## 1) Clarification questions (ordered FR → NFR)

**Functional (FR)**

1. Do we support card networks only (Visa/MC/Amex) or also ACH/wire/other payment rails?
2. Is the external payment service (gateway/acquirer) single provider or multiple with failover?
3. Which operations must be synchronous vs asynchronous? (e.g., authorization needs sync; settlement can be daily async batch)
4. What failure semantics do we promise to merchants/users (exactly-once vs at-least-once)?
5. Are refunds/captures/voids supported? Same-day or next-batch?
6. Do we need fraud detection/risk scoring integrated into the authorization flow?
7. Are chargebacks and dispute management in scope?
8. Do we handle recurring payments / subscriptions?
9. What compliance requirements: PCI DSS (required), PSD2 (EU), AML/KYC?
10. Multi-currency support? Real-time FX conversion?

**Non-functional (NFR)**

1. Target throughput: 10,000 TPS sustained (peak traffic assumption)?
2. Max acceptable latency for authorization endpoints (SLO) — e.g., p95 < 300ms, p99 < 500ms?
3. Durability / consistency needs: financial correctness (no double-charges, no lost transactions)
4. Security / compliance: PCI-DSS compliant; minimize card data scope (tokenization)
5. Availability target: e.g., 99.95% for critical endpoints; degrade gracefully for downstream outages
6. Multi-region requirements: data residency (GDPR, local regulations), cross-region failover?
7. Reconciliation SLA: how quickly must we detect and resolve discrepancies? (e.g., <1 hour for daily batch)
8. Audit retention: how long to keep transaction records? (typically 7 years for compliance)

---

## 2) Functional requirements (FR) — condensed

* Accept and validate payment requests (card/ACH/token)
* Forward auth requests to external gateway/acquirer and return approve/decline
* Hold approved amounts (auth hold) for a configurable period (typically 7 days)
* Store transaction ledger and durable state for settlement
* Generate daily settlement batches (group approved holds), submit batch to acquirer, reconcile results
* Expose APIs for capture, refund, void, and dispute handling
* Ensure idempotency for payment creation to prevent duplicates
* Fraud detection: real-time risk scoring integrated into authorization flow
* Chargeback handling: dispute workflow, fund holds, representment
* Multi-device/multi-merchant support with proper isolation

## 3) Non-functional requirements (NFR) with specific SLOs

* **Scale:** 10k TPS ingestion with horizontal scaling, burst capacity to 15k TPS
* **Latency SLOs:**
  * Authorization endpoint: p95 < 300ms, p99 < 500ms (includes gateway roundtrip)
  * Idempotency check: p95 < 50ms (fast KV lookup)
  * Batch submission: complete within 30 minutes of cutoff time
  * Reconciliation: exceptions flagged within 1 hour of batch completion
* **Availability SLOs:**
  * Authorization API: 99.95% uptime (22 minutes downtime/month allowed)
  * Ingestion (event acceptance): 99.99% uptime (4.3 minutes/month)
  * Settlement pipeline: 99.9% batch success rate
* **Durability:** 
  * Transaction records must be ACID/durable (11 nines: 99.999999999%)
  * Zero data loss for accepted payments (write to durable log before ack)
* **Consistency:** 
  * Monetary correctness: exactly-once net effect for settlements (idempotency + reconciliation)
  * Ledger balance consistency: strong consistency within region, eventual across regions
* **Security:** 
  * PCI DSS scope minimized via tokenization
  * Encryption in transit (TLS 1.3) and at rest (AES-256)
  * Key rotation: automatic every 90 days
  * Audit logs: immutable, retained for 7 years
* **Observability:** 
  * End-to-end tracing with correlation IDs
  * Metrics: authorization success rate, settlement success rate, reconciliation exception rate
  * Alerts: page on double-charge detection, settlement failure, reconciliation SLA breach

---

## 4) Back-of-envelope calculation (BoE)

**Transaction volume:**
* Peak: 10,000 TPS → ~100M transactions/day
* Per-transaction: 1 Kafka write + 1 Redis read + 1 DB write + 1 gateway call + 1 event publish

**Storage:**
* Transaction DB: 150 GB/day → 55 TB/year (20 TB hot, 35 TB cold with compression)
* Kafka: 7 days retention = 700 GB
* Redis: 72h TTL = 150 GB

**Compute:**
* API servers: 40-50 instances (500 RPS each, multi-AZ with 2x headroom)
* Kafka: 50 partitions, 10 MB/sec write, 30 MB/sec read
* Database: 5-10 shards (1-2k writes/sec per shard)
* Workers: 1,500-2,000 instances (gateway calls ~150ms, ~7 TPS per worker)

**Costs (monthly):**
* Infrastructure: ~$170k (compute $100k + DB $30k + Kafka $15k + Redis $10k + S3 $5k + network $10k)
* **Gateway fees: $32M** (2.9% + $0.30 per transaction - dominant cost)

---

## 5) Core data entities

1. **PaymentRequest:** payment_id, merchant_id, idempotency_key (UNIQUE), amount, currency, status, external_auth_id, created_at
   * Indexes: (merchant_id, created_at), (idempotency_key) UNIQUE, (status, created_at)
   * Partitioning: Shard by hash(merchant_id)

2. **CardToken:** token_id, masked_pan, last4, expiry, card_brand (reduces PCI scope)

3. **AuthHold:** hold_id, payment_id, amount, external_auth_id, hold_expiry, status (held/captured/released)
   * TTL: Auto-expire after 7 days

4. **LedgerEntry** (immutable append-only): entry_id, payment_id, merchant_id, event_type (auth/capture/settle/refund/chargeback), amount, balance_delta, running_balance, effective_date, external_ref
   * Indexes: (payment_id, created_at), (merchant_id, effective_date)
   * Partitioning: Monthly time-based + shard by merchant_id
   * **INSERT-only, no UPDATEs** (audit trail)

5. **SettlementBatch:** batch_id, merchant_id, batch_date, total_amount, total_count, acquirer_id, status (prepared/submitted/reconciled)

6. **ReconciliationRecord:** record_id, batch_id, payment_id, expected_amount, actual_amount, match_status (matched/mismatch/missing), discrepancy_type

7. **IdempotencyStore (Redis):** Key: idempotency_key → Value: {payment_id, request_hash, response, status} (TTL: 72h)

8. **FraudCheck:** fraud_id, payment_id, risk_score (0-100), risk_level (low/medium/high/block), fraud_signals (JSON)

9. **Chargeback:** chargeback_id, payment_id, reason_code, amount, status (open/won/lost), representment_deadline

---

## 6) System interfaces (APIs)

**Client-facing:**
* `POST /v1/payments` — create payment (idempotent), SLO: p95 < 300ms
* `GET /v1/payments/{payment_id}` — fetch payment status
* `POST /v1/payments/{payment_id}/capture` — capture held funds
* `POST /v1/payments/{payment_id}/refund` — initiate refund
* `POST /v1/payments/{payment_id}/void` — void uncaptured auth
* `GET /v1/batches/{batch_date}` — query settlement batch
* `GET /v1/reconciliation/exceptions` — unresolved issues

**Internal RPCs:**
* AuthService.authorize() → {approved, external_auth_id, risk_score}
* LedgerService.recordEntry() → {entry_id, balance}
* BatchService.buildBatch() → {batch_id, item_count}
* ReconciliationService.reconcileBatch() → {matched_count, exception_count}
* FraudService.checkRisk() → {risk_score, decision}

**Webhooks (to merchants):**
* Events: payment.authorized, payment.captured, payment.failed, chargeback.created
* Signature: HMAC SHA-256

---

## 7) Start from simple design (ASCII diagram)

**Goal:** Correct, easy-to-explain flow: client → authorize/hold → store → daily batch → acquirer

### ASCII diagram (simple)

```
[Client]
   |
   | POST /v1/payments (idempotency_key)
   v
[API Gateway / LB]
   |
   v
[Auth Service] <---------> [Payment Gateway (external)]
   |                           ↓ (authorize card)
   | 1) Check idempotency      (approved/declined)
   | 2) Call gateway
   | 3) Persist to DB
   v
[Idempotency Store]  <---> [Transaction DB (PostgreSQL)]
(Redis, 72h TTL)           (PaymentRequest + AuthHold + LedgerEntry)
                                    |
                                    | (EOD)
                                    v
                           [Batch Job (daily cron)]
                                    |
                                    v
                           [Batch Submitter] ------> [Acquirer]
                                    |                    |
                                    v                    |
                           [Reconciler] <----------------+
                                    |
                                    v
                           [Updates LedgerEntry: settled/exception]
```

### Flow (happy path)

1. Client POST /payments with idempotency_key
2. Auth Service checks Redis (idempotency)
3. Calls gateway → returns approved + external_auth_id
4. Writes to DB (single ACID transaction): PaymentRequest + AuthHold + LedgerEntry
5. Returns payment_id to client
6. EOD: Batch Job queries authorized holds, generates batch file
7. Batch Submitter → Acquirer
8. Reconciler matches response, updates ledger (settled) or flags exceptions

**Limitations:** Single DB bottleneck >2k TPS, no retries, no fraud detection, manual reconciliation

---

## 8) Enrich the design (production-grade: scale / HA / reliability)

### ASCII diagram (enriched)

```
[Client] --POST /payments--> [API Gateway] --validate--> [API Service]
                                                              |
                     +----------------------------------------+
                     |                                        |
                     v                                        v
            [Idempotency Store]                     [Kafka: payments-ingest]
            (Redis + DB backup)                      (durable, ordered)
                                                              |
                                                              v
                                                    [Authorization Workers]
                                                    (consume, autoscale)
                                                         |  |  |
                          +------------------------------+  |  +----------------------+
                          |                                 |                         |
                          v                                 v                         v
                  [Fraud Detection]              [Payment Gateway]           [Token Vault]
                   (risk scoring)                  (authorize)              (PAN storage)
                          |                                 |
                          +-----------------+---------------+
                                            |
                              +-------------+-------------+
                              |                           |
                              v                           v
                    [Transaction DB]           [Kafka: payments-events]
                    (sharded, ACID)            (status updates)
                         |                              |
                         |                              v
                         |                    [Batch Builder (stream)]
                         |                              |
                         |                              v
                         |                    [Batch Submitter] --> [Acquirer(s)]
                         |                              |
                         v                              v
                  [Read Replicas]              [Reconciler] --> [DLQ / Ops UI]
```

### Key improvements vs simple

* **Kafka decoupling:** Durable ingest isolates spikes, enables replay
* **Async workers:** Scale independently, handle retries
* **Hybrid idempotency:** Redis (fast) + DB (durable)
* **Fraud detection:** Integrated before gateway call
* **Multi-acquirer:** Routing + failover
* **Streaming reconciler:** Auto-match responses, DLQ for exceptions

### Enriched flow (numbered steps)

**Phase 1: Ingestion (fast ack <50ms)**
1. Client → API Gateway → API Service
2. Validate + check Redis idempotency
3. Publish to Kafka payments-ingest (sync, wait for ack)
4. Return 202 Accepted {payment_id, status=processing}

**Phase 2: Authorization (async ~200ms)**
5. Worker consumes from Kafka (partitioned by merchant_id)
6. Fetch token from vault → call fraud detection → get risk_score
7. If risk_score >80: decline; if >60: require 3DS; else: call gateway
8. Gateway returns approved/declined + external_auth_id
9. Write to sharded Transaction DB (ACID transaction)
10. Publish to payments-events topic
11. Update idempotency store with final result

**Phase 3: Batch & Settlement (EOD)**
12. Batch Builder (stream processor) accumulates authorized txns
13. At cutoff, close batch → format for acquirer → submit
14. Acquirer processes overnight → returns response file
15. Reconciler matches responses, updates ledger (settled), flags exceptions to DLQ
16. Ops reviews exceptions in UI

---

## 9) Important design patterns & components

1. **Idempotency Store (hybrid):** Redis L1 (fast <10ms) + DB L2 (durable, unique constraint) + 72h TTL

2. **Event-sourcing + ACID ledger:** Kafka for audit/replay + ACID DB for monetary correctness = best of both

3. **Batch formation:** Time-based cutoff (11:59 PM daily) with merchant overrides, stream processor accumulates

4. **Multi-acquirer routing:** Routing table {merchant_id, currency, region} → acquirer_id; adapter pattern for format transformation; failover on circuit breaker open

5. **Reconciliation:** 3-phase (95% auto-match by external_auth_id → 4% fuzzy match → 1% manual exceptions)

6. **Tokenization:** Gateway-managed or network tokens (reduces PCI scope to SAQ-A)

7. **Retry & circuit breaker:** Max 3 retries, exponential backoff (1s, 2s, 4s), jitter ±20%; circuit breaker: closed/open (after 10 failures for 60s)/half-open

---

## 10) Possible deep dives (options, trade-offs, preferred)

### A. Idempotency implementation

**Options:**
1. Fast KV only (Redis): Low latency but data loss risk
2. DB-only (unique constraint): Durable but slower (50ms vs 10ms)
3. Hybrid (Redis + DB): Fast + durable (preferred)
4. Kafka-centric: Complex, doesn't protect against client retries

**Preferred: Hybrid**
* L1: Redis check (fast path, 72h TTL)
* L2: DB INSERT ... ON CONFLICT DO NOTHING (safety net, unique constraint)
* On Redis miss → check DB → cache result
* On Redis down → fall back to DB (slower but correct)
* Store: {idempotency_key, request_hash, response, status, created_at, payment_id}

**Key insight:** Redis alone risky (eviction), DB alone slow, hybrid balances speed + durability

---

### B. Ledger & transactional correctness

**Options:**
1. ACID DB only: Strong consistency, easy queries, but scaling writes hard
2. Event-sourced: Perfect audit, replay, but eventual consistency challenges
3. Hybrid (Kafka + ACID DB): Audit trail + correctness (preferred)
4. Blockchain: Tamper-proof but extremely slow

**Preferred: Hybrid**
* Write events to Kafka for audit/replay (7-day retention)
* Write ledger entries to ACID DB for queries (source of truth for balances)
* Use transactional outbox pattern: DB INSERT payment + INSERT outbox event → separate poller publishes to Kafka
* Ledger table: append-only, no UPDATEs, monthly partitions, unique constraints prevent double-spend

**Why transactional outbox?** Avoids dual write problem; if DB commits but Kafka fails, poller retries

---

### C. Exactly-once vs at-least-once delivery — CRITICAL

**Problem:** True exactly-once impossible with external gateway; payments require effectively exactly-once (no double charges)

**Options:**
1. Two-phase commit: **Impossible** (gateway doesn't support 2PC; blocks; high latency)
2. Idempotent delivery (at-least-once + client dedup): Industry standard (preferred)
3. Transactional outbox: At-least-once with ordering
4. Saga: Eventual consistency, too complex for sync payments

**Preferred: Idempotent delivery + reconciliation**

**Why true exactly-once impossible:**
* Gateway is autonomous - can't control when it commits
* Network partition scenarios: gateway approves → DB write fails → orphaned auth
* No atomic operation across local DB + external gateway

**Solution:**
* Idempotency keys make operations safe to retry
* DB unique constraint prevents duplicates
* Reconciliation catches orphaned authorizations (gateway approved but no local record)
* Daily job: query gateway for all auths → match against local DB → flag orphans → ops investigates

**Result:** Effectively exactly-once over time horizon (not immediate, but guaranteed within 24h)

---

### D. Batch formation & multi-acquirer routing

**Options:**
1. Single acquirer, fixed time: Simple but single point of failure
2. Static routing rules: {merchant, currency} → acquirer; redundancy but static
3. Dynamic routing with health: Route to healthiest; resilient but complex
4. Hybrid: Static primary + dynamic fallback (preferred)

**Preferred: Hybrid**
* Routing table with merchant overrides → currency defaults
* Circuit breaker per acquirer (10 failures → open for 60s)
* Fallback to secondary if primary unhealthy
* Acquirer adapter pattern: abstract interface, concrete implementations per acquirer (CSV/XML/JSON formats)
* Batch-level idempotency: batch_idempotency_key prevents duplicate submissions

---

### E. Fraud detection & risk management

**Options:**
1. Rule-based: Simple but high false positives (10-15%), easy to bypass
2. ML scoring: Adapts, lower false positives (3-5%), but complex
3. 3D Secure (3DS): Very high detection (98%+) but adds friction (~20% abandonment)
4. Post-auth review: Low friction but delayed settlement

**Preferred: ML scoring + 3DS for high-risk + post-auth review**

**Key signals:** Velocity (txns per time window), geo mismatches, amount anomalies (>3σ), device fingerprinting, billing/shipping mismatches, time-of-day patterns

**Decision thresholds:**
* risk_score ≥80: BLOCK
* risk_score 60-79: REQUIRE_3DS (cardholder authentication)
* risk_score 40-59: REVIEW (authorize but flag for manual review)
* risk_score <40: ALLOW

---

### F. Refunds & chargebacks

**Refunds:**
1. Same-day void (if unsettled): Call gateway to void, no fees, fast
2. Next-batch refund (standard): Include in next settlement, 1-3 day delay, low cost (preferred for settled txns)

**Chargebacks (customer disputes):**
1. Notification: Acquirer alerts us → debit merchant account immediately (chargeback_hold)
2. Representment: Merchant has 30 days to provide evidence
3. Resolution: Card network decides → merchant wins (release funds) or loses (merchant loses funds + $15 fee)
4. Ledger entries: chargeback_hold, chargeback_won, chargeback_lost

---

### G. Database sharding & partitioning strategy

**Options:**
1. Shard by merchant_id: Even distribution, single-merchant queries hit one shard, but hot merchants create hotspots
2. Shard by time: Natural archival, but current shard gets all writes (hotspot)
3. Hybrid: merchant_id sharding + time partitioning (preferred)
4. Consistent hashing: Easy resharding but complex

**Preferred: Hybrid (merchant sharding + monthly time partitioning)**
* 5-10 shards by hash(merchant_id)
* Within each shard, monthly partitions (DROP old partitions for archival)
* Hot merchant mitigation: monitor writes/shard; if >50%, move to dedicated shard; use read replicas
* Cross-shard reconciliation: daily job compares shard totals vs acquirer reports

---

### H. Kafka partitioning & consumer patterns

**Options:**
1. By merchant_id: Per-merchant ordering, workload distribution, but hot merchants → hot partitions
2. By payment_id: Perfect distribution, loses ordering
3. Custom: merchant_id for ordering-sensitive (preferred)

**Preferred: Hash by merchant_id with 50 partitions**
* Consumer lag threshold: alert >5k, page >20k
* Hot partition: if merchant causes >50% of partition traffic, route to dedicated partition or rate limit
* Autoscale consumers: target 1 consumer per partition

---

### I. Multi-region architecture

**Options:**
1. Active-passive: Simple, wastes capacity, high latency for distant users
2. Active-active: Low latency globally, data consistency challenges, duplicate risk
3. Regional isolation: EU data in EU, US in US; compliance-friendly (GDPR) (preferred)
4. Hybrid: Regional write + global read replicas

**Preferred: Regional isolation with cross-region read replicas**
* Each region: full stack (API, Kafka, DB, workers)
* Merchant assigned to home region (based on legal entity location)
* Payments written only to home region (compliance)
* Read replicas async-replicated to other regions (lag <1s)
* Reconciliation per-region independently
* Cross-region merchants: separate merchant IDs per region

---

### J. Testing strategy for payments

**Cannot test in production; need comprehensive pre-prod validation**

**Layers:**
1. **Sandbox mode:** Test acquirer, fake cards (4242...), safe, repeatable
2. **Shadow mode:** Fork production traffic, process but don't charge, compare results, real patterns
3. **Canary deployment:** 1% merchants first → monitor errors/latency → gradually increase to 100%
4. **Chaos engineering:** Kill workers, partition networks, inject latency, verify recovery

**Reconciliation testing:** Synthetic batches with known discrepancies, verify reconciler catches all

---

### K. Retry strategies & circuit breakers

**Retry policy:**
* Max 3 attempts
* Exponential backoff: 1s, 2s, 4s (multiplier 2.0, max 10s)
* Jitter: ±20% randomization
* Retry on: GatewayTimeout, ServiceUnavailable
* Don't retry on: InvalidRequest, DeclinedCard

**Circuit breaker:**
* **CLOSED:** Normal (all requests pass)
* **OPEN:** After 10 consecutive failures, block requests for 60s
* **HALF_OPEN:** After 60s, try 1 request; success → CLOSED, failure → OPEN (120s)

**Multi-acquirer failover:** Primary down (circuit open) → route to fallback acquirer

---

### L. Reconciliation deep dive

**Three-phase approach:**

**Phase 1: Real-time streaming (~95%)**
* As settlements come in, match by external_auth_id + amount
* Update ledger entry: status=settled
* Auto-matched, no human intervention

**Phase 2: Batch (daily, ~4%)**
* EOD: compare total amounts (internal ledger vs acquirer report)
* Fuzzy match unmatched by amount + date + merchant
* Flag for confirmation

**Phase 3: Exception handling (manual, ~1%)**
* Cannot auto-match → create ReconciliationRecord
* Ops UI shows full context: Kafka offsets, DB tx IDs, acquirer response
* Tools to replay events, create compensating transactions

**Orphaned authorization recovery:**
* Daily job: query gateway for all auths from previous day
* Match against local external_auth_ids
* Orphans: try to replay from Kafka, else create exception for ops review

---

## 11) Reliability & edge cases

**Critical scenarios:**

1. **Gateway outage during peak:** Circuit breaker → route to fallback acquirer → failing that, return 503 and queue for retry → orphaned auths detected by reconciliation

2. **DB partition failure during settlement:** Affected shard's batches delayed → health check fails write → alert ops → restore from replica or failover

3. **Clock skew:** Use sequence numbers (not timestamps) for ordering → enforce NTP sync → monotonic clocks for timeouts

4. **Split-brain (network partition):** Use consensus (etcd/ZooKeeper) for leader election → reject writes if can't reach quorum → CP system

5. **Integer overflow:** Use BIGINT or DECIMAL(19,4) → validation: amount > 0 and amount < MAX_SAFE_AMOUNT

6. **Duplicate external_auth_id from gateway:** Use composite key (external_auth_id + timestamp) → reconciler flags duplicates

7. **Thundering herd on retry:** Exponential backoff with jitter → rate limit retry queue → gradual ramp-up

8. **Byzantine failures (malicious merchant):** Server-side validation (never trust client) → rate limits per merchant → fraud detection → immutable audit logs

9. **Cascading failure (DB slow query):** Query timeouts (5s max) → circuit breaker per shard → read replicas for slow queries → kill long-running queries

10. **Partial network partition (can reach Kafka but not DB):** Worker health check includes DB connectivity → reject events if unhealthy → message remains in Kafka for retry

---

## 12) Operational concerns & observability

### Dashboard hierarchy

**Page 1: Health Overview**
* Authorization success rate (24h): target >99%
* Settlement success rate: target >99.9%
* Reconciliation exception rate: target <0.1%
* p50/p95/p99 authorization latency
* Active payments processing/second

**Page 2: Component Health**
* Kafka consumer lag (alert >5k, page >20k)
* DB replication lag (alert >5s, page >30s)
* Worker queue depth (alert >10k, page >50k)
* Circuit breaker states per acquirer
* Retry queue depth

**Page 3: Per-Merchant**
* Top 10 by volume, settlement failures, fraud blocks
* Chargeback rate (industry avg: 0.5-1%)

### Alerting levels

**Page (immediate):** Auth success <95% >5min, settlement batch failed, reconciliation exceptions >100/hour, DB unavailable, all acquirers circuit breakers open

**Urgent (15 min):** Auth p95 >500ms >10min, Kafka lag >20k, worker queue >50k, fraud service down, any shard >80% capacity

**Warn (next day):** Reconciliation 0.1-1%, circuit breaker flapping, DB query p95 >200ms, disk >70%

**Info:** New large merchant (>1k TPS), approaching capacity (80% in 7 days), unusual spike in refunds (+3σ)

### Tracing

* OpenTelemetry with correlation IDs
* Spans: API ingestion → Kafka → worker → gateway → DB → reconciliation
* Include: idempotency_key, payment_id, external_auth_id, merchant_id

---

## 13) Security & compliance

### PCI DSS
* Minimize scope: tokenization (gateway-managed or network tokens)
* Encryption: at-rest (AES-256), in-transit (TLS 1.3)
* Key management: KMS/Vault, auto-rotation every 90 days
* Access: least privilege, MFA for production, audit all access
* Network: isolated VPC, no direct internet, whitelist IPs

### AML/KYC
* Flag txns >$10k (CTR threshold)
* Detect structuring (multiple $9,999 to avoid $10k)
* Velocity: >$50k/merchant/day
* Merchant verification: business license, tax ID, beneficial ownership, OFAC screening

### PSD2 (EU)
* SCA requirement: >€30 transactions require 3D Secure
* Exemptions: low-risk (<0.13% fraud rate), recurring (after first auth), merchant-initiated

### GDPR
* Retention: 7 years (financial records override right to erasure)
* Data minimization: only store necessary PII
* Explicit consent for processing

### Audit logging
* Immutable append-only logs (write-only, no updates/deletes)
* Log: all state changes, manual interventions, access to sensitive data, config changes
* Storage: immutable S3 (versioning enabled, MFA delete)

---

## 14) Cost optimization

### Infrastructure (monthly, 10k TPS)
* Compute: $100k (2,000 instances)
* Database: $30k (sharded)
* Kafka: $15k
* Redis: $10k
* S3: $5k
* Network: $10k
* **Total infra: ~$170k**

### Transaction costs
* **Gateway fees: $32M** (2.9% + $0.30 per txn - dominant cost)
* Chargeback fees: $150k ($15 per chargeback × 1% rate)

### Optimization
* Spot instances for workers (70% savings)
* Autoscale aggressively
* Compress old ledger entries (5:1 gzip)
* Archive >1yr to S3 Glacier (90% cheaper)
* Partition tables, drop old partitions
* Cache hot merchants in Redis
* Negotiate volume discounts with gateway (>10M txns/month)
* Use cheaper acquirers for low-risk
* VPC endpoints (avoid NAT charges)

---

## 15) Interview Presentation Guide (60-minute interview)

### Time budget

**Phase 1: Clarifications (5 min)**
* Must-ask: Scope (card only?), scale (10k TPS?), delivery semantics (at-least-once?), fraud in scope?, multi-region?
* Tip: State assumptions clearly

**Phase 2: Requirements & BoE (5 min)**
* FR: auth, holds, batch, refunds, idempotency, fraud
* NFR with SLOs: auth p95 <300ms, 99.95% availability, zero double-charges, settlement >99.9%
* BoE: 10k TPS = 100M txns/day, 150 GB/day storage, 2000 workers, $32M gateway fees/month
* Skip: Detailed storage derivations

**Phase 3: Simple Design (8 min)**
* Draw Section 7 diagram
* Walk through: client → auth service → gateway → DB → batch job → acquirer → reconciler
* Limitations: single DB bottleneck >2k TPS, no retries, manual reconciliation

**Phase 4: Enriched Design (12 min)**
* Draw Section 8 diagram
* Add: Kafka, async workers, hybrid idempotency, fraud detection, multi-acquirer, streaming reconciler, DLQ
* Flow: API → Kafka (202 Accepted) → workers → fraud + gateway → sharded DB → batch builder → reconciler
* Failure handling: DB fails → retry, gateway timeout → circuit breaker → fallback, orphaned auths → reconciliation

**Phase 5: Deep Dive (20 min) — Pick 3-4**

**Most likely (OpenAI Staff Infra):**
1. **Exactly-once vs at-least-once (10C)** — Why impossible, idempotent solution, orphaned auth handling, reconciliation
2. **Database sharding (10G)** — Hybrid (merchant + time), hot shard mitigation, cross-shard reconciliation
3. **Ledger correctness (10B)** — Transactional outbox, double-spend prevention, append-only design
4. **Multi-region (10I)** — Regional isolation, GDPR compliance, cross-region reads

**Also common:** Idempotency (10A), fraud (10E), Kafka (10H), retries (10K)

**Strategy:** Go deep on 2 topics (15 min), show breadth on others (5 min)

**Phase 6: Trade-offs (5 min)**
* At-least-once vs exactly-once: Chosen for simplicity, industry standard; trade-off requires idempotency + reconciliation
* Async vs sync: Chosen for scale (10k TPS); trade-off slightly higher complexity
* Hybrid idempotency vs Redis-only/DB-only: Chosen for fast + durable; trade-off dual write
* Regional isolation vs active-active: Chosen for compliance; trade-off no automatic global failover

**Phase 7: Q&A (5 min)**
* "What if gateway returns 200 but doesn't actually authorize?" → Reconciliation detects mismatch
* "How prevent one merchant affecting others?" → Sharding, rate limiting, circuit breakers
* "What if 100M merchants (1M TPS)?" → 500+ Kafka partitions, 50+ DB shards, 20k+ workers, $5M infra + $300M gateway fees
* "How test without charging real money?" → Sandbox, shadow mode, canary, chaos
* "What if Kafka fails?" → Replicated (factor 3), if catastrophic: replay from DB
* "Schema evolution?" → Schema registry, backward-compatible only, version events

### Must-cover sections
* ✅ Clarifications (5 questions)
* ✅ FR/NFR with SLOs
* ✅ Simple design
* ✅ Enriched design
* ✅ Deep dives: Idempotency, Ledger, Exactly-once
* ✅ Trade-offs

### Pro tips
* **Signal:** "Let me start with clarifications, then simple design, then scale it up"
* **Check in:** "Should I go deeper on X or move to Y?"
* **Manage time:** If 35 min and no enriched design, speed up
* **Show trade-offs:** "For X, we have 4 options [list]. I prefer Y because [reason], but if [constraint], I'd choose Z"
* **Cite industry:** "Stripe uses at-least-once, PayPal uses batch settlement"
* **Quantify:** "Direct DB works up to 2k TPS, beyond that need sharding"
* **Discuss failures:** "If gateway approves but we crash, reconciliation detects within 24h"

### Common mistakes to avoid
* ❌ Jumping to complex design without simple
* ❌ Not stating assumptions
* ❌ Over-designing for unasked scale
* ❌ Ignoring failure scenarios
* ❌ Not discussing trade-offs
* ❌ Too long on BoE

### For OpenAI Staff role
* Emphasize distributed systems: exactly-once semantics, partitioning, consensus, failure modes
* Show operational maturity: monitoring, alerting, incident response
* Demonstrate cost awareness: $170k infra vs $32M gateway fees
* Scalability thinking: 10k TPS → 100k TPS → 1M TPS progression

---

**Good luck! 🚀**
