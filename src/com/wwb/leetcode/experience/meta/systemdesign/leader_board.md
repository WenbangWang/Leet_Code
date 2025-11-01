https://bytebytego.com/courses/system-design-interview/real-time-gaming-leaderboard

# Real-Time Leaderboard Design (Durable Storage + Cache when needed)

## 1. Clarification Questions (FR → NFR)

### Functional
1. Real-time or eventual consistency acceptable?
2. Maximum number of users (global scale)?
3. Do scores only increase, or can they decrease?
4. Do we need top-k around user for both global and friends?
5. Should we support pagination?
6. How do we break ties?
7. Do we need historical snapshots?

### Non-Functional
1. Latency target for queries (<100ms)?
2. Expected throughput of updates/queries?
3. SLA for availability (99.9% vs 99.99%)?
4. Tolerance for slightly stale cache results?

---

## 2. Functional Requirements (FR)

- Update user scores in real time.
- Provide global leaderboard.
- Provide friend-circle leaderboard.
- Fetch **top-k users around a given user** (both global & friends).
- Efficient lookup of a user’s rank.
- Handle ties consistently.

---

## 3. Non-Functional Requirements (NFR)

- **Durability**: DB as source of truth.
- **Scalability**: 100M+ users, 100K updates/sec.
- **Availability**: Highly available even under node failures.
- **Latency**: Queries <100ms for most cases.
- **Consistency**: Cache may be slightly stale, DB always correct.

---

## 4. Back-of-Envelope Calculation

- Users: 100M
- Friends/user: 500 avg
- Updates: 100K/sec
- Reads: 500K/sec
- Score storage: 100M × ~16 bytes (id + score) ≈ 1.6GB (fits in DB + cache)

---

## 5. Core Data Entities

| Entity      | Attributes                                  |
|-------------|---------------------------------------------|
| User        | user_id, name, friends_list                 |
| Score       | user_id, score, last_update_time            |
| Leaderboard | leaderboard_id, type (global/friend), rank_snapshot |

---

## 6. System Interfaces (REST Endpoints)

```
POST   /scores/update
  body: { "user_id": "u123", "score": 4500 }

GET    /leaderboard/global/top?k=100
  → top 100 users globally

GET    /leaderboard/global/{user_id}/rank
  → rank of a user globally

GET    /leaderboard/global/{user_id}/around?k=10
  → k users before and after user globally

GET    /leaderboard/friends/{user_id}/top?k=50
  → top 50 friends of user

GET    /leaderboard/friends/{user_id}/around?k=10
  → k friends before/after user
```

---

## 7. Start from Simple Design

- **Durable DB (Postgres / Cassandra / DynamoDB)** stores scores.
- Index on `score DESC` for ranking.
- Queries directly hit DB.

**ASCII Diagram — Simple Design**

```
             +---------------+
             |   User DB     |
             +-------+-------+
                     |
                     v
               +-----------+
               | Leaderboard|
               |   Service  |
               +-----------+
                /         \
               v           v
        +-----------+   +-----------+
        | Global DB |   | Friend DB |
        +-----------+   +-----------+
```

---

## 8. Enriched Design (to satisfy NFR)

- **Durable DB**: source of truth for all writes.
- **Cache layer (Redis/memory)**: only for hot queries (top-100 global, popular friend leaderboards).
- **Async processing**: updates via Kafka → workers → DB → optional cache refresh.
- **Sharding**: DB sharded by user_id.

**ASCII Diagram — Enriched Design**

```
           +-------------------+
           |   Clients (API)   |
           +---------+---------+
                     |
                     v
            +-----------------+
            | Leaderboard Svc |
            +--------+--------+
                     |
           +---------+----------+
           |                    |
    +-------------+       +-------------+
    | Persistent  |       | Cache Layer |
    | DB Cluster  |       | (hot keys)  |
    +------+------+       +------+------+
           |                     ^
           v                     |
    +-------------+              |
    | Kafka Topic |              |
    +------+------+              |
           |                     |
  +--------v--------+            |
  | Worker Consumers|------------+
  |  (update views, |
  |  refresh cache) |
  +-----------------+
```

---

## 9. Possible Deep Dives

### 9.1 Concurrency & Atomicity
- Use DB atomic upserts (`UPDATE score = GREATEST(old, new)`).
- Transactions prevent race conditions.

### 9.2 Friend Circle Leaderboard
- Query friend IDs → fetch scores → sort.
- For active users, maintain **precomputed friend leaderboard** in cache.

### 9.3 Scalability & Sharding
- DB sharded by user_id.
- Global leaderboard queries read from **materialized rank view** built by workers.

### 9.4 Top-K Around a User
- Compute rank: `SELECT COUNT(*) WHERE score > X`.
- Fetch surrounding scores with range query.
- Optimize with precomputed rank table or histogram buckets.

### 9.5 Real-Time Updates
- API → Kafka → Worker → DB.
- Workers refresh cache for hot leaderboards.
- Slightly stale reads acceptable.

### 9.6 Cache Management
- Cache only hot queries (top-100 global, active friend boards).
- TTL-based invalidation (e.g., 10s).

### 9.7 Fault Tolerance
- Persistent DB replication for durability.
- Kafka replication for event durability.
- Cache failures = fallback to DB.  
