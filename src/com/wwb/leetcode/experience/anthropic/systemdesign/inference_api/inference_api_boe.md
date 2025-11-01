
# **Top-Down BoE — Updated with Output Tokens**

---

## **1️⃣ Entry Point — Requests per Second (RPS)**

* Active users: 100k
* 1 request every 10s → **RPS ≈ 10,000**
* Avg prompt length: 50 tokens → **Token inflow ≈ 500,000 tokens/sec**

---

## **2️⃣ Scheduler / Dynamic Batching**

* **Batching window:** 20ms (max wait)
* **Batch size:** 16 sequences
* Forward pass latency per batch: 50ms

**Total latency for first request in batch:**

```
≈ batch wait + GPU execution = 20 + 50 ≈ 70 ms
```

---

## **3️⃣ Worker Layer — GPU Memory & Single GPU**

Assume **A100 40GB GPU**:

* Model weights (7B FP16): 14GB
* KV cache per session: 50 tokens × 1MB ≈ 50MB
* Batch of 16 sequences → KV ≈ 16 × 50 MB ≈ 0.8GB

**GPU memory usage:**

```
Weights + KV per batch ≈ 14.8 GB
Available for other sessions ≈ 25 GB
```

* Max concurrent sequences per GPU ≈ 25 GB / 50 MB ≈ 500 sequences
* Token throughput per GPU (50 tokens/sec per sequence) → 500 × 50 ≈ 25,000 tokens/sec (optimal)

**KV offload:** 20% → throughput ≈ 22,000 tokens/sec

---

## **4️⃣ Multi-GPU Scaling (Multiple GPUs)**

* Total token inflow = 500,000 tokens/sec
* Throughput per GPU ≈ 22,000 tokens/sec

```
# GPUs required ≈ 500,000 / 22,000 ≈ 23 GPUs
```

* Autoscaling triggers when queues grow → adds more GPU cards
* Off-peak: fewer GPUs active to save cost

---

## **5️⃣ KV Cache Management**

* GPU-resident KV for active sessions
* Idle sessions offloaded to CPU/NVMe
* Eviction: LRU / TTL ~30min

---

## **6️⃣ Model Lifecycle / Versioning**

* Sessions pinned to model version
* Blue-green deployment for new versions
* KV cache invalidated per version

---

## **7️⃣ Network Layer**

* Request: 2 KB JSON → ingress ≈ 20 MB/sec
* Response: 50 tokens × 200 bytes = 10 KB → egress ≈ 100 MB/sec

---

## **8️⃣ Conversation History Storage (Updated)**

* **Average conversation:** 50 messages × 50 tokens per message

    * User input tokens: 50 × 50 = 2,500 tokens
    * Model output tokens: 50 × 50 = 2,500 tokens
    * **Total tokens per conversation:** 5,000 tokens
* **Storage estimate:** 5,000 tokens × 4 bytes/token ≈ **20 KB per conversation**
* **Total for 100k users:** 100,000 × 20 KB ≈ 2 GB

**Optimizations:**

* Store token IDs for faster KV cache reconstruction
* Keep only recent N tokens/messages in memory
* Offload older messages to persistent storage

---

## **✅ Summary Table — Updated BoE**

| Layer                | Metric               | BoE Estimate            | Notes                                                 |
| -------------------- | -------------------- | ----------------------- | ----------------------------------------------------- |
| Entry Point          | Requests/sec         | 10,000 RPS              | 50 tokens/request                                     |
| Token inflow         | Tokens/sec           | 500,000                 | 50 tokens × 10k RPS                                   |
| Batching             | Batch size / latency | 16 sequences / 70 ms    | 20 ms wait + 50 ms GPU                                |
| GPU memory           | Weights + KV         | 14 GB + 0.8 GB          | Batch of 16 sequences                                 |
| GPU concurrency      | Max sequences        | ~500 sequences          | Accounts for activations/padding                      |
| GPU token throughput | Tokens/sec           | ~22,000                 | Includes 20% KV offload                               |
| Multi-GPU            | GPUs required        | 23 GPUs                 | To handle 500k tokens/sec                             |
| KV cache             | Per session          | 50 MB                   | Partial offload possible                              |
| Network              | Ingress / Egress     | 20 / 100 MB/sec         | JSON request/response                                 |
| Conversation history | Storage              | ~20 KB per conversation | Includes user input + model output; 100k users ≈ 2 GB |

