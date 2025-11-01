https://www.hellointerview.com/learn/system-design/problem-breakdowns/ticketmaster
# Ticketing System Design

## Clarification Questions (Ordered by FR → NFR)

### Functional Clarifications
1. Do we support both **assigned seating** (seat maps) and **general admission (GA)** tickets?
2. Should users be able to **view their booked events** in their account/profile?
3. Do we need an **admin portal** for event creation, management, and reporting?
4. Is **dynamic pricing** required for high-demand events, or do we keep fixed tiers?
5. Do we need to handle **refunds, cancellations, and ticket transfers**?
6. Should the system support **search** (by event, location, date, artist) or only browsing?
7. Should we expose APIs for **third-party sellers/venues**?
8. How should **reservation/hold windows** work (e.g., 10 minutes hold before payment)?

### Non-Functional Clarifications
1. Should **event browsing/search** optimize for high **availability** with possible slight staleness, while **booking** requires strict **consistency**?
2. What’s the expected concurrency scale during **flash sales** (e.g., 200k concurrent vs 10M attempts for one event)?
3. What is the assumed **read:write ratio** (commonly ~100:1)?
4. Should search queries return in **<500ms globally**?
5. What’s the level of **fault tolerance / DR** needed (multi-region active-active or active-passive)?
6. Are there **compliance requirements** (PCI DSS, GDPR, auditing)?
7. Should the system ensure **regular backups, CI/CD, and observability pipelines** as core requirements?
8. Is **real-time seat availability** required (instant seatmap updates) or can we tolerate small eventual consistency delays?

---

## Functional Requirements (FR)

1. Users can browse/search for events by location, date, category, or performer.
2. Users can view details of an event, including available seats, pricing tiers, and seat maps.
3. Users can select tickets (seat-based or GA) and place them in a cart/hold.
4. Users can purchase tickets within a time-limited hold window.
5. Users can view their purchased tickets and booking history.
6. Admins can create, update, and manage events, pricing, and seating maps.
7. Support both **assigned seating** and **general admission** models.
8. Support **refunds, cancellations, and ticket transfers** where policies allow.
9. Provide an **API** for venues or partners to integrate with the system.

---

## Non-Functional Requirements (NFR)

1. **Scalability**: Handle tens of millions of users browsing and hundreds of thousands concurrently purchasing during peak events.
2. **Consistency**: Prevent double-sells of the same ticket; bookings must be ACID-compliant.
3. **Performance**: Search and browsing responses under 500ms; booking transactions under 2s.
4. **High availability**: 99.99% uptime; multi-AZ redundancy.
5. **Fault tolerance**: Automatic failover; recover from regional outages with minimal downtime.
6. **Security & compliance**: PCI DSS for payments, GDPR for user data, audit logging.
7. **Resilience**: System should degrade gracefully under heavy load (e.g., queue requests instead of failing).
8. **Monitoring/observability**: Real-time metrics, logging, and alerting for failures.
9. **Data retention**: Store booking records and payment history for compliance.

---

## Back of Envelope Calculations

- **Users**: 100M registered users.
- **Peak load**: 10M concurrent users during a global on-sale (e.g., Taylor Swift concert).
- **Read/write ratio**: ~100:1 (lots of browsing, fewer purchases).
- **Event size**: A large stadium event = 100k seats.
- **Ticket size**: Each ticket record ~500 bytes (metadata, user, seat info, audit).
- **Storage needs**:
  - 1B tickets = ~500 GB raw ticket data (not including indexes & logs).
  - Add replication (×3) → ~1.5 TB storage.
- **Traffic**:
  - Peak booking requests = 500k/sec globally.
  - Peak search queries = 50M/sec during big events.
- **Hold window**: 10 minutes = must maintain up to millions of concurrent holds in cache (Redis).

---

## Core Data Entities

1. **User**
  - user_id, name, email, payment methods.

2. **Event**
  - event_id, title, performer, venue_id, date_time, metadata.

3. **Venue**
  - venue_id, name, location, seating_map.

4. **Seat**
  - seat_id, venue_id, section, row, number, status (available, held, sold).

5. **Ticket**
  - ticket_id, event_id, user_id, seat_id/GA_count, status (reserved, sold, cancelled).

6. **Order**
  - order_id, user_id, event_id, tickets[], payment_status, timestamp.

7. **Payment**
  - payment_id, order_id, processor_id, status, amount.

8. **Hold**
  - hold_id, seat_id or GA bucket, user_id, TTL.

---

## System Interfaces

- **User APIs**
  - `GET /events?query=...`
  - `GET /events/{id}`
  - `POST /cart/hold`
  - `POST /checkout`
  - `GET /orders`
  - `POST /orders/{id}/cancel`

- **Admin APIs**
  - `POST /events`
  - `PUT /events/{id}`
  - `POST /venues`
  - `POST /pricing`

- **Internal APIs**
  - `POST /payment/process`
  - `POST /notification/send`
  - `POST /analytics/stream`

---

## Simple Design

```
+---------+        +------------+        +------------+ 
|  Client | <----> |  API GW    | <----> |  Services  |
+---------+        +------------+        +------------+
                                      /-> Booking Service
                                     /-> Search Service
                                    /-> Payment Service
                                   /-> Admin Service
                                      
          +------------------+ 
          |  Database (SQL)  |  <--- holds ticket/order data
          +------------------+
          |   Cache (Redis)  |  <--- holds, seat availability
          +------------------+
```

---

## Enriched Design

```
             +-------------------+
             |      Clients      |
             |  Web / Mobile App |
             +-------------------+
                        |
                        v
                 +--------------+
                 |   API GW     |
                 +--------------+
                  /      |      \
                 /       |       \
                v        v        v
        +-----------+  +-------------+  +-------------+
        | Search    |  | Booking     |  | Payment     |
        | Service   |  | Service     |  | Service     |
        +-----------+  +-------------+  +-------------+
            |              |                 |
            v              v                 v
    +---------------+  +---------+     +-------------+
    | ElasticSearch |  | Redis   |     | Payment API |
    +---------------+  +---------+     +-------------+
                         |   ^
                         v   |
                    +-------------+
                    | SQL DB      |
                    | (Orders,    |
                    | Tickets,    |
                    | Seats)      |
                    +-------------+
```

---

## Possible Deep Dives (expanded)

### 1) Concurrency control & avoiding double-sells
- **Problem**: Multiple users try to buy the same seat simultaneously.
- **Solutions**:
  - DB transactions (`SELECT FOR UPDATE`): simple, strong consistency, bottlenecks at scale.
  - Distributed locks (Redis/ZooKeeper): scalable, risk of lock expiry issues.
  - **Recommended**: Redis holds + DB finalization (SETNX with TTL for holds, then DB transaction at checkout).
- **Implementation Sketch**:
  - User clicks seat → Redis `SETNX seat:123 holdId TTL=10m`.
  - At checkout → verify hold ownership, commit DB seat→sold, delete Redis hold.

---

### 2) Handling flash sales & thundering herd
- **Problem**: Millions of concurrent attempts overload system.
- **Mitigations**:
  - Queue-based admission (Kafka/Redis streams).
  - Client polling with backpressure.
  - Lotteries/pre-reservation for fairness.
  - Edge caching (pre-rendered seatmaps).
- **Implementation Sketch**: Admission queue throttles booking attempts, workers process sequentially.

---

### 3) Data modeling & partitioning for scale
- **Approach**:
  - Shard orders/tickets by `event_id`.
  - Separate OLTP DB (transactions) vs OLAP DB (analytics).
  - Consider distributed SQL (Spanner/CockroachDB) if global strong consistency needed.
- **Trade-offs**: Sharding simpler but requires routing infra; distributed SQL more powerful but complex.

---

### 4) Caching, search, and seatmap UX
- **Problem**: Seatmap must reflect availability near real-time.
- **Approach**:
  - Redis cache per event → seat state bitmap.
  - Update cache on holds/bookings.
  - Elasticsearch for search queries.
- **Trade-offs**: High churn in Redis if too many updates; batching can help.

---

### 5) Payments & failures
- **Problem**: Payment succeeds but DB write fails → inconsistent state.
- **Approach**:
  - Two-step flow: mark order pending, call payment API, finalize in DB.
  - Reconciliation worker for stuck orders.
  - Idempotency keys for all payment calls.

---

### 6) High-availability & disaster recovery
- **Approach**:
  - Multi-AZ deployment with replication.
  - Cross-region failover (active-passive or active-active).
  - Audit logs and PITR (point-in-time recovery).

---

### 7) Security & compliance
- **Approach**:
  - PCI-compliant payment providers (Stripe, Adyen).
  - Signed/encrypted tickets (JWT).
  - Bot defense (rate limiting, CAPTCHA, device fingerprinting).  
