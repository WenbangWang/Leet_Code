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

# 12) Additional operational concerns

* **Sharding keys**: partition channels by `channel_id` hash; for DM (1:1) treat as its own channel type.
* **Hot partitions**: detect and split hot channels (re-hash or move to special fans-out-to-inbox pipeline).
* **Backpressure & retries**: decouple write path and fan-out via durable queue—prevents cascade failures.
* **Monitoring & alerting**: websocket connection counts, message QPS, dispatcher lag, Kafka consumer lag (if used).
* **Security / Auth**: token-based auth on websockets, per-channel ACL checks on writes/reads.
* **Rate limiting & abuse controls**: per-user and per-channel QPS limits.

