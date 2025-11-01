# **Table of Contents (Top-Level Reference)**

1. [Token-aware adaptive batching](#1-token-aware-adaptive-batching-deep-dive)
2. [Demultiplexing / splitting batched model outputs](#2-demultiplexing-splitting-batched-model-outputs)
3. [Inference worker runtime: model parallelism, memory, and kernels](#3-inference-worker-runtime-model-parallelism-memory-and-kernels)
4. [Streaming design (SSE / gRPC), partial responses, and backpressure](#4-streaming-design-sse-grpc-partial-responses-and-backpressure)
5. [Multi-tenancy, QoS, fairness, and rate limiting](#5-multi-tenancy-qos-fairness-and-rate-limiting)
6. [Observability, tracing, SLOs, and alerting](#6-observability-tracing-slos-and-alerting)
7. [Security, data retention, logging policies](#7-security-data-retention-logging-policies)
8. [Model lifecycle: registry, canary, warmup, rollback](#8-model-lifecycle-registry-canary-warmup-rollback)
9. [Cost optimisation patterns](#9-cost-optimizations)
10. [Failure modes, mitigations, and chaos tests](#10-failure-modes-mitigations-with-chaos-tests)
11. [Capacity planning worked example + quick math](#11-capacity-planning-worked-example-step-by-step)
12. [Testing & benchmarks (what to simulate and metrics to collect)](#12-testing-benchmarking)
13. [Concrete pseudocode for batcher, assembler, streaming loop, model loader](#13-concrete-pseudocode-detailed)
14. [Short checklist / interview talking points you can use](#14-short-checklist-interview-talking-points)


# 1 Token-aware adaptive batching (deep dive)

Why: batching increases GPU throughput but increases queue waiting latency. Simple fixed-size batching is insufficient because different requests cost different amounts of compute (input length, requested output tokens, decoding strategy).

Goals:

* Maximize GPU utilization (tokens/sec, FLOPs/sec).
* Keep p95/p99 latency within SLO.
* Avoid stragglers (one long input blocks a batch).

Core ideas:

* **Batch by compatibility keys**: model, model_version, device_type, decoding flags (sampling vs greedy), same dtype/quantization. Only combine compatible requests.
* **Cost estimation**: estimate per-request cost = `c_in * input_tokens + c_out * expected_output_tokens + c_overhead`. Use historical averages per tenant or prompt-length buckets to estimate `expected_output_tokens`.
* **Weight-based packing**: treat batch capacity as total allowed compute units rather than a slot count. Example: max_compute_units = 1000; each req uses 50 units; pack until capacity filled or timer expired.
* **Adaptive timers**:

    * `T_max` (absolute max wait): e.g., 20–50ms for interactive SLOs.
    * `T_min` (minimum time to wait for more requests after first arrives): e.g., 3–10ms.
    * `T_idle` (when queue is empty): no batching.
* **Priority classes**: interactive vs bulk. Interactive uses lower `T_max` and smaller batch capacity. Bulk (e.g., offline summarization) can use large `T_max` to maximize throughput.
* **Dynamic sizing**: monitor pack success — if typical batch fills quickly, raise batch capacity; if not, shrink.
* **Slot preemption**: do not block entire GPU because of one giant request — either reject/route the long request to a dedicated worker or run small and big requests separately (separate queues).
* **Backpressure & admission control**:

    * If queue length > threshold → reject or degrade lower-priority requests.
    * Token-based quotas per tenant reduce noisy neighbors.

Example algorithm (conceptual):

* One queue per (model, version, priority).
* Batcher loop:

    1. Pop first request → start timer `T_pack`.
    2. Set `remaining_compute = max_compute`.
    3. Add first request's weight.
    4. While `remaining_compute` and `now() - start < T_pack`:

        * Peek next request.
        * If compatible and weight <= remaining_compute, pop + add.
        * Else if incompatible or too large: break or requeue (consider a separate "large" queue).
    5. Submit batch.

Tuning knobs:

* `max_compute` per model per GPU (benchmarks).
* `T_pack` per priority (interactive 10–30ms, standard 50–200ms, batch 200–1000ms).
* `weight_calc` coefficients learned from historical latency (you can run linear regression mapping tokens→GPU-ms to set `c_in` and `c_out`).

Tradeoffs to talk about:

* Larger `T_pack` increases throughput but raises p95 latency.
* Aggressive packing increases utilization but may increase p99 due to stragglers if mix of lengths.
* Predictive weight estimation improves packing but may mispredict.

# 2 Demultiplexing / splitting batched model outputs

Problem: GPU returns a batched tensor; we must map tokens and states back to each request preserving order and streaming semantics.

Key approaches:

* Maintain strict **index map**: when building batch, create `batch_slots = [req1_id, req2_id, ...]`. Every output row corresponds to a `slot`.
* **Padding & masking**: pad input to same length; maintain `attention_mask` and position indices for each slot.
* **KV-cache (for autoregressive decoding)**: store KV cache per slot. When decoding step t returns new key/value, store them indexed by `slot`.
* **Streaming demux**:

    * For each decode step: runtime returns tokens per slot (some may be EOS). Emit token events per slot in the original order.
    * If model returns entire output for each slot at once (non-streaming), slice outputs by slot and send.
* **Partial completion & timeouts**:

    * If client disconnects mid-batch: mark slot cancelled; continue processing other slots. On finish, drop results for canceled slot.
    * If one slot causes OOM: detect early in allocation phase, evict/reject that slot, run remaining slots.
* **Output post-processing**: decoding (BPE detokenization), safety filters, redaction — perform per-slot.
* **Ordering guarantees**:

    * Provide each chunk a `slot_seq_no` and `global_chunk_seq`. Clients can reorder on their side if needed but server should ensure monotonic chunk seq per request.

Edge cases / optimizations:

* **Variable decoding speeds**: different slots may finish earlier; assembler still waits for their final tokens — but can stream tokens as they arrive.
* **Micro-batching for decode**: to reduce kernel launch cost, group decode steps across slots into micro-batches within the large batch.

# 3 Inference worker runtime — model parallelism, memory, and kernels

Serving large LLMs efficiently is about the runtime design.

Worker types:

* **Single-GPU worker**: small/medium models that fit on one GPU. Easy to batch and autoscale.
* **Sharded workers**: very large models split across multiple GPUs (tensor/model parallelism). Requires cross-GPU communication (NCCL) and careful scheduling.
* **Pipeline-parallel workers**: stages hosted on different GPUs; introduce pipeline bubbles and complexity.

Key capabilities:

* **High-performance runtime**: custom C++/CUDA runtime or Triton that supports:

    * Fast token generation loop.
    * KV cache per slot.
    * FlashAttention / fused kernels.
    * Mixed precision (FP16, BF16), quantization (INT8) path.
* **Memory management**:

    * Keep hot model in GPU memory.
    * Use pinned CPU memory for activations when possible.
    * Activation offloading for extremely large models (trade latency).
* **Model loading & warmup**:

    * Preload model shards and run a small warmup prompt to JIT kernels and allocate buffers.
* **Batch scheduling & GPU occupancy**:

    * For GPU occupancy, schedule batches so that compute and memory usage is balanced.
    * Use multi-streaming or concurrent kernels if supported.
* **Inter-GPU comms**:

    * For tensor-parallel: overlap computation and communication. Minimize latency by NCCL optimum ring.
* **Runtime scalability**:

    * Have a coordinator that maps batch’s device topology needs to available workers.

Tradeoffs:

* Sharded models reduce per-GPU memory demand but increase inter-GPU bandwidth and latency.
* Quantization reduces memory and increases throughput but may harm quality.
* Offloading reduces memory pressure but increases latency.

Operational notes:

* Benchmark each model variant: tokens/sec, latency per input length, GPU memory usage at various batch sizes.
* Maintain per-worker health checks including GPU ECC errors, memory pressure, temperature.

# 4 Streaming design (SSE / gRPC), partial responses, and backpressure

Streaming is critical for low perceived latency. Two common APIs: SSE/WebSockets (HTTP/1.1 or 2) and gRPC streaming.

Design elements:

* **Protocol**:

    * SSE: text/event-stream. Simple for browsers.
    * WebSocket: bi-directional, good for interactive editing/chat.
    * gRPC: efficient, typed, works well for binary tokens.
* **Chunk format**:

    * Include `request_id`, `chunk_seq`, `slot_token_index`, `is_final`, `batch_id`, `latency_ms`.
    * Optionally include `delta_text` (most clients want deltas).
* **Server streaming loop**:

    * Batcher runs decode steps; after each decode micro-step (e.g., 1–8 tokens), flush available tokens for any slot that produced tokens.
    * Provide `heartbeat` events to keep connection alive if long gaps.
* **Backpressure**:

    * Client can signal `ACK` or `window` size; otherwise, maintain small server-side send buffers and drop or pause if client is slow.
    * If client is slow and queue backlog grows, either:

        * Buffer up to limit, then pause reading further slots for that client.
        * Abort slot after timeout and notify client.
* **Ordering in streaming**:

    * Guarantee per-request sequential ordering of emitted chunks.
* **Safety checks mid-stream**:

    * Run safety model concurrently (or step-wise) to filter tokens that would violate policy. If flagged, emit blocked event and stop stream.
* **Reconnect & resume**:

    * Provide a way to resume a stream given client reconnect (streaming session tokens + last acknowledged token index). This requires you to keep generated tokens in short-term cache for resume window.

Implementation sketch (gRPC pseudo):

* `Predict(stream PredictRequest) returns (stream PredictResponse)`
* Client sends initial message with prompt + params.
* Server responds with a stream of PredictResponse messages containing token_deltas and meta.
* For reconnection: a `ResumePredict` RPC that takes `session_id` + `last_seq`.

# 5 Multi-tenancy, QoS, fairness, and rate limiting

Multi-tenant systems must enforce fairness while maximizing utilization.

Policies:

* **Per-tenant quotas**: rps & tokens-per-minute. Tokens-based quotas are better for cost fairness.
* **Reserved capacity**: allow customers to reserve X slots or tokens for guaranteed latency. Implement via capacity tokens on the batcher.
* **Priority & isolation**:

    * Logical queues per priority tier.
    * Ensure each tenant cannot occupy more than N slots in a batch (to avoid single tenant monopolizing a batch).
* **Burst capability**:

    * Implement leaky bucket or token bucket. Allow short bursts above baseline but enforce refill rates.
* **Soft vs hard isolation**:

    * Soft: shared workers with quotas enforced at admission control.
    * Hard: dedicate workers or GPUs for high-value tenants.
* **Noisy neighbor detection & mitigation**:

    * Metrics to detect tenants with high token usage and high tail latency impact. Apply throttling or move to dedicated pool.

Implementation details:

* At ingress: check tenant’s token-balance. If insufficient, reply with 429 or 402 depending on billing model.
* At batcher: when assembling batch, limit per-tenant slots.
* At worker: add tenant id to logs/traces for chargeback.

Security & fairness tradeoffs:

* Overly conservative per-tenant limits harm utilization; too lenient allows abuse.
* Clear SLA tiers prevent surprises.

# 6 Observability, tracing, SLOs, and alerting

Observability is the difference between reactive ops and proactive ops.

Essential telemetry:

* Request-level: request_id, client_id, model, version, input_tokens, output_tokens, latency (ingress→egress), batch_id.
* Queue metrics: queue_length, average_wait_time, packing_time distribution.
* Batch metrics: batch_size_distribution, compute_units_used.
* GPU metrics: utilization, memory_used, temp, power draw, kernel durations, OOMs.
* Error metrics: OOMs, model_load_failures, inference_errors.
* Cost metrics: tokens_generated_per_customer, cost_per_gpu_hour.

Tracing:

* Use distributed tracing (e.g., OpenTelemetry). Propagate trace_id across frontend, batcher, worker, model manager.
* Instrument stages: API_gateway → frontend_validate → enqueue → packer_wait → GPU_execute → assemble → safety_filter → response_send.

SLOs & SLIs:

* Latency SLIs: p50/p95/p99 for successful requests.
* Availability: percent of requests returning 200 (excluding client cancellations).
* Cost/efficiency SLI: tokens per GPU-hour.
* Response correctness / safety: percent of requests blocked by moderation (monitor for spikes).

Alerting:

* High p99 latency, increasing queue length, rising OOM rate, model load failures, sudden drop in GPU utilization (indicates scheduler bugs), high error rates from a specific tenant.

Dashboards:

* Per-model heatmap: latency vs qps vs batch_size.
* Per-tenant billing dashboard: tokens, dollars, errors.

# 7 Security, data retention, logging policies

Security and privacy are critical especially for Anthropic-style interviews.

Data policies:

* **Prompt logging**: default to not using customer data for training. If collecting for debugging, redact PII and store only for a short TTL (e.g., 7–30 days).
* **Encryption**: TLS for in-flight, KMS-backed encryption for stored artifacts.
* **Access control**: RBAC for ops team, audit logs for model downloads and key usage.
* **Secret handling**: API keys rotate; use ephemeral tokens for internal services.
* **Privacy-preserving options**: Tenant can opt into "no-logging" or "redacted-logging". For extremely sensitive customers, provide on-prem or VPC-hosted deployments.

Runtime security:

* Sandbox model execution; restrict model’s access to internal network and filesystem.
* Rate-limit admin APIs; apply auth + mTLS.
* Monitor for prompt-injection patterns in downstream usage.

Legal & compliance:

* Allow data export and deletion requests.
* Keep model artifacts access-auditable.

# 8 Model lifecycle — registry, canary, warmup, rollback

Model lifecycle management is essential for safe rollouts and stable inference.

Model registry:

* Immutable model artifacts with metadata: name, version, build hash, tokenizer version, required resources, benchmark profile (latency, tokens/sec).
* Store in object store + manifest (e.g., S3 + DB).

Deployment pipeline:

1. Upload model artifact → store hash & metadata.
2. Canary deploy (1–5% traffic): route a small percentage to new model on dedicated workers.
3. Monitor: correctness metrics (task-specific), latency, OOMs, user complaints, safety flags.
4. Roll forward or roll back based on thresholds.

Warmup:

* Pre-allocate GPU memory.
* Run a set of canonical prompts to JIT kernels and populate caches.
* Pre-generate small synthetic batch to ensure runtime kernels configured.

Rollback:

* Use traffic-splitting router that can atomically swap weights. Maintain previous model’s workers as warm standby to reduce rollback time.

A/B testing:

* Send 50/50 or weighted traffic and compare per-tenant business metrics, not just technical metrics.

# 9 Cost optimizations

Large costs are GPU-hours and storage. Optimize both.

Techniques:

* **Quantization**: INT8 or mixed quantization for cheaper endpoints.
* **Model distillation**: offer smaller distilled models for lower-cost workloads.
* **Spot instances**: non-critical background workloads run on spot GPUs with checkpointing.
* **Autoscale by queue length**: scale workers based on pending compute units, not CPU.
* **Batching efficiency**: improve packing to reduce empty capacity.
* **Caching**: response cache for deterministic prompts; embed short-term token cache for streaming resumptions.
* **Tiered infrastructure**: dedicated fast-path for premium customers; shared cheap path for others.

Billing units:

* Bill by tokens (input + output), possibly with premium pricing for high temperature or streaming.

# 10 Failure modes & mitigations (with chaos tests)

Common failures & responses:

1. GPU OOMs:

    * Mitigation: early cost estimation; reject huge requests with 413; separate large-request workers; dynamic reduction of batch size; monitor OOM rate.
    * Test: send many oversized inputs.

2. Slow model load on deploy:

    * Mitigation: pre-warm new model on small subset; maintain warm standby nodes.
    * Test: orchestrate mass model reloads.

3. Noisy neighbor:

    * Mitigation: enforce per-tenant slot limits; rate-limit; move to dedicated pool.
    * Test: simulate single tenant saturating tokens.

4. Network congestion between sharded GPUs:

    * Mitigation: measure cross-GPU bandwidth; colocate shards on same host where possible.
    * Test: saturate NIC with synthetic traffic.

5. Client disconnect mid-stream:

    * Mitigation: detect socket closure; stop streaming for that slot and free KV cache.
    * Test: create flaky client disconnects.

6. Partial model correctness / regression post update:

    * Mitigation: shadow traffic, canary, and offline unit tests with golden inputs.
    * Test: run regression suite with scoring.

Chaos testing suggestions:

* Kill a worker mid-batch (should not corrupt other slots).
* Inject latency on NCCL calls.
* Spike number of long input requests.

# 11 Capacity planning worked example (step-by-step)

We’ll do careful arithmetic.

Assumptions (example realistic numbers you would benchmark):

* Model A throughput per GPU: 10,000 tokens/sec (this is a benchmarked number for a certain configuration).
* Average request: 50 output tokens (avg_out = 50) and 25 input tokens (avg_in = 25).
* Total tokens per request = 25 + 50 = 75 tokens.
* Effective requests/sec per GPU = tokens/sec / tokens_per_request.

Compute:

1. tokens/sec per GPU = 10,000.
2. tokens_per_request = 75.
3. requests_per_sec_per_gpu = 10,000 ÷ 75.

    * Step by step: 75 × 100 = 7,500 tokens for 100 requests; remaining tokens = 10,000 − 7,500 = 2,500 tokens which is 33.333... requests. So 100 + 33.333... = 133.333...
    * Therefore requests_per_sec_per_gpu ≈ 133.33 rps.

If target total RPS = 4,000 rps:
4) num_gpus = ceil(4000 ÷ 133.33).

* 133.33 × 30 = 4,000 (since 133.33 × 30 = 3,999.9 ≈ 4,000). So roughly 30 GPUs.
* So you need ~30 GPUs (plus buffer for SLO headroom and autoscale).

Accounting for batching multiplier:

* If batching increases throughput by 2× at same latency profile, effective rps/gpu = 266.66 and GPUs needed = 4000 ÷ 266.66 ≈ 15.
* Always benchmark: theoretical doubling may not hold for your exact prompt mix.

Include redundancy:

* Add safety factor (e.g., +20% headroom): 30 × 1.2 = 36 GPUs.

Explain in interview:

* Show you’d run real microbenchmarks on the exact model, with representative prompt distribution, and compute tokens/sec at different batch sizes. This is what sets your `max_compute` and autoscale thresholds.

# 12 Testing & benchmarking

What to test:

* Microbenchmarks:

    * Single request latency for short/long prompts.
    * Throughput for fixed batch sizes.
    * Token/sec with streaming vs non-streaming.
* End-to-end tests:

    * Tail latency under realistic mixed workload.
    * Queueing behavior under burst traffic.
* Stress tests:

    * Sustained high QPS to validate autoscale.
    * Noisy neighbor scenarios.
* Chaos:

    * Kill workers mid-batch; ensure no memory leaks.
    * Network partitions across GPUs.
* Safety/regression:

    * Golden prompts for correctness.
    * Safety classifier runs on outputs.

Tools:

* Locust or k6 for traffic generation.
* JMeter for HTTP-level testing.
* Custom generator to simulate token distributions and chunked streaming.

Metrics to collect:

* p50/p95/p99 latency, queue waiting time, batch_size histogram, batch_pack_time, GPU_utilization, OOM count, cost per 1M tokens.

# 13 Concrete pseudocode (detailed)

I’ll show you compact but practical pseudocode for:

* Request enqueuer (frontend)
* Batcher (token-aware)
* Worker decode loop (streaming micro-step)
* Response assembler

### Frontend enqueue (simplified)

```python
def handle_request(req):
    req_id = uuid4()
    meta = {
        "tenant": req.tenant,
        "model": req.model,
        "version": req.version,
        "priority": classify_priority(req),
        "est_out": estimate_out(req.prompt, req.max_tokens)
    }
    queue = get_queue(meta["model"], meta["version"], meta["priority"])
    if not admit_request(meta["tenant"], meta["est_out"]):
        return HTTP_429("quota exceeded")
    queue.push((req_id, req.payload, meta))
    return wait_or_stream_response(req_id, req.stream)
```

### Token-aware batcher

```python
def batcher_loop(model, version, priority):
    queue = get_queue(model, version, priority)
    while True:
        first = queue.get()  # blocking
        batch_slots = [first]
        compute_budget = MAX_COMPUTE_UNITS[model]  # tunable
        compute_budget -= weight(first.meta)
        start = now()
        while compute_budget > 0 and (now() - start) < T_PACK_MAX[priority]:
            next_item = queue.peek(timeout=T_PACK_MAX[priority] - (now()-start))
            if not next_item:
                break
            if compatible(next_item, batch_slots) and weight(next_item.meta) <= compute_budget and tenant_slot_limit_not_exceeded(next_item):
                batch_slots.append(queue.get())  # consume
                compute_budget -= weight(next_item.meta)
            else:
                # either incompatible or too heavy — break to avoid starvation.
                break
        submit_batch_to_worker(batch_slots)
```

### Worker inference loop (streaming-aware)

```python
def handle_batch(batch_slots, model):
    # prepare tensors with padding
    inputs, attention_masks, positions = collate_batch(batch_slots)
    kv_caches = [get_or_create_kv_cache(slot.id) for slot in batch_slots]
    # run encoder/initial stages if any
    outputs = model.forward(inputs, attention_masks, kv_caches)
    if batch_is_streaming(batch_slots):
        # autoregressive loop
        while not all_slots_done(batch_slots):
            logits = model.decode_step(kv_caches)
            # sampling / argmax per slot according to its decoding config
            tokens = sample_tokens_per_slot(logits, batch_slots)
            update_kv_caches(kv_caches, tokens)
            send_tokens_back_to_assembler(batch_id, tokens)  # per-slot mapping
            if reached_max_steps or all_slots_eos(): break
    else:
        # for non-streaming full generation, run decode until finished
        full_outputs = model.generate_full(inputs, config)
        send_full_outputs(batch_id, full_outputs)
```

### Response assembler

```python
def assemble_and_respond(event):  # event = tokens from worker
    for slot_id, token in event.tokens_by_slot:
        if connection_alive(slot_id):
            decoded_text = detokenize_incremental(slot_id, token)
            # apply safety filter on delta or on final output
            if safety_block(decoded_text):
                send_blocked_event(slot_id)
                cancel_slot(slot_id)
            else:
                send_chunk_to_client(slot_id, decoded_text, seq_no=next_seq(slot_id))
        else:
            drop_slot_resources(slot_id)
```

# 14 Short checklist / interview talking points

When discussing in an interview, mention:

* The central tradeoff: latency vs throughput → token-aware adaptive batching.
* Per-model benchmarking to set actual batch sizes and compute budgets.
* Multi-tenant fairness: token quotas, per-request admission control, tenant slot caps.
* Streaming semantics: how to emit per-slot deltas and support resume.
* Safety integration inline with streaming (mid-stream stop).
* Model lifecycle: registry, warmup, canary, rollback.
* Observability: tracing across stages and SLO metrics (p95/p99).
* Capacity example: run microbenchmarks, then compute GPUs = ceil(target_rps ÷ rps_per_gpu).
* Failures & tests: OOM mitigation, chaos testing, noisy neighbor simulation.

# Quick interview-ready “one-slide” summary (2–3 sentences)

“Build a multi-tenant inference service with per-model token-aware adaptive batchers and GPU worker pools. Use priority queues and tenant quotas to balance fairness, stream tokens using SSE/gRPC with per-slot KV-caches for decoding, and coordinate deploys via a model registry + canary pipeline. Instrument every stage (pack-time, GPU-time, assemble-time) to meet p95/p99 SLOs and autoscale by pending compute units.”


