# Messenger System Design (WhatsApp-like)

> References:
>
> * [HelloInterview — Design WhatsApp](https://www.hellointerview.com/learn/system-design/problem-breakdowns/whatsapp)
> * [ByteByteGo — Design a Chat System](https://bytebytego.com/courses/system-design-interview/design-a-chat-system)

This design follows a structured system design interview approach for Meta-style interviews.

---

## 1) Clarification Questions (FR → NFR)

**Functional Requirements (FR)**

1. Support 1:1 and group chats (up to 100 participants).
2. Support media: images, audio, video, attachments.
3. Server must retain undelivered messages for offline users (e.g., 30 days).
4. End-to-end encryption (E2EE) required.
5. Clients support multiple devices per user (multi-device sync).
6. Delivery/read receipts, typing indicators, presence.

**Non-Functional Requirements (NFR)**

1. Low latency for online delivery (<500ms).
2. High scale: global, billions of messages/day.
3. Durability: messages reliably stored until delivered or retention expires.
4. Availability/SLA: 99.99% across regions.
5. Privacy & security: E2EE, data-in-transit encryption.
6. Cost-efficient storage for large media volumes.

---

## 2) Functional Requirements (FR)

* Create chats (1:1 and groups).
* Send/receive messages (text + media).
* Offline delivery with retention.
* Delivery & read receipts; message ordering.
* Attachment upload/download support.
* Multi-device message sync.

---

## 3) Non-Functional Requirements (NFR)

* Low latency (<500ms) online delivery.
* High throughput: \~25B messages/day.
* Durable storage and retries for offline devices.
* Resilient: survive component failures.
* Secure: E2EE & encryption in transit.
* Cost-efficient media storage.

---

## 4) Back-of-the-Envelope (BOTE) Calculations

* Users: 500M MAU; peak concurrent: 50M.
* Messages/day: 25B (avg 50/user).
* Peak RPS: \~578k messages/sec.
* Average message size: 1 KB (text only).
* Text storage/day: 25 TB; 30-day retention: 750 TB.
* Media offloaded to object storage (S3).
* Bandwidth must handle online delivery + fan-out for groups.

---

## 5) Core Data Entities

* **User**: `userId`, `displayName`, `phoneNumber`, `lastSeen`, `devices[]`
* **Device**: `deviceId`, `userId`, `connectionStatus`, `lastSync`
* **Chat**: `chatId`, `type`, `participants[]`, `createdAt`, `metadata`
* **MessageMeta**: `messageId`, `chatId`, `senderId`, `sentAt`, `seq`, `status`, `attachments[]`
* **MessageBody**: text or pointer to blob.
* **Attachment**: `attachmentId`, `blobLocation`, `size`, `hash`, `mimeType`
* **ConversationState**: per-user/device cursor for last seen/read.
* **Ack/Receipt**: delivery/read state per device.

---

## 6) System Interfaces (APIs & Socket Commands)

**WebSocket Commands:**

* `createChat {participants[], name?}` → `{chatId}`
* `sendMessage {chatId, messageBody, attachments[], clientMsgId}` → `{serverAck}`
* `messageAck {chatId, messageId, status: RECEIVED|DELIVERED|READ}`
* `syncState {lastKnownSeqPerChat[]}` — for multi-device sync
* `presenceUpdate {userId, status, lastSeen}`

**REST/HTTP:**

* `POST /attachments` — upload via signed URL.
* `GET /chats/{chatId}/history?since=` — fetch message history.
* `GET /users/{userId}/devices` — device management.

---

## 7) Simple Design (Single Node)

```
[Client A] ---ws---> [Chat Server] <----ws---- [Client B]
                         |
                         v
                    [Primary DB]
                         |
                         v
                    [Blob Store]
```

**Flow:** Client → Chat Server → DB + Blob Store → recipient(s). Offline messages stored until delivery.

---

## 8) Enriched Design (Scale & Production — Using Meta FOQ)

```
                         +-------------+
[Client A] --ws--> LB -- | Chat Gate   | --(internal rpc)--> Chat Workers  --> FOQ (Meta FOQ)
                        |  (L4/L7)    |                              | 
[Client B] --ws--> LB -- |  (sticky)   |                              v
                         +-------------+                       Delivery Svc
                                                                    |
                                                                    v
                                                          +-------------------+
                                                          |  DB (Cassandra/...)|
                                                          +-------------------+
                                                                    |
                                                                    v
                                                          Blob Store (S3)
```

**Components & Flow with FOQ:**

* **Chat Workers:** Receive messages, write to **FOQ** (durable, ordered, partitioned by `chatId`) and DB.
* **FOQ:** Durable queue, guarantees per-chat ordering; handles fan-out.
* **Delivery Service:** Reads FOQ, pushes to online devices, retries for offline, updates DB with delivery/read status.
* **DB:** Stores metadata & device cursors.
* **Blob Store:** Media files via pre-signed URLs.
* **Gateway:** Maintains WebSocket connections, routes messages.

---

## 9) Offline Device Delivery with FOQ

### Flow:

1. **Message Ingestion:**

    * Client → Gateway → Chat Worker → FOQ + DB.

2. **Online Devices:**

    * Delivery Service consumes FOQ → pushes via gateway → device acks → update device state.

3. **Offline Devices:**

    * Delivery Service sees device offline → message stays in FOQ until delivery.
    * Each device tracks `lastDeliveredSeq`.

4. **Device Reconnection / Sync:**

    * Device reconnects → queries `lastDeliveredSeq` → FOQ replay messages after that seq → device acks → update device state.

5. **Multi-device:**

    * Each device has independent cursor.
    * FOQ stores encrypted messages; safe for E2EE replay.
    * Messages expire after delivery to all devices or retention window.

**ASCII Diagram:**

```
      +------------------+           +----------------+
      |   FOQ Partition  |           | Device State   |
      |  (per chatId)    | <-------> | Table (seq)    |
      +------------------+           +----------------+
               ^
               |
         Delivery Service
               |
     +---------+---------+
     |                   |
 [Online Device]     [Offline Device]
   receives           waits for reconnect
    message
```

---

## 10) Deep Dives (Problem → Solutions → Preferred → Trade-offs)

### 10.1 Message Durability & Ordering

* **Problem:** Ensure messages are reliably delivered in order and never lost.
* **Solutions:**

    * Synchronous DB write before delivery (simple but increases latency).
    * Write to durable queue (FOQ) + async DB write (low latency, scalable).
* **Preferred:** FOQ write + Delivery Service consuming for fan-out.
* **Trade-offs:** Slight complexity vs. latency and scalability benefit; FOQ guarantees per-chat ordering.

### 10.2 Fan-out (Group Delivery)

* **Problem:** Efficiently deliver messages to large groups (N devices).
* **Solutions:**

    * Sender-side fan-out (synchronous) vs. queued fan-out via FOQ.
* **Preferred:** FOQ handles fan-out; Delivery Service pushes to online devices, queues offline.
* **Trade-offs:** Complexity of Delivery Service vs. avoiding spikes on Chat Workers; per-device ordering preserved.

### 10.3 Offline Delivery & Retries

* **Problem:** Devices offline during message send.
* **Solutions:**

    * Persist undelivered messages per device.
    * Retry via Delivery Service with exponential backoff.
* **Preferred:** FOQ stores messages; per-device `lastDeliveredSeq`; replay on reconnect.
* **Trade-offs:** Retention and storage cost vs. guaranteed delivery.

### 10.4 Multi-device Sync

* **Problem:** Messages must sync across multiple devices.
* **Solutions:**

    * Per-device cursors with FOQ replay.
    * Full chat history fetch.
* **Preferred:** FOQ replay from `lastDeliveredSeq` for efficiency.
* **Trade-offs:** Slightly more bookkeeping per device, but avoids redundant delivery.

### 10.5 End-to-End Encryption (E2EE)

* **Problem:** Ensure server cannot read message content.
* **Solutions:**

    * Server-managed keys (not E2EE) vs. client-side E2EE (Signal protocol).
* **Preferred:** Client-side E
