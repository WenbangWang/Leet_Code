# CamelCamelCamel System Design Summary

---

## Clarification Questions (Ordered by FR → NFR)

**Functional Requirements:**

1. Which e-commerce platforms should be supported besides Amazon? (e.g., eBay, Walmart, AliExpress)
2. What notification channels are required? (email, push, SMS)
3. Should users be able to create multiple watches per product?
4. Are price history charts required for all platforms or only selected ones?
5. Should affiliate tracking be platform-specific or unified?

**Non-Functional Requirements:**

1. What is the expected number of products per platform?
2. What is the target latency for price update notifications?
3. Are there storage constraints for time-series price data?
4. How frequently do we expect users to check the product pages (polling rate)?
5. Are there platform-specific API rate limits or scraping restrictions to consider?
6. Is high availability required for all components, or only for the notification system?

---

## 1️⃣ Functional Requirements (FR)

1. Track historical prices for multiple e-commerce platforms (Amazon, eBay, etc.).
2. Users can create “watches” with target price / % drop.
3. Notify users via email, push, or other channels when price thresholds are met.
4. Browser extension shows price history and allows watch creation.
5. Product search and discovery.
6. Integrate affiliate program to track clicks and purchases, providing affiliate revenue.

---

## 2️⃣ Non-Functional Requirements (NFR)

1. **Scalability:** Millions of products and watchers across multiple platforms.
2. **Freshness:** Near real-time notifications, especially for hot products.
3. **Reliability:** Durable storage of price history and alerts.
4. **Performance:** Low-latency reads for UI/extension; fast chart queries.
5. **Cost-efficient:** Optimized scraping/API calls and storage.
6. **Resilience & Observability:** Handle failures, retries, monitoring.
7. **Affiliate Tracking:** Accurate click and conversion tracking without affecting core performance.

---

## 3️⃣ Back-of-Envelope Calculation

* Products: 10M per platform, 3 platforms → 30M products.
* Checks/day: 6 per product → 180M samples/day.
* Storage/day: 180M × 100B ≈ 18 GB → \~6.5 TB/year.
* Active watches: 1M users × 10 watches = 10M watches.
* Alerts/day: \~10% trigger → \~1M alerts/day.
* Affiliate clicks/day: estimate 5% of users click → 50K clicks/day.

---

## 4️⃣ Core Data Entities

| Entity         | Fields                                                                          | Notes                       |
| -------------- | ------------------------------------------------------------------------------- | --------------------------- |
| Product        | product\_id (platform + id), title, url, category, last\_price, affiliate\_link | Metadata + affiliate URL    |
| PriceSample    | product\_id, timestamp, price, currency, availability, source                   | Time-series data            |
| Watch          | watch\_id, user\_id/email, product\_id, target\_price, last\_alert              | Subscription rules          |
| Alert          | alert\_id, watch\_id, product\_id, price\_sample\_id, sent\_at, status          | Alert history               |
| User           | user\_id, email, preferences                                                    | Optional                    |
| CrawlJob       | crawl\_id, product\_id, platform, start\_ts, end\_ts, status                    | Track scraping per platform |
| AffiliateEvent | event\_id, user\_id, product\_id, timestamp, click\_type, conversion            | Track clicks & conversions  |

---

## 5️⃣ Simple Design — Satisfy FR

### Architecture Diagram

```
================ SIMPLE DESIGN =================

[Scheduler / Cron] 
        |
   enqueue(product_id + platform)
        |
 [Scraper Workers]
(GET product page → parse price)
        |
Insert PriceSample & Update Products.last_price
        |
  [Notifier Job] --> Email/Push
(scan watches table)
        |
      [Web/API]
(GET /product/{id}/history, POST /watch, affiliate clicks)

Interfaces:
- Product Fetch: internal GET (scraper)
- Watch Creation: POST /watch
- Price History: GET /product/{id}/history
- Notification: internal job → SMTP/push
- Affiliate Click Tracking: POST /affiliate_click

Pros: simple, fast to implement
Cons: DB bottleneck, polling latency, hot product hotspots, brittle scraping
================================================
```

### Workflow

1. Scheduler enqueues products to scrape for each platform.
2. Scraper fetches → parses → inserts sample → updates product last price.
3. Notifier scans watches → sends notifications.
4. API/extension reads price + history.
5. Affiliate clicks logged via API and stored in AffiliateEvent table.

---

## 6️⃣ Enriched Design — Satisfy NFR

### Architecture Diagram

```
================ ENRICHED DESIGN =================

[Product Index + Scheduler]
        |
    Job Queue (Kafka/RMQ)
        |
  [Scraper Fleet] --> Raw HTML S3
(GET product page → raw HTML, multi-platform)
        |
   Parser & Normalizer
        |
   PriceChangeEvent Stream
        |
 ┌──────────────┐
 |              |
TSDB (hot)   Latest KV (Redis/Dynamo)
        |
Stateful Watch Evaluator
(partitioned by product_id + platform)
        |
     Alert Queue
        |
 Notifier Workers
(email/push, retries, batching)
        |
      Users
        |
   Web/API/Extension
(GET latest_price, history, POST /watch, POST /affiliate_click)
        |
 Affiliate Analytics & Dashboard
```

### Workflow

1. Scheduler → Job Queue (multi-platform).
2. Scraper → raw HTML → ParsedPriceEvent.
3. Stream processor → deduplicate & normalize → PriceSample → update Latest KV.
4. PriceChangeEvent → Watch Evaluator → AlertEvent → Alert Queue.
5. Notifier → deliver notifications.
6. API/extension → reads Latest KV + TSDB / cached chart tiles.
7. Affiliate clicks → stored in AffiliateEvent → analytics/dashboard.

### Pros

* Horizontal scaling + real-time alerts.
* Multi-platform support.
* Efficient chart queries & historical analytics.
* Affiliate revenue tracking integrated.
* Debuggable & resilient: DLQs, raw HTML, monitoring.

### Cons

* More infra complexity.
* Higher operational cost.
* Requires partitioning & hot-key mitigation.

---

## 7️⃣ Expanded Deep Dives

### 7.1 Watch Evaluation at Scale

* Partition by `product_id + platform` → local stream state.
* Stateful streaming (Flink/Kafka Streams).
* Deduplication & hot product mitigation: batch alerts, shard hot products.
* Exactly-once semantics: checkpointing + idempotent notifier.
* Failure Handling: Stream checkpoint recovery + DLQ.

### 7.2 Crawler Scaling & Anti-blocking

* Stateless scraper workers (serverless/containerized).
* Proxy pool + token bucket per IP.
* Adaptive polling scheduler: prioritize high-watch/high-volatility products.
* Raw HTML storage for debugging & re-parsing.
* DLQ for failed jobs, CAPTCHA/failure detection.

### 7.3 Time-Series Storage & Query Optimization

* Hot TSDB (partitioned by product\_id + platform + date).
* Cold aggregated storage (daily/weekly rollups in Parquet).
* Latest KV store (Redis/Dynamo) for low-latency extension reads.
* Precompute chart tiles & aggregates for caching.

### 7.4 Notification Delivery at Scale

* Event-driven Alert Queue.
* Notifier workers: idempotent, retry, multi-channel (email/push).
* Optional batching.
* DLQ for failed notifications + monitoring metrics.

### 7.5 Affiliate Program Integration

* Track user clicks via API/extension → AffiliateEvent table.
* Monitor conversions (purchases) → associate with product\_id & platform.
* Analytics dashboard for affiliate revenue, top products, click-through rates.
* Ensure high performance: async logging, batching, partitioned storage.
* Deduplicate clicks/conversions for accurate revenue reporting.

---

## 8️⃣ Deep-Dive Diagram

```
================ DEEP DIVE =================

       ┌───────────────┐
       │ Scheduler / ML│
       └───────┬───────┘
               |
          Job Queue
               |
     ┌─────────┴─────────┐
     │                   │
 [Scraper Workers]   [Crawler Monitor]
- Proxy rotation    - Track proxy/IP health
- Token buckets     - CAPTCHA/failure detection
- Fetch page        - Retry / DLQ
- Emit Raw HTML → S3
               |
         Parser & Normalizer
               |
        PriceChangeEvent Stream
        /                     \
   TSDB (hot)                Latest KV
   - partition product_id + platform      - latest price
   - hot vs cold storage
   - precompute aggregates
               |
       Stateful Watch Evaluator
       - Partition by product_id + platform
       - Hot-key mitigation
       - Deduplication
               |
          Alert Queue
               |
      Notifier Workers
      - email/push, retries, batching
      - DLQ / monitoring
               |
       Affiliate Tracking
      - API logs clicks
      - Conversion tracking
      - Analytics dashboard
               |
             Users
        Web/API/Extension
- GET latest_price
- GET history/chart
- POST/DELETE watch
- POST affiliate click
```

**Narration Tips:**

1. Start from Scheduler → Job Queue → Scraper → Parser → Stream.
2. TSDB + Latest KV for storage + fast reads.
3. Watch Evaluator: partitioning, hot-key mitigation, dedupe.
4. Notifier: idempotent, retries, DLQ.
5. Affiliate Tracking: clicks → events → dashboard.
6. End with Web/API/Extension interfaces.
7. Emphasize resilience, scalability, and exactly-once semantics.
