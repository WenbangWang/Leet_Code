# Design: Point of Interest (POI) System (Yelp/Uber-like)

---

## 1) Clarification questions (ordered FR → NFR)

**Functional (FR)**

1. What types of POIs do we support? (restaurants, hotels, gas stations, landmarks, events?)
2. Do we need to support user-generated content (reviews, ratings, photos)?
3. Should we support real-time updates (e.g., restaurant closing temporarily, live wait times)?
4. Do we need search by category, name, or both? What about filters (price range, rating, open now)?
5. Is there a requirement for recommendations/personalization based on user history?
6. Do we support business owner features (claim listing, update hours, respond to reviews)?

**Non-functional (NFR)**

1. Target scale: number of POIs (millions?), daily active users (DAUs), concurrent queries?
2. Query latency target (e.g., p95 < 200ms for nearby search)?
3. Read vs write ratio (typically 99:1 for POI systems since reads dominate)?
4. Geographic coverage: global or specific regions?
5. Availability requirements (e.g., 99.99%)?
6. Consistency requirements: eventual consistency acceptable for reviews/ratings?

---

## 2) Functional requirements (FR) — condensed

* Store and manage POI data (name, location, category, hours, contact, metadata)
* Search for nearby POIs by location (lat/lng) with optional filters
* Search by name, category, or text query
* Return ranked results based on distance, rating, relevance
* Support user reviews and ratings (CRUD operations)
* Display aggregate metrics (average rating, review count, price level)
* Handle POI updates from business owners
* Support pagination for search results
* Provide POI detail view with photos, hours, reviews
* Real-time availability (optional: open now, wait time)

## 3) Non-functional requirements (NFR)

* Low latency for search queries (p95 < 200ms, p99 < 500ms)
* High availability (99.99%) for read path
* Horizontal scalability for billions of searches per day
* Eventually consistent for reviews/ratings (tolerate slight delays)
* Strong consistency for POI core data (location, hours)
* Cost-efficient storage for large POI datasets and media
* Handle hot POIs (popular restaurants in dense cities) without hotspots

---

## 4) Back-of-envelope calculation (BoE)

Assume: 500M total POIs worldwide, 100M DAU, each user performs 5 searches/day.

**Query load:**
* Total searches/day = 100M * 5 = 500M searches/day
* Peak QPS (assuming 10x peak during lunch/dinner): 500M / 86400 ≈ 5,787 avg QPS → 60k peak QPS

**Storage:**

* **POI metadata:** 500M POIs * 10 KB each (name, location, category, hours, etc.) = 5 TB raw
* **Reviews:** Assume 10% of POIs have reviews, avg 50 reviews/POI, 2 KB/review
  * 50M POIs * 50 reviews * 2 KB = 5 TB for reviews
* **Photos:** Assume 20% of POIs have photos, avg 10 photos/POI, 500 KB/photo (thumbnail + full)
  * 100M POIs * 10 * 500 KB = 500 TB for photos (store in object storage)
* **Total storage:** ~6 TB structured data + 500 TB media (compressed/CDN-cached)

**Geospatial index:**
* Using geohash (6-8 character precision covers ~1km² grids)
* Index size: 500M POIs * (8 bytes lat + 8 bytes lng + 8 bytes geohash + 8 bytes poi_id) ≈ 16 GB (easily fits in memory for hot regions)

**Caching:**
* Popular queries (e.g., "restaurants near Times Square") should be cached
* Cache hit ratio target: 80-90% for popular searches
* Cache size: 100M cache entries * 10 KB ≈ 1 TB (distributed across cache cluster)

---

## 5) Core data entities

1. **POI (Point of Interest)**
   * `poi_id (UUID), name, latitude, longitude, geohash, category, subcategory, address, phone, website, hours_of_operation (JSON), price_level (1-4), status (active/closed/temporarily_closed), created_at, updated_at, owner_id`

2. **Review**
   * `review_id, poi_id, user_id, rating (1-5), text, photos[], helpful_count, created_at, updated_at`

3. **AggregateRating**
   * `poi_id, average_rating, review_count, rating_distribution (JSON: {5: count, 4: count, ...}), last_updated_at`
   * Materialized view updated periodically or via stream processing

4. **POICategory**
   * `category_id, name, parent_category_id` (hierarchical: Food > Restaurants > Italian)

5. **User**
   * `user_id, username, location (optional for personalization), preferences (JSON)`

6. **Photo**
   * `photo_id, poi_id, user_id, url (CDN), thumbnail_url, created_at`

7. **BusinessOwner**
   * `owner_id, poi_id, verified, claim_date`

8. **SearchIndex (Elasticsearch document)**
   * Denormalized: `poi_id, name, category, location (geo_point), rating, review_count, tags[], open_now (boolean)`

---

## 6) System interfaces (APIs)

**Client-facing:**

* `GET /search/nearby?lat={lat}&lng={lng}&radius={meters}&category={cat}&filters={...}&page={n}` → list of POIs with distance
* `GET /search/text?query={text}&location={lat,lng}&page={n}` → text search results
* `GET /poi/{poi_id}` → detailed POI info (with reviews, photos, hours)
* `POST /reviews` {poi_id, rating, text, photos[]} → create review
* `PUT /reviews/{review_id}` → update review
* `DELETE /reviews/{review_id}` → delete review
* `POST /poi/{poi_id}/photos` → upload photo
* `POST /business/claim` {poi_id, proof} → claim business ownership
* `PUT /business/poi/{poi_id}` → update POI details (owner only)

**Internal services:**

* POI Service: CRUD for POI data
* Search Service: geospatial + text search
* Review Service: CRUD for reviews, rating aggregation
* Media Service: photo upload/storage/CDN
* Indexing Service: sync POI data to Elasticsearch
* Cache Service: distributed cache (Redis)

---

## 7) Start from simple design (ASCII diagram)

Simple assumption: modest scale, single region, basic geospatial query using database with PostGIS or geohash filtering.

```
[Client]
   |
   v
[API Gateway / LB]
   |
   v
[Search Service]
   |
   +---> [POI DB (PostgreSQL + PostGIS)]
   |       (SELECT * FROM poi WHERE ST_Distance(location, point) < radius)
   |
   +---> [Review DB]
   |
   v
[Cache (Redis)] <---> [CDN (photos)]
```

**Flow (nearby search):**

1. Client sends `GET /search/nearby?lat=40.7&lng=-74.0&radius=1000&category=restaurant`
2. API Gateway routes to Search Service
3. Search Service:
   a. Check cache for query key (lat, lng, radius, category) → cache miss
   b. Query POI DB with geospatial filter (PostGIS `ST_Distance` or geohash range query)
   c. Fetch top N results sorted by distance (or rating if specified)
   d. Enrich with aggregate ratings from Review DB or materialized view
   e. Store results in cache with TTL (e.g., 5 minutes)
4. Return results to client

**Rationale:** Simple design works for small-medium scale (<10M POIs, <1k QPS). PostGIS provides geospatial indexing (GIST index). Single DB is acceptable if properly indexed and cached.

---

## 8) Enrich the design (scale / HA / performance) — ASCII diagram

At scale we add:

* Sharded POI database (by geohash prefix or region)
* Elasticsearch for text search and complex filtering
* Distributed cache (Redis cluster) for hot queries
* Read replicas for POI/Review DBs
* CDN for photos and static assets
* Write path via message queue for async processing (reviews, rating aggregation)
* Stream processing for real-time aggregations

```
                     +------------------+
                     |   Client (Web/   |
                     |   Mobile App)    |
                     +--------+---------+
                              |
                              v
                     +------------------+
                     | API Gateway / LB |
                     | (Auth, RateLimit)|
                     +--------+---------+
                              |
              +---------------+---------------+
              |                               |
              v                               v
     +----------------+              +------------------+
     | Search Service |              | Review Service   |
     +----------------+              +------------------+
              |                               |
              |                               v
              |                      [Review DB (sharded)]
              |                               |
              |                               v
              |                      [Kafka: review-events]
              |                               |
              v                               v
  +------------------------+        +-------------------+
  | Geospatial Query Path  |        | Rating Aggregator |
  +------------------------+        | (Stream Processor)|
              |                     +-------------------+
      +-------+-------+                      |
      |               |                      v
      v               v              [AggregateRating DB]
[Elasticsearch]  [POI DB (sharded)]
(text search,    (core POI data,
 filters)         geohash indexed)
      |               |
      +-------+-------+
              |
              v
     +----------------+
     | Redis Cluster  |
     | (cache hot     |
     |  queries)      |
     +----------------+
              |
              v
          [CDN] <--- [Object Storage (S3)]
                     (photos, media)
```

---

### **Flow Explanation (enriched)**

**Nearby search (geospatial):**

1. Client → API Gateway → Search Service
2. Search Service checks Redis cache with key: `nearby:{geohash}:{radius}:{category}:{filters}`
   * Cache hit → return immediately (p50 < 10ms)
   * Cache miss → proceed to query
3. Search Service determines geohash prefix for query location (e.g., 6-char geohash covers ~1km²)
4. Query POI DB (sharded by geohash prefix) for POIs in geohash range
   * Use geohash range query: `WHERE geohash BETWEEN 'dr5ru0' AND 'dr5ruz'`
   * Filter by category, price_level, rating
5. Fetch aggregate ratings from AggregateRating DB (or join with materialized view)
6. Rank results by distance (Haversine formula) or relevance score
7. Cache results in Redis with TTL (5-10 minutes)
8. Return top N results to client

**Text search (e.g., "best pizza near me"):**

1. Search Service queries Elasticsearch with:
   * Text query on `name`, `category`, `tags` fields
   * Geo distance filter for "near me"
   * Boosting by rating, review count
2. Elasticsearch returns ranked POI IDs
3. Fetch full POI details from POI DB or cache
4. Return results

**Write path (submit review):**

1. Client → API Gateway → Review Service
2. Review Service:
   a. Validate review (auth, rate limit, duplicate check)
   b. Write review to Review DB (sharded by `poi_id`)
   c. Publish event to Kafka topic `review-events`
3. Rating Aggregator (stream processor) consumes `review-events`:
   a. Update `AggregateRating` table (increment review_count, recompute avg_rating)
   b. Invalidate cache for affected POI
4. Indexing Service (async) updates Elasticsearch with new rating/review_count

---

## 9) Important design patterns & components

1. **Geospatial indexing (Geohash vs QuadTree vs S2)**
   * **Geohash:** Encode lat/lng into base32 string (e.g., `dr5ru`). Nearby locations share prefix. Good for range queries and sharding. Limitation: edge cases at geohash boundaries.
   * **QuadTree:** Recursively divide map into 4 quadrants. Good for in-memory spatial index. Limitation: harder to shard.
   * **S2 (Google's Hilbert curve):** More accurate for spherical geometry, avoids boundary issues. Used by Google Maps, Uber.
   * **Preferred:** Use **Geohash for sharding and rough filtering** + **Haversine distance for final ranking**. For very high accuracy, consider S2.

2. **Sharding strategy**
   * **By geohash prefix:** Shard POI DB by first N characters of geohash (e.g., 2-char → 1024 shards). Pros: geographically co-located POIs in same shard. Cons: hot shards in dense cities.
   * **By POI ID hash:** Even distribution. Pros: no hotspots. Cons: nearby queries span multiple shards.
   * **Hybrid:** Use geohash for read path (query single shard for nearby), poi_id hash for write path. Replicate POIs to multiple shards for availability.

3. **Caching strategy**
   * **Query-level cache:** Cache results of common queries (key = geohash + radius + filters). TTL 5-10 minutes.
   * **POI-level cache:** Cache individual POI details (key = poi_id). TTL 1 hour.
   * **Negative cache:** Cache "no results" to avoid repeated empty queries.

4. **Read replica & materialized views**
   * Read replicas for POI DB to handle high read QPS.
   * Materialized view for `AggregateRating` to avoid computing on every query.

5. **Stream processing for aggregations**
   * Use Kafka + Flink/Spark Streaming to update aggregate ratings in real-time without blocking write path.

---

## 10) Possible deep dives (options, trade-offs, preferred)

### A. Geospatial indexing — Geohash vs QuadTree vs S2 vs QuadKeys

**Options:**

1. **Geohash**
   * Base32 encoding of lat/lng into string (e.g., `dr5ru6f`)
   * Precision: 6 chars ≈ 1.2km × 0.6km, 7 chars ≈ 153m × 153m
   * Pros: Simple, good for sharding, supports prefix queries
   * Cons: Edge discontinuities (nearby locations may have different prefixes at geohash boundaries)

2. **QuadTree (Traditional)**
   * Recursively partition space into 4 quadrants
   * Pros: Efficient for in-memory spatial index, good for dynamic datasets
   * Cons: **Complex to shard**, harder to persist

3. **QuadKeys (Shardable QuadTree)**
   * Encode tree path as string (e.g., `"120301"`)
   * Essentially a **Morton code** / Z-order curve
   * Pros: Combines QuadTree semantics with sharding capability, prefix sharing preserves proximity
   * Cons: Still has boundary issues like Geohash

4. **S2 (Hilbert curve-based)**
   * Google's library for spherical geometry
   * Pros: Accurate on sphere (no projection distortion), solves boundary issues, supports covering queries
   * Cons: More complex, requires external library

---

#### **Why Traditional QuadTree is Hard to Shard**

**Problem 1: Hierarchical Tree Structure**

```
                [Root: Entire World]
                /      |      |      \
          [NW]      [NE]    [SW]     [SE]
          / | \ \    / | \ \
       [...children...]  [...children...]
```

* Tree has parent-child **dependencies** - can't split cleanly across machines
* Queries traverse from root → leaves, requiring cross-machine hops if distributed
* No flat structure to partition

**Problem 2: No Natural Partitioning Key**

```
Geohash: "dr5ru6" → Shard by prefix "dr" → Easy!

QuadTree node: How do you express "Root→NW→NE→SE" as a shard key?
```

* Bounding box coordinates don't preserve spatial ordering
* Can't efficiently compute which shard a point belongs to

**Problem 3: Unbalanced Distribution**

```
Dense City (NYC):      Rural Area (Montana):
    Root                   Root
    / | \ \                /
  [10 levels]          [3 levels]
```

* Dense areas → deep branches; sparse areas → shallow branches
* Impossible to evenly distribute across shards

---

#### **Solution: QuadKeys (Bing Maps Approach)**

Instead of storing tree structure, **encode the path from root to leaf as a string**.

**Quadrant Numbering:**
```
┌────┬────┐
│ 0  │ 1  │  NW=0, NE=1
├────┼────┤  SW=2, SE=3
│ 2  │ 3  │
└────┴────┘
```

**Encoding Example: New York City (40.7128, -74.0060)**

```
Level 1: World → 4 quadrants
┌────────┬────────┐
│   0    │   1    │  Point in NE (quadrant 1)
├────────┼────────┤  QuadKey: "1"
│   2    │   3    │
└────────┴────────┘

Level 2: Subdivide quadrant 1
┌────┬────┐
│ 0  │ 1  │  Point in SW (quadrant 2)
├────┼────┤  QuadKey: "12"
│ 2  │ 3  │
└────┴────┘

Level 3: Continue...
QuadKey: "120"

Level 8: Building-level precision
QuadKey: "12030012"
```

**Why QuadKeys Work for Sharding:**

```python
# Property 1: Prefix sharing = proximity
times_square = "12030012"
fifth_avenue = "12030013"  # Same prefix "1203001" → nearby!
tokyo        = "13300210"  # Different prefix → far away

# Property 2: Simple shard mapping
def get_shard(quadkey):
    return f"shard_{quadkey[:2]}"  # Shard by first 2 chars

# Property 3: Point → QuadKey is computable (O(level))
def lat_lng_to_quadkey(lat, lng, level):
    quadkey = ""
    lat_range, lng_range = [-90, 90], [-180, 180]
    
    for _ in range(level):
        lat_mid = (lat_range[0] + lat_range[1]) / 2
        lng_mid = (lng_range[0] + lng_range[1]) / 2
        
        if lat >= lat_mid:  # North
            quadkey += "1" if lng >= lng_mid else "0"
            lat_range[0] = lat_mid
            lng_range = [lng_mid, lng_range[1]] if lng >= lng_mid else [lng_range[0], lng_mid]
        else:  # South
            quadkey += "3" if lng >= lng_mid else "2"
            lat_range[1] = lat_mid
            lng_range = [lng_mid, lng_range[1]] if lng >= lng_mid else [lng_range[0], lng_mid]
    
    return quadkey
```

---

#### **QuadKeys = Morton Codes (Z-Order Curve)**

QuadKeys are **Morton codes** with interleaved X/Y coordinates:

```
QuadKey: "132"
Binary:   01 11 10
          ↓  ↓  ↓
          NE SE SW

Each quadrant = 2 bits:
00 = NW (0), 01 = NE (1), 10 = SW (2), 11 = SE (3)

This is conceptually similar to Geohash (interleaves lng/lat bits)!
```

---

#### **Comparison Table**

| Aspect | Traditional QuadTree | QuadKeys | Geohash | S2 |
|--------|---------------------|----------|---------|-----|
| **Structure** | Hierarchical tree | Flat string (path) | Flat string (interleaved) | Hilbert curve |
| **Proximity preserved?** | ❌ Depends on tree | ✅ Prefix sharing | ✅ Prefix sharing | ✅ Best |
| **Sharding** | ❌ Very hard | ✅ Easy (prefix) | ✅ Easy (prefix) | ✅ Moderate |
| **Point → Key** | ❌ O(N) shard lookup | ✅ O(level) | ✅ O(precision) | ✅ O(1) |
| **In-memory benefit** | ✅ High | ⚠️ Medium | ⚠️ Low | ⚠️ Medium |
| **Boundary issues** | ⚠️ Yes | ⚠️ Yes | ⚠️ Yes | ✅ Minimal |
| **Used by** | Small systems | Bing Maps | Redis, many DBs | Google, Uber |

---

#### **Trade-offs:**

* **Geohash:** Best for simple implementation and sharding. Most databases support it. Acceptable for most use cases.
* **Traditional QuadTree:** Only good for in-memory index on single machine. Don't use for distributed systems.
* **QuadKeys:** If you need QuadTree semantics with sharding capability. Similar complexity to Geohash.
* **S2:** Best for global-scale systems with highest accuracy requirements (Google Maps, Uber). Steeper learning curve.

---

#### **Preferred Approach:**

**For most POI systems:**
* Use **Geohash for sharding and initial filtering** (query geohash range from DB)
* Compute **Haversine distance** in application layer for accurate ranking
* Handle boundary cases by querying neighboring geohashes

**For systems needing QuadTree semantics:**
* Use **QuadKeys** (combines tree structure with sharding)
* Store both QuadKey and bounding box for flexibility

**For global-scale, high-precision:**
* Migrate to **S2** geometry (Google's solution)

---

#### **Implementation Examples:**

**Option 1: Geohash (Simplest)**
```sql
CREATE TABLE poi (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    geohash_6 CHAR(6),  -- ~600m precision
    geohash_8 CHAR(8)   -- ~19m precision
);
CREATE INDEX idx_geohash_6 ON poi(geohash_6);

-- Query
SELECT * FROM poi 
WHERE geohash_6 BETWEEN 'dr5ru0' AND 'dr5ruz' 
  AND category = 'restaurant' 
ORDER BY distance LIMIT 20;
```

**Option 2: QuadKeys (QuadTree-like)**
```sql
CREATE TABLE poi (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    quadkey_6 CHAR(6),  -- 6 levels deep
    quadkey_8 CHAR(8)   -- 8 levels deep
);
CREATE INDEX idx_quadkey_6 ON poi(quadkey_6);

-- Shard by prefix
shard_id = quadkey[:2]
```

**Option 3: Hybrid (Best flexibility)**
```sql
CREATE TABLE poi (
    geohash CHAR(8),    -- For simple proximity
    quadkey CHAR(8),    -- For hierarchical queries
    bbox GEOMETRY       -- For exact spatial filtering
);
```

---

#### **Real-World Usage:**

* **Bing Maps:** Uses QuadKeys for tile system (`http://t0.tiles.virtualearth.net/tiles/a120300120.jpeg`)
* **Redis Geo:** Uses Geohash internally for GEORADIUS commands
* **PostgreSQL + PostGIS:** Uses R-Tree (similar to QuadTree) but in-memory, not sharded
* **Google Maps:** Uses S2 geometry library
* **Uber:** Uses H3 (hexagonal grid) for analytics, S2 for routing

---

#### **Key Takeaway:**

Traditional QuadTree is **fundamentally hard to shard** due to hierarchical dependencies. The solution is to **flatten the tree into a linear encoding** (QuadKeys/Morton codes), which gives you QuadTree semantics with Geohash-like sharding properties. For most use cases, **Geohash is simpler and sufficient**.

---

### B. Sharding strategy for POI database

**Goal:** Distribute 500M+ POIs across multiple DB shards to handle high QPS.

**Options:**

1. **Geohash-based sharding**
   * Partition by geohash prefix (e.g., first 2 characters → 1024 shards)
   * Pros: Nearby queries hit single shard (low latency)
   * Cons: Hot shards in dense cities (NYC, Tokyo); uneven distribution

2. **POI ID hash-based sharding**
   * Partition by hash(poi_id) → shard_id
   * Pros: Uniform distribution, no hotspots
   * Cons: Nearby queries must fan out to multiple shards (increased latency)

3. **Hybrid / Composite sharding**
   * Primary: geohash (for reads)
   * Secondary: replicate popular POIs across shards or use consistent hashing
   * Reads use geohash shard; writes use poi_id hash shard

**Trade-offs:**

* **Geohash sharding:** Optimized for nearby queries (most common), but suffers from hotspots.
* **ID hash sharding:** Even load, but nearby queries require scatter-gather across shards.
* **Hybrid:** Best of both, but increases complexity (dual indexing).

**Preferred:**

* Start with **geohash sharding** (2-3 char prefix for regional sharding).
* Handle hot shards with:
  1. **Read replicas** for each shard
  2. **Caching layer** to absorb read load
  3. **Split hot geohashes** into smaller shards (e.g., dense cities get dedicated shards)

**Example sharding logic:**

```
shard_id = geohash.substring(0, 2)  // First 2 chars
// dr -> shard_dr, 9q -> shard_9q, etc.
```

**Handling cross-shard queries (boundary cases):**

* If query center is near geohash boundary, query neighboring shards.
* Use application-level merge-sort to combine results from multiple shards and rank by distance.

---

### C. Caching strategy — what to cache and TTL

**Goal:** Reduce DB load and improve latency for frequent queries.

**Options:**

1. **Query-result cache (full search results)**
   * Key: `hash(lat, lng, radius, category, filters)`
   * Value: list of POI IDs with metadata
   * TTL: 5-10 minutes (balance freshness vs cache hit rate)
   * Pros: Fastest for identical queries
   * Cons: Low hit rate for unique queries (e.g., slight location variations)

2. **POI-level cache (individual POI details)**
   * Key: `poi:{poi_id}`
   * Value: full POI object (name, location, hours, aggregate rating)
   * TTL: 1 hour (static data changes infrequently)
   * Pros: High reuse across different queries
   * Cons: Still requires DB query for geospatial filtering

3. **Geohash-region cache (POIs in a geohash cell)**
   * Key: `geohash:{prefix}:{category}`
   * Value: list of POI IDs in that geohash
   * TTL: 30 minutes
   * Pros: Covers multiple nearby queries within same geohash
   * Cons: Requires application-level distance filtering

4. **Negative cache (no results)**
   * Cache queries with no results to avoid repeated DB hits
   * TTL: 1 minute (short to allow new POIs)

**Trade-offs:**

* **Query-result cache:** Best for high-traffic identical queries (e.g., "restaurants near Times Square lunch time"). Low hit rate for unique locations.
* **POI-level cache:** Good for popular POIs, complements query-level cache.
* **Geohash-region cache:** Good middle ground, handles variations in query center within same region.

**Preferred:**

* **Multi-layer caching:**
  1. **L1: Query-result cache** (5 min TTL) for exact query matches
  2. **L2: Geohash-region cache** (30 min TTL) for approximate matches
  3. **L3: POI-level cache** (1 hour TTL) for individual POI details
* Use **cache-aside pattern** (check cache → miss → query DB → populate cache).
* **Invalidation:** On POI update or new review, invalidate affected cache keys (by poi_id and geohash).

**Cache sizing:**

* L1 (query-result): 10M entries * 10 KB = 100 GB
* L2 (geohash-region): 1M entries * 50 KB = 50 GB
* L3 (POI-level): 100M entries (popular POIs) * 5 KB = 500 GB
* **Total:** ~650 GB (distributed across Redis cluster)

---

### D. Text search vs geospatial search — Elasticsearch design

**Goal:** Support both "pizza near me" (geospatial) and "Joe's Pizza Brooklyn" (text + geo).

**Options:**

1. **Elasticsearch only**
   * Store all POI data in Elasticsearch
   * Use `geo_point` field for location + text fields for search
   * Pros: Single index for both text and geo, fast
   * Cons: Not ideal for transactional writes, eventual consistency

2. **Hybrid: PostgreSQL + Elasticsearch**
   * PostgreSQL (source of truth for POI data)
   * Elasticsearch (read-optimized index for search)
   * Sync via Change Data Capture (CDC) or Kafka
   * Pros: Strong consistency in DB, fast search in ES
   * Cons: Dual write complexity, sync delay

**Trade-offs:**

* **ES-only:** Simpler architecture, but risky for critical writes (POI ownership, hours).
* **Hybrid:** More reliable, but adds sync complexity.

**Preferred:**

* Use **hybrid approach:**
  1. **PostgreSQL** as source of truth for POI core data (location, hours, owner)
  2. **Elasticsearch** for search index (synced via Kafka or Debezium CDC)
  3. Write path: write to PostgreSQL → publish event to Kafka → ES indexer consumes and updates ES
  4. Read path: query ES for search → fetch full details from PostgreSQL (or cache)

**Elasticsearch index mapping example:**

```json
{
  "mappings": {
    "properties": {
      "poi_id": { "type": "keyword" },
      "name": { "type": "text" },
      "category": { "type": "keyword" },
      "location": { "type": "geo_point" },
      "rating": { "type": "float" },
      "review_count": { "type": "integer" },
      "tags": { "type": "keyword" },
      "open_now": { "type": "boolean" }
    }
  }
}
```

**Query example (text + geo):**

```json
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "pizza" } }
      ],
      "filter": [
        { "geo_distance": {
            "distance": "5km",
            "location": { "lat": 40.7, "lon": -74.0 }
          }},
        { "term": { "category": "restaurant" } }
      ]
    }
  },
  "sort": [
    { "_score": "desc" },
    { "rating": "desc" }
  ]
}
```

---

### E. Handling hot POIs and hotspots

**Problem:** Popular POIs (e.g., Statue of Liberty, top-rated restaurant) receive disproportionate traffic, creating hotspots.

**Options:**

1. **Read replicas**
   * Replicate POI DB with read replicas for hot shards
   * Pros: Simple, effective for read-heavy workload
   * Cons: Replication lag, doesn't solve write hotspots

2. **Aggressive caching**
   * Cache hot POI details at multiple layers (app-level, CDN)
   * Pros: Offloads reads from DB completely
   * Cons: Cache invalidation complexity

3. **Shard splitting**
   * Split hot geohashes into smaller shards or dedicated shards
   * Pros: Balances load
   * Cons: Operational complexity, resharding

4. **Denormalization / materialized views**
   * Pre-compute popular POI lists for common queries
   * Pros: Ultra-fast for common queries
   * Cons: Staleness, maintenance

**Trade-offs:**

* **Read replicas:** Easy to implement, scales reads, but doesn't help with write hotspots or cache misses.
* **Caching:** Most effective, but requires good invalidation strategy.
* **Shard splitting:** Effective but complex; use only if other methods insufficient.

**Preferred:**

* **Multi-tier caching + read replicas:**
  1. Cache hot POI details in Redis with long TTL (1 hour)
  2. Cache popular query results (e.g., "top restaurants in NYC") with medium TTL (10 min)
  3. Use CDN for photos and media
  4. Add read replicas for hot shards
* **Rate limiting:** Apply per-user rate limits to prevent abuse.
* **Monitoring:** Detect hotspots with metrics (query count per geohash/POI) and dynamically scale/cache.

---

### F. Reviews and rating aggregation — real-time vs batch

**Goal:** Update POI aggregate rating (avg, count) when new reviews are submitted.

**Options:**

1. **Synchronous update (transactional)**
   * On review insert, immediately update `AggregateRating` table in same transaction
   * Pros: Always consistent
   * Cons: Write contention on popular POIs, slower write path

2. **Asynchronous update (event-driven)**
   * On review insert, publish event to Kafka
   * Stream processor (Flink/Spark) consumes events and updates aggregates
   * Pros: Decouples write path, handles spikes
   * Cons: Eventual consistency (slight delay in rating updates)

3. **Batch update (periodic job)**
   * Nightly or hourly job recomputes aggregates
   * Pros: Simple, no real-time complexity
   * Cons: Stale ratings, poor UX

**Trade-offs:**

* **Synchronous:** Best for small-scale or critical accuracy, but doesn't scale for hot POIs.
* **Asynchronous:** Scales well, slight delay acceptable for most use cases.
* **Batch:** Too stale for user-facing systems.

**Preferred:**

* Use **asynchronous event-driven aggregation:**
  1. Review Service writes review to Review DB
  2. Publish `review-created` event to Kafka
  3. Rating Aggregator (Flink/Spark Streaming) consumes events and updates `AggregateRating` table
  4. Invalidate cache for affected POI
  5. Latency: typically <1 second (acceptable for UX)
* Use **eventual consistency** — display stale rating with disclaimer ("Rating updates within 1 minute") if necessary.

**Handling write contention:**

* Use **optimistic locking** or **atomic updates** for aggregate table:
  ```sql
  UPDATE aggregate_rating
  SET review_count = review_count + 1,
      total_rating = total_rating + :new_rating,
      average_rating = (total_rating + :new_rating) / (review_count + 1)
  WHERE poi_id = :poi_id;
  ```
* Or use **CRDT (Conflict-free Replicated Data Type)** for distributed counters.

---

### G. Handling updates from business owners

**Goal:** Allow verified business owners to update POI details (hours, photos, menu) without disrupting system.

**Options:**

1. **Direct updates (immediate)**
   * Owner updates → immediate write to POI DB
   * Pros: Instant changes
   * Cons: Risk of abuse, data quality issues

2. **Pending approval workflow**
   * Owner submits changes → moderation queue → admin approves → update
   * Pros: Quality control
   * Cons: Slow, manual effort

3. **Version-controlled updates**
   * Owner changes create new version; system compares and auto-approves low-risk changes (hours), flags high-risk (location) for review
   * Pros: Balance speed and quality
   * Cons: Complex logic

**Trade-offs:**

* **Direct updates:** Fast UX, but needs strict validation and abuse detection.
* **Approval workflow:** Safe but slow; frustrates owners.
* **Version control:** Best balance, requires smart auto-approval rules.

**Preferred:**

* Use **version-controlled updates with risk-based approval:**
  1. **Low-risk changes** (hours, photos, description) → auto-approve with audit log
  2. **Medium-risk** (phone, address) → auto-approve but flag for review
  3. **High-risk** (location change >100m, category change) → require manual approval
* Implement **rollback** capability in case of errors.
* Use **Kafka event log** for audit trail of all changes.

---

### H. Multi-region and global scale

**Goal:** Serve users worldwide with low latency.

**Options:**

1. **Single-region deployment**
   * All services in one region (e.g., us-east-1)
   * Pros: Simple
   * Cons: High latency for distant users

2. **Multi-region with geo-routing**
   * Deploy replicas in multiple regions (us, eu, asia)
   * Route users to nearest region via geo-DNS
   * Pros: Low latency globally
   * Cons: Data replication complexity, consistency challenges

3. **Sharded by geography**
   * Each region has its own DB with local POIs
   * Cross-region queries route to appropriate region
   * Pros: Scales well, simple replication
   * Cons: Complicated for travelers querying distant POIs

**Trade-offs:**

* **Single-region:** Acceptable for regional service (e.g., Yelp USA only).
* **Multi-region:** Required for global service (e.g., Google Maps, Uber).
* **Geo-sharded:** Good for strict data locality requirements (GDPR).

**Preferred:**

* Use **multi-region with geo-routing + selective replication:**
  1. Deploy API Gateway, Search Service, Cache in each region
  2. POI DB sharded by geohash (naturally geographic)
  3. Replicate popular POIs cross-region for travelers (e.g., tourist attractions)
  4. Route queries to region closest to query location (not user's current region)
  5. Reviews/ratings use **eventual consistency** via cross-region Kafka replication

**Consistency model:**

* **POI core data:** Strong consistency within region, eventual across regions (acceptable since POIs change infrequently)
* **Reviews/ratings:** Eventual consistency (acceptable delay <1 minute)

---

## 11) Reliability & edge cases

* **Stale POI data:** Business closes but POI still shown → implement user reports, automated freshness checks (call business to verify), periodic audits.
* **Boundary queries:** User near geohash edge may miss nearby POIs → query neighboring geohashes if results < threshold.
* **Duplicate POIs:** Same restaurant listed multiple times → deduplication service using fuzzy matching on name + location.
* **Malicious reviews:** Fake positive/negative reviews → ML-based spam detection, user reputation system, rate limiting.
* **Hot region failures:** Entire region outage → failover to neighboring region, serve cached results.
* **Race conditions in aggregation:** Concurrent review submissions → use optimistic locking or CRDT for aggregates.

---

## 12) Operational concerns & observability

* **Metrics:** QPS per endpoint, p95/p99 latency, cache hit rate, DB query latency, search relevance (click-through rate).
* **Tracing:** Distributed tracing (OpenTelemetry) for end-to-end request flow: API → Cache → DB → ES.
* **Alerts:** High error rate, latency spike, cache misses >50%, DB connection pool exhaustion, ES cluster health.
* **Dashboards:** Real-time map showing query heatmap, top POIs by traffic, shard load distribution.
* **Logging:** Structured logs for all write operations (reviews, POI updates) with audit trail.
* **Testing:** Load testing for peak QPS (lunch/dinner rush), chaos engineering for region failures.

---

## 13) Security & privacy

* **Authentication:** OAuth 2.0 / JWT for user authentication.
* **Authorization:** Role-based access control (RBAC) for business owners vs regular users.
* **Rate limiting:** Per-user and per-IP limits to prevent scraping and abuse.
* **Data privacy:** Anonymize user location history, comply with GDPR/CCPA.
* **PII protection:** Encrypt PII in database (names, emails), use data masking in logs.
* **Input validation:** Sanitize all user inputs (reviews, POI edits) to prevent XSS/SQL injection.
* **API security:** HTTPS only, API key authentication for business partners.

---

## 14) Cost optimization

* **Storage:** Use S3 Glacier for old photos/reviews, compress images, use CDN for media delivery.
* **Compute:** Auto-scale API servers based on traffic, use spot instances for non-critical batch jobs.
* **Database:** Use read replicas for hot shards, archive old reviews to cold storage.
* **Cache:** Right-size Redis cluster, use TTL to evict stale data, monitor hit rate to adjust cache size.
* **Elasticsearch:** Use tiered storage (hot/warm/cold) for time-series data, compact old indices.

---

## References

* **Geohash:** http://geohash.org/
* **S2 Geometry:** https://s2geometry.io/
* **Elasticsearch Geo Queries:** https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-queries.html
* **Haversine Formula:** https://en.wikipedia.org/wiki/Haversine_formula
* **Yelp Engineering Blog:** https://engineeringblog.yelp.com/
* **Uber Engineering:** https://eng.uber.com/ (H3 geospatial indexing)
* **System Design Interview Volume 2** (Chapter: Nearby Friends / Proximity Service)

---

**Interview Tips:**

1. **Start with clarifications:** Nail down scale, read/write ratio, consistency requirements.
2. **Draw simple design first:** Single DB with geospatial index → show you understand basics.
3. **Scale iteratively:** Add sharding, caching, Elasticsearch, replication as you discuss bottlenecks.
4. **Deep dive on geospatial:** Be ready to explain geohash, boundary cases, distance calculations.
5. **Discuss trade-offs:** Always present multiple options (geohash vs quadtree vs S2) and justify your choice.
6. **Mention real-world systems:** Reference Yelp, Google Maps, Uber to show awareness of production patterns.
7. **Edge cases matter:** Boundary queries, duplicate POIs, stale data, hot POIs.

---

## Appendix A: How Geohash Works (Deep Dive)

### The Core Idea

**Goal:** Convert 2D coordinates (latitude, longitude) into a **1D string** that preserves spatial locality.

**Key Property:** Nearby locations share common **prefixes**.

```
Joe's Pizza:      dr5ru6
Mario's Pizza:    dr5ru7  ← Same prefix "dr5ru" = nearby!
Tokyo Restaurant: xn774c  ← Different prefix = far away
```

---

### Step-by-Step Encoding Algorithm

Let's encode **latitude = 40.7128°, longitude = -74.0060°** (New York City).

#### **Step 1: Normalize Ranges**

```
Longitude range: [-180°, 180°]
Latitude range:  [-90°, 90°]

Our point: (-74.0060, 40.7128)
```

#### **Step 2: Binary Encoding via Bisection**

**Process: Alternate between longitude and latitude, bisecting range each time**

##### **Longitude (-74.0060) encoding:**

```
Iteration 1:
├─ Range: [-180, 180], midpoint = 0
├─ -74.0060 < 0 → LEFT half
├─ Bit: 0
└─ New range: [-180, 0]

Iteration 2:
├─ Range: [-180, 0], midpoint = -90
├─ -74.0060 > -90 → RIGHT half
├─ Bit: 1
└─ New range: [-90, 0]

Iteration 3:
├─ Range: [-90, 0], midpoint = -45
├─ -74.0060 < -45 → LEFT half
├─ Bit: 0
└─ New range: [-90, -45]

Iteration 4:
├─ Range: [-90, -45], midpoint = -67.5
├─ -74.0060 < -67.5 → LEFT half
├─ Bit: 0
└─ New range: [-90, -67.5]

... continue for desired precision
```

**Longitude bits: 01001...**

##### **Latitude (40.7128) encoding:**

```
Iteration 1:
├─ Range: [-90, 90], midpoint = 0
├─ 40.7128 > 0 → RIGHT half
├─ Bit: 1
└─ New range: [0, 90]

Iteration 2:
├─ Range: [0, 90], midpoint = 45
├─ 40.7128 < 45 → LEFT half
├─ Bit: 0
└─ New range: [0, 45]

Iteration 3:
├─ Range: [0, 45], midpoint = 22.5
├─ 40.7128 > 22.5 → RIGHT half
├─ Bit: 1
└─ New range: [22.5, 45]

Iteration 4:
├─ Range: [22.5, 45], midpoint = 33.75
├─ 40.7128 > 33.75 → RIGHT half
├─ Bit: 1
└─ New range: [33.75, 45]

... continue for desired precision
```

**Latitude bits: 1011...**

#### **Step 3: Interleave Bits (Longitude first)**

```
Longitude bits: 0 1 0 0 1 1 0 1 0 1 ...
Latitude bits:   1 0 1 1 1 0 1 0 1 ...

Interleaved:    01 10 01 01 11 10 01 10 01 ...
```

Group into 5-bit chunks (for base32):

```
Interleaved:    01100 11110 01101 001...
                └───┘ └───┘ └───┘
                  12    30    13
```

#### **Step 4: Convert to Base32**

**Base32 alphabet:** `0123456789bcdefghjkmnpqrstuvwxyz` (32 chars, excludes a, i, l, o)

```
Binary → Decimal → Base32:
01100 → 12 → 'd'
11110 → 30 → 'r'
01101 → 13 → '5'
10010 → 18 → 'r'
11010 → 26 → 'u'

Final geohash: "dr5ru" (5 characters ≈ 5km precision)
Full 8-char: "dr5ru6f2"
```

---

### Visual Representation: Progressive Subdivision

Each character subdivides the region into 32 sub-regions:

```
1 character (5 bits):
┌─────────────────────────────────┐
│                                 │
│      32 regions worldwide       │
│                                 │
└─────────────────────────────────┘

2 characters (10 bits):
┌───┬───┬───┬───┬───┬───┬───┬───┐
│   │   │   │   │   │   │   │   │
│     1,024 regions (32²)         │
└─────────────────────────────────┘

'dr5ru' breakdown:
'd'  → Northeast USA region
'dr' → New York state area
'dr5' → NYC metro (≈156km × 156km)
'dr5r' → Manhattan area (≈39km × 20km)
'dr5ru' → Midtown Manhattan (≈5km × 5km)
```

---

### Precision Levels

| Length | Cell Width | Cell Height | Example Use |
|--------|-----------|-------------|-------------|
| 1 | ±2,500 km | ±2,500 km | Continent |
| 2 | ±630 km | ±630 km | Country |
| 3 | ±78 km | ±156 km | Large city |
| 4 | ±20 km | ±20 km | City district |
| 5 | ±2.4 km | ±4.9 km | Neighborhood |
| 6 | ±0.61 km | ±0.61 km | Village/Street |
| 7 | ±76 m | ±153 m | Building block |
| 8 | ±19 m | ±19 m | Individual building |
| 9 | ±2.4 m | ±4.8 m | Room |
| 10 | ±60 cm | ±60 cm | Precise location |

**Note:** Cells are rectangular (not square) because lat/lng cover different ranges.

---

### Why Geohash is Powerful

#### 1. **Prefix Matching = Proximity**

```sql
-- Find all POIs in neighborhood "dr5ru"
SELECT * FROM poi WHERE geohash LIKE 'dr5ru%';

-- Simple B-tree index lookup! Very fast.
CREATE INDEX idx_geohash ON poi(geohash);
```

#### 2. **Range Queries**

```sql
-- All POIs in geohash range dr5ru0 to dr5ruz
SELECT * FROM poi 
WHERE geohash >= 'dr5ru0' AND geohash <= 'dr5ruz';
```

#### 3. **Easy Sharding**

```python
def get_shard(geohash):
    # Shard by first 2 characters
    return f"shard_{geohash[:2]}"

get_shard("dr5ru6")  # → "shard_dr" (New York)
get_shard("9q8yy0")  # → "shard_9q" (San Francisco)
```

---

### The Boundary Problem

**Issue:** Nearby locations can have different prefixes at cell boundaries.

```
Cell boundaries:

    dr5rv | dr5rw     ← Adjacent cells
    ------+------
    dr5rt | dr5ru     ← Current location
```

**Example:**
- Location A: `dr5ru9` (eastern edge of cell)
- Location B: `dr5rv0` (just across boundary, 100m away)
- **They don't share 5-character prefix!**

**Solution:** Query neighboring geohashes.

```python
def nearby_search(lat, lng, radius_km):
    geohash = encode(lat, lng, precision=6)
    
    # Get 9 cells: center + 8 neighbors
    cells = [geohash] + get_8_neighbors(geohash)
    
    # Query all cells
    results = []
    for cell in cells:
        results += db.query(f"geohash LIKE '{cell}%'")
    
    # Filter by actual distance (Haversine formula)
    filtered = [r for r in results 
                if haversine_distance(r.lat, r.lng, lat, lng) <= radius_km]
    
    return sorted(filtered, key=lambda r: distance(r, lat, lng))

def get_8_neighbors(geohash):
    """Returns N, NE, E, SE, S, SW, W, NW neighbors"""
    return [
        neighbor(geohash, 'north'),
        neighbor(geohash, 'northeast'),
        neighbor(geohash, 'east'),
        neighbor(geohash, 'southeast'),
        neighbor(geohash, 'south'),
        neighbor(geohash, 'southwest'),
        neighbor(geohash, 'west'),
        neighbor(geohash, 'northwest')
    ]
```

---

### Code Example: Encoding Geohash

```python
BASE32 = '0123456789bcdefghjkmnpqrstuvwxyz'

def encode(lat, lng, precision=8):
    """Encode latitude/longitude to geohash"""
    lat_range = [-90.0, 90.0]
    lng_range = [-180.0, 180.0]
    
    bits = []
    even = True  # Start with longitude
    
    # Generate precision * 5 bits (each base32 char = 5 bits)
    for _ in range(precision * 5):
        if even:  # Longitude bit
            mid = (lng_range[0] + lng_range[1]) / 2
            if lng > mid:
                bits.append(1)
                lng_range[0] = mid
            else:
                bits.append(0)
                lng_range[1] = mid
        else:  # Latitude bit
            mid = (lat_range[0] + lat_range[1]) / 2
            if lat > mid:
                bits.append(1)
                lat_range[0] = mid
            else:
                bits.append(0)
                lat_range[1] = mid
        even = not even
    
    # Convert bits to base32
    geohash = ''
    for i in range(0, len(bits), 5):
        chunk = bits[i:i+5]
        idx = sum(bit << (4-j) for j, bit in enumerate(chunk))
        geohash += BASE32[idx]
    
    return geohash

# Test
print(encode(40.7128, -74.0060, precision=6))  
# Output: "dr5ru6"
```

---

### Code Example: Decoding Geohash

```python
def decode(geohash):
    """Decode geohash to latitude/longitude"""
    lat_range = [-90.0, 90.0]
    lng_range = [-180.0, 180.0]
    
    even = True
    for char in geohash:
        idx = BASE32.index(char)
        # Convert to 5 bits
        bits = [(idx >> i) & 1 for i in range(4, -1, -1)]
        
        for bit in bits:
            if even:  # Longitude
                mid = (lng_range[0] + lng_range[1]) / 2
                if bit == 1:
                    lng_range[0] = mid
                else:
                    lng_range[1] = mid
            else:  # Latitude
                mid = (lat_range[0] + lat_range[1]) / 2
                if bit == 1:
                    lat_range[0] = mid
                else:
                    lat_range[1] = mid
            even = not even
    
    # Return center of final range
    lat = (lat_range[0] + lat_range[1]) / 2
    lng = (lng_range[0] + lng_range[1]) / 2
    return lat, lng

# Test
print(decode("dr5ru6"))  
# Output: (40.7128, -74.0060) approximately
```

---

### Production Usage Pattern

#### Database Schema

```sql
CREATE TABLE poi (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    geohash_4 CHAR(4),  -- ~20km precision (city level)
    geohash_6 CHAR(6),  -- ~600m precision (neighborhood)
    geohash_8 CHAR(8)   -- ~19m precision (building)
);

-- Multi-level indexes for different query ranges
CREATE INDEX idx_geohash_4 ON poi(geohash_4);
CREATE INDEX idx_geohash_6 ON poi(geohash_6);
CREATE INDEX idx_geohash_8 ON poi(geohash_8);
```

#### Smart Query Logic

```python
def search_nearby(lat, lng, radius_km):
    # Choose precision based on search radius
    if radius_km <= 0.5:
        precision = 8  # Fine-grained
    elif radius_km <= 5:
        precision = 6  # Medium
    else:
        precision = 4  # Coarse
    
    # Get geohash for query center
    center_hash = encode(lat, lng, precision)
    
    # Get center + 8 neighbors
    cells = [center_hash] + get_8_neighbors(center_hash)
    
    # Query database
    query = f"""
        SELECT * FROM poi
        WHERE geohash_{precision} IN ({','.join(['?']*len(cells))})
    """
    candidates = db.execute(query, cells)
    
    # Filter by actual distance
    results = []
    for poi in candidates:
        dist = haversine_distance(lat, lng, poi.lat, poi.lng)
        if dist <= radius_km:
            results.append((poi, dist))
    
    # Sort by distance
    return sorted(results, key=lambda x: x[1])
```

---

### Summary

**Geohash works by:**
1. **Bisecting** ranges recursively (binary search in 2D)
2. **Interleaving** longitude and latitude bits
3. **Encoding** as base32 string

**Key benefits:**
- ✅ Prefix matching = proximity
- ✅ Simple B-tree indexing
- ✅ Easy sharding
- ✅ Works with any database

**Key limitation:**
- ⚠️ Boundary discontinuities (solve by querying neighbors)

**Best for:** Distributed POI systems, simple implementation, database-agnostic solution.

---


