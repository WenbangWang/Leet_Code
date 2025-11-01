- https://www.hellointerview.com/learn/system-design/problem-breakdowns/dropbox
- https://www.geeksforgeeks.org/system-design/design-dropbox-a-system-design-interview-question/
- https://medium.com/@lazygeek78/system-design-of-dropbox-6edb397a0f67

# Clarification questions (ordered FR → NFR)

(You gave the problem scope via references; I’ll assume the following unless you tell me otherwise.)

1. Target scale: start small (10k users) and scale to hundreds of millions — design should be horizontally scalable.
2. File size limit: support up to **\~50 GB** per file (per references). ([Hello Interview][1])
3. Required features (FR): upload/download, share, sync across devices, list/changes feed. No inline editing / preview required. ([Hello Interview][1])
4. Priorities (NFR): high availability, low latency for uploads/downloads, durability, security (encryption at rest/in transit). Weak/ eventual cross-region consistency for sync is acceptable (strong consistency not required for reads immediately after remote writes). ([Hello Interview][1])

---

# Functional Requirements (FR)

1. Upload file from any device (desktop/mobile/web).
2. Download file to any device.
3. Share files with other users (ACLs).
4. Auto-sync files across devices (detect changes, upload/download diffs).
5. List files and get changes (delta) for a device to sync. ([Hello Interview][1])

# Non-functional Requirements (NFR)

1. Highly available (favor availability for reads/writes; eventual cross-region replication ok). ([Hello Interview][1])
2. Support large files (≥50 GB). ([Hello Interview][1])
3. Low upload/download latency; efficient client-side sync.
4. Durable storage (replication, versioning/retention as optional).
5. Secure (authN/authZ, encryption, secure sharing links).
6. Cost-efficient storage, with cold/hot tiers and lifecycle policies. ([Medium][2])

---

# Back-of-Envelope (B.o.E.) Calculation — quick numbers to reason about capacity

(assume scale target for reasoning)

* Users: 100M active users.
* Average files per active user: 200 files. → 20B file objects.
* Average file size: 10 MB → total \~200 PB raw (use object storage + dedupe + compression to manage).
* Request rate: say 1% of users active per minute → 1M ops/min ≈ 16.7k ops/sec (mix of uploads/downloads/list).
  Use these to justify distributed blob storage (S3-like), CDN, sharded metadata DB, and event streaming for processing. (Numbers are illustrative — tune based on product metrics.)

---

# Core Data Entities

1. **User**: userId, name, email, auth info, storage quota (if any).
2. **FileMetadata**: fileId (GUID), ownerUserId, filename, size, mimeType, createdAt, updatedAt, versionId, ACLs, storageObjectKey(s), chunk metadata (if chunked), contentHash(s).
3. **FileChunk / Object**: chunkId / objectKey, byte range, size, contentHash, storage location, replication metadata.
4. **DeviceState**: deviceId, userId, lastSyncCursor, local file hashes for fast diffing.
5. **ChangeLog / SyncLog**: ordered logs of changes per user (or per namespace) for sync clients to consume (sequence number, opType, fileId, versionId, timestamp).
6. **Share / ACL**: mapping of fileId → sharedUserIds or public link metadata.

Design note: metadata in a NoSQL document store (query patterns: list by user, list by folder, lookup by fileId) — good fit. Blob data kept in object store (S3/GCS) and referenced by metadata. ([Hello Interview][1])

---

# System Interfaces (APIs)

Minimal set (expandable):

1. `POST /files/presign`

    * Request: {userToken, path, size, mimeType, chunked?\:bool}
    * Response: {uploadId, presignedUrls\[] (for chunks) or single URL}
      (Client uses presigned URL(s) to upload directly to blob store.)

2. `POST /files/commit`

    * Request: {uploadId, metadata}
    * Response: {fileId, versionId}
      (Server validates upload completion, records metadata, emits sync events.)

3. `GET /files/{fileId}`

    * Response: metadata + presigned download URL(s).

4. `GET /users/{userId}/changes?cursor=`

    * Stream or paginate change events for client sync.

5. `POST /files/{fileId}/share`

    * Modify ACLs, create share links.

6. `GET /files?path=` / `GET /folders/{id}/children`

Auth: JWT or session tokens in headers. For large uploads downloads, clients use presigned URLs to avoid backend as bottleneck. ([Hello Interview][1])

---

# Start from Simple Design (whiteboard / ASCII)

Simple initial system (single region, works for small scale):

```
[Client] ---> [App Servers (Auth + Metadata API)] ---> [Relational/NoSQL DB (metadata)]
                     |
                     v
                [Blob storage (S3)]
```

Flow (simple):

1. Client `POST /files` -> App server.
2. App server uploads to blob store (or returns presigned URL).
3. App server inserts metadata in DB.

Problems: app servers become throughput bottleneck for large uploads; storing blobs on app servers is not durable/scaleable. We'll improve next.

(Reference: presigned URLs & blob storage recommended). ([Hello Interview][1])

---

# Enriched Design (scalable, production-like) — ASCII diagram

```
[Clients: Desktop / Mobile / Web]
   |  (1) presign request
   v
[API Gateway / Load Balancer]
   v
[Auth & Metadata Service (stateless app servers)] --- [Metadata DB (sharded NoSQL)]
   |                                       \
   | (2) issue presigned URLs               \-- [Share/ACL Service]
   v                                         \
[Blob Storage (S3/GCS) + CDN] <---(3) direct uploads/downloads-- [Edge CDN]
   ^
   | (4) Object notifications (S3 events) --> [Ingestion / Worker Cluster (Kafka)]
   |                                             |
   |                                             v
   |                                         [ChangeLog / SyncService]
   |                                             |
   v                                             v
[Long-term Storage / Archival]             [Push / Push Notification services]
```

Flow (detailed):

1. Client requests presigned upload URLs from Metadata service (small request). Metadata service creates an `uploadId` and stores a tentative metadata record with `status=uploading`. ([Hello Interview][1])
2. Client uploads *directly* to object storage using presigned URL(s). Large files are chunked and uploaded as multiple objects (multipart upload). ([Hello Interview][1])
3. Object storage emits completion notifications (e.g., S3 Event -> SNS -> SQS / Kafka). Worker consumes event, verifies checksum(s), marks metadata as `committed`, writes final file manifest (chunk list + hashes), emits change event into `ChangeLog`. ([Hello Interview][1])
4. Sync service consumes `ChangeLog` and updates per-user sequence cursors; pushes notifications to devices or waits for clients to poll `GET /changes?cursor=`. ([Medium][2])

Key components: presigned uploads, chunking + multipart, object notifications + worker to finalize metadata, change log/event stream for sync. ([Hello Interview][1])

---

# Design Details, Tradeoffs & Components

## 1) Upload path: presigned URLs + multipart uploads

* **Why:** avoids backend bandwidth bottleneck; clients upload big blobs directly to object store. Backend only handles metadata & auth. ([Hello Interview][1])
* **Implementation:** client requests presigned upload (single for small files, multiple for chunked uploads). For >X MB, use chunked/multipart (each chunk a part). After upload, client calls `commit` to register file in metadata. Worker verifies all parts and composes final object. ([Hello Interview][1])

## 2) Chunking & resumable uploads

* Split large files into chunks (e.g., 8MB - 64MB). Advantages: resumability, parallel upload streams, dedupe at chunk level, cache friendliness. Use multipart upload APIs (S3 style). ([GeeksforGeeks][3])

## 3) Content-addressed storage & deduplication

* Compute chunk-level content hashes (SHA256). Use a map from chunkHash → objectKey. If chunk exists, reuse rather than re-upload. This provides storage savings (common for backup/sync products). Tradeoff: index (hash map) size and lookup cost; need a fast key-value store (e.g., RocksDB, DynamoDB) for chunk index. ([Medium][2])

## 4) Metadata storage

* Use a NoSQL store (DynamoDB / Cassandra / Bigtable) with partitioning by userId (common access pattern). Store file manifest (list of chunk hashes + sizes), latest versionId, ACLs, timestamps. SQL can be used but NoSQL better for predictable scale & single-row lookups. ([Hello Interview][1])

## 5) Sync & ChangeLog

* Maintain an append-only per-user change stream (change sequence numbers). When a file is committed/updated, emit entry to ChangeLog (Kafka). Sync clients poll `GET /changes?cursor=` or open long-poll / websocket. This enables efficient incremental sync and avoids expensive scans. ([Medium][2])

## 6) Conflict resolution / versioning

* Use optimistic versioning: each file has a `versionId`. Client includes baseVersion when committing an update. If baseVersion != currentVersion, server treats as a conflict. Strategies:

    * Auto-merge (for certain file types) — not always possible.
    * Create conflict copy (`file (conflicted copy from device)`) and notify user.
      This favors availability and avoids lost updates. ([Medium][2])

## 7) Consistency model

* Strong within a single metadata update operation (atomic update of metadata row). Across regions, eventual replication is fine (users tolerable of short delays). Use per-file versioning to handle race conditions. Prefer AP in CAP globally across regions; within single region, provide read-after-write if desired for the originating device (sticky session or read from primary). ([Hello Interview][1])

## 8) Download path & CDN

* For downloads, issue presigned download URLs to clients (edge CDN in front of object storage to accelerate reads). CDN caches frequently accessed objects and reduces egress latency. For partial file downloads, serve by chunks/byte-range requests. ([GeeksforGeeks][3])

## 9) Security & Sharing

* Access control: ACLs stored in metadata; presigned URLs are time-limited and signed. For share links, generate opaque tokens with expiry and optional password. Encrypt data at rest (server-side encryption with KMS or client-side encryption for zero-knowledge). Use TLS for transit. ([Medium][2])

## 10) Durability & Lifecycle

* Use object storage replication (multi-AZ or cross-region replicates), versioning, and lifecycle rules (move cold objects to infrequent access / archive). Periodic integrity checks (checksums). ([Hello Interview][1])

---

# Possible Deep Dives (expanded)

These are areas interviewers often drill into; I’ll expand each with concrete designs and tradeoffs.

## A. Concurrency / Conflicts & Versioning (how to avoid lost updates)

**Problem:** two devices update same file concurrently.

**Design options:**

1. **Optimistic locking with versionId:** Client reads version V, writes with baseVersion=V. If baseVersion ≠ current → conflict. Server returns conflict. Simple and widely used. (Preferred)
2. **Operational Transform / CRDTs:** Use for collaborative editing (beyond scope for file blobs). Complex but enables real-time merges for structured data.
3. **Server-side merges for certain types:** e.g., merge for text files. Complex and error-prone.

**Implementation details:**

* Keep `versionId` as monotonically increasing integer or vector timestamp. On conflict, create a new object (conflict copy) and emit user notification; optionally provide UI to merge.
* Record `lastModifiedBy`, `lastModifiedAt` to help users resolve.

**Tradeoffs:** Optimistic versioning is simple and available; OT/CRDT needed for real-time collaborative editors (higher complexity). ([Medium][2])

## B. Efficient Sync (minimize bandwidth & latency)

**Problem:** keep multiple devices in sync without re-downloading entire files.

**Techniques:**

* **Chunk-level hashing + delta sync:** send only changed chunks. If clients compute chunk hashes locally, they can upload only changed chunks and server can avoid storing duplicates.
* **Rsync-like rolling checksums for small edits:** useful for very large files with small edits; more complex to implement and CPU-heavy on client/server.
* **ChangeLog + cursor model:** clients poll or long-poll for changes since last cursor; server returns metadata and changed chunk manifests. ([Medium][2])

**Implementation note:** Choose chunk size to balance overhead vs dedupe granularity (e.g., 8–64 MB). Smaller chunks improve dedupe but increase metadata overhead.

## C. Chunk index / deduplication scale & consistency

**Problem:** chunk hash table (chunkHash → objectKey) can be huge.

**Design:**

* Use a scalable key-value store (DynamoDB / Bigtable) to store chunk index. Shard by hash prefix.
* Store reference counts to garbage collect unreferenced chunks. Maintain atomic increments/decrements when metadata commits/ deletes. Use CRDT counters or transactional updates if available.
* For speed, use a cache (Redis) for hot chunk hashes.

**Tradeoffs:** reference counting needs correct accounting (race conditions on concurrent commits). Use atomic DB ops or event-sourced reconciliation. ([Medium][2])

## D. Metadata scalability & partitioning

**Problem:** listing files by user and reading metadata must scale.

**Design:**

* Primary partition key: `userId` (range queries for folder listing). Secondary sort key: `path` or `lastModified`.
* Use materialized views for frequently used queries (e.g., shared files). For search, index metadata into search service (Elasticsearch).
* Use cold storage or archival for rarely accessed metadata if needed.

**Tradeoffs:** hot users (power users) can cause partition skew; mitigate with fanout, splitting user namespace, or using consistent hashing with hotspots handled by adding capacity. ([Hello Interview][1])

## E. Handling Large Files & Multipart uploads

**Problem:** large file uploads must be resumable and reliable.

**Design:**

* Use multipart upload API (S3 style) with chunk checksums and an upload manifest. Client retries per part. Worker composes parts into final object.
* For reliability, store metadata with status `uploading` and expiry; garbage collect incomplete uploads.
* Use parallelism for high throughput.

**Tradeoffs:** multipart complexity vs robustness. Ensure idempotency of part uploads (use unique part numbers, checksums). ([GeeksforGeeks][3])

## F. Cross-region replication & latency

**Problem:** user in region A uploads, user in region B wants quick read.

**Design choices:**

* **Active-active with eventual replication:** write to nearest region, asynchronously replicate metadata and objects to other regions via cross-region replication pipeline (Kafka + replication workers). Reads in other region might see old version briefly. Use versioning + conflict handling.
* **Read-after-write for origin device:** direct clients to read from primary or use strong-consistency API if necessary (skip replicating read).
* **Edge caches/ CDN**: serve hot files worldwide with CDN invalidation on update.

**Tradeoffs:** Strong cross-region consistency increases latency; prefer availability & eventual consistency for file sync product. ([Hello Interview][1])

## G. Monitoring, Observability & SLOs

**Key metrics:** upload/download latency, error rates, storage used, replication lag, incomplete upload counts, change log backlog. Alert on anomalies. Use distributed tracing for request flows (from presign through commit). SLO examples: 99.9% availability for API, 99.99% durability for stored objects. ([Medium][2])

## H. Cost optimization

* Tier storage (hot/hot-infrequent/archive).
* Deduplication reduces storage.
* Use lifecycle policies to expire stale versions.
* Garbage collection of orphaned chunks. ([Medium][2])

---

# Operational concerns

* **Deployment:** stateless app servers behind LB; autoscaling; blue/green deploys.
* **Backups & DR:** cross-region replication, versioning, retention policies.
* **Security:** IAM for object store usage, KMS for keys, audit logs for sharing operations.
* **Testing:** chaos testing for blob store failures, network partitions.

---

# Short example interview script (how to present)

1. Clarify scope & scale (I’d confirm file size limit \~50GB and expected user scale).
2. State the core idea: metadata in NoSQL, blobs in object storage, clients upload directly with presigned URLs, change log for sync. (cite brief rationale). ([Hello Interview][1])
3. Sketch simple design, then show enriched design and walk through upload/download flows.
4. Deep dive into one of: chunking/dedupe or conflict resolution — show tradeoffs and final choice.
5. Summarize NFR tradeoffs: availability vs consistency, eventual replication, CDN for reads, cost tiers.


