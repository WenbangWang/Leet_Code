https://pyemma.github.io/How-to-design-auction-system/

# Auction Bidding System Design

## 1. Clarification Questions (Ordered by FR → NFR)

### Functional Requirements (FR) Questions

1. **Auction Types & Rules**

    * Are multiple auction types supported (English, Dutch, Sealed-bid), or only one?

        * **Maps to:** FR → Auction Management (support for multiple auction types).
    * Can a single auction have multiple items, or is it limited to one?

        * **Maps to:** FR → Auction Management (single-item vs multi-item support).

2. **Bid Handling**

    * Are users allowed to update or cancel bids after submission?

        * **Maps to:** FR → Bid Management (place/update/cancel bid).
    * How strict should bid timing be near auction close (handling last-second bids)?

        * **Maps to:** FR → Bid Management (timing rules).

3. **Payment & Transactions**

    * Should payments be processed immediately at auction end or can settlement be deferred?

        * **Maps to:** FR → Payments & Transactions.
    * Are refunds required for all non-winning bidders, or only under specific conditions?

        * **Maps to:** FR → Payments & Transactions.

4. **Notifications & Real-Time Updates**

    * Are notifications limited to outbid/winning alerts or more extensive event tracking required?

        * **Maps to:** FR → Notifications & Real-Time Updates.

5. **Search & Analytics**

    * Do we need historical analytics, or only analytics for current auctions?

        * **Maps to:** FR → Search & Analytics.

### Non-Functional Requirements (NFR) Questions

1. **Scale & Concurrency**

    * How many concurrent users or bids should the system handle?

        * **Maps to:** NFR → Performance & Scalability.
    * Will there be “hot auctions” with extremely high traffic spikes?

        * **Maps to:** NFR → Availability & Fault Tolerance, Performance & Scalability.

2. **Bid Handling Timing**

    * How strictly should bid timing be enforced near auction close?

        * **Maps to:** NFR → Consistency (prevent race conditions).

3. **Notifications & Real-Time Updates**

    * What is the required latency for bid updates (WebSocket-level or eventual consistency)?

        * **Maps to:** NFR → Performance & Consistency.

4. **Search & Analytics Data Retention**

    * How long should auction and bid data be stored?

        * **Maps to:** NFR → Durability & Data Retention.

5. **Failure Handling**

    * What level of fault tolerance is expected for high-value auctions?

        * **Maps to:** NFR → Availability & Fault Tolerance.
    * Should the system guarantee that no bids are lost under any failure scenario?

        * **Maps to:** NFR → Consistency & Durability.

## 2. Functional Requirements (FR)

Derived from the clarification questions:

1. **Auction Management**

    * Support multiple auction types (English, Dutch, Sealed-bid).
    * Single-item per auction initially, extendable to multi-item.
    * Create, update, start, and end auctions with defined timing rules.

2. **User & Bid Management**

    * User registration, login, and authentication.
    * Place, update, and optionally cancel bids before auction close.
    * Handle last-second bid concurrency.

3. **Payments & Transactions**

    * Process winning bid payments immediately after auction ends.
    * Refund non-winning bidders as required.

4. **Notifications & Real-Time Updates**

    * Notify users on outbid, auction ending, or winning.
    * Support low-latency updates using WebSockets or SSE.

5. **Search & Analytics**

    * Search auctions by keyword, category, and filters.
    * Track bid trends, auction popularity, and historical analytics.

## 3. Non-Functional Requirements (NFR)

1. **Performance & Scalability**

    * Handle thousands of bids/sec for hot auctions.
    * Enable horizontal scaling for services and databases.

2. **Availability & Fault Tolerance**

    * Maintain high availability during peak auction periods.
    * Ensure no lost bids under failures.

3. **Consistency & Durability**

    * Guarantee atomic updates for concurrent bids.
    * Persist all auctions, bids, and transaction data reliably.

4. **Security & Compliance**

    * Secure authentication and payment processes.
    * Protect against bid fraud or system tampering.

## 4. Back of Envelope Calculation

* **Users:** 10M
* **Concurrent users:** 100K
* **Active auctions:** 50K
* **Peak bids/sec:** 5K

**Storage:**

* Each auction: 1 KB
* Each bid: 0.5 KB
* Bids/day: 5,000 \* 86,400 ≈ 432M → 216 GB/day
* 1-month storage: \~6.5 TB

**Traffic:**

* Average bid request: 1 KB payload
* Peak: 5 MB/sec (\~43 Gbps/day)

## 5. Core Data Entities

| Entity       | Attributes                                                              | Description                |
| ------------ | ----------------------------------------------------------------------- | -------------------------- |
| User         | user\_id, name, email, password\_hash, balance                          | Registered bidder          |
| Auction      | auction\_id, item\_name, start\_time, end\_time, reserve\_price, status | Auction details            |
| Bid          | bid\_id, auction\_id, user\_id, bid\_amount, timestamp                  | Tracks bids                |
| Transaction  | transaction\_id, auction\_id, user\_id, amount, status                  | Payment/refund tracking    |
| Notification | notification\_id, user\_id, auction\_id, type, status                   | Outbid, won, ending alerts |

## 6. System Interfaces

1. **User Service API**: `/users/register`, `/users/login`, `/users/{id}`
2. **Auction Service API**: `/auctions` (create), `/auctions/{id}`, `/auctions?filters`
3. **Bidding Service API**: `/bids` (place), `/bids/{id}` (update), `/bids?auction_id`
4. **Notification Service API**: `/notifications/{user_id}`, `/notifications`
5. **Payment Service API**: `/payments`, `/refunds`

## 7. Simple Design

```
           +-------------------+
           |    User Service   |
           +-------------------+
                     |
                     v
           +-------------------+
           |   Auction Service |
           +-------------------+
                     |
                     v
           +-------------------+
           |  Bidding Service  |
           +-------------------+
                     |
                     v
           +-------------------+
           |     Database      |
           +-------------------+
```

**Pros:** Simple, easy to implement
**Cons:** Single DB bottleneck, poor scalability

## 8. Enriched Design

```
           +-------------------+
           |    User Service   |
           +-------------------+
                     |
                     v
           +-------------------+           +-------------------+
           |   Auction Service | <-------> | Notification Svc  |
           +-------------------+           +-------------------+
                     |
                     v
           +-------------------+           +-------------------+
           |  Bidding Service  | <-------> |  Payment Service  |
           +-------------------+           +-------------------+
                     |
                     v
           +-------------------+           +-------------------+
           |     DB Shards     |           |    Redis Cache    |
           +-------------------+           +-------------------+
                     |
                     v
               Message Queue (Kafka/RabbitMQ)
```

**Enhancements:**

* Redis for hot auctions
* Message queue for decoupled notifications and payments
* DB sharding for scalability
* Microservices separation

## 8. Possible Deep Dives

### 8.1 Concurrency Handling

**Problem:** Multiple users may place bids at the same time.

**Solution:**

* Use optimistic locking with version numbers in the database.
* Use Redis as a distributed in-memory store for hot auctions.
* Implement atomic operations for updating current highest bid.

### 8.2 Real-Time Bidding Updates

**Problem:** Users need immediate feedback for bids.

**Solution:**

* Use WebSockets or Server-Sent Events (SSE).
* Update bid cache in Redis and push notifications.

### 8.3 Payment and Transaction Reliability

**Problem:** Winning bids must be processed reliably.

**Solution:**

* Use two-phase commit or saga pattern.
* Maintain idempotency in payment processing.
* Log all transactions for audit purposes.

### 8.4 Auction Types

**Problem:** Different auctions have different rules.

**Solution:**

* Abstract auction logic in a strategy pattern.
* English auction: highest bid wins.
* Dutch auction: price decreases until a bid.
* Sealed-bid: all bids revealed at auction close.

### 8.5 Scaling Hot Auctions

**Problem:** Popular items may receive thousands of bids/sec.

**Solution:**

* Use bid caching with Redis.
* Partition auctions across multiple servers.
* Queue bid writes to DB asynchronously to reduce DB contention.

### 8.6 Search and Discovery

**Problem:** Users must find auctions efficiently.

**Solution:**

* Use Elasticsearch or similar search engine.
* Index auctions by name, category, end time.
* Support filtering, sorting, and full-text search.
