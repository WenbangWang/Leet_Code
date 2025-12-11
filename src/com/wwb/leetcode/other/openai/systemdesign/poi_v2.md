# Design: Point of Interest (POI) System (Yelp/Foursquare-like)

**Complete system design with User & POI integration**

---

## 1) Clarification questions (ordered FR → NFR)

**Functional (FR) — POI Features**

1. What types of POIs do we support? (restaurants, hotels, gas stations, landmarks, events?)
2. Do we need search by category, name, or both? What about filters (price range, rating, open now)?
3. Should we support real-time updates (e.g., restaurant closing temporarily, live wait times)?
4. Do we support business owner features (claim listing, update hours, respond to reviews)?

**Functional (FR) — User Features**

5. Do we need user registration and authentication? (email/password, OAuth, SSO?)
6. What user-generated content do we support? (reviews, ratings, photos, check-ins?)
7. Do we need personalization/recommendations based on user history and preferences?
8. Social features required? (follow users, friends, share favorites, activity feed?)
9. Do users need bookmarks/favorites/lists? ("Want to Try", "My Favorites")
10. User privacy requirements? (anonymous browsing, location privacy, data export/deletion?)

**Non-functional (NFR)**

1. Target scale: number of POIs (millions?), total users (100M-1B?), daily active users (DAUs)?
2. Query latency target (e.g., p95 < 200ms for nearby search, <100ms for auth)?
3. Read vs write ratio (typically 99:1 for POI systems, 95:5 for user actions)?
4. Geographic coverage: global or specific regions?
5. Availability requirements (e.g., 99.99%)?
6. Consistency requirements: eventual consistency acceptable for reviews/ratings? Strong for user accounts?
7. Concurrent sessions: how many users online simultaneously?
8. Compliance requirements: GDPR, CCPA, data residency laws?

---

## 2) Functional requirements (FR) — condensed

**POI Features:**
* Store and manage POI data (name, location, category, hours, contact, metadata)
* Search for nearby POIs by location (lat/lng) with optional filters
* Search by name, category, or text query
* Return ranked results based on distance, rating, relevance, user preferences
* Display aggregate metrics (average rating, review count, price level)
* Handle POI updates from business owners
* Support pagination for search results
* Provide POI detail view with photos, hours, reviews

**User Features:**
* User registration and authentication (email/password, OAuth)
* User profiles (name, bio, profile photo, preferences)
* User reviews and ratings (CRUD operations)
* Photo uploads from users
* Bookmarks/favorites/lists (organize saved POIs)
* Search history tracking for personalization
* Personalized recommendations based on user behavior
* Social features: follow users, view friends' activity
* User check-ins at POIs
* User notifications (new reviews on followed POIs, friend activity)

## 3) Non-functional requirements (NFR)

**Performance:**
* Low latency for search queries (p95 < 200ms, p99 < 500ms)
* Authentication/authorization latency (p95 < 100ms)
* Personalization recommendations (p95 < 500ms)

**Scalability:**
* Handle 100M DAU, 500M total users
* Support 60k peak QPS for searches
* 100M concurrent sessions
* Horizontal scalability for billions of searches per day

**Consistency:**
* Eventually consistent for reviews/ratings (acceptable <1 minute delay)
* Eventually consistent for user activity tracking
* Strong consistency for user accounts, authentication

**Availability:**
* 99.99% uptime for read path (searches)
* 99.95% uptime for write path (reviews, bookmarks)

**Security & Privacy:**
* Encrypt PII (email, location history) at rest
* GDPR/CCPA compliance (data export, right to deletion)
* Secure authentication (JWT, password hashing)
* User location privacy (don't store exact search locations permanently)

**Cost:**
* Cost-efficient storage for large POI datasets and media
* Optimize for read-heavy workload

---

## 4) Back-of-envelope calculation (BoE)

**Assumptions:**
* 500M total POIs worldwide
* 500M total registered users
* 100M DAU (Daily Active Users)
* Each user performs 5 searches/day

### Query Load

**Search queries:**
* Total searches/day = 100M * 5 = 500M searches/day
* Average QPS: 500M / 86400 ≈ 5,787 QPS
* Peak QPS (10x during lunch/dinner): **60k QPS**

**User actions (auth, profile updates, bookmarks):**
* Auth requests: 100M DAU * 2 logins/day = 200M/day ≈ 2,315 QPS
* Peak auth: **23k QPS**

### Storage Calculation

#### **POI Data:**

* **POI metadata:**
  * 500M POIs * 10 KB each (name, location, category, hours, etc.)
  * = **5 TB**

* **Reviews:**
  * Assume 10% of POIs have reviews, avg 50 reviews/POI, 2 KB/review
  * 50M POIs * 50 reviews * 2 KB
  * = **5 TB**

* **Photos:**
  * Assume 20% of POIs have photos, avg 10 photos/POI, 500 KB/photo (thumbnail + full)
  * 100M POIs * 10 * 500 KB
  * = **500 TB** (object storage)

**POI Data Subtotal: 510 TB (5 + 5 + 500)**

---

#### **User Data:**

* **User profiles:**
  * 500M users * 1 KB each (username, email, hashed_password, profile_data, preferences)
  * = **500 GB (0.5 TB)**

* **Search history** (for personalization):
  * 100M DAU * 5 searches/day * 90 days retention * 300 bytes/event
  * = 45B searches * 300 bytes
  * = **4 TB**

* **Bookmarks/Favorites:**
  * 30% of users bookmark POIs, avg 20 bookmarks/user
  * 150M users * 20 bookmarks * 300 bytes
  * = **900 GB (0.9 TB)**

* **User → Reviews index:**
  * 2.5B reviews (from above), need user_id → review_ids mapping
  * 2.5B * 16 bytes (user_id + review_id)
  * = **40 GB (0.04 TB)**

* **Social graph** (if enabled):
  * 20% of users have followers, avg 50 connections
  * 100M users * 50 connections * 24 bytes (follower_id, following_id, created_at)
  * = **120 GB (0.12 TB)**

* **Sessions** (in-memory):
  * 100M DAU * 2 sessions (web + mobile) * 256 bytes
  * = **51 GB** (Redis, 30-day TTL)

* **Activity logs** (analytics, 90-day retention):
  * 100M DAU * 10 events/day * 90 days * 250 bytes
  * = 9B events * 250 bytes
  * = **2 TB**

**User Data Subtotal: 8.6 TB (0.5 + 4 + 0.9 + 0.04 + 0.12 + 0.05 + 2)**

---

#### **Geospatial Indexes:**

* Using geohash (6-8 character precision)
* Index size: 500M POIs * (8+8+8+8 bytes for lat, lng, geohash, poi_id)
* = **16 GB** (easily fits in memory for hot regions)

---

#### **Caching:**

* Popular queries (e.g., "restaurants near Times Square")
* Cache hit ratio target: 80-90%
* Query-result cache: 100M entries * 10 KB = **1 TB**
* POI-level cache: 100M hot POIs * 5 KB = **500 GB**
* User-level cache: 100M users * 2 KB (profile, preferences) = **200 GB**

**Cache Subtotal: 1.7 TB (distributed Redis cluster)**

---

### **Total Storage:**

| Category | Structured Data | Media (Object Storage) | In-Memory Cache |
|----------|----------------|------------------------|----------------|
| **POI Data** | 10 TB | 500 TB | 0.5 TB |
| **User Data** | 8.6 TB | - | 0.25 TB |
| **Indexes** | 0.016 TB | - | - |
| **Cache** | - | - | 1.7 TB |
| **Total** | **18.6 TB** | **500 TB** | **2.45 TB** |

**Grand Total: ~521 TB (18.6 + 500 + 2.45)**

---

## 5) Core data entities

### **POI Entities:**

1. **POI (Point of Interest)**
   ```
   poi_id (UUID)
   name (VARCHAR)
   latitude (DECIMAL)
   longitude (DECIMAL)
   geohash_4, geohash_6, geohash_8 (CHAR) -- Multi-level indexes
   category, subcategory (VARCHAR)
   address, phone, website (VARCHAR)
   hours_of_operation (JSON)
   price_level (1-4)
   status (active/closed/temporarily_closed)
   owner_id (FK to User) -- Business owner
   created_at, updated_at (TIMESTAMP)
   ```

2. **Review**
   ```
   review_id (UUID)
   poi_id (FK to POI)
   user_id (FK to User)
   rating (1-5)
   text (TEXT)
   photos[] (URLs)
   helpful_count (INT) -- "Was this helpful?" votes
   created_at, updated_at (TIMESTAMP)
   ```

3. **AggregateRating** (Materialized view)
   ```
   poi_id (FK to POI)
   average_rating (DECIMAL)
   review_count (INT)
   rating_distribution (JSON: {5: count, 4: count, ...})
   last_updated_at (TIMESTAMP)
   ```

4. **POICategory**
   ```
   category_id (UUID)
   name (VARCHAR)
   parent_category_id (FK to POICategory) -- Hierarchical
   icon_url (VARCHAR)
   ```

5. **Photo**
   ```
   photo_id (UUID)
   poi_id (FK to POI)
   user_id (FK to User)
   url (CDN URL)
   thumbnail_url (CDN URL)
   created_at (TIMESTAMP)
   ```

---

### **User Entities:**

6. **User**
   ```
   user_id (UUID)
   username (VARCHAR, UNIQUE)
   email (VARCHAR, UNIQUE)
   hashed_password (VARCHAR) -- bcrypt
   name (VARCHAR)
   bio (TEXT)
   profile_photo_url (VARCHAR)
   last_known_location (lat, lng, DECIMAL) -- For "near me" searches
   preferences (JSON) -- {"favorite_cuisines": [...], "price_range": [1,3], ...}
   created_at, updated_at (TIMESTAMP)
   email_verified (BOOLEAN)
   status (active/suspended/deleted)
   ```

7. **UserSession**
   ```
   session_id (UUID)
   user_id (FK to User)
   device_info (JSON) -- device type, OS, browser
   ip_address (VARCHAR)
   auth_token_hash (VARCHAR) -- Hashed JWT refresh token
   created_at (TIMESTAMP)
   expires_at (TIMESTAMP)
   last_active_at (TIMESTAMP)
   ```

8. **SearchHistory**
   ```
   search_id (UUID)
   user_id (FK to User)
   query_location (lat, lng, DECIMAL)
   search_radius (INT) -- meters
   search_params (JSON) -- {category, filters, sort_by}
   clicked_poi_ids (ARRAY) -- Which POIs user clicked
   timestamp (TIMESTAMP)
   ```

9. **Bookmark** (User favorites/lists)
   ```
   bookmark_id (UUID)
   user_id (FK to User)
   poi_id (FK to POI)
   list_name (VARCHAR) -- "Want to Try", "Favorites", "Date Night"
   notes (TEXT) -- Optional user notes
   created_at (TIMESTAMP)
   ```

10. **UserFollow** (Social graph)
    ```
    follower_id (FK to User)
    following_id (FK to User)
    created_at (TIMESTAMP)
    PRIMARY KEY (follower_id, following_id)
    ```

11. **CheckIn** (User check-in at POI)
    ```
    checkin_id (UUID)
    user_id (FK to User)
    poi_id (FK to POI)
    location (lat, lng, DECIMAL) -- Verify user is actually there
    comment (TEXT)
    photo_urls (ARRAY)
    is_public (BOOLEAN)
    created_at (TIMESTAMP)
    ```

12. **ActivityLog** (Analytics & personalization)
    ```
    event_id (UUID)
    user_id (FK to User)
    event_type (ENUM: search, click, bookmark, review_submit, checkin)
    event_data (JSON) -- Flexible event payload
    timestamp (TIMESTAMP)
    ```

13. **Notification**
    ```
    notification_id (UUID)
    user_id (FK to User)
    type (ENUM: new_review_on_followed_poi, friend_checkin, reply_to_review)
    data (JSON) -- Notification payload
    read (BOOLEAN)
    created_at (TIMESTAMP)
    ```

---

## 6) System interfaces (APIs)

### **POI APIs:**

**Search:**
* `GET /search/nearby?lat={lat}&lng={lng}&radius={m}&category={cat}&filters={...}&page={n}`
  → Returns POIs sorted by distance + personalized boost
* `GET /search/text?query={text}&location={lat,lng}&page={n}`
  → Text search with geo filter
* `GET /search/recommendations?user_id={id}`
  → Personalized recommendations based on user history

**POI Details:**
* `GET /poi/{poi_id}`
  → Full POI details (with reviews, photos, hours)
* `GET /poi/{poi_id}/reviews?page={n}`
  → Paginated reviews for POI
* `POST /poi` (business owner only)
  → Create new POI
* `PUT /poi/{poi_id}` (owner/admin only)
  → Update POI details

---

### **User & Auth APIs:**

**Authentication:**
* `POST /auth/register` {email, password, username}
  → Register new user, returns access + refresh tokens
* `POST /auth/login` {email, password}
  → Login, returns JWT access token + refresh token
* `POST /auth/logout`
  → Invalidate session
* `POST /auth/refresh` {refresh_token}
  → Get new access token
* `POST /auth/oauth/{provider}` (Google, Facebook, Apple)
  → OAuth login flow

**User Profile:**
* `GET /users/me/profile`
  → Get current user's profile
* `PUT /users/me/profile` {name, bio, profile_photo, preferences}
  → Update profile
* `GET /users/{user_id}/profile`
  → Public profile view
* `DELETE /users/me` (GDPR compliance)
  → Delete user account and all data

---

### **User Content APIs:**

**Reviews:**
* `POST /reviews` {poi_id, rating, text, photos[]}
  → Submit review
* `PUT /reviews/{review_id}`
  → Update own review
* `DELETE /reviews/{review_id}`
  → Delete own review
* `GET /users/me/reviews?page={n}`
  → Get all reviews by current user
* `POST /reviews/{review_id}/helpful`
  → Mark review as helpful

**Bookmarks:**
* `GET /users/me/bookmarks?list={name}`
  → Get bookmarked POIs (optionally filter by list)
* `POST /users/me/bookmarks` {poi_id, list_name, notes}
  → Bookmark a POI
* `DELETE /users/me/bookmarks/{bookmark_id}`
  → Remove bookmark
* `GET /users/me/lists`
  → Get all user's bookmark lists

**Check-ins:**
* `POST /checkins` {poi_id, location, comment, photos[], is_public}
  → Check in at POI
* `GET /users/me/checkins?page={n}`
  → Get user's check-in history
* `GET /users/{user_id}/checkins?page={n}`
  → Public check-ins for user

---

### **Social APIs:**

**Follow/Unfollow:**
* `POST /users/{user_id}/follow`
  → Follow a user
* `DELETE /users/{user_id}/follow`
  → Unfollow a user
* `GET /users/me/following?page={n}`
  → Users I follow
* `GET /users/me/followers?page={n}`
  → My followers

**Activity Feed:**
* `GET /users/me/feed?page={n}`
  → Activity feed from followed users (reviews, check-ins)

**Notifications:**
* `GET /users/me/notifications?page={n}`
  → Get notifications
* `PUT /users/me/notifications/{id}/read`
  → Mark notification as read

---

### **Internal Service APIs:**

* **User Service:** CRUD for user accounts, profile management
* **Auth Service:** JWT generation/validation, session management
* **POI Service:** CRUD for POI data
* **Search Service:** Geospatial + text search
* **Review Service:** CRUD for reviews, rating aggregation
* **Media Service:** Photo upload/storage/CDN
* **Personalization Service:** Recommendations engine
* **Activity Tracking Service:** Log user actions to Kafka
* **Notification Service:** Push notifications, email
* **Social Service:** Follow/unfollow, activity feed

---

## 7) Start from simple design (ASCII diagram)

**Simple assumption:** Modest scale, single region, basic functionality

```
[Client (Web/Mobile)]
        |
        v
[API Gateway / Load Balancer]
    (Auth middleware: JWT validation)
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

**Flow (user search nearby):**

1. Client sends `GET /search/nearby?lat=40.7&lng=-74.0&radius=1000&category=restaurant` with JWT token
2. API Gateway validates JWT → extracts user_id
3. Search Service:
   a. Fetch user preferences from User Service (or cache)
   b. Query POI DB with geospatial filter (PostGIS ST_Distance)
   c. Boost results based on user preferences
   d. Return top N results
4. Client displays results

**Flow (user submits review):**

1. Client sends `POST /reviews` {poi_id, rating, text} with JWT
2. API Gateway validates JWT → user_id
3. Review Service:
   a. Validate user hasn't reviewed this POI already
   b. Write review to Review DB
   c. Publish event to event stream for rating aggregation
4. Return success to client

---

## 8) Enrich the design (scale / HA / performance) — ASCII diagram

**At scale:** Add sharding, caching, microservices, event streaming, CDN

```
                        [Client (Web/Mobile)]
                                |
                                v
                    [API Gateway / Load Balancer]
                    [Auth Middleware: JWT validation]
                                |
        +-----------------------+---------------------------+
        |                       |                           |
        v                       v                           v
[User Service]       [POI/Search Service]         [Review Service]
        |                       |                           |
        v                       v                           v
[User DB              [POI DB (sharded by geohash)]  [Review DB (sharded)]
 (sharded by          [Elasticsearch (text search)]   (by poi_id or user_id)
  user_id)]                    |                           |
        |                       v                           |
        v            [Geospatial Index: Redis/PostGIS]      |
[Session Store]                 |                           |
(Redis cluster)                 v                           v
        |            [Redis Cache: hot queries]    [Kafka: review-events]
        |                       |                           |
        +-------+---------------+--------------+------------+
                |                              |
                v                              v
    [Personalization Service]     [Rating Aggregator]
    [ML Model: Collaborative      [Stream Processor]
     Filtering]                           |
                |                         v
                v               [AggregateRating DB]
    [Recommendation Cache]               |
                |                         |
                +-------------------------+
                            |
                            v
                    [CDN (photos, static)]
                    [Object Storage (S3)]
                            |
                            v
                [Notification Service]
                [Push notifications, Email]
```

---

### **Key Components Explained:**

#### **User Service**
* Handles user registration, login, profile management
* Manages user sessions in Redis
* Sharded by user_id (consistent hashing)

#### **Auth Service / Middleware**
* JWT token generation and validation
* Sits in API Gateway for all requests
* Session management with refresh tokens

#### **POI/Search Service**
* Geospatial queries (PostGIS or geohash-based)
* Text search via Elasticsearch
* Applies personalization boost based on user preferences

#### **Review Service**
* CRUD for reviews
* Publishes review events to Kafka
* Prevents duplicate reviews (user can only review POI once)

#### **Personalization Service**
* ML-based recommendations
* Collaborative filtering (users who liked X also liked Y)
* Content-based filtering (based on user preferences)
* Pre-computes user embeddings nightly

#### **Rating Aggregator**
* Stream processor (Flink/Spark Streaming)
* Consumes review events from Kafka
* Updates aggregate ratings in real-time

#### **Notification Service**
* Sends push notifications (Firebase/APNs)
* Email notifications
* Triggered by events (new review on followed POI, friend check-in)

---

### **Sharding Strategy:**

**User Data: Shard by user_id**
```
Shard_0: user_id % num_shards == 0
Shard_1: user_id % num_shards == 1
...

Rationale: User queries are user-centric (get my profile, my bookmarks)
```

**POI Data: Shard by geohash**
```
Shard_dr: geohash starts with "dr" (Northeast USA)
Shard_9q: geohash starts with "9q" (Bay Area)
...

Rationale: POI queries are location-based (nearby search)
```

**Reviews: Dual indexing**
```
Primary storage: In POI shard (for "all reviews for this restaurant")
Secondary index: User shard maintains user_id → review_ids mapping (for "all my reviews")
```

---

## 9) Important design patterns & components

### 1. **Dual Sharding Strategy (User vs POI)**

**Challenge:** Reviews belong to both POI and User. How to store?

**Solution: Denormalize with dual indexing**

```sql
-- Primary storage: Review stored in POI shard
CREATE TABLE poi_shard_dr.review (
    review_id UUID PRIMARY KEY,
    poi_id UUID,
    user_id UUID,
    rating INT,
    text TEXT,
    user_display_name VARCHAR, -- Denormalized for display
    user_photo_url VARCHAR,    -- Denormalized
    created_at TIMESTAMP
);

-- Secondary index: User shard has lightweight index
CREATE TABLE user_shard_0.user_reviews_index (
    user_id UUID,
    review_id UUID,
    poi_id UUID,
    rating INT,
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, created_at DESC)
);
```

**Write path:**
1. Write review to POI shard (source of truth)
2. Write index entry to User shard
3. Use distributed transaction or eventual consistency (Kafka event)

**Read path:**
* "Get all reviews for POI" → Query POI shard
* "Get all my reviews" → Query User shard index, optionally fetch full reviews from POI shards

---

### 2. **Authentication & Session Management**

**JWT-based authentication with refresh tokens**

```
[Client]
   |
   | POST /auth/login {email, password}
   v
[Auth Service]
   |
   | 1. Validate credentials (check hashed password)
   | 2. Generate access token (JWT, expires 24h)
   | 3. Generate refresh token (random UUID, expires 30 days)
   | 4. Store refresh token in Redis with user_id
   v
[Returns: {access_token, refresh_token}]

[Subsequent requests]
   |
   | GET /search/nearby
   | Header: Authorization: Bearer {access_token}
   v
[API Gateway]
   |
   | 1. Extract JWT from header
   | 2. Verify signature (using public key)
   | 3. Check expiration
   | 4. Extract user_id from claims
   | 5. Attach user_id to request context
   v
[Route to service with user_id]
```

**Token structure:**
```json
{
  "sub": "user_12345",
  "iat": 1702345678,
  "exp": 1702432078,
  "roles": ["user"]
}
```

**Session storage (Redis):**
```
Key: session:{user_id}:{device_id}
Value: {
  refresh_token_hash,
  device_info,
  created_at,
  last_active_at
}
TTL: 30 days
```

---

### 3. **Personalization Algorithm**

**Hybrid approach: Collaborative + Content-based filtering**

**Collaborative Filtering (User-User similarity):**
```
1. Build user-POI interaction matrix:
   User1: [POI_A: 5*, POI_B: 4*, ...]
   User2: [POI_A: 4*, POI_C: 5*, ...]

2. Compute user similarity (cosine similarity, Pearson correlation)
   Sim(User1, User2) = 0.85 (highly similar)

3. Recommend POIs that similar users liked:
   User1 hasn't visited POI_C, but User2 (similar) rated it 5*
   → Recommend POI_C to User1
```

**Content-based Filtering (User preferences):**
```
User preferences: {
  "favorite_cuisines": ["Italian", "Japanese"],
  "price_range": [2, 3],
  "min_rating": 4.0
}

For nearby search:
1. Get POIs within radius (geospatial query)
2. Boost POIs matching preferences:
   - Italian restaurant + 4.5* rating → Boost ×1.5
   - Chinese restaurant + 4.8* rating → Boost ×1.0
3. Re-rank results by (distance × boost)
```

**Implementation:**
```python
def personalized_search(user_id, lat, lng, radius):
    # 1. Get user preferences
    user = get_user(user_id)
    prefs = user.preferences
    
    # 2. Geospatial query
    pois = search_nearby(lat, lng, radius)
    
    # 3. Apply personalization boost
    for poi in pois:
        boost = 1.0
        
        # Boost by cuisine preference
        if poi.cuisine in prefs.favorite_cuisines:
            boost *= 1.5
        
        # Boost by price match
        if poi.price_level in prefs.price_range:
            boost *= 1.2
        
        # Boost by collaborative filtering
        similar_users_rating = get_collaborative_score(user_id, poi.id)
        boost *= (1 + similar_users_rating / 5)
        
        poi.score = (1 / poi.distance) * boost
    
    # 4. Sort by personalized score
    return sorted(pois, key=lambda p: p.score, reverse=True)
```

---

### 4. **Caching Strategy (Multi-tier)**

**L1: Query-result cache** (hot queries)
```
Key: search:geohash:{gh}:radius:{r}:cat:{c}:user:{uid}
Value: [poi_id_1, poi_id_2, ...]
TTL: 5 minutes

Rationale: Same user searching same area multiple times
```

**L2: POI-level cache** (individual POI details)
```
Key: poi:{poi_id}
Value: {name, location, rating, hours, ...}
TTL: 1 hour

Rationale: Popular POIs accessed by many users
```

**L3: User-level cache** (user profile, preferences)
```
Key: user:{user_id}:profile
Value: {username, preferences, last_location, ...}
TTL: 30 minutes

Rationale: User makes multiple requests, avoid DB hits
```

**Cache invalidation:**
* POI update → Invalidate `poi:{poi_id}` and related query caches
* New review → Invalidate `poi:{poi_id}` (rating changed)
* User profile update → Invalidate `user:{user_id}:profile`

---

### 5. **Geospatial Indexing (Geohash + PostGIS)**

**Already covered in Deep Dive A (see Section 10)**

Quick summary:
* Store multiple geohash precisions (4, 6, 8 chars)
* Use for sharding and initial filtering
* PostGIS for accurate distance calculations
* Handle boundary cases by querying neighbors

---

### 6. **Event Streaming for Async Processing**

**Kafka topics:**
```
user-events:        User actions (search, click, bookmark)
review-events:      New reviews, updates
notification-events: Trigger notifications
activity-events:    Social activity (check-ins, follows)
```

**Consumers:**
* Rating Aggregator: Consumes review-events → Updates aggregate ratings
* Personalization Engine: Consumes user-events → Updates ML models
* Notification Service: Consumes notification-events → Sends push/email
* Analytics: Consumes all events → Data warehouse

**Benefits:**
* Decouples services
* Enables async processing (non-blocking writes)
* Replay capability for failures
* Audit trail

---

## 10) Possible deep dives (options, trade-offs, preferred)

### A. Geospatial indexing — Geohash vs QuadTree vs S2 vs QuadKeys

*[This section remains largely the same as the previous version with QuadKeys explanation]*

*(Keeping this detailed section from the original document - ~200 lines)*

**Summary:**
* **Geohash:** Best for simple implementation and sharding
* **QuadKeys:** If you need QuadTree semantics with sharding
* **S2:** For global-scale with highest precision (Google, Uber)

**Preferred:** Geohash for most POI systems

---

### B. Sharding strategy for POI database

*[This section remains the same as the previous version]*

**Key decision:**
* POI data: Shard by geohash (location-based queries)
* User data: Shard by user_id (user-centric queries)
* Reviews: Dual indexing (primary in POI shard, index in user shard)

---

### C. User Authentication & Session Management

**Goal:** Secure, scalable authentication for 100M DAU with 100M concurrent sessions

#### **Options:**

1. **Stateless JWT (Access tokens only)**
   * Pros: No server-side session storage, scales easily
   * Cons: Can't revoke tokens before expiry, token contains user claims (can get large)

2. **Stateful sessions (Redis-backed)**
   * Pros: Can revoke sessions immediately, smaller tokens
   * Cons: Requires Redis lookup on every request (latency)

3. **Hybrid: JWT access token + Redis refresh token** ✅ Preferred
   * Pros: Best of both worlds - fast validation, revocable
   * Cons: Slight complexity

#### **Preferred Architecture:**

```
┌─────────────────────────────────────────┐
│      Authentication Flow                │
├─────────────────────────────────────────┤
│                                         │
│ 1. User Login                          │
│    POST /auth/login                     │
│    {email, password}                    │
│         ↓                               │
│ 2. Auth Service                         │
│    - Validate credentials               │
│    - Hash matches? (bcrypt verify)      │
│    - Generate access token (JWT)        │
│      • Expires in 24 hours              │
│      • Contains: {user_id, roles}       │
│    - Generate refresh token (UUID)      │
│      • Expires in 30 days               │
│      • Stored in Redis                  │
│         ↓                               │
│ 3. Return tokens                        │
│    {                                    │
│      access_token: "eyJhbG...",        │
│      refresh_token: "uuid-123...",     │
│      expires_in: 86400                  │
│    }                                    │
│                                         │
│ 4. Subsequent requests                  │
│    GET /search/nearby                   │
│    Header: Authorization: Bearer {JWT}  │
│         ↓                               │
│ 5. API Gateway validates JWT            │
│    - Verify signature (asymmetric key)  │
│    - Check expiration                   │
│    - Extract user_id                    │
│    - No Redis lookup needed! ✅         │
│         ↓                               │
│ 6. Route to service with user context   │
│                                         │
└─────────────────────────────────────────┘
```

#### **Token Structure:**

**Access Token (JWT):**
```json
{
  "alg": "RS256",
  "typ": "JWT"
}
.
{
  "sub": "user_12345",
  "iat": 1702345678,
  "exp": 1702432078,
  "roles": ["user"],
  "email": "user@example.com"
}
.
[signature]
```

**Refresh Token (Opaque):**
```
UUID: "550e8400-e29b-41d4-a716-446655440000"

Stored in Redis:
Key: refresh_token:550e8400-e29b-41d4-a716-446655440000
Value: {
  user_id: "user_12345",
  device_id: "device_abc",
  created_at: 1702345678,
  last_used_at: 1702345678
}
TTL: 30 days
```

#### **Session Revocation:**

```python
def logout(user_id, refresh_token):
    # Remove refresh token from Redis
    redis.delete(f"refresh_token:{refresh_token}")
    
    # Add access token to blacklist (until expiry)
    # Only needed for critical security events
    access_token_hash = sha256(access_token)
    redis.setex(
        f"blacklist:{access_token_hash}",
        ttl=time_until_expiry,
        value="revoked"
    )

def validate_access_token(token):
    # 1. Verify JWT signature and expiration
    claims = jwt.verify(token, public_key)
    
    # 2. Check blacklist (only for critical cases)
    token_hash = sha256(token)
    if redis.exists(f"blacklist:{token_hash}"):
        raise Unauthorized("Token revoked")
    
    return claims
```

#### **Trade-offs:**

| Aspect | Stateless JWT | Stateful Sessions | Hybrid (Preferred) |
|--------|--------------|-------------------|-------------------|
| **Validation speed** | ✅ Fast (no DB) | ❌ Slow (Redis lookup) | ✅ Fast (JWT) |
| **Revocable** | ❌ No | ✅ Yes | ✅ Yes (refresh token) |
| **Scalability** | ✅ Excellent | ⚠️ Redis dependency | ✅ Excellent |
| **Token size** | ⚠️ Large (claims) | ✅ Small (UUID) | ✅ Small |
| **Security** | ⚠️ Can't revoke | ✅ Instant revoke | ✅ Revoke refresh |

#### **Security Best Practices:**

1. **Password hashing:** bcrypt with cost=12 (or Argon2)
2. **JWT algorithm:** RS256 (asymmetric), never HS256 with shared secret
3. **Token rotation:** Refresh tokens rotate on use (one-time use)
4. **Rate limiting:** 5 failed login attempts → lock account for 15 minutes
5. **MFA support:** Optional 2FA with TOTP (Google Authenticator)
6. **Token binding:** Bind refresh token to device_id to prevent theft

---

### D. Personalization Engine Design

**Goal:** Provide personalized POI recommendations for 100M users

#### **Options:**

1. **Rule-based (Simple)**
   * User prefers "Italian" → Boost Italian restaurants
   * Pros: Simple, explainable
   * Cons: Limited, doesn't learn from behavior

2. **Collaborative Filtering (User-based)**
   * "Users similar to you liked X"
   * Pros: Discovers new preferences
   * Cons: Cold start problem, expensive computation

3. **Content-based Filtering**
   * Recommend based on POI attributes (cuisine, price, rating)
   * Pros: No cold start, explainable
   * Cons: Filter bubble (only similar to past likes)

4. **Hybrid: Collaborative + Content-based + Rules** ✅ Preferred
   * Combine multiple signals
   * Pros: Best results, handles cold start
   * Cons: Complex implementation

#### **Preferred Architecture:**

```
┌──────────────────────────────────────────────┐
│       Personalization Pipeline               │
├──────────────────────────────────────────────┤
│                                              │
│ 1. Offline Training (Nightly batch)         │
│    ┌──────────────────────────┐            │
│    │ User-POI Interaction      │            │
│    │ Matrix (Spark)            │            │
│    │                           │            │
│    │   POI_1  POI_2  POI_3 .. │            │
│    │ U1  5*    null   4*      │            │
│    │ U2  null  5*     4*      │            │
│    │ U3  4*    5*     null    │            │
│    └───────────┬───────────────┘            │
│                ↓                             │
│    ┌──────────────────────────┐            │
│    │ Matrix Factorization     │            │
│    │ (ALS Algorithm)           │            │
│    │                           │            │
│    │ User Embedding:           │            │
│    │ U1 → [0.8, 0.3, -0.2, ...]│            │
│    │                           │            │
│    │ POI Embedding:            │            │
│    │ POI_1 → [0.7, 0.4, -0.1,..]│           │
│    └───────────┬───────────────┘            │
│                ↓                             │
│    ┌──────────────────────────┐            │
│    │ Store in Redis            │            │
│    │ user:{uid}:embedding      │            │
│    │ poi:{pid}:embedding       │            │
│    └──────────────────────────┘            │
│                                              │
│ 2. Online Serving (Real-time)               │
│    ┌──────────────────────────┐            │
│    │ User searches nearby      │            │
│    └───────────┬───────────────┘            │
│                ↓                             │
│    ┌──────────────────────────┐            │
│    │ Candidate Generation      │            │
│    │ - Geospatial filter       │            │
│    │ - Category filter         │            │
│    │ → 1000 candidate POIs     │            │
│    └───────────┬───────────────┘            │
│                ↓                             │
│    ┌──────────────────────────┐            │
│    │ Personalization Scoring   │            │
│    │ For each POI:             │            │
│    │   base_score = 1/distance │            │
│    │   collab_score = dot(      │            │
│    │     user_emb, poi_emb)    │            │
│    │   content_score = cuisine_│            │
│    │     match + price_match   │            │
│    │   final_score = weighted_ │            │
│    │     sum(base, collab,     │            │
│    │     content)               │            │
│    └───────────┬───────────────┘            │
│                ↓                             │
│    ┌──────────────────────────┐            │
│    │ Re-rank & Return Top N    │            │
│    └──────────────────────────┘            │
│                                              │
└──────────────────────────────────────────────┘
```

#### **Implementation Details:**

**Collaborative Filtering (Matrix Factorization with ALS):**

```python
from pyspark.ml.recommendation import ALS

# Build interaction matrix from reviews + bookmarks
interactions = [
    (user_id, poi_id, rating),  # rating = review rating or implicit (bookmark=5)
    ...
]

# Train ALS model
als = ALS(
    rank=50,  # Embedding dimension
    maxIter=10,
    regParam=0.1,
    userCol="user_id",
    itemCol="poi_id",
    ratingCol="rating"
)

model = als.fit(interactions)

# Extract embeddings
user_embeddings = model.userFactors  # (user_id, [vector])
poi_embeddings = model.itemFactors   # (poi_id, [vector])

# Store in Redis
for user_id, embedding in user_embeddings:
    redis.set(f"user:{user_id}:embedding", pickle.dumps(embedding))

for poi_id, embedding in poi_embeddings:
    redis.set(f"poi:{poi_id}:embedding", pickle.dumps(embedding))
```

**Real-time Scoring:**

```python
def personalized_search(user_id, lat, lng, radius, category):
    # 1. Candidate generation (geospatial)
    candidates = search_nearby(lat, lng, radius, category)  # Returns 1000 POIs
    
    # 2. Get user embedding
    user_emb = redis.get(f"user:{user_id}:embedding")
    if not user_emb:
        user_emb = np.zeros(50)  # Cold start: zero vector
    
    # 3. Score each candidate
    scored_pois = []
    for poi in candidates:
        # Base score (distance)
        base_score = 1.0 / (poi.distance + 1)  # Closer = higher score
        
        # Collaborative filtering score
        poi_emb = redis.get(f"poi:{poi.id}:embedding")
        if poi_emb:
            collab_score = np.dot(user_emb, poi_emb)  # Dot product
        else:
            collab_score = 0
        
        # Content-based score
        user_prefs = get_user_preferences(user_id)
        content_score = 0
        if poi.cuisine in user_prefs.favorite_cuisines:
            content_score += 1.0
        if poi.price_level in user_prefs.price_range:
            content_score += 0.5
        if poi.rating >= user_prefs.min_rating:
            content_score += 0.5
        
        # Weighted combination
        final_score = (
            0.4 * base_score +
            0.4 * collab_score +
            0.2 * content_score
        )
        
        scored_pois.append((poi, final_score))
    
    # 4. Sort by score and return top N
    scored_pois.sort(key=lambda x: x[1], reverse=True)
    return [poi for poi, score in scored_pois[:20]]
```

#### **Cold Start Handling:**

**New User (no history):**
1. Use content-based filtering only (preferences from registration)
2. Boost popular POIs in user's area
3. Ask explicit preferences during onboarding

**New POI (no reviews):**
1. Use content-based features (category, price, location)
2. Boost slightly for diversity (exploration vs exploitation)
3. As reviews come in, update embedding

#### **Performance Optimization:**

1. **Pre-compute embeddings:** Nightly batch job (Spark)
2. **Cache user embeddings:** Redis with 7-day TTL
3. **Approximate nearest neighbor:** Use FAISS for "similar POIs" queries
4. **Sampling:** For users with 1000+ interactions, sample representative subset

#### **Metrics:**

* **Offline:** AUC-ROC, RMSE on held-out test set
* **Online:** Click-through rate (CTR), conversion rate (bookmark/review rate)
* **A/B testing:** Compare personalized vs non-personalized results

---

### E. Social Graph Storage (Follow/Followers)

**Goal:** Store and query social relationships for 100M users

#### **Options:**

1. **Relational DB (MySQL/Postgres)**
   ```sql
   CREATE TABLE user_follow (
       follower_id UUID,
       following_id UUID,
       created_at TIMESTAMP,
       PRIMARY KEY (follower_id, following_id)
   );
   CREATE INDEX idx_following ON user_follow(following_id);
   ```
   * Pros: Simple, ACID guarantees
   * Cons: Slow for graph traversals ("friends of friends")

2. **Graph Database (Neo4j, Amazon Neptune)**
   ```cypher
   CREATE (u1:User {id: "user_123"})-[:FOLLOWS]->(u2:User {id: "user_456"})
   
   // Query: Get mutual follows (friends)
   MATCH (u1:User {id: "user_123"})-[:FOLLOWS]->(u2:User)-[:FOLLOWS]->(u1)
   RETURN u2
   ```
   * Pros: Fast graph traversals, natural model
   * Cons: Operational complexity, less mature ecosystem

3. **Hybrid: Postgres + Caching** ✅ Preferred for moderate scale
   * Store in Postgres for durability
   * Cache hot paths in Redis (user's following/followers lists)
   * Pros: Simple, good enough for most use cases
   * Cons: Limited graph query capability

#### **Preferred Architecture (Hybrid):**

```sql
-- Postgres (source of truth)
CREATE TABLE user_follow (
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id)
);

CREATE INDEX idx_follower ON user_follow(follower_id);
CREATE INDEX idx_following ON user_follow(following_id);

-- Redis cache (hot data)
-- Key: user:{user_id}:following
-- Value: Set[user_id, user_id, ...]
-- TTL: 1 hour
```

**Common Queries:**

```python
# 1. Get users I follow
def get_following(user_id):
    # Check cache first
    cached = redis.smembers(f"user:{user_id}:following")
    if cached:
        return cached
    
    # Cache miss: query DB
    following = db.query("""
        SELECT following_id FROM user_follow
        WHERE follower_id = ?
    """, user_id)
    
    # Populate cache
    redis.sadd(f"user:{user_id}:following", *following)
    redis.expire(f"user:{user_id}:following", 3600)
    
    return following

# 2. Get my followers
def get_followers(user_id):
    cached = redis.smembers(f"user:{user_id}:followers")
    if cached:
        return cached
    
    followers = db.query("""
        SELECT follower_id FROM user_follow
        WHERE following_id = ?
    """, user_id)
    
    redis.sadd(f"user:{user_id}:followers", *followers)
    redis.expire(f"user:{user_id}:followers", 3600)
    
    return followers

# 3. Follow a user
def follow_user(follower_id, following_id):
    # Write to DB
    db.execute("""
        INSERT INTO user_follow (follower_id, following_id)
        VALUES (?, ?)
        ON CONFLICT DO NOTHING
    """, follower_id, following_id)
    
    # Invalidate caches
    redis.delete(f"user:{follower_id}:following")
    redis.delete(f"user:{following_id}:followers")
    
    # Optional: Publish event for notification
    kafka.publish("social-events", {
        "type": "follow",
        "follower_id": follower_id,
        "following_id": following_id
    })
```

**Handling Celebrity Users (1M+ followers):**

Problem: User with 1M followers → 1M fanout on every action

Solution: **Hybrid push-pull model**
```python
CELEBRITY_THRESHOLD = 100000  # 100k followers

def publish_activity(user_id, activity):
    follower_count = count_followers(user_id)
    
    if follower_count < CELEBRITY_THRESHOLD:
        # Push model: Write to each follower's feed
        followers = get_followers(user_id)
        for follower_id in followers:
            write_to_feed(follower_id, activity)
    else:
        # Pull model: Followers fetch on-demand
        write_to_celebrity_feed(user_id, activity)

def get_user_feed(user_id):
    # Get activities from users I follow
    following = get_following(user_id)
    
    feed = []
    for followed_user_id in following:
        follower_count = count_followers(followed_user_id)
        
        if follower_count < CELEBRITY_THRESHOLD:
            # Already in my feed (push model)
            feed.extend(get_my_feed_entries(user_id))
        else:
            # Fetch from celebrity's feed (pull model)
            feed.extend(get_celebrity_feed(followed_user_id, limit=10))
    
    return sorted(feed, key=lambda x: x.timestamp, reverse=True)
```

#### **When to Use Graph DB:**

Use Neo4j/Neptune if you need:
* "Friends of friends" queries (2+ hops)
* Recommendation based on social graph ("people you may know")
* Complex graph algorithms (PageRank, community detection)

For Yelp-like system with simple follow/unfollow, **Postgres + Redis is sufficient**.

---

### F. Privacy & GDPR Compliance

**Goal:** Handle user data privacy, comply with GDPR/CCPA

#### **Key Requirements:**

1. **Right to Access:** User can export all their data
2. **Right to Deletion:** User can request account deletion
3. **Right to Rectification:** User can update incorrect data
4. **Data Portability:** Export in machine-readable format (JSON)
5. **Consent Management:** Track user consent for data processing
6. **Data Minimization:** Don't store unnecessary data
7. **Encryption:** PII encrypted at rest and in transit

#### **Implementation:**

**1. Data Export (Right to Access):**

```python
def export_user_data(user_id):
    """
    Export all user data in JSON format
    GDPR: Right to access
    """
    data = {
        "user_profile": get_user_profile(user_id),
        "reviews": get_user_reviews(user_id),
        "bookmarks": get_user_bookmarks(user_id),
        "search_history": get_search_history(user_id),
        "check_ins": get_check_ins(user_id),
        "following": get_following(user_id),
        "followers": get_followers(user_id),
        "activity_log": get_activity_log(user_id),
        "exported_at": datetime.now().isoformat()
    }
    
    # Generate download link (S3 pre-signed URL, expires in 7 days)
    filename = f"user_data_{user_id}_{uuid.uuid4()}.json"
    s3.put_object(
        Bucket="gdpr-exports",
        Key=filename,
        Body=json.dumps(data),
        ServerSideEncryption="AES256"
    )
    
    download_url = s3.generate_presigned_url(
        'get_object',
        Params={'Bucket': 'gdpr-exports', 'Key': filename},
        ExpiresIn=7*24*3600  # 7 days
    )
    
    # Email user with download link
    send_email(user.email, "Your data export is ready", download_url)
    
    return download_url
```

**2. Account Deletion (Right to Deletion):**

```python
def delete_user_account(user_id, reason="user_request"):
    """
    Delete user account and all associated data
    GDPR: Right to erasure ("Right to be forgotten")
    """
    # 1. Mark user as deleted (soft delete first for safety)
    db.execute("""
        UPDATE user
        SET status = 'deleted',
            email = CONCAT('deleted_', user_id, '@deleted.local'),
            username = CONCAT('deleted_', user_id),
            deleted_at = NOW(),
            deletion_reason = ?
        WHERE user_id = ?
    """, reason, user_id)
    
    # 2. Anonymize reviews (keep review content for POI owners)
    #    GDPR allows keeping data if legitimate business need
    db.execute("""
        UPDATE review
        SET user_id = '00000000-0000-0000-0000-000000000000',  -- Anonymous user
            user_display_name = 'Former User',
            user_photo_url = NULL
        WHERE user_id = ?
    """, user_id)
    
    # 3. Delete personal data
    delete_search_history(user_id)      # Search history
    delete_bookmarks(user_id)           # Bookmarks
    delete_activity_log(user_id)        # Activity logs
    delete_check_ins(user_id)           # Check-ins
    delete_follows(user_id)             # Social graph
    delete_sessions(user_id)            # Sessions
    delete_notifications(user_id)       # Notifications
    
    # 4. Delete photos uploaded by user
    photos = get_user_photos(user_id)
    for photo in photos:
        s3.delete_object(Bucket="user-photos", Key=photo.key)
    
    # 5. Invalidate all sessions
    redis.delete(f"session:{user_id}:*")
    
    # 6. Add to deletion log (for audit)
    db.execute("""
        INSERT INTO deletion_log (user_id, deleted_at, reason, deleted_by)
        VALUES (?, NOW(), ?, 'system')
    """, user_id, reason)
    
    # 7. Schedule hard delete after 30 days (grace period)
    schedule_hard_delete(user_id, delay_days=30)
    
    logger.info(f"User {user_id} account deleted (reason: {reason})")
```

**3. Location Privacy:**

```python
# Don't store exact search locations permanently
def log_search(user_id, lat, lng, query_params):
    # Option 1: Don't log at all (most private)
    pass
    
    # Option 2: Log with reduced precision
    # Round to ~1km precision (2 decimal places)
    anonymized_lat = round(lat, 2)
    anonymized_lng = round(lng, 2)
    
    db.execute("""
        INSERT INTO search_history (user_id, approx_lat, approx_lng, query_params, timestamp)
        VALUES (?, ?, ?, ?, NOW())
    """, user_id, anonymized_lat, anonymized_lng, query_params)
    
    # Option 3: Aggregate only (no individual tracking)
    # Just track "user searched in NYC" not exact coordinates
    city = geocode_to_city(lat, lng)
    db.execute("""
        INSERT INTO search_analytics (user_id, city, query_params, timestamp)
        VALUES (?, ?, ?, NOW())
    """, user_id, city, query_params)
```

**4. PII Encryption at Rest:**

```python
from cryptography.fernet import Fernet

# Encrypt sensitive fields
def encrypt_pii(plaintext):
    cipher = Fernet(ENCRYPTION_KEY)  # Stored in KMS
    return cipher.encrypt(plaintext.encode()).decode()

def decrypt_pii(ciphertext):
    cipher = Fernet(ENCRYPTION_KEY)
    return cipher.decrypt(ciphertext.encode()).decode()

# Schema with encryption
CREATE TABLE user (
    user_id UUID PRIMARY KEY,
    username VARCHAR,  -- Not PII
    email_encrypted VARCHAR,  -- Encrypted
    phone_encrypted VARCHAR,  -- Encrypted
    name_encrypted VARCHAR,   -- Encrypted
    ...
);

# Application layer encryption/decryption
class User:
    @property
    def email(self):
        return decrypt_pii(self._email_encrypted)
    
    @email.setter
    def email(self, value):
        self._email_encrypted = encrypt_pii(value)
```

**5. Consent Management:**

```sql
CREATE TABLE user_consent (
    user_id UUID,
    consent_type VARCHAR,  -- 'marketing', 'analytics', 'personalization'
    granted BOOLEAN,
    timestamp TIMESTAMP,
    PRIMARY KEY (user_id, consent_type)
);

-- Check consent before processing
def can_track_user(user_id, purpose):
    consent = db.query("""
        SELECT granted FROM user_consent
        WHERE user_id = ? AND consent_type = ?
    """, user_id, purpose)
    
    return consent.granted if consent else False

# Use in code
if can_track_user(user_id, 'personalization'):
    log_user_activity(user_id, activity)
else:
    # Skip tracking
    pass
```

#### **GDPR Checklist:**

- [x] Data export API
- [x] Account deletion (soft + hard delete)
- [x] Location data minimization
- [x] PII encryption at rest
- [x] Consent management
- [x] Data retention policies (auto-delete after N days)
- [x] Audit logging (who accessed what data when)
- [x] Data Processing Agreement (DPA) with third parties
- [x] Privacy policy and terms of service
- [x] Cookie consent banner

---

### G. Caching Strategy — Deep Dive

*[Keep the detailed caching section from previous version with user-level cache added]*

**Additional layer: User-level cache**

```
Key: user:{user_id}:profile
Value: {username, preferences, last_location, ...}
TTL: 30 minutes

Key: user:{user_id}:following
Value: Set[user_ids]
TTL: 1 hour
```

---

### H. Reviews and Rating Aggregation

*[Keep the existing section, ensure it mentions user_id → reviews index]*

---

### I. Multi-Region and Global Scale

*[Keep existing section, add note about user data residency for GDPR]*

**Additional consideration: Data residency**

For GDPR compliance, EU user data must stay in EU:
```
User table: Partition by region
  - user_eu_shard: EU users only
  - user_us_shard: US users only
  - user_asia_shard: Asian users only

Route write/read to appropriate region based on user's registration country
```

---

## 11) Reliability & edge cases

**POI-related:**
* Stale POI data (business closed) → User reports, periodic verification
* Boundary queries → Query neighboring geohashes
* Duplicate POIs → Deduplication service with fuzzy matching
* Hot POI failures → Multi-tier caching, read replicas

**User-related:**
* Account takeover → Rate limiting, MFA, suspicious login detection
* Password reset attacks → Email verification, rate limiting
* Concurrent profile updates → Optimistic locking with version number
* Session hijacking → Bind tokens to device fingerprint
* Spam reviews → ML-based spam detection, user reputation system
* User data corruption → Regular backups, point-in-time recovery
* GDPR deletion failures → Retry queue, manual intervention log

---

## 12) Operational concerns & observability

**Metrics:**
* **POI metrics:** Search QPS, p95/p99 latency, cache hit rate
* **User metrics:** Active users, login success rate, registration funnel
* **Auth metrics:** Token validation latency, failed login attempts
* **Personalization metrics:** Recommendation CTR, user engagement

**Tracing:**
* End-to-end distributed tracing (OpenTelemetry)
* Trace from login → search → click → bookmark
* Include user_id in all trace contexts

**Alerts:**
* High error rate (>1%)
* Latency spike (p99 > 1s)
* Cache hit rate drop (<70%)
* Failed login spike (credential stuffing attack?)
* GDPR export queue backlog

**Dashboards:**
* Real-time query heatmap
* User activity funnel (search → click → review)
* Auth success/failure rates
* Database shard load distribution

---

## 13) Security & privacy

**Authentication:**
* JWT with RS256 (asymmetric keys)
* Password hashing: bcrypt (cost=12) or Argon2
* MFA support (TOTP, SMS backup)
* OAuth 2.0 for social login (Google, Facebook, Apple)

**Authorization:**
* Role-based access control (RBAC): user, business_owner, admin
* Row-level security (user can only modify their own data)

**Rate Limiting:**
* Per-user: 100 requests/minute
* Per-IP: 1000 requests/minute
* Login attempts: 5 failures → 15 minute lockout

**Data Privacy:**
* Encrypt PII at rest (email, phone, location history)
* Anonymize search locations (round to 2 decimals)
* GDPR/CCPA compliance (data export, deletion)
* User consent management

**Input Validation:**
* Sanitize all user inputs (reviews, POI edits)
* Prevent XSS, SQL injection
* Image uploads: virus scanning, content moderation

**API Security:**
* HTTPS only (TLS 1.3)
* CORS policies for web clients
* API key authentication for business partners
* DDoS protection (CloudFlare, AWS Shield)

**Secrets Management:**
* Store secrets in KMS (AWS Secrets Manager, HashiCorp Vault)
* Rotate credentials regularly
* Never log secrets

---

## 14) Cost optimization

**Storage:**
* Use S3 Glacier for old photos/reviews (>1 year)
* Compress images (WebP format, 80% quality)
* CDN for media delivery (reduce origin bandwidth)
* Archive deleted user data after 30 days

**Compute:**
* Auto-scale API servers based on traffic
* Use spot instances for batch jobs (ML training)
* Serverless for infrequent tasks (GDPR export)

**Database:**
* Read replicas for hot shards
* Archive old search history (>90 days) to cold storage
* Use connection pooling

**Cache:**
* Right-size Redis cluster (monitor hit rate)
* Use TTL to evict stale data automatically
* Cache only hot data (top 20% of users/POIs)

**Personalization:**
* Train ML models nightly (not real-time)
* Use sampling for large datasets
* Cache embeddings in Redis

---

## References

* **Geohash:** http://geohash.org/
* **QuadKeys (Bing Maps):** https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system
* **S2 Geometry:** https://s2geometry.io/
* **Elasticsearch Geo Queries:** https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-queries.html
* **JWT Best Practices:** https://tools.ietf.org/html/rfc8725
* **GDPR Compliance:** https://gdpr.eu/
* **Yelp Engineering Blog:** https://engineeringblog.yelp.com/
* **Uber Engineering:** https://eng.uber.com/ (H3 geospatial indexing)
* **Foursquare Engineering:** https://foursquare.com/developers
* **System Design Interview Volume 2** (Chapter: Nearby Friends / Proximity Service)

---

## Interview Tips

1. **Start with clarifications:** Nail down scale, user features vs POI features, privacy requirements
2. **Draw simple design first:** Show you understand both POI and User services
3. **Discuss dual sharding early:** POI by geohash, User by user_id (critical architectural decision)
4. **Deep dive on auth:** Interviewers often focus on authentication security
5. **Personalization:** Show you understand ML basics (collaborative filtering)
6. **Privacy matters:** Mention GDPR, PII encryption, consent management
7. **Discuss trade-offs:** Always present multiple options and justify choice
8. **Mention real-world systems:** Reference Yelp, Foursquare, Google Maps
9. **User lifecycle:** Registration → Search → Bookmark → Review → Share
10. **Edge cases:** Account takeover, spam reviews, celebrity users, GDPR deletion

---

## Appendix A: How Geohash Works (Deep Dive)

*[Keep the entire detailed Geohash explanation from the previous version (~350 lines)]*

*(This appendix remains unchanged - includes step-by-step encoding, visual representation, precision levels, boundary problem, code examples, etc.)*

---

## Appendix B: Geohash Resharding Process

### Why Resharding is Easy with Geohash

**Key property:** Geohash is hierarchical - changing shard boundaries = adjusting prefix rules (no need to recompute hashes)

### Resharding Strategies

#### **Strategy 1: Prefix-Based Split**

```
Before (2-char prefix):
Shard_dr: All "dr*" (Northeast USA) - 1M POIs

After (3-char prefix):
Shard_dr0, Shard_dr1, ..., Shard_drz: 32 shards
Each ~31K POIs
```

**Migration:**
```sql
-- Old mapping
shard_id = geohash[:2]

-- New mapping
shard_id = geohash[:3]

-- No recomputation! Just reassign based on existing geohash
```

#### **Strategy 2: Hotspot Isolation**

```
Before:
Shard_dr5r: All of "dr5r*" (Manhattan + surrounding)

After:
Shard_dr5r_hot: "dr5ru" (Manhattan midtown) - dedicated
Shard_dr5r_cold: Rest of "dr5r*"
```

### Step-by-Step Resharding Process

**Phase 1: Planning**
* Analyze shard distribution
* Choose new boundaries
* Provision new infrastructure

**Phase 2: Dual-Write**
```python
if migration_mode:
    # Write to BOTH old and new shards
    write_to_shard(old_shard_id, data)
    write_to_shard(new_shard_id, data)
```

**Phase 3: Backfill**
* Copy existing data from old to new shards
* Batch process, throttle to avoid overload

**Phase 4: Verification**
* Compare row counts
* Sample data integrity checks

**Phase 5: Cutover**
* Gradually shift read traffic (1% → 5% → 25% → 50% → 100%)
* Monitor metrics (latency, errors)

**Phase 6: Cleanup**
* Stop dual writes
* Decommission old shards
* Archive to S3

### Key Advantages

| Aspect | Geohash Resharding | Hash-based Resharding |
|--------|-------------------|----------------------|
| **Recompute keys?** | ❌ No | ✅ Yes (rehash all IDs) |
| **Partial resharding** | ✅ Split hot prefixes | ❌ All-or-nothing |
| **Predictable** | ✅ 32-way per char | ❌ Hash-dependent |
| **Zero downtime** | ✅ Dual-write | ⚠️ Complex |

---

**End of Document**

---

