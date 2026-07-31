package com.wwb.leetcode.easy;

/**
 * Design a logger system that enforces a 10-second rate limit per unique message. The shouldPrintMessage(timestamp, message) method should return true if the message hasn't been printed in the last 10 seconds, otherwise false. For example, if "foo" is logged at timestamp 1, it can't be logged again until timestamp 11 or later.
 * <p>
 * Input:
 * <p>
 * shouldPrintMessage(1, "foo") → true
 * shouldPrintMessage(2, "bar") → true
 * shouldPrintMessage(3, "foo") → false
 * shouldPrintMessage(8, "bar") → false
 * shouldPrintMessage(10, "foo") → false
 * shouldPrintMessage(11, "foo") → true
 * Output:
 * <p>
 * true, true, false, false, false, true
 * <p>
 * Explanation: "foo" at timestamp 1 blocks until 11. "bar" at timestamp 2 blocks until 12. Requests within the 10-second window return false.
 * <p>
 * Constraints:
 * <p>
 * Timestamps are in seconds and arrive in chronological order
 * Multiple unique messages can be tracked simultaneously
 * Each message has an independent 10-second window
 */
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class No359 {

    // ===== Base solution: single-threaded, window = 10s =====
    private final Map<String, Integer> lastPrinted = new HashMap<>();

    boolean shouldPrintMessage(int timestamp, String message) {
        Integer last = lastPrinted.get(message);
        if (last == null || timestamp - last >= 10) {
            lastPrinted.put(message, timestamp);
            return true;
        }
        return false;
    }

    // ===== Branch A: concurrency =====
    // A1: HashMap -> ConcurrentHashMap alone is not enough — check-then-put is
    // two ops, racy (TOCTOU). Use compute() for an atomic check-and-update.
    private final Map<String, Integer> lastPrintedConcurrent = new ConcurrentHashMap<>();

    boolean shouldPrintMessageConcurrent(int timestamp, String message) {
        AtomicBoolean printed = new AtomicBoolean(false);
        lastPrintedConcurrent.compute(message, (k, last) -> {
            if (last == null || timestamp - last >= 10) {
                printed.set(true);
                return timestamp;
            }
            return last;
        });
        return printed.get();
    }

    // A1a: out-of-order timestamps across threads (chronological-order
    // assumption breaks under concurrency). Guard with Math.max so a late
    // arriving smaller timestamp can't roll the window backward.
    boolean shouldPrintMessageConcurrentOrdered(int timestamp, String message) {
        AtomicBoolean printed = new AtomicBoolean(false);
        lastPrintedConcurrent.compute(message, (k, last) -> {
            if (last == null || timestamp - last >= 10) {
                printed.set(true);
                return Math.max(last == null ? timestamp : last, timestamp);
            }
            return last;
        });
        return printed.get();
    }

    // ===== Branch B: memory / scale =====
    // B1: unbounded key growth. LRU eviction via LinkedHashMap
    // removeEldestEntry — bounds memory when messages are seen once and never
    // repeated.
    private static final int MAX_TRACKED_MESSAGES = 10_000;
    private final Map<String, Integer> lastPrintedLru = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > MAX_TRACKED_MESSAGES;
        }
    };

    // B1a: lazy expiration — sweep opportunistically on read instead of a
    // background thread. simpl: full scan per call — switch to a min-heap
    // keyed by expiry if MAX_TRACKED_MESSAGES grows large enough that this
    // scan becomes the bottleneck.
    boolean shouldPrintMessageLazyEvict(int timestamp, String message) {
        lastPrintedLru.entrySet().removeIf(e -> timestamp - e.getValue() >= 10);
        Integer last = lastPrintedLru.get(message);
        if (last == null) {
            lastPrintedLru.put(message, timestamp);
            return true;
        }
        return false;
    }

    // B1a upgrade: min-heap keyed by expiry instead of a full scan per call.
    // Only pop entries that are actually expired — amortized O(1) per call
    // instead of O(n) per call.
    private record Expiry(int expiresAt, String message) {
    }

    // Sharded to raise throughput beyond a single global lock: each shard
    // owns its own map+heap+lock, so unrelated messages (different shard)
    // never contend. Message routed to a shard by hash — same message always
    // lands on the same shard, so its own history stays consistent.
    private static final int SHARD_COUNT = 16;
    private final Object[] shardLocks = new Object[SHARD_COUNT];
    private final Map<String, Integer>[] shardMaps = new Map[SHARD_COUNT];
    private final PriorityQueue<Expiry>[] shardHeaps = new PriorityQueue[SHARD_COUNT];

    {
        for (int i = 0; i < SHARD_COUNT; i++) {
            shardLocks[i] = new Object();
            shardMaps[i] = new HashMap<>();
            shardHeaps[i] = new PriorityQueue<>(Comparator.comparingInt(e -> e.expiresAt));
        }
    }

    private int shardFor(String message) {
        return (message.hashCode() & Integer.MAX_VALUE) % SHARD_COUNT;
    }

    boolean shouldPrintMessageSharded(int timestamp, String message) {
        int shard = shardFor(message);
        Object lock = shardLocks[shard];
        Map<String, Integer> map = shardMaps[shard];
        PriorityQueue<Expiry> heap = shardHeaps[shard];
        synchronized (lock) {
            while (!heap.isEmpty() && heap.peek().expiresAt() <= timestamp) {
                Expiry expired = heap.poll();
                map.remove(expired.message(), expired.expiresAt() - 10);
            }
            Integer last = map.get(message);
            if (last == null || timestamp - last >= 10) {
                map.put(message, timestamp);
                heap.add(new Expiry(timestamp + 10, message));
                return true;
            }
            return false;
        }
    }

    // B1b: distributed across servers — in-memory map doesn't share state
    // across instances. Redis SET key val PX 10000 NX is an atomic
    // set-if-absent-with-TTL, replacing map+check+put in one round trip.
    // Stub only — needs a real Redis client.
    interface DistributedRateLimiter {
        boolean shouldPrintMessage(int timestamp, String message);
    }

    // ===== Branch C: requirement variations =====
    // C1: configurable window per call — just add a param, no new abstraction.
    boolean shouldPrintMessage(int timestamp, String message, int windowSeconds) {
        Integer last = lastPrinted.get(message);
        if (last == null || timestamp - last >= windowSeconds) {
            lastPrinted.put(message, timestamp);
            return true;
        }
        return false;
    }

    // C2: "at most N messages per T seconds" is a different algorithm family
    // (sliding window log), not a tweak of the single-timestamp map — need the
    // full history per key, not just the last timestamp, to count how many
    // fall inside the current window.
    private final Map<String, Deque<Integer>> messageTimestamps = new HashMap<>();

    boolean allow(int timestamp, String message, int maxCount, int windowSeconds) {
        Deque<Integer> timestamps = messageTimestamps.computeIfAbsent(message, k -> new ArrayDeque<>());
        while (!timestamps.isEmpty() && timestamp - timestamps.peekFirst() >= windowSeconds) {
            timestamps.pollFirst();
        }
        if (timestamps.size() < maxCount) {
            timestamps.addLast(timestamp);
            return true;
        }
        return false;
    }

}
