# Instagram System Design

---

## Clarification Questions

### Functional Requirement Clarifications

* Should the system support both photos and videos from day one, or start with photos only?
* Do we need ephemeral content (e.g., Stories/Reels), or focus only on permanent posts?
* Should the feed be strictly chronological, or ranked/personalized?
* Is real-time feed update required, or is eventual consistency acceptable?
* Do we need to support user tagging, mentions, or hashtags in posts?
* Should likes and comments update in real-time, or can they be eventually consistent?
* Are notifications push-only, or also available in-app?
* Do we need support for content discovery (Explore tab, recommended users/hashtags)?

### Non-Functional Requirement Clarifications

* What is the expected scale at launch (DAU, posts per day, read-to-write ratio)?
* What are the latency SLAs for feed fetch, post creation, and media delivery?
* Should availability prioritize feed reads over writes during failures?
* How critical is durability of likes/comments (are occasional losses tolerable)?
* What is the tolerance for stale data in feeds (seconds, minutes)?
* Should the system be multi-region active-active, or is active-passive acceptable?
* Is GDPR/CCPA compliance (data deletion/export) required from the beginning?
* What is the expected growth trajectory (region-specific rollout vs global scale)?

---

## 1. Functional Requirements (FR)

* **User Management**

  * Signup/login/logout
  * Follow/unfollow users
  * Profile info (bio, profile picture, etc.)
* **Posts**

  * Upload photos/videos
  * Caption, hashtags, tagging
  * Like, comment, share
* **Feed**

  * Personalized feed showing posts from followed users and recommended content based on interests and engagement
  * Infinite scroll / pagination
* **Stories**

  * Temporary media (24h)
  * View status
* **Notifications**

  * Likes, comments, follows
* **Search**

  * By username, hashtag
* **Direct Messaging** (optional for first pass)

---

## 2. Non-Functional Requirements (NFR)

* High Availability: handle billions of active users
* Low Latency: feed load < 200ms
* Scalability: horizontal scaling for users, posts, media
* Consistency vs Availability: eventual consistency acceptable for feed
* Durability: user-generated content must not be lost
* Extensibility: support new features (Reels, Ads, Shopping)

---

## 3. Back-of-the-Envelope Calculations

* **DAU:** 500M
* **Posts/day:** 100M
* **Average post size:** 1 MB
* **Likes/comments per post:** 50 on average
* **Feed load per user/day:** 100 posts

**Storage**

* Posts: 100 TB/day → \~36 PB/year
* Metadata (likes/comments): 500 GB/day

**Traffic**

* 500M users \* 100 posts/day = 50B feed items/day
* \~1M reads/sec assuming 2 feed loads/user/day

---

## 4. Core Data Entities

| Entity  | Fields                                                                       |
| ------- | ---------------------------------------------------------------------------- |
| User    | id, username, email, password\_hash, bio, profile\_pic, followers, following |
| Post    | id, user\_id, media\_url, caption, timestamp, likes\_count, comments\_count  |
| Comment | id, post\_id, user\_id, text, timestamp                                      |
| Like    | id, post\_id, user\_id, timestamp                                            |
| Feed    | user\_id, list of post\_ids, last\_updated                                   |

---

## 5. Simple Design

**Workflow:**

1. User uploads a post → stored in DB, media in local storage
2. Feed generated on-the-fly from followed users
3. Likes/comments stored in relational DB
4. User fetches feed → query DB → return top 20 posts → pagination

**ASCII Diagram:**

```
+--------+       +---------+       +---------+
| Client | --->  | Web/API | --->  |  DB     |
+--------+       +---------+       +---------+
     |                                 ^
     | Feed Fetch / Post Upload        |
     +---------------------------------+
```

**Pros:**

* Easy to implement
* Single source of truth, simple consistency

**Cons:**

* Not scalable beyond a few million users
* Slow feed generation for users with many follows
* Single point of failure

---

## 6. Enriched Design (Scalable)

**Improvements for scale:**

* **Media Storage:** Object storage + CDN
* **Databases:**

  * Users: Relational DB (sharded by user\_id)
  * Posts: NoSQL DB (sharded by post\_id)
  * Likes/comments: wide-column or relational DB
  * Feed: precomputed feeds in cache
* **Feed Generation:** Hybrid fan-out (write for normal users, read for celebrities, content-based recommendations handled at read time)
* **Asynchronous Processing:** Message queue (Kafka) for feed updates, notifications, analytics
* **Search:** Elasticsearch
* **Analytics:** Offline batch jobs for recommendations/trending

**ASCII Diagram:**

```
        +--------+
        | Client |
        +---+----+
            |
            v
        +---+---+
        | Web/API|
        +---+---+
            |
   +--------+--------+
   |                 |
   v                 v
+------+          +--------+
| Cache| <------> | Feed   |
+------+          | Service|
                  +---+----+
                      |
   +------------------+------------------+
   |       Database / Storage Layer       |
   | +--------+  +---------+  +--------+ |
   | | Users  |  | Posts   |  | Likes/  | |
   | | DB     |  | NoSQL   |  | Comments| |
   | +--------+  +---------+  +--------+ |
   +-------------------------------------+
            |
            v
     +-------------+
     | Message Q   |
     | (Kafka)     |
     +-------------+
```

**Workflow:**

1. Post upload → media stored in object storage → metadata written to DB → event pushed to queue → feeds updated asynchronously
2. Feed fetch → check cache → cache miss → generate feed → store in cache
3. Likes/comments → write to DB → update feed cache asynchronously
4. Recommendation service injects content-based posts outside follow graph

**Pros:**

* Scalable to hundreds of millions of users
* Low-latency feed via cache
* Supports offline analytics and ML

**Cons:**

* Increased complexity
* Cache invalidation can be tricky
* Fan-out for celebrities can be heavy

---

## 7. Simple API Design

### User APIs

| Endpoint            | Method | Request                     | Response                                                          | Notes               |
| ------------------- | ------ | --------------------------- | ----------------------------------------------------------------- | ------------------- |
| `/signup`           | POST   | {username, email, password} | {user\_id, token}                                                 | Create user         |
| `/login`            | POST   | {username/email, password}  | {token}                                                           | Auth token returned |
| `/user/{id}`        | GET    | -                           | {username, bio, profile\_pic, followers\_count, following\_count} | Public profile      |
| `/user/{id}/follow` | POST   | {target\_user\_id}          | {success}                                                         | Follow/unfollow     |

### Post APIs

| Endpoint              | Method | Request          | Response                                                                 | Notes            |
| --------------------- | ------ | ---------------- | ------------------------------------------------------------------------ | ---------------- |
| `/posts`              | POST   | {caption, media} | {post\_id}                                                               | Upload post      |
| `/posts/{id}`         | GET    | -                | {post\_id, user\_id, media\_url, caption, likes\_count, comments\_count} | Fetch post       |
| `/posts/{id}/like`    | POST   | {user\_id}       | {success}                                                                | Like/unlike post |
| `/posts/{id}/comment` | POST   | {user\_id, text} | {comment\_id}                                                            | Add comment      |

### Feed APIs

| Endpoint | Method | Request                   | Response     | Notes          |
| -------- | ------ | ------------------------- | ------------ | -------------- |
| `/feed`  | GET    | {user\_id, limit, cursor} | \[{post}, …] | Paginated feed |

### Search APIs

| Endpoint           | Method | Request | Response     | Notes              |
| ------------------ | ------ | ------- | ------------ | ------------------ |
| `/search/users`    | GET    | {query} | \[{user}, …] | Search by username |
| `/search/hashtags` | GET    | {query} | \[{post}, …] | Search by hashtag  |

---

## Deep Dives

### 1. Feed Generation Strategies

* **Fan-out-on-write:** Push new posts into followers’ feed caches at write time. Fast reads but high write amplification for celebrity posts.
* **Fan-out-on-read:** Fetch followed users’ posts at read time. Low write cost, but read latency increases.
* **Hybrid:** Normal users handled by fan-out-on-write, celebrities by fan-out-on-read. Feed service merges both at fetch.
* **Content-Based Candidates:** Recommend posts outside follow graph based on interests and interactions. Injected dynamically at read time.
* **Preferred:** Hybrid with content-based injection for personalized feed.

### 2. Ranking

* Ranking signals: recency, engagement probability, user relationship strength, content type.
* **Precompute at write time:** Fast reads, may become stale.
* **Dynamic at read time:** Fresh, but costly.
* **Preferred:** Hybrid — precompute for normal users, dynamic for celebrity-heavy feeds, apply content-based ranking at read time.

### 3. Message Queue Partitioning

* Used to propagate post, like, comment, follow events.
* **Partitioning options:**

  * By user\_id → preserves action ordering
  * By post\_id → groups engagement
  * By region → reduces cross-DC latency
* **Preferred:** user\_id partitioning for feed updates, region-based for media pipelines.

### 4. Database Sharding

* **Range-based:** simple but hotspot prone
* **Hash-based:** balanced, harder range queries
* **Directory-based:** flexible, extra lookup
* **Preferred:** hash-based for posts/likes/comments, directory-based for profiles at scale

### 5. Counters for Likes & Comments

* **Direct DB increments:** strong consistency, slower
* **Redis counters with flush:** fast, eventually consistent
* **Batch aggregation:** scalable, slightly stale
* **Preferred:** Redis counters with periodic flush to persistent store

### 6. Caching & Invalidation

* **TTL-based:** simple, may serve stale results
* **Write-through:** consistent, higher write load
* **Write-back:** fast writes, risk on crash
* **Event-driven invalidation:** accurate, complex
* **Preferred:** event-driven for feeds, TTL fallback for counters

### 7. Search & Hashtag Indexing

* **Elasticsearch/OpenSearch:** flexible, full-text, suited for hashtags
* **Custom NoSQL index:** lightweight, limited query
* **Preferred:** Elasticsearch for discovery

### 8. Failure Recovery

* **Replay from message queue:** ensures consistency, recovery lag possible
* **Cache warmup pipelines:** reduces cold start, adds infra cost
* **Cross-DC replication:** improves HA, adds latency
* **Preferred:** combination — replay for correctness, replication for HA, background warmup for hot feeds

---

## Workflow Summary

```
User creates post
      |
      v
   DB write + Media storage
      |
      v
  Event published to Queue
      |
      v
+-------------------+
| Feed Service      |
|------------------|
| Normal Users: fan-out to follower caches
| Celebrities: store in DB/hot cache
+-------------------+
      |
      v
Recommendation Service injects content-based posts
      |
      v
      Feed returned to user
```
