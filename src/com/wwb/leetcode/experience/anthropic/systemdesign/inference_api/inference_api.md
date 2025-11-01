

# **Inference API / Service — Full Overview**

---

## **Functional Requirements (FR)**

~~1. Inference request lifecycle: client request → batching → model execution → decoding → streaming output~~
2. Token streaming
3. Multi-model
~~2. Dynamic batching: combine requests efficiently, manage slots, tradeoff latency vs throughput~~
3. KV cache management: store past token hidden states for multi-turn conversation
4. Memory management: GPU memory partitioning, OOM handling, eviction/offload
5. Scheduling and load balancing: per-GPU queues or central scheduler, multi-model routing, priority handling
6. Speculative decoding / streaming: token-level streaming, draft model predictions
~~7. Autoscaling: scale GPU workers based on queue depth, GPU utilization, tokens/sec~~
8. Model lifecycle management: registry, deployment, rollback, version pinning per session
~~9. Temperature / sampling controls: temperature, Top-K, Top-P per request~~
10. Observability: metrics, tracing, profiling, alerts

---

## **Non-Functional Requirements (NFR)**

* Scalability: horizontal GPU scaling, batching efficiency
* Latency: low token streaming latency, speculative decoding
* Throughput: high token/sec, GPU utilization
* Availability: redundant schedulers, session draining, worker failover
* Fault tolerance: recovery from worker crash, session replay
* Consistency: session version stickiness, KV cache coherence
* Cost efficiency: model consolidation, GPU utilization optimization
* Security: tenant isolation, rate limits, authentication/authorization
* Observability: dashboards, alerts, trace analysis

---

## **Phased System Evolution (Stages 1 → 7)**

Perfect — streaming is an essential architectural layer that touches **both scheduling and runtime**.
Below is the **final, enhanced version** of the “Inference Service Evolution” — now fully integrated with **streaming token generation** and how it affects batching, networking, and fairness.
I preserved the same clean progression while making Stage 7 (multi-tenancy) fully coherent with streaming workloads.

---

# 🧠 **Evolution of Inference Service (with Streaming)**

---

### **Stage 1 — Monolithic Prototype**

```
+---------------------------------------------------+
|                Inference Server                   |
|---------------------------------------------------|
| REST/gRPC API | Scheduler | Model Runtime (GPU)   |
|  (single process, one GPU)                        |
+---------------------------------------------------+
```

* Every request runs end-to-end in one thread: encode → decode → return full text.
* No token streaming — response is delivered **after full generation**.
* Simple, low throughput, high latency for long responses.

---

### **Stage 2 — Split API, Scheduler, Worker (Batching Introduced)**

```
+---------------------+        +--------------------+        +----------------------+
|    API Gateway      | -----> |  Scheduler / Queue | -----> |   Inference Worker   |
| (auth, rate limit)  |        | (batching, routing)|        | (GPU runtime)        |
+---------------------+        +--------------------+        +----------------------+
```

* Scheduler groups requests into **microbatches** (e.g., 32 reqs or 20 ms window).
* Worker runs forward passes for all requests in the batch.
* Still **non-streaming** — the batch finishes before returning anything.
* Works fine for short tasks (classification, embedding), not for chat.

---

### **Stage 3 — Streaming Decode & KV Cache**

```
+---------------------+        +--------------------+        +----------------------------+
|    API Gateway      | -----> |  Scheduler / Queue | -----> |  Inference Worker (GPU)    |
|   (WebSocket/SSE)   |        |                    |        |  - Model runtime           |
|   Streams tokens ↑  |        |                    |        |  - KV Cache Manager        |
+---------------------+        +--------------------+        +----------------------------+
```

* **Token streaming introduced:**

   * Clients receive tokens incrementally via WebSocket or Server-Sent Events.
   * User sees words appear live (“typing” effect).
* **KV cache** now crucial: reuse hidden states between decode steps.
* Worker runs multiple **concurrent decoding loops**:

   * Each step: `forward(next_token, past_kv)` → emit token → repeat.
* Scheduler multiplexes multiple streams per GPU to keep utilization high.

---

### **Stage 4 — Smart Scheduler & Control Plane Split**

```
     +---------------------+        +----------------------------+
     |   Scheduler Service  | <---->|   KV Metadata Store        |
     |  - Batch manager     |       | session→gpu, kv_size       |
     |  - Stream tracker    |       | last_token, last_used      |
     +---------------------+        +----------------------------+
                  |                               |
                  v                               v
     +-----------------------------------------------------------+
     |   Inference Worker (GPU)                                  |
     |   - Model Runtime | Stream Manager | KV Cache Manager     |
     +-----------------------------------------------------------+
```

* **Control plane** (scheduler + metadata) tracks session state & placement.
* **Data plane** (workers) handle continuous streaming decode steps.
* Token-level batching: group multiple sessions mid-generation when
  they request the next token at similar time steps (a.k.a. **interleaved batching**).
* Scheduler uses metadata to avoid OOM and preempt expired streams.

---

### **Stage 5 — Conversation History Persistence**

```
+-----------+       +------------------+        +------------------+
|  Client   | <-->  | Inference API    | <-->   | Conversation DB  |
+-----------+       +------------------+        +------------------+
                         |    ^
                         v    |
                   +----------------+
                   |  GPU Workers   |
                   |  (KV Cache)    |
                   +----------------+
```

* Persist chat history and generated tokens.
* Enables **reconstruction** of context if session dropped or KV evicted.
* Token streaming remains continuous — client replays from last token if needed.
* **Checkpointing:** stream manager flushes partial outputs periodically.

---

### **Stage 6 — Multi-Model, Streaming-Aware Autoscaling**

```
+----------------------+       +----------------------+
|   Model Registry     |<----->|  Model Orchestrator  |
| (weights, configs)   |       | (placement, scaling) |
+----------------------+       +----------------------+
                                      |
             +---------------------------------------------+
             | Scheduler Cluster (per-model queues)        |
             | - Token-level interleaving batching         |
             | - Streaming-aware load prediction           |
             +---------------------------------------------+
                          |
        +---------------------------------------------+
        | GPU Pool (mixed hot/cold models)            |
        |                                             |
        | Hot Pool: pinned, active models             |
        | Cold Pool: load-on-demand                   |
        +---------------------------------------------+
```

* Autoscaler now tracks **streaming compute units** (tokens/sec, not req/sec).
* **Predictive scaling:**

   * Increase capacity if `pending_token_steps > 1.2×current_capacity`.
   * Downscale when GPU utilization < 30% for 10 min.
* **Streaming-aware batcher:**

   * Dynamically re-batches tokens from ongoing streams across users.
   * Maintains per-stream latency SLO (e.g., ≤50 ms/token).

---

### **Stage 7 — Multi-Tenancy + QoS Isolation (Streaming Integrated)**

```
           +-----------------------+
           |   Frontend Router     |
           +----------+------------+
                      |
        +-----------------------------------+
        | Admission & Quota Gate (per-tenant)|
        +-----------------------------------+
                      |
        +-----------------------------------+
        | Priority / Tenant-Aware Queues     |
        +-----------------------------------+
                      |
        +-----------------------------------+
        | Token-Aware Adaptive Batcher       |
        | (interleaved streaming batches)    |
        +-----------------------------------+
                      |
        +-----------------------------------+
        | Scheduler -> Shared GPU Pools      |
        |  (QoS, Autoscale, Eviction)        |
        +-----------------------------------+
```

**Streaming-specific responsibilities**

| Area                       | Mechanism                                                      | Notes                                                 |
| -------------------------- | -------------------------------------------------------------- | ----------------------------------------------------- |
| **Quota Enforcement**      | Token-based rate limiting (bucket size = tokens/sec)           | Each emitted token counts toward quota                |
| **Fair Streaming**         | Weighted round-robin per tenant, fair batching per decode step | Ensures one tenant’s long stream doesn’t block others |
| **Streaming Multiplexing** | Interleaved decode steps from many sessions in one GPU batch   | Maximizes throughput                                  |
| **Eviction / Preemption**  | Pause or migrate idle streams when memory low                  | Maintain QoS for active ones                          |
| **Autoscaling Signals**    | Pending token backlog, per-tenant throughput                   | Keeps latency stable even during bursty chat sessions |
| **Resumable Streams**      | Resume on reconnect (token offset checkpoint)                  | Client gets seamless continuation                     |

**Pooling Options**

| Mode                | Description                              | Tradeoff                          |
| ------------------- | ---------------------------------------- | --------------------------------- |
| **Dedicated GPUs**  | One tenant per pool                      | Strong isolation, low utilization |
| **Shared QoS Pool** | Multiple tenants, token-level fairness ✅ | Best efficiency                   |
| **Hybrid Slice**    | Reserve slice + shared base              | Predictable cost + flexibility    |

**Streaming fairness math (simplified)**

```
Per decode step:
  tokens_per_batch = min(sum_active_streams, MAX_BATCH)
  gpu_util ≈ tokens_per_batch / model_max_throughput

Autoscaler target:
  latency ≤ 1.1 × SLA (e.g. 50 ms/token)
  utilization ≈ 70–80 %
```



---

### **Stage 8 — Observability, TTL, and Reliability**

```
+------------------------------------+
| Observability Layer                |
| - GPU utilization metrics          |
| - Queue depth, latency             |
| - Session lifecycle (TTL, evictions)|
+------------------------------------+
```

* Metrics: GPU utilization, latency, tokens/sec
* Session lifecycle management: idle TTL, eviction, offload
* Full monitoring, alerting, and fault recovery

---

### 


---

### ✅ **Final Phase Summary**

| Stage | Focus                    | Key Advancement                                |
| ----- | ------------------------ | ---------------------------------------------- |
| 1     | Monolithic               | Simple prototype, full response only           |
| 2     | Batching & Routing       | Improved GPU efficiency                        |
| 3     | Streaming + KV Cache     | Incremental token generation                   |
| 4     | Smart Scheduling         | Control/Data plane split, interleaved batching |
| 5     | Conversation Persistence | Session resume, fault tolerance                |
| 6     | Multi-Model + Autoscale  | Streaming-aware scaling, hot/cold pools        |
| 7     | Multi-Tenancy + QoS      | Fair token-level scheduling, quotas, isolation |

