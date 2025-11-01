https://www.hellointerview.com/learn/system-design/problem-breakdowns/fb-live-comments

# Live Comments System Design

## Clarification Questions (Ordered by FR → NFR)

### Functional Requirement Clarifications

1. Should users be able to edit or delete their comments after posting?
2. Are threaded/replies to comments required or only flat comment streams?
3. Should comments support rich media (images, GIFs) in addition to text and emoji?
4. Should reactions be real-time (visible instantly) or eventually consistent?
5. Should new users joining a live stream see all historical comments or only the last N comments?
6. Are there any content moderation requirements (e.g., profanity filtering, spam detection)?

### Non-Functional Requirement Clarifications

1. What is the maximum acceptable latency for displaying a new comment to users?
2. What is the peak expected concurrent viewer count for the most popular videos?
3. Are strong ordering guarantees required across all comments, or is eventual consistency acceptable?
4. How long should comments be retained in the system (short-term vs long-term storage)?
5. Should the system handle network partition gracefully and allow for eventual reconciliation?
6. What are the acceptable levels of availability (e.g., 99.9%, 99.99%)?
7. Are there specific SLA requirements for reaction counts or likes per comment?
8. Should rate-limiting/throttling be enforced on a per-user, per-video, or global basis?


## 1. Functional Requirements (FR)

* Users can post comments in real-time on posts or live videos.
* Users can see new comments in near real-time without refreshing the page.
* Users can like or react to comments.
* Comments should be delivered in the correct chronological order (or roughly correct at scale).
* Users can retrieve recent comments when joining a live session mid-stream.
* Support for different content types: text, emoji, GIFs.

## 2. Non-Functional Requirements (NFR)

* **Low latency:** \~100–300ms.
* **High availability:** Millions of concurrent users.
* **Scalability:** Horizontal scaling for popular live streams.
* **Durability:** No lost comments; replay possible after failure.
* **Consistency:** Eventual consistency acceptable for comment ordering; strong consistency for reactions.
* **Throughput:** Thousands of writes/sec for popular videos.
* **Fault tolerance:** System continues functioning if some nodes fail.

## 3. Back-of-Envelope Calculation

* Popular live video: 10M viewers.
* Average comments per viewer per minute: 0.01 → \~1.7K comments/sec.
* Average comment size: 200B → \~340 KB/sec ingestion.
* Daily storage for 1M live videos: \~29 GB/day.
* Peak fan-out per second: 10M viewers.

## 4. Core Data Entities

| Entity         | Fields                                                                                              |
| -------------- | --------------------------------------------------------------------------------------------------- |
| **Comment**    | comment\_id, video\_id/post\_id, user\_id, timestamp, content, parent\_comment\_id, reaction\_count |
| **Video/Post** | video\_id/post\_id, title, creator\_id, timestamp, view\_count                                      |
| **User**       | user\_id, name, avatar, metadata                                                                    |
| **Reaction**   | reaction\_id, comment\_id, user\_id, type, timestamp                                                |

## 5. System Interfaces

* `POST /comments` → Send a new comment.
* `GET /comments?video_id=...&limit=...&offset=...` → Fetch recent comments.
* `POST /comments/{id}/react` → Add a reaction.
* `GET /comments/stream?video_id=...` → WebSocket/SSE live updates.

## 6. Simple Design (to satisfy FR)

**Architecture Diagram (Simple Design):**

```
          ┌───────────────┐
          │    Clients    │
          └───────┬───────┘
                  │
        ┌─────────▼─────────┐
        │     API Server     │
        └─────────┬─────────┘
                  │
          ┌───────▼───────┐
          │      DB       │
          │ (SQL/NoSQL)   │
          └───────────────┘
```

**Workflow:**

1. Client posts comment → API server → DB.
2. Clients poll periodically to fetch new comments.

**Pros:**

* Simple, easy to implement.
* Works for small-scale scenarios.

**Cons:**

* Polling latency: 1–5 seconds.
* Doesn’t scale well for millions of users.
* High read load on DB.

## 7. Enriched Design (to satisfy NFR)

**Architecture Diagram (Enriched Design):**

```
          ┌───────────────┐
          │    Clients    │
          └───────┬───────┘
                  │ WebSocket/SSE
        ┌─────────▼─────────┐
        │ Comment Delivery   │
        │    Servers         │
        └─────────┬─────────┘
                  │
        ┌─────────▼─────────┐
        │  Message Queue    │
        │ (Kafka/Pulsar)    │
        └───────┬───────────┘
      ┌─────────┴─────────┐
      │  Cache (Redis)    │
      └─────────┬─────────┘
                │
        ┌───────▼─────────┐
        │   Sharded DB     │
        └─────────────────┘
```

**Workflow:**

1. Client connects via WebSocket → subscribes to `video_id`.
2. Posting a comment → API server → message queue.
3. Delivery servers fan out comments to subscribed clients.
4. Comments persisted asynchronously in cache and DB.
5. Reactions processed via aggregation layer.

**Pros:**

* Low-latency, real-time updates.
* Horizontal scaling.
* Reduced DB load with cache.

**Cons:**

* More complex.
* Requires careful partitioning for hot videos.

## 8. Possible Deep Dives (Expanded)

### 8.1 Scalability / Partitioning

* Partition comments by `video_id`.
* Replicate partitions for hot videos with millions of viewers.
* Horizontal scaling with multiple comment delivery servers behind load balancers.

### 8.2 Ordering Guarantees

* Use timestamp + comment\_id for ordering.
* Sequence numbers per video for stronger guarantees.
* Eventual consistency acceptable at extreme scale.

### 8.3 Caching Strategy

* Store last N comments per video in Redis for fast retrieval.
* Evict older comments asynchronously to DB.
* Cache supports new viewers joining mid-stream.

### 8.4 Message Queue Selection

* Kafka/Pulsar: Durable, scalable, supports fan-out.
* RabbitMQ: Simpler, for smaller-scale deployments.
* Each partition handles a subset of hot video streams.

### 8.5 Data Retention / Storage

* Cache: recent comments.
* Sharded DB: historical comments.
* Cold storage (S3/Bigtable) for analytics and long-term retention.

### 8.6 Delivery Optimization

* WebSocket clusters handle millions of concurrent clients.
* Hierarchical fan-out for extremely large live streams.
* Optional CDN edge nodes for reduced latency.

### 8.7 Throttling / Rate Limiting

* Prevent spam using token bucket or sliding window algorithms.
* Limits per user and per video.
* Excess comments may be queued or dropped.

### 8.8 Reaction Aggregation

* Reactions/likes aggregated in cache for fast read.
* Async update to DB to persist counts.
* Helps reduce write amplification on DB.

### 8.9 Fault Tolerance / Replay

* Message queue allows replay of missed comments.
* Cache replication ensures no data loss.
* Delivery servers reconnect clients after failures.

### 8.10 Analytics / Metrics

* Track CPS (comments/sec), latency, hot video detection.
* Metrics feed auto-scaling and throttling rules.
* Useful for operational insights and capacity planning.
