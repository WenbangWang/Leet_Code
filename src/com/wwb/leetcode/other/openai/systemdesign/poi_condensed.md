# Design: Point of Interest (POI) System (Yelp/Foursquare-like)

**Complete system design with User & POI integration**

---

## 1) Clarification questions (ordered FR → NFR)

**Functional (FR) — POI Features**

1. What types of POIs? (restaurants, hotels, gas stations, landmarks, events?)
2. Search by category, name, or both? Filters (price range, rating, open now)?
3. Real-time updates? (restaurant closing temporarily, live wait times)
4. Business owner features? (claim listing, update hours, respond to reviews)

**Functional (FR) — User Features**

5. User registration and authentication? (email/password, OAuth, SSO?)
6. User-generated content? (reviews, ratings, photos, check-ins?)
7. Personalization/recommendations based on user history and preferences?
8. Social features? (follow users, friends, share favorites, activity feed?)
9. Bookmarks/favorites/lists? ("Want to Try", "My Favorites")
10. Privacy requirements? (anonymous browsing, location privacy, data export/deletion?)

**Non-functional (NFR)**

1. Target scale: POIs (millions?), total users (100M-1B?), DAUs?
2. Latency: p95 < 200ms for searches, <100ms for auth?
3. Read vs write ratio: 99:1 for POI, 95:5 for user actions?
4. Geographic coverage: global or regional?
5. Availability: 99.99%?
6. Consistency: eventual for reviews/ratings? Strong for user accounts?
7. Concurrent sessions: how many users online simultaneously?
8. Compliance: GDPR, CCPA, data residency laws?

---

## 2) Functional requirements (FR) — condensed

**POI Features:**
* Store/manage POI data (name, location, category, hours, contact)
* Search nearby POIs by location with filters
* Search by name, category, or text query
* Return ranked results (distance, rating, relevance, user preferences)
* Display aggregate metrics (avg rating, review count, price level)
* Business owner POI updates
* Pagination, POI detail view with photos/hours/reviews

**User Features:**
* User registration/authentication (email/password, OAuth)
* User profiles (name, bio, photo, preferences)
* Reviews and ratings (CRUD)
* Photo uploads
* Bookmarks/favorites/lists
* Search history for personalization
* Personalized recommendations
* Social: follow users, view friends' activity
* Check-ins at POIs
* Notifications (new reviews, friend activity)

## 3) Non-functional requirements (NFR)

**Performance:**
* Search queries: p95 < 200ms, p99 < 500ms
* Auth: p95 < 100ms
* Recommendations: p95 < 500ms

**Scalability:**
* 100M DAU, 500M total users
* 60k peak QPS for searches
* 100M concurrent sessions
* Horizontal scalability

**Consistency:**
* Eventually consistent: reviews/ratings (<1 min delay), user activity
* Strong consistency: user accounts, authentication

**Availability:**
* 99.99% uptime for reads
* 99.95% uptime for writes

**Security & Privacy:**
* Encrypt PII at rest
* GDPR/CCPA compliance (data export, deletion)
* Secure authentication (JWT, password hashing)
* Location privacy

---

## 4) Back-of-envelope calculation (BoE)

**Assumptions:**
* 500M POIs worldwide
* 500M total users, 100M DAU
* Each user: 5 searches/day

### Query Load

* **Search queries:** 500M/day ≈ 5,787 avg QPS → **60k peak QPS**
* **Auth requests:** 200M/day ≈ 2,315 QPS → **23k peak QPS**

### Storage

#### **POI Data: 510 TB**
* POI metadata: 500M × 10 KB = **5 TB**
* Reviews: 50M POIs × 50 reviews × 2 KB = **5 TB**
* Photos: 100M POIs × 10 × 500 KB = **500 TB** (object storage)

#### **User Data: 8.6 TB**
* User profiles: 500M × 1 KB = **0.5 TB**
* Search history (90 days): 45B searches × 300 bytes = **4 TB**
* Bookmarks: 150M users × 20 × 300 bytes = **0.9 TB**
* User → Reviews index: 2.5B × 16 bytes = **0.04 TB**
* Social graph: 100M users × 50 connections × 24 bytes = **0.12 TB**
* Sessions (in-memory): 100M × 2 × 256 bytes = **51 GB**
* Activity logs (90 days): 9B events × 250 bytes = **2 TB**

#### **Caching: 1.7 TB**
* Query cache: 100M entries × 10 KB = 1 TB
* POI cache: 100M × 5 KB = 500 GB
* User cache: 100M × 2 KB = 200 GB

### **Total Storage: ~521 TB**
* Structured: 18.6 TB
* Media: 500 TB
* Cache: 2.45 TB

---

## 5) Core data entities

### **POI Entities:**

1. **POI:** poi_id, name, lat, lng, geohash (multiple precisions), category, address, phone, website, hours (JSON), price_level, status, owner_id, timestamps

2. **Review:** review_id, poi_id, user_id, rating (1-5), text, photos[], helpful_count, timestamps

3. **AggregateRating:** poi_id, avg_rating, review_count, rating_distribution (JSON), last_updated_at

4. **POICategory:** category_id, name, parent_category_id (hierarchical), icon_url

5. **Photo:** photo_id, poi_id, user_id, url (CDN), thumbnail_url, created_at

### **User Entities:**

6. **User:** user_id, username, email, hashed_password, name, bio, profile_photo_url, last_known_location, preferences (JSON), timestamps, status

7. **UserSession:** session_id, user_id, device_info (JSON), ip_address, auth_token_hash, created_at, expires_at, last_active_at

8. **SearchHistory:** search_id, user_id, query_location, search_radius, search_params (JSON), clicked_poi_ids (array), timestamp

9. **Bookmark:** bookmark_id, user_id, poi_id, list_name, notes, created_at

10. **UserFollow:** follower_id, following_id, created_at (PK: follower_id, following_id)

11. **CheckIn:** checkin_id, user_id, poi_id, location, comment, photo_urls, is_public, created_at

12. **ActivityLog:** event_id, user_id, event_type (enum), event_data (JSON), timestamp

13. **Notification:** notification_id, user_id, type (enum), data (JSON), read, created_at

---

## 6) System interfaces (APIs)

### **POI APIs:**

**Search:**
* `GET /search/nearby?lat&lng&radius&category&filters&page` → POIs sorted by distance + personalization
* `GET /search/text?query&location&page` → Text search with geo filter
* `GET /search/recommendations?user_id` → Personalized recommendations

**POI Details:**
* `GET /poi/{id}` → Full details (reviews, photos, hours)
* `GET /poi/{id}/reviews?page` → Paginated reviews
* `POST /poi`, `PUT /poi/{id}` → Create/update (owner only)

### **User & Auth APIs:**

**Authentication:**
* `POST /auth/register`, `POST /auth/login`, `POST /auth/logout`
* `POST /auth/refresh` → Get new access token
* `POST /auth/oauth/{provider}` → OAuth flow

**Profile:**
* `GET /users/me/profile`, `PUT /users/me/profile`
* `GET /users/{id}/profile` → Public view
* `DELETE /users/me` → GDPR deletion

### **User Content APIs:**

**Reviews:**
* `POST /reviews`, `PUT /reviews/{id}`, `DELETE /reviews/{id}`
* `GET /users/me/reviews?page`
* `POST /reviews/{id}/helpful`

**Bookmarks:**
* `GET /users/me/bookmarks?list`, `POST /users/me/bookmarks`, `DELETE /users/me/bookmarks/{id}`
* `GET /users/me/lists`

**Check-ins:**
* `POST /checkins`, `GET /users/me/checkins?page`, `GET /users/{id}/checkins?page`

### **Social APIs:**
* `POST /users/{id}/follow`, `DELETE /users/{id}/follow`
* `GET /users/me/following?page`, `GET /users/me/followers?page`
* `GET /users/me/feed?page` → Activity feed
* `GET /users/me/notifications?page`, `PUT /users/me/notifications/{id}/read`

---

## 7) Simple design (ASCII diagram)

```
[Client (Web/Mobile)]
        |
        v
[API Gateway / LB]
  (Auth middleware: JWT)
        |
  +-----+-----+
  |           |
  v           v
[User       [POI Service]
 Service]       |
  |             +---> [POI DB (PostgreSQL + PostGIS)]
  v             +---> [Review DB]
[User DB]       +---> [Photo Storage (S3)] --> [CDN]
  |
  v
[Session Store (Redis)]
```

**Flow (nearby search):**
1. Client → API Gateway (validates JWT, extracts user_id)
2. Search Service fetches user preferences
3. Query POI DB with geospatial filter
4. Boost results based on user preferences
5. Return top N results

**Flow (submit review):**
1. Client → API Gateway (validates JWT)
2. Review Service validates (no duplicate review)
3. Write to Review DB
4. Publish event for rating aggregation
5. Return success

---

## 8) Enriched design (scale / HA) — ASCII diagram

```
                    [Client (Web/Mobile)]
                            |
                            v
                [API Gateway / LB + Auth Middleware]
                            |
        +-------------------+-------------------+
        |                   |                   |
        v                   v                   v
[User Service]   [POI/Search Service]   [Review Service]
        |                   |                   |
        v                   v                   v
[User DB          [POI DB (sharded          [Review DB
 (sharded by       by geohash)]              (sharded)]
  user_id)]       [Elasticsearch]                |
        |                   |                     v
        v                   v            [Kafka: review-events]
[Session Store]   [Redis Cache]                  |
(Redis cluster)   (hot queries)                  v
        |                   |         [Rating Aggregator]
        |                   |         [Stream Processor]
        +--------+----------+                   |
                 |                              v
                 v                  [AggregateRating DB]
    [Personalization Service]
    [ML: Collaborative Filtering]
                 |
                 v
    [Recommendation Cache]
                 |
                 v
          [CDN + S3]
                 |
                 v
    [Notification Service]
```

### **Key Components:**

* **User Service:** User CRUD, profiles, sharded by user_id
* **Auth Service/Middleware:** JWT generation/validation, session management
* **POI/Search Service:** Geospatial queries (PostGIS/geohash), text search (Elasticsearch), personalization boost
* **Review Service:** CRUD, publishes to Kafka, prevents duplicate reviews
* **Personalization Service:** ML recommendations (collaborative + content-based)
* **Rating Aggregator:** Stream processor (Flink/Spark), consumes Kafka events
* **Notification Service:** Push notifications (Firebase/APNs), email

---

## 9) Important design patterns

### 1. **Dual Sharding Strategy**

**Challenge:** Reviews belong to both POI and User

**Solution:** Denormalize with dual indexing
* **Primary:** Review stored in POI shard (source of truth for "all reviews for this restaurant")
* **Secondary:** User shard has lightweight index (user_id → review_ids for "all my reviews")
* **Write path:** Write to POI shard + write index to User shard (eventual consistency via Kafka)

### 2. **Authentication & Session Management**

**Hybrid approach: JWT access token + Redis refresh token** ✅ Preferred

**Flow:**
1. Login → Generate access token (JWT, 24h) + refresh token (UUID, 30 days stored in Redis)
2. Subsequent requests → Validate JWT (fast, no DB lookup)
3. Token expiry → Use refresh token to get new access token
4. Logout → Remove refresh token from Redis

**Benefits:**
* Fast validation (JWT verified by signature, no DB)
* Revocable (refresh tokens in Redis can be deleted)
* Scalable (stateless JWT for most requests)

### 3. **Personalization Algorithm**

**Hybrid: Collaborative + Content-based filtering**

**Collaborative Filtering (User-User similarity):**
* Build user-POI interaction matrix
* Matrix factorization (ALS algorithm)
* Find similar users, recommend POIs they liked

**Content-based Filtering:**
* User preferences: favorite cuisines, price range, min rating
* Boost POIs matching preferences

**Real-time scoring:**
* Base score: 1/distance
* Collab score: dot product of user embedding × POI embedding
* Content score: cuisine match + price match
* Final score: weighted sum(base, collab, content)

### 4. **Caching Strategy (Multi-tier)**

**L1: Query-result cache**
* Key: `search:geohash:{gh}:radius:{r}:cat:{c}:user:{uid}`
* TTL: 5 minutes
* Use case: Same user searching same area

**L2: POI-level cache**
* Key: `poi:{poi_id}`
* TTL: 1 hour
* Use case: Popular POIs accessed by many users

**L3: User-level cache**
* Key: `user:{user_id}:profile`, `user:{user_id}:following`
* TTL: 30 minutes - 1 hour
* Use case: User makes multiple requests

**Invalidation:**
* POI update → Invalidate `poi:{id}` and related query caches
* New review → Invalidate `poi:{id}` (rating changed)
* Profile update → Invalidate `user:{id}:profile`

### 5. **Geospatial Indexing**

**Geohash approach:**
* Store multiple precisions (geohash_4, geohash_6, geohash_8)
* Shard by geohash prefix (e.g., first 2 chars → 1024 shards)
* Initial filtering by geohash range
* Final ranking by Haversine distance

**Boundary handling:**
* Query center geohash + 8 neighbors
* Merge results and filter by actual distance

### 6. **Event Streaming**

**Kafka topics:**
* `user-events`: Search, click, bookmark
* `review-events`: New reviews, updates
* `notification-events`: Trigger notifications
* `activity-events`: Check-ins, follows

**Benefits:**
* Decouples services
* Async processing (non-blocking writes)
* Replay capability
* Audit trail

---

## 10) Deep dives (options, trade-offs, preferred)

### A. Geospatial Indexing — Geohash vs QuadTree vs S2 vs QuadKeys

**Options:**

1. **Geohash**
   * Base32 encoding of lat/lng
   * Pros: Simple, good for sharding, prefix queries
   * Cons: Boundary discontinuities

2. **QuadTree (Traditional)**
   * Recursively partition into 4 quadrants
   * Pros: Efficient in-memory
   * Cons: **Hard to shard** (hierarchical dependencies, no natural partition key)

3. **QuadKeys (Shardable QuadTree)**
   * Encode tree path as string (e.g., "120301")
   * Morton code / Z-order curve
   * Pros: QuadTree semantics + sharding capability
   * Cons: Similar complexity to Geohash

4. **S2 (Google's Hilbert curve)**
   * Spherical geometry, no projection distortion
   * Pros: Most accurate, solves boundary issues
   * Cons: Complex, external library required

**Why Traditional QuadTree is Hard to Shard:**
* Hierarchical tree with parent-child dependencies
* No flat structure to partition
* Unbalanced distribution (dense cities = deep branches, rural = shallow)
* Point-to-shard lookup is O(N) without encoding

**QuadKeys Solution:**
* Flatten tree by encoding path: Root→NW→NE→SE = "012"
* Quadrant numbering: NW=0, NE=1, SW=2, SE=3
* Prefix sharing = proximity (like Geohash)
* Used by Bing Maps for tile system

**Comparison:**

| Aspect | Traditional QuadTree | QuadKeys | Geohash | S2 |
|--------|---------------------|----------|---------|-----|
| **Sharding** | ❌ Very hard | ✅ Easy | ✅ Easy | ✅ Moderate |
| **Point → Key** | ❌ O(N) | ✅ O(level) | ✅ O(precision) | ✅ O(1) |
| **Boundary issues** | ⚠️ Yes | ⚠️ Yes | ⚠️ Yes | ✅ Minimal |
| **Used by** | Small systems | Bing Maps | Redis, DBs | Google, Uber |

**Preferred:** Geohash for most POI systems (simple, well-supported)

---

### B. User Authentication & Session Management

**Options:**

1. **Stateless JWT only**
   * Pros: No server storage, scales easily
   * Cons: Can't revoke before expiry

2. **Stateful sessions (Redis)**
   * Pros: Can revoke immediately
   * Cons: Redis lookup on every request

3. **Hybrid: JWT + Redis refresh** ✅ Preferred
   * Pros: Fast validation + revocable
   * Cons: Slight complexity

**Preferred Architecture:**

**Access Token (JWT):**
* Contains: user_id, roles, expiration
* Expires: 24 hours
* Validation: Verify signature (asymmetric RS256), no DB lookup

**Refresh Token (UUID):**
* Stored in Redis: `refresh_token:{uuid}` → {user_id, device_id, created_at}
* Expires: 30 days
* One-time use (rotate on refresh)

**Session Revocation:**
* Logout → Remove refresh token from Redis
* For critical events → Add access token hash to blacklist (temporary, until expiry)

**Security:**
* Password: bcrypt (cost=12) or Argon2
* JWT: RS256 (asymmetric keys)
* Rate limiting: 5 failed logins → 15 min lockout
* MFA: Optional TOTP (Google Authenticator)

---

### C. Personalization Engine Design

**Options:**

1. **Rule-based:** Simple preference matching (limited, doesn't learn)
2. **Collaborative filtering:** User similarity (cold start problem)
3. **Content-based:** POI attributes (filter bubble)
4. **Hybrid** ✅ Preferred

**Preferred Architecture:**

**Offline Training (Nightly batch):**
1. Build user-POI interaction matrix (Spark)
2. Matrix factorization (ALS algorithm)
3. Extract user embeddings + POI embeddings
4. Store in Redis: `user:{id}:embedding`, `poi:{id}:embedding`

**Online Serving (Real-time):**
1. Candidate generation: Geospatial query → 1000 POIs
2. For each POI: Calculate score = weighted(distance, collab, content)
3. Re-rank and return top 20

**Cold Start Handling:**
* New user → Use content-based only + popular POIs
* New POI → Use category/price features + slight exploration boost

**Metrics:**
* Offline: AUC-ROC, RMSE
* Online: Click-through rate (CTR), bookmark rate
* A/B testing: Compare personalized vs non-personalized

---

### D. Social Graph Storage

**Options:**

1. **Relational DB (Postgres)**
   * Pros: Simple, ACID
   * Cons: Slow for graph traversals

2. **Graph DB (Neo4j, Neptune)**
   * Pros: Fast graph queries
   * Cons: Operational complexity

3. **Hybrid: Postgres + Redis cache** ✅ Preferred

**Preferred for Follow/Unfollow:**

**Storage:**
* Postgres: `user_follow` table with (follower_id, following_id)
* Redis cache: `user:{id}:following` (Set), `user:{id}:followers` (Set)
* TTL: 1 hour

**Celebrity Users (1M+ followers):**

**Problem:** 1M fanout on every action

**Solution: Hybrid push-pull**
* Regular users (<100k followers): Push model (write to each follower's feed)
* Celebrities (>100k followers): Pull model (followers fetch on-demand from celebrity's feed)

**When to Use Neo4j:**
* "Friends of friends" queries (2+ hops)
* Complex graph algorithms (PageRank, community detection)
* For simple follow/unfollow, **Postgres + Redis is sufficient**

---

### E. Privacy & GDPR Compliance

**Key Requirements:**

1. **Right to Access:** User can export all data (JSON format)
2. **Right to Deletion:** Account deletion + data cleanup
3. **Right to Rectification:** User can update data
4. **Data Portability:** Machine-readable export
5. **Consent Management:** Track user consent for data processing
6. **Data Minimization:** Don't store unnecessary data
7. **Encryption:** PII encrypted at rest

**Implementation Highlights:**

**Data Export:**
* Generate JSON with all user data (profile, reviews, bookmarks, history)
* Store in S3 with pre-signed URL (expires 7 days)
* Email user with download link

**Account Deletion:**
1. Soft delete: Mark user as deleted, anonymize email/username
2. Anonymize reviews: Set user_id to "Anonymous User" (keep review content for POI owners)
3. Delete personal data: Search history, bookmarks, activity logs, check-ins, follows, sessions
4. Delete uploaded photos from S3
5. Schedule hard delete after 30-day grace period

**Location Privacy:**
* Don't store exact search locations permanently
* Round to 2 decimals (~1km precision) or aggregate to city level
* Use for personalization but with user consent

**PII Encryption:**
* Encrypt email, phone, full name at rest (application-layer encryption with KMS)
* Use data masking in logs
* Encrypted backups

**Consent Management:**
* Track consent types: marketing, analytics, personalization
* Check consent before processing: `if can_track_user(user_id, 'personalization'): log_activity()`

---

### F. Sharding Strategy

**Critical Decision: Different sharding for POI vs User data**

**POI Data → Shard by geohash:**
* Rationale: POI queries are location-based (nearby search)
* Implementation: First 2-3 chars of geohash determines shard
* Example: `dr5ru6` → Shard `dr` (Northeast USA)

**User Data → Shard by user_id:**
* Rationale: User queries are user-centric (get my profile, my bookmarks)
* Implementation: Hash(user_id) % num_shards
* Example: `user_12345` → Shard 3

**Cross-Cutting: Reviews**

**Problem:** Reviews belong to both POI and User

**Solution: Dual indexing**
1. Primary storage: In POI shard (for "all reviews for this restaurant")
2. Secondary index: User shard has lightweight mapping (user_id → review_ids for "all my reviews")
3. Denormalize user info in review (username, photo_url) for display

**Write coordination:**
* Option A: Distributed transaction (2PC) - too slow
* Option B: Write to POI shard, async propagate to User shard via Kafka ✅ Preferred
* Trade-off: Eventual consistency (acceptable for reviews)

---

### G. Geohash Resharding

**Why Easy with Geohash:**
* Hierarchical by design
* No need to recompute hashes
* Just adjust prefix rules

**Strategies:**

**1. Prefix-Based Split:**
* Before: Shard by 2-char → Shard_dr (all "dr*")
* After: Shard by 3-char → Shard_dr0, dr1, ..., drz (32 shards)
* Predictable 32-way split per character

**2. Hotspot Isolation:**
* Before: Shard_dr5r (Manhattan + surrounding)
* After: Shard_dr5r_hot ("dr5ru" Manhattan only), Shard_dr5r_cold (rest)

**Process:**

1. **Planning:** Analyze distribution, choose boundaries, provision infrastructure
2. **Dual-Write:** Write to both old and new shards during migration
3. **Backfill:** Copy existing data from old to new shards (batch, throttled)
4. **Verification:** Compare counts, sample integrity checks
5. **Cutover:** Gradually shift reads (1% → 5% → 25% → 50% → 100%)
6. **Cleanup:** Stop dual writes, decommission old shards, archive

**Key Advantage:**

| Aspect | Geohash | Hash-based |
|--------|---------|------------|
| **Recompute keys?** | ❌ No | ✅ Yes |
| **Partial resharding** | ✅ Yes | ❌ All-or-nothing |
| **Zero downtime** | ✅ Dual-write | ⚠️ Complex |

---

## 11) Reliability & edge cases

**POI-related:**
* Stale data (business closed) → User reports, periodic verification
* Boundary queries → Query neighboring geohashes
* Duplicate POIs → Fuzzy matching deduplication
* Hot POIs → Multi-tier caching, read replicas

**User-related:**
* Account takeover → Rate limiting, MFA, suspicious login detection
* Password reset attacks → Email verification, rate limiting
* Concurrent profile updates → Optimistic locking with version numbers
* Session hijacking → Bind tokens to device fingerprint
* Spam reviews → ML spam detection, user reputation system
* GDPR deletion failures → Retry queue, manual intervention log

---

## 12) Operational concerns & observability

**Metrics:**
* Search QPS, p95/p99 latency, cache hit rate
* Active users, login success rate, registration funnel
* Token validation latency, failed login attempts
* Recommendation CTR, user engagement

**Tracing:**
* End-to-end distributed tracing (OpenTelemetry)
* Trace: login → search → click → bookmark
* Include user_id in trace contexts

**Alerts:**
* Error rate >1%, latency spike (p99 > 1s)
* Cache hit rate <70%
* Failed login spike (credential stuffing?)
* GDPR export queue backlog

**Dashboards:**
* Real-time query heatmap
* User funnel (search → click → review)
* Auth success/failure rates
* Shard load distribution

---

## 13) Security & privacy

**Authentication:**
* JWT with RS256 (asymmetric), bcrypt (cost=12) or Argon2
* MFA support (TOTP, SMS backup)
* OAuth 2.0 for social login

**Authorization:**
* RBAC: user, business_owner, admin
* Row-level security (users modify own data only)

**Rate Limiting:**
* Per-user: 100 req/min
* Per-IP: 1000 req/min
* Login: 5 failures → 15 min lockout

**Data Privacy:**
* Encrypt PII at rest (email, phone, location history)
* Anonymize search locations (round to 2 decimals)
* GDPR/CCPA compliance (export, deletion)
* User consent management

**Input Validation:**
* Sanitize inputs (XSS, SQL injection prevention)
* Image uploads: virus scanning, content moderation

**API Security:**
* HTTPS only (TLS 1.3), CORS policies
* API keys for business partners
* DDoS protection (CloudFlare, AWS Shield)

**Secrets Management:**
* KMS (AWS Secrets Manager, Vault)
* Rotate credentials regularly
* Never log secrets

---

## 14) Cost optimization

* **Storage:** S3 Glacier for old photos/reviews, compress images (WebP), CDN
* **Compute:** Auto-scale, spot instances for batch jobs, serverless for infrequent tasks
* **Database:** Read replicas, archive old data, connection pooling
* **Cache:** Right-size Redis, TTL eviction, cache only hot data (top 20%)
* **ML:** Train models nightly (not real-time), use sampling, cache embeddings

---

## References

* Geohash: http://geohash.org/
* QuadKeys (Bing Maps): https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system
* S2 Geometry: https://s2geometry.io/
* JWT Best Practices: https://tools.ietf.org/html/rfc8725
* GDPR: https://gdpr.eu/
* Yelp Engineering: https://engineeringblog.yelp.com/
* Uber Engineering: https://eng.uber.com/
* System Design Interview Vol. 2 (Nearby Friends / Proximity Service)

---

## Interview Tips

1. **Clarify user vs POI scope early** - Many interviewers expect both
2. **Dual sharding is critical** - POI by geohash, User by user_id
3. **Deep dive on auth** - Interviewers often focus here
4. **Personalization** - Show ML understanding (collaborative filtering)
5. **Privacy matters** - Mention GDPR, PII encryption, consent
6. **Trade-offs** - Always present options and justify choice
7. **Real-world** - Reference Yelp, Foursquare, Google Maps
8. **User lifecycle** - Registration → Search → Bookmark → Review → Share
9. **Edge cases** - Account takeover, spam, celebrity users, GDPR deletion
10. **Start simple** - Single DB → Then scale iteratively

---

