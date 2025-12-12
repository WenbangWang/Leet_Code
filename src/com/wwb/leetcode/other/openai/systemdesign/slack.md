# 1) Clarifying questions (ordered FR → NFR)

(Ask these at interview start — answer them based on your assumptions if interviewer doesn't specify.)

Functional (FR)

1. Target scope: only messaging (channels + DMs + threads) or also file sharing, reactions, message edits/deletes, presence, search, unified inbox, apps/bots?
2. User scale: how many MAUs/DAUs and peak concurrent connections? (e.g., 1M DAU with 100k concurrent vs. 100M DAU)
3. Channel size limits: max members per channel? (e.g., small teams vs. massive org-wide channels with 100k members)
4. Retention & persistence: should messages be stored indefinitely (server-side), or ephemeral like WhatsApp? (affects storage and compliance). ([Hello Interview][2])
5. Multi-device: must support multi-device sync and read state across devices?
6. Delivery semantics: at-least-once, at-most-once, or exactly-once? (Slack-like often accepts at-least-once with dedupe). ([Coding Monkey][1])

Non-Functional (NFR)

1. Latency target for real-time delivery (e.g., <200ms for active channel messages).
2. Availability/SLA (e.g., 99.95% vs. 99.999%).
3. Durability / compliance / eDiscovery requirements (legal holds, export).
4. Consistency model: causal consistency within a channel vs. strong global ordering. (Slack prioritizes causal consistency). ([Coding Monkey][1])
5. Throughput: peak QPS for sends/reads and writes to storage.
6. Cost constraints: budget for storage and real-time infra (impacts choice of fan-out vs. pub/sub).

---

# 2) Functional requirements (FR) — succinct

* Create/join/leave channels (public/private)
* Send and receive messages in channels and DMs
* Threads (replies to a message)
* Offline delivery & retrieval (messages sent while recipient offline)
* Presence (online/away) and typing indicators (optional)
* Message metadata: edits, deletes, reactions, attachments (files)
* Multi-device sync and read receipts

# 3) Non-functional requirements (NFR) — succinct

* Low latency (real-time feel)
* High availability and horizontal scalability
* Causal consistency shown to users (message order in channel)
* Durable storage for messages (configurable retention)
* At-least-once delivery with idempotence/dedup for senders/receivers
* Cost-efficient storage and fan-out behavior for large channels

### Specific SLOs:

* **Message delivery latency**: p95 < 200ms for active users, p99 < 500ms
* **Availability**: 99.95% uptime (22 min downtime/month allowed)
* **Websocket health**: connection success rate > 99.9%, reconnection time p95 < 3s
* **Message loss rate**: < 0.01% (with retry and inbox fallback mechanisms)
* **History fetch latency**: p95 < 500ms for last 100 messages per channel
* **Write throughput**: sustain 6k writes/sec with burst capacity to 10k writes/sec
* **Storage SLO**: message write durability 99.999999999% (11 nines)

---

# 4) Back-of-the-envelope (BoTE) calculation (example assumptions)

Assume: 10M users, 1M DAU, 100k concurrent connections, average message size 1 KB, average messages per DAU = 50/day.

* Messages/day = 1M * 50 = 50M messages/day ≈ 578 msgs/sec (50M / 86400 ≈ 579) — this is modest; if hot usage is during work hours, peak QPS could be 5–10× => ~3–6k QPS.
* Storage: 50M messages/day * 1 KB ≈ 50 GB/day raw. With metadata and indexing, say 150 GB/day. 1 year ≈ 55 TB. (Choose retention policy.)
* Concurrent websockets: 100k. Each connection needs memory and a heartbeat; plan for servers sized at ~10k ws each → ~10 websocket servers. (Numbers scale with per-connection memory.)
  These numbers guide DB sharding, message queue throughput, and fan-out capacity.

---

# 5) Core data entities (DB schema primitives)

(Keep it simple — RDB for metadata, scalable store for messages.)

1. **User**

    * `user_id, username, display_name, hashed_creds, last_active, devices[]`

2. **Workspace / Org**

    * `org_id, name, plan, org_admins[]`

3. **Channel**

    * `channel_id, org_id, name, is_private, topic, member_count`

4. **ChannelMembership**

    * `channel_id, user_id, role, join_time` (used to compute who to deliver to)

5. **Message**

    * `message_id (UUID), channel_id, author_id, parent_message_id (nullable), content (blob), attachments[], created_at, edited_at, tombstone_flag`
    * Indexed by `(channel_id, created_at)` and `message_id`

6. **MessageDeliveryState / PerUserInboxIndex** (optional for fan-out-to-inbox design)

    * `user_id, channel_id, message_id, delivered_at, read_at, seq_no` — used when fan-out is per-user.

7. **DeviceConnection** (in-memory K/V + persisted heartbeat)

    * `user_id -> list of (websocket_server_id, connection_id, last_heartbeat)`

8. **Dedup / Idempotency Store**

    * `client_message_id -> message_id` for de-dup on retries.

---

# 6) System interfaces / API surface

(External/internal APIs)

Client-facing:

* `POST /send_message` {channel_id, content, client_msg_id, parent_message_id?} → ack (message_id, timestamp)
* `WS /connect` → persistent websocket for events (message, typing, presence)
* `GET /messages?channel_id&since_ts&page_size` → fetch history / offline catch-up
* `POST /ack` {message_id, device_id} → read/delivered receipts
* `POST /edit_message` / `POST /delete_message` / `POST /react`

Internal services:

* Websocket server: manage client conns and subscriptions
* API server / Write server: validate and persist incoming messages to Message DB then push into dispatcher/pubsub
* Channel Dispatcher (stateful) OR Pub/Sub backbone
* Message Storage service (read/write) — could be partitioned by channel_id
* Presence & Device Registry (in-memory KV) — e.g., Redis cluster
* Notification & Push worker (for offline mobile push via APNs/FCM)

---

# 7) Start from a simple design (ASCII diagram + flow)

Simple assumption: modest scale, use dispatcher pattern (recommended for Slack-style). Dispatcher holds subscription mapping for channels to websocket servers. Use WebSocket for real-time delivery, HTTP write to write-servers for reliability.

ASCII — Simple dispatcher design:

```
 +---------+        +------------+       +-------------+
 | ClientA |<-----> | Websocket  | <---> | DeviceReg   |
 | (ws)    |        | Server W1  |       | (Redis)     |
 +---------+        +------------+       +-------------+
     |                   ^  ^
     |                   |  | subscription updates
     |                   |  | (channel -> websocket server id)
     v                   |  |
 +---------+     HTTP    |  |             +-------------+
 | ClientB |----POST---->|  |             | ChannelSrv  |
 | (http)  |             |  |             | (Dispatcher)|
 +---------+             |  |             +-------------+
                         |  |                 |
                         |  | broadcast msg   |
                         |  +-----------------+ (fan-out to subscribed WS servers)
                         |
                +------------------+
                | Write/API Server  |
                | (persist message) |
                +------------------+
                         |
                     Message DB
```

Flow (send message):

1. Client sends HTTP POST `/send_message` (idempotent key).
2. API/Write server persists message to Message DB, returns `message_id`.
3. API forwards message to Channel Dispatcher (or publishes to topic).
4. Channel Dispatcher looks up which websocket servers subscribed to the channel and pushes messages to those Websocket Servers.
5. Websocket Servers forward to the connected clients.

Rationale: separates concerns (writes vs. live connections), easier scaling of write path and connection handling. This is the Dispatcher pattern suggested in the Slack notes. ([Coding Monkey][1])

---

# 8) Enrich the design (scale challenges & improved architecture) — ASCII

At scale we add:

* Sharded Channel Dispatchers (by channel_id range or consistent hashing)
* Durable Pub/Sub for fallback (Kafka/NSQ) to handle spikes & replay
* Per-user inbox (fan-out-to-inbox) for extremely large fan-outs (e.g., 100k+ members) to avoid hot loops
* Message partitions (by channel_id) in storage (Cassandra / DynamoDB / sharded MySQL)
* Read replicas and caching for recent history

```
          +--------------------+
          |      Client        |
          | (websocket/http)   |
          +--------------------+
                    |
                    | POST /send_message
                    v
          +--------------------+
          |   API / Write      |
          |   Server           |
          | (persist to DB)    |
          +--------------------+
                    |
                    | message persisted (message_id)
                    v
          +--------------------+
          |  Durable Queue     |
          | (Kafka / PubSub)  |
          +--------------------+
                    |
                    v
           +-------------------+
           | Channel Dispatcher|  <-- sharded by channel_id
           +-------------------+
             /                 \
  small channel? yes           no (large channel)
     /                         \
   v                             v
+----------------+         +-------------------+
| WS Servers     |         | Per-User Inbox    |
| (push live)    |         | (sharded by user) |
+----------------+         +-------------------+
       |                             |
       | push to connected users      | background consumer/fetch
       v                             v
+----------------+             +----------------+
| Client devices |             | Client devices |
| (online users) |             | (offline/active|
|                |             | users poll)    |
+----------------+             +----------------+

```

---

### **Flow Explanation**

1. **Write-first:**

    * Client sends message → API/Write Server persists message → returns `message_id`.
    * Ensures durability before dispatch.

2. **Durable Queue / PubSub:**

    * Message pushed to Kafka / PubSub for reliable delivery and replay.
    * Decouples dispatcher from write path.

3. **Channel Dispatcher (sharded):**

    * Maintains in-memory mapping: `channel_id → ws_server_ids`.
    * For **small channels**, push message to all connected websocket clients.

4. **Per-User Inbox (for large channels):**

    * Dispatcher inserts an inbox entry per user: `user_id, channel_id, message_id, seq_no, read_at`.
    * Clients poll or websocket servers pull updates using **per-channel cursor** (last delivered sequence number).
    * Ensures offline and multi-device delivery without flooding network.

5. **Client Delivery:**

    * **Online clients:** WS servers push messages (small channels + per-user inbox optional).
    * **Offline clients:** fetch from DB or per-user inbox by sequence cursor when reconnecting.

6. **Failure Handling:**

    * **DB write fails:** return error, client retries with same `client_msg_id`.
    * **Dispatcher crash:** replay messages from durable queue.
    * **WS server down:** message buffered in per-user inbox or retried later.
    * **Client disconnected:** message fetched later by cursor or inbox poll.

---

### **Sequence Cursor Concept**

* Each user tracks:

```
channel_id -> last_delivered_seq
```

* When fetching new messages:

```
SELECT * FROM PerUserInbox
WHERE user_id = X AND channel_id = Y AND seq_no > last_delivered_seq
ORDER BY seq_no ASC;
```

* Cursor updated after client receives messages → ensures **exactly-once delivery per device**.



# 9) Delivery/consistency/duplication handling

* **Idempotence:** require `client_msg_id` from client, store mapping to server `message_id` to dedupe retries.
* **At-least-once delivery:** implement ACKs from client (WS ack) and server retries until ack or TTL expiry. Dedup on client display using message_id. (Slack uses similar approach.) ([Coding Monkey][1])
* **Causal consistency:** ensure when user reconnects we fetch missing history from Message DB and show them before live WS stream to preserve causal order. Use `last_active_time` or vector of per-channel cursors to compute deltas. ([Coding Monkey][1])

---

# 10) Storage choices & rationale

* **Message storage (hot):** time-series or wide-column store (Cassandra / DynamoDB) partitioned by `channel_id` for high write throughput and horizontal scale. Good for append-heavy workloads.
* **Metadata (SQL):** Workspace, channels, membership in RDB (MySQL/Postgres) — consistent joins are easier. For very large systems, membership may be stored in a dedicated service or column store.
* **Websocket subscription & presence:** in-memory KV store (Redis cluster) for fast lookup. Persist critical membership to DB for recovery.
* **Per-user inbox / indices:** if using fan-out-to-inbox, use a fast KV store or queue system optimized for small writes per user (e.g., Redis Streams or Kafka compacted topics + small consumer offsets).
  Slack-style systems often prefer dispatcher + message DB (not per-user persistent inbox) except for large channels, where fan-out-to-inbox makes sense. ([Coding Monkey][1])

---

# 11) Possible deep dives (pick 3–5 in interview) — for each: options, trade-offs, preferred

### A — WebSocket vs HTTP POST for sending messages

Options:

1. Send via existing WebSocket connection (client -> WebSocket server)
2. Send via HTTP POST to stateless write servers (then those forward to dispatcher)

Trade-offs:

* WebSocket: lower latency (no extra RTT), simpler for mobile since single connection; harder to horizontally scale writes because WS servers hold state and might get overloaded. Retries/failed sends trickier.
* HTTP POST: easier to scale, reuse existing HTTP stack (load balancers, autoscaling), easier to apply idempotency & retries; slightly higher latency.

Preferred: **HTTP POST for write path + WebSocket for push notifications** (Slack reportedly uses similar pattern). This separates responsibilities and simplifies scaling. ([Coding Monkey][1])

---

### B — Fan-out strategies (Dispatcher vs Pub/Sub vs Fan-out-to-inbox)

Options:

1. **Dispatcher** (stateful servers keeping channel->ws-server subscription). Low latency, good for moderate-sized channels. Slack-style.
2. **Pub/Sub topics per channel** (Kafka/managed pubsub): durable, good for replay, but needs many topics if per-channel and careful topic scaling.
3. **Fan-out-to-inbox (per-user copy)**: write one message copy per user (inbox table/queue), simplifies offline delivery and per-user read state; expensive in storage and writes.

Trade-offs:

* **Dispatcher**: efficient for many medium channels; must handle subscription state and scale it (sharding).
* **Pub/Sub**: durable and scales well but delivery semantics change (consumer lag).
* **Per-user inbox**: great for huge fan-outs (very large channels) and offline, but increases write amplification.

Preferred: Hybrid — **Dispatcher + durable Pub/Sub + selective per-user inbox for giant channels**. Start with dispatcher; add Kafka for durability and replay; for very large channels (>X members) switch to inbox strategy. ([Coding Monkey][1])

---

### C — Message storage engine: RDB vs Cassandra/Dynamo vs Search index

Options:

* **RDB (MySQL/Postgres)**: strong consistency, simple queries, but scaling large append workloads is harder.
* **Cassandra / DynamoDB (NoSQL)**: excellent write throughput, horizontal scalability, good for time-series append pattern (`partition = channel_id`).
* **Search index (Elasticsearch)**: for full-text search and message search.

Trade-offs:

* RDB for metadata; NoSQL for messages at scale. Use ElasticSearch for search over message content (async indexing).

Preferred: **Cassandra/Dynamo style store for messages + RDB for metadata + ElasticSearch for search**.

---

### D — Handling very large channels (100k+ members)

Options:

1. Dispatcher push (inefficient — too many ops).
2. Fan-out-to-inbox (write per-user link/tombstone) and let users pull.
3. Use multicast/batching or specialized delivery (e.g., CDN-like large broadcast).

Trade-offs:

* Direct push is expensive and creates hotspots. Fan-out-to-inbox increases storage and write load. Batching reduces overhead but increases delivery latency.

Preferred: **Fan-out-to-inbox for huge channels**, or hybrid: limit live pushes to active subset and let others poll inbox. This optimizes for cost and avoids single-event amplification. ([Coding Monkey][1])

---

### E — Offline delivery & syncing across devices

Approaches:

1. `last_active_time` + query messages after that timestamp (simple).
2. Client-maintained vector of per-channel cursors/snapshots and server computes deltas (more accurate for multi-device).
3. Per-user inbox pointers (fast lookup of unread messages per user).

Trade-offs:

* `last_active_time` is simple but error-prone (requires clock sync).
* Per-channel cursors or per-user sequences are robust for multi-device sync.

Preferred: **Per-channel per-user sequence numbers (cursors)** plus message DB queries for deltas. Use the cursor approach for multi-device precise sync; fallback to timestamp when necessary. ([Coding Monkey][1])

---

# 12) Edge cases & failure scenarios

**1. Thundering herd on popular channel:**
* Problem: 100k users all subscribed, one message → 100k websocket writes simultaneously
* Solution: Use fan-out-to-inbox + rate-limited polling instead of direct push for channels >10k members

**2. Clock skew / time synchronization:**
* Problem: Multi-server deployments with clock drift → message ordering issues
* Solution: Use sequence numbers generated from centralized counter (per channel) rather than wall clock timestamps; include both seq_no and timestamp in messages

**3. Partial partition failure:**
* Problem: Dispatcher can reach Kafka but not some websocket servers
* Solution: Dispatcher retries with exponential backoff (1s, 2s, 4s); messages buffered in per-user inbox as fallback; alert on sustained failures

**4. User in 1000+ channels:**
* Problem: On connect, need to fetch last_read cursors for all channels → slow initial load
* Solution: Lazy load cursors (fetch only for active channels in last 7 days); paginate channel list; cache cursor state in Redis

**5. Message edit race condition:**
* Problem: User A edits message while user B is replying to original version → inconsistent state
* Solution: Include message_version in edit; clients show "edited" indicator; backend stores edit history; replies reference specific version

**6. Duplicate message on client retry:**
* Problem: Client sends message, timeout before receiving ack, retries with same client_msg_id
* Solution: Dedup store keyed by (client_msg_id, user_id) with 24h TTL; server returns existing message_id on duplicate

**7. Zombie websocket connections:**
* Problem: Client network fails but TCP connection not properly closed → server thinks user online, wastes resources
* Solution: Aggressive heartbeat timeout (3 missed pings = 90s total); client sends ping every 30s; server-initiated close on timeout

**8. Cascading failure from slow channel:**
* Problem: One channel has very high message rate → dispatcher queue backs up → affects all channels
* Solution: Per-channel rate limiting (100 msg/sec); isolated priority queues for hot channels; circuit breaker pattern (pause channel if queue depth >10k)

**9. Split brain / network partition:**
* Problem: Websocket servers partitioned from database but clients still connected → messages accepted but not persisted
* Solution: Health check includes DB connectivity; reject writes if DB unreachable; return 503 to trigger client retry; use distributed consensus (etcd/ZooKeeper) for cluster health

**10. Message reordering on multi-device:**
* Problem: User sends message from mobile, then desktop → desktop message arrives first due to network latency
* Solution: Use per-user sequence numbers; backend reorders before persistence; clients display optimistically but reorder on server confirmation

---

# 13) Monitoring & alerting

### Dashboard Hierarchy:

**Page 1 (Health Overview):**
* Messages sent/delivered per second (24h rolling average)
* Active websocket connections (current / 1h avg / 24h max)
* p50/p95/p99 message delivery latency
* Message loss rate (target < 0.01%, alert at 0.05%)

**Page 2 (Component Health):**
* Dispatcher queue depth per shard (alert at 10k, page at 50k)
* Kafka consumer lag (alert at 5k, page at 20k)
* Per-user inbox backlog (top 10 users, alert if any >5k)
* Database write throughput and replication lag (alert at 5s lag)

**Page 3 (Per-Channel Metrics):**
* Top 10 largest channels by member count
* Top 10 channels by message volume (msgs/hour)
* Channels using fan-out-to-inbox vs direct push (distribution)
* Failed message deliveries per channel (top 10 failures)

### Alerting Levels:

* **Page** (immediate response required):
  * Message loss > 0.1% over 5 min
  * All dispatchers down or >80% unhealthy
  * Database unavailable or write failures >10%
  * Websocket server cluster <30% healthy capacity

* **Urgent** (respond within 15 min):
  * p95 latency > 2x baseline (>400ms) for 10 min
  * Kafka consumer lag > 10k messages
  * >50% of websocket servers down
  * Dispatcher queue depth >50k for any shard

* **Warn** (investigate next business day):
  * Any user inbox backlog >1000 messages
  * Dispatcher queue depth >10k sustained for 1 hour
  * Slow queries detected (p95 >200ms for reads)
  * Disk usage >70% on any database node

* **Info** (visibility only):
  * Trending toward capacity limits (80% capacity forecast in 7 days)
  * New large channel created (>5k members)
  * Unusual spike in messages (3x baseline, not yet impacting latency)

---

# 14) Additional operational concerns

* **Sharding keys**: partition channels by `channel_id` hash; for DM (1:1) treat as its own channel type.
* **Hot partitions**: detect and split hot channels (re-hash or move to special fans-out-to-inbox pipeline).
* **Backpressure & retries**: decouple write path and fan-out via durable queue—prevents cascade failures.
* **Security / Auth**: token-based auth on websockets, per-channel ACL checks on writes/reads.
* **Rate limiting & abuse controls**: per-user and per-channel QPS limits.

---

# 15) Interview Presentation Guide (45-minute interview)

## Time Budget & Prioritization

### Phase 1: Clarifications (5 min)

**Must-ask questions:**
* Scope: messaging only (channels + DMs + threads) or also file sharing, reactions, search, bots?
* Scale: how many DAU? Peak concurrent connections? (e.g., 1M DAU with 100k concurrent vs 100M DAU)
* Channel sizes: max members per channel? (affects fan-out strategy)
* Delivery semantics: at-least-once or exactly-once?
* Multi-device: must support sync across devices with read state?

**Nice-to-have questions:**
* Retention policy: messages stored indefinitely or ephemeral?
* Latency target: <200ms for active messages?
* Consistency model: causal consistency within channel?

**Tip**: State assumptions clearly if interviewer doesn't specify. Example: "I'll assume 10M users, 1M DAU, 100k concurrent, at-least-once delivery with client dedup."

---

### Phase 2: Requirements & BoE (5 min)

**Must-cover:**
* **FR summary**: Create/join channels, send/receive messages, threads, offline delivery, presence, multi-device sync
* **NFR summary**: Low latency (<200ms p95), high availability (99.95%), causal consistency, durable storage, at-least-once delivery
* **BoE focus**: 
  * Message throughput: 1M DAU × 50 msgs/day = 50M msgs/day ≈ 578 msg/sec avg, peak ~3-6k msg/sec
  * Storage: 50M × 1KB = 50 GB/day raw ≈ 55 TB/year
  * Websocket servers: 100k concurrent ÷ 10k per server = ~10 servers

**Skip if time-constrained**: Detailed storage calculations (just mention order of magnitude)

---

### Phase 3: Simple Design (8 min)

**Must-draw**: Section 7 diagram (dispatcher pattern)

**Walk through message send flow:**
1. Client sends HTTP POST `/send_message` with idempotent key
2. API/Write server persists message to Message DB, returns message_id
3. API forwards to Channel Dispatcher (or publishes to topic)
4. Dispatcher looks up websocket servers subscribed to channel
5. Websocket servers push to connected clients

**Key points to emphasize:**
* Separation of concerns: write path (HTTP) vs live connections (WebSocket)
* Idempotency for safe retries
* Dispatcher holds subscription mapping (channel → ws_server_ids)

**Tip**: Draw on whiteboard/screen share, narrate as you go. Check: "Does this make sense so far?"

---

### Phase 4: Enriched Design (10 min)

**Must-add components**: (use Section 8 diagram as reference)
* Sharded Channel Dispatchers (by channel_id hash)
* Durable Pub/Sub (Kafka) for reliability and replay
* Per-user inbox for large channels (>10k members)
* Message storage partitions (by channel_id)
* Read replicas and caching for recent history

**Walk through scale improvements:**
* "For small channels (<1k members), dispatcher pushes directly to websocket servers"
* "For large channels (>10k members), we use fan-out-to-inbox to avoid hot loops"
* "Kafka decouples dispatcher from write path, enables replay on failures"

**Failure handling to mention:**
* DB write fails → return error, client retries with same client_msg_id
* Dispatcher crash → replay from Kafka
* WS server down → message buffered in inbox or retried
* Client disconnected → fetch later by cursor

**Tip**: Draw incrementally. Start with simple, then say "Now let's add for scale..." and overlay new components.

---

### Phase 5: Deep Dive (12 min) — Pick 2-3 based on interviewer interest

**Most likely to be asked:**

1. **Fan-out strategies (Section 11B — CRITICAL for Slack)**
   * Options: Dispatcher, Pub/Sub, Fan-out-to-inbox
   * Trade-offs: latency vs scale vs cost
   * When to use which: small channels (dispatcher), large channels (inbox)
   * Show hybrid approach decision logic

2. **Offline delivery & multi-device sync (Section 11E)**
   * Cursor-based approach: per-channel sequence numbers
   * Query: `SELECT * WHERE user_id=X AND channel_id=Y AND seq_no > last_delivered`
   * Multi-device: each device maintains own cursor, server merges read state
   * Reconnection flow: fetch missed messages before resuming live stream

3. **Large channel handling (Section 11D — >100k members)**
   * Why direct push fails: too many write ops, hotspots
   * Per-user inbox solution: write once per user, users pull
   * Optimization: push to active subset, others poll inbox
   * Cost analysis: write amplification vs network cost

**Also common:**

4. **Consistency & ordering (Section 9)**
   * Causal consistency: show messages in send order within channel
   * Use sequence numbers, not timestamps (avoids clock skew)
   * Reconnection: fetch history before live stream to preserve order

5. **Storage choices (Section 10)**
   * Messages: Cassandra/DynamoDB (time-series, high write throughput)
   * Metadata: MySQL/Postgres (consistent joins)
   * Presence: Redis (in-memory, fast)

6. **WebSocket vs HTTP for writes (Section 11A)**
   * Trade-off: latency vs scalability
   * Preferred: HTTP for writes, WS for push (Slack pattern)

**Strategy**: Go deep on 1-2 topics, show breadth on others. Example: "I can also discuss storage partitioning, consistency models, or websocket connection management if you're interested."

---

### Phase 6: Tradeoffs & Wrap-up (5 min)

**Key tradeoffs to discuss:**
* **At-least-once (chosen) vs Exactly-once**: Simpler, lower latency, rely on client dedup. Exactly-once requires distributed transactions (complex, slow).
* **Dispatcher (chosen) vs Pure Pub/Sub**: Dispatcher lower latency for small channels, easier subscription management. Pub/Sub better for very large scale but higher complexity.
* **Per-user inbox (for large channels only) vs Always direct push**: Inbox prevents fan-out explosions but increases storage and complexity. Use selectively.
* **HTTP write + WS push (chosen) vs WS for everything**: Separates concerns, easier to scale writes independently. WS-only couples write path to connection state.

**Mention alternative approaches briefly:**
* "For even larger scale (100M+ users), we could use geo-distributed dispatchers with regional failover"
* "For stronger ordering guarantees, we could use Raft consensus per channel (high cost)"

**Be ready for follow-up questions** (see Phase 6 common questions below)

---

### Phase 7: Q&A (5 min)

**Common follow-ups to prepare:**

* **"What if dispatcher crashes mid-delivery?"**
  * Answer: Messages are already persisted in DB. Kafka retains events for replay. On recovery, dispatcher replays from last checkpoint. Clients fetch missed messages via cursor on reconnect.

* **"How do you handle 100M users / 10M concurrent connections?"**
  * Answer: Shard dispatchers by channel_id hash. Use consistent hashing for even distribution. Each dispatcher handles subset of channels. Scale horizontally (100 dispatcher servers for 10M concurrent). Use Redis cluster for subscription state with partitioning.

* **"What if database becomes bottleneck?"**
  * Answer: Partition Message table by channel_id (hot query path). Use read replicas for history fetches. Cache recent messages (last 100 per channel) in Redis. Use write-optimized storage (Cassandra/DynamoDB with partition keys).

* **"How do you prevent one user/channel from impacting others?"**
  * Answer: Per-user rate limiting (100 msg/min). Per-channel rate limiting (1k msg/min). Isolated queues for hot channels. Circuit breaker pattern (pause channel if queue depth exceeds threshold). Multi-tenancy isolation at dispatcher level.

* **"What happens on network partition (split brain)?"**
  * Answer: Use distributed consensus (etcd/ZooKeeper) for cluster health. Websocket servers check DB connectivity before accepting writes. If partitioned, reject writes with 503 (triggers client retry). Use quorum writes for critical metadata.

* **"How do you test at scale?"**
  * Answer: Load testing with simulated clients (100k bots). Shadow traffic from production. Chaos engineering (randomly kill dispatchers, partition network). Gradual rollout with feature flags. Metrics and canary analysis.

---

## Sections to Skip/Abbreviate if Time-Constrained

**Skip entirely:**
* Detailed BoE derivations (mention key numbers only: "~600 msg/sec avg, ~6k peak, ~50 GB/day storage")
* Presence/typing indicators (mention briefly: "use Redis pub/sub for ephemeral state")
* Search indexing (mention: "ElasticSearch for full-text search, async indexing")
* Threads implementation details (unless specific question)
* File attachments (unless specific question: "upload to S3 via presigned URL, store metadata reference")

**Abbreviate:**
* Section 5 (Data entities): Show Message, Channel, ChannelMembership, skip detailed indexes unless performance question
* Section 6 (APIs): Mention key endpoints, skip exhaustive list
* Section 9 (Consistency): Mention causal consistency and at-least-once, skip deep dive unless asked
* Section 12 (Edge cases): Pick 2-3 most interesting (thundering herd, zombie connections, clock skew)

---

## Sections That Are MUST-COVER

1. ✅ **Section 1**: Clarifications (at least 4-5 key questions with assumptions)
2. ✅ **Section 2/3**: FR/NFR summary (concise, <2 min)
3. ✅ **Section 4**: BoE key numbers (throughput, storage, server count)
4. ✅ **Section 7**: Simple design diagram + message send flow
5. ✅ **Section 8**: Enriched design diagram + scale components
6. ✅ **Section 11B**: Fan-out strategies (dispatcher vs inbox) — CRITICAL
7. ✅ **Section 11E**: Offline delivery & cursors
8. ✅ **Tradeoffs**: At-least-once vs exactly-once, HTTP+WS vs WS-only

---

## Pro Tips for Interview Success

### Signaling & Pacing
* **Signal what you're doing**: "Let me start with clarifying questions, then I'll draw a simple design, then scale it up."
* **Check in with interviewer**: "Should I go deeper on fan-out strategies, or move to storage design?"
* **Manage time**: If 30 min in and haven't drawn enriched design yet, speed up. Say "Let me move to the scaled architecture."
* **Pause for questions**: After each phase, pause 2-3 seconds. Give interviewer chance to interject.

### Showing Depth
* **Show tradeoffs**: Don't just present one solution. Example: "For fan-out, we have three options: [list]. I prefer hybrid because [reason], but if we had [different constraint], I'd choose [alternative]."
* **Cite industry examples**: "Slack uses dispatcher pattern for this, Discord uses Pub/Sub, we'll follow Slack's approach because our channel sizes are similar."
* **Quantify decisions**: "Direct push works for <1k members, but at 10k+ members we'd have 10k writes per message which is unsustainable, so we switch to inbox."

### Handling Curveballs
* **If stuck**: "Let me think through this systematically..." then work through options out loud
* **If don't know**: "I'm not certain, but here's how I'd approach finding out..." or "In production, I'd benchmark these approaches..."
* **If running out of time**: "We have 10 min left, should I focus on [X] or would you like to dive into [Y]?"

### Common Mistakes to Avoid
* ❌ Jumping straight to complex design without simple version
* ❌ Not stating assumptions clearly upfront
* ❌ Over-designing for scale that wasn't asked for
* ❌ Ignoring failure scenarios and edge cases
* ❌ Not discussing tradeoffs (just presenting "the answer")
* ❌ Spending too long on BoE calculations (keep it quick)

---

## Interview Confidence Checklist

Before the interview, make sure you can:
- [ ] Draw simple dispatcher diagram from memory in <3 min
- [ ] Draw enriched design with Kafka + inbox in <5 min
- [ ] Explain fan-out strategies (dispatcher vs inbox) in <2 min
- [ ] Walk through cursor-based offline sync in <2 min
- [ ] Calculate rough BoE numbers for 1M DAU in <2 min
- [ ] Discuss 3 edge cases (thundering herd, clock skew, zombie connections)
- [ ] Explain at-least-once vs exactly-once tradeoff in <1 min
- [ ] Answer "what if 100M users?" (sharding, partitioning, horizontal scale)

