package com.wwb.leetcode.other.openai.versionedkvstore;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * PHASE 2: Multi-Threaded Consistency (12-15 minutes)
 * 
 * PROBLEM STATEMENT:
 * The store is now accessed by multiple threads concurrently.
 * Ensure thread safety while maintaining performance.
 * 
 * NEW REQUIREMENTS:
 * 1. Multiple threads can read/write concurrently
 * 2. Queries at a specific timestamp should be linearizable
 * 3. Minimize lock contention (avoid global lock)
 * 
 * CLARIFYING QUESTIONS TO ASK:
 * 1. "What consistency model do we need?"
 *    → Linearizability for same key, eventual consistency across keys OK
 * 2. "Are reads or writes more frequent?"
 *    → Affects whether to use ReadWriteLock vs regular Lock
 * 3. "Can we use lock-free data structures?"
 *    → Yes, but discuss trade-offs (ConcurrentSkipListMap)
 * 4. "What about queries during writes?"
 *    → Reads should not block on writes to different keys
 * 5. "Do we need snapshot isolation across keys?"
 *    → Not required for Phase 2, but good bonus discussion
 * 
 * KEY INSIGHTS:
 * - TreeMap is NOT thread-safe → need synchronization
 * - Global lock is simple but kills concurrency → use per-key locks
 * - ConcurrentHashMap for outer map (no lock needed)
 * - ReadWriteLock per key allows multiple concurrent reads
 * - Must be careful with lock acquisition order to avoid deadlocks
 * 
 * CONCURRENCY STRATEGY:
 * - Outer map: ConcurrentHashMap (thread-safe by default)
 * - Inner map: TreeMap (protected by per-key ReadWriteLock)
 * - Lock granularity: Per key (not global, not per-version)
 * - Lock type: ReentrantReadWriteLock (multiple readers, single writer)
 * 
 * TIME COMPLEXITY (same as Phase 1):
 * - put(key, value, timestamp): O(log V) + lock overhead
 * - get(key, timestamp): O(log V) + lock overhead
 * - get(key): O(log V) + lock overhead
 * 
 * SPACE COMPLEXITY:
 * - O(K × V) for data (unchanged)
 * - O(K) for locks (one per key)
 * 
 * TRADE-OFFS:
 * 
 * | Approach | Concurrency | Memory | Complexity |
 * |----------|-------------|--------|------------|
 * | Global Lock | Poor | Low | Simple |
 * | Per-Key Lock | Good | Medium | Moderate |
 * | Lock-Free (ConcurrentSkipListMap) | Excellent | High | Complex |
 * | Copy-on-Write | Excellent for reads | High | Moderate |
 * 
 * EXTENSIONS FOR LATER PHASES:
 * - Phase 3: Thread-safe serialization (need to lock during serialize)
 * - Bonus: Snapshot isolation, optimistic locking, MVCC
 */
public class Phase2VersionedKVStore {
    
    // Outer map: Use ConcurrentMap interface (ConcurrentHashMap is thread-safe by default)
    // Note: ConcurrentMap preferred over Map for concurrent collections
    private final ConcurrentMap<String, VersionHistory> store;
    
    /**
     * Inner class to encapsulate version history with its lock
     * 
     * DESIGN PATTERN: Each key has its own lock for fine-grained concurrency
     * 
     * INTERVIEW NOTE:
     * We encapsulate TreeMap + Lock together so they can't be separated.
     * This makes it impossible to access TreeMap without acquiring lock.
     */
    private static class VersionHistory {
        private final NavigableMap<Long, String> versions;
        private final ReadWriteLock lock;
        
        VersionHistory() {
            this.versions = new TreeMap<>();
            this.lock = new ReentrantReadWriteLock();
        }
        
        Lock readLock() {
            return lock.readLock();
        }
        
        Lock writeLock() {
            return lock.writeLock();
        }
        
        NavigableMap<Long, String> getVersions() {
            return versions;
        }
    }
    
    /**
     * Initialize an empty thread-safe versioned KV store
     */
    public Phase2VersionedKVStore() {
        // Concrete type: ConcurrentHashMap
        this.store = new ConcurrentHashMap<>();
    }
    
    /**
     * Store a key-value pair at a specific timestamp (thread-safe)
     * 
     * CONCURRENCY:
     * - ConcurrentHashMap.computeIfAbsent is atomic
     * - WriteLock ensures exclusive access to TreeMap
     * - Multiple threads can write to DIFFERENT keys concurrently
     * - Multiple threads writing to SAME key are serialized
     * 
     * Time: O(log V) + lock acquisition
     * 
     * INTERVIEW NOTE:
     * We use writeLock because we're modifying the TreeMap.
     * ReadWriteLock allows multiple readers but only one writer.
     */
    public void put(String key, String value, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        // Get or create VersionHistory (atomic operation)
        VersionHistory history = store.computeIfAbsent(key, k -> new VersionHistory());
        
        // Acquire write lock for this key
        Lock writeLock = history.writeLock();
        writeLock.lock();
        try {
            history.getVersions().put(timestamp, value);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Retrieve value at or before the given timestamp (thread-safe)
     * 
     * CONCURRENCY:
     * - ReadLock allows multiple concurrent readers
     * - Readers don't block other readers
     * - Readers DO block on writers (and vice versa)
     * 
     * Time: O(log V) + lock acquisition
     * 
     * INTERVIEW NOTE:
     * Using readLock allows high read concurrency.
     * If workload is 99% reads, this gives near-linear scaling with cores.
     */
    public String get(String key, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        VersionHistory history = store.get(key);
        if (history == null) {
            return null;
        }
        
        // Acquire read lock for this key
        Lock readLock = history.readLock();
        readLock.lock();
        try {
            NavigableMap<Long, String> versions = history.getVersions();
            Map.Entry<Long, String> entry = versions.floorEntry(timestamp);
            return entry != null ? entry.getValue() : null;
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Retrieve the latest value for a key (thread-safe)
     */
    public String get(String key) {
        validateKey(key);
        
        VersionHistory history = store.get(key);
        if (history == null) {
            return null;
        }
        
        Lock readLock = history.readLock();
        readLock.lock();
        try {
            NavigableMap<Long, String> versions = history.getVersions();
            Map.Entry<Long, String> entry = versions.lastEntry();
            return entry != null ? entry.getValue() : null;
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Get the number of keys stored (thread-safe)
     * 
     * INTERVIEW NOTE:
     * ConcurrentHashMap.size() is thread-safe but may not reflect
     * concurrent modifications (weakly consistent).
     */
    public int getKeyCount() {
        return store.size();
    }
    
    /**
     * Get the number of versions for a specific key (thread-safe)
     */
    public int getVersionCount(String key) {
        VersionHistory history = store.get(key);
        if (history == null) {
            return 0;
        }
        
        Lock readLock = history.readLock();
        readLock.lock();
        try {
            return history.getVersions().size();
        } finally {
            readLock.unlock();
        }
    }
    
    // ============================================================================
    // Validation Helpers
    // ============================================================================
    
    private void validateKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }
    
    private void validateTimestamp(long timestamp) {
        if (timestamp < 0) {
            throw new IllegalArgumentException("Timestamp cannot be negative: " + timestamp);
        }
    }
    
    // ============================================================================
    // Test Cases (Multi-threaded)
    // ============================================================================
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Phase 2: Multi-Threaded Consistency Tests ===\n");
        
        // Test 1: Basic single-threaded (sanity check)
        System.out.println("Test 1: Single-threaded sanity check");
        Phase2VersionedKVStore store = new Phase2VersionedKVStore();
        store.put("user:1", "Alice", 100);
        store.put("user:1", "Alice Smith", 200);
        assert store.get("user:1", 150).equals("Alice") : "Test 1a failed";
        assert store.get("user:1").equals("Alice Smith") : "Test 1b failed";
        System.out.println("  ✓ Basic operations work");
        System.out.println("✓ Test 1 passed\n");
        
        // Test 2: Concurrent writes to DIFFERENT keys
        System.out.println("Test 2: Concurrent writes to different keys");
        final Phase2VersionedKVStore store2 = new Phase2VersionedKVStore();
        int numThreads = 10;
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    String key = "key" + threadId;
                    store2.put(key, "value" + j, j);
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        assert store2.getKeyCount() == numThreads : "Test 2a failed";
        for (int i = 0; i < numThreads; i++) {
            assert store2.getVersionCount("key" + i) == 100 : "Test 2b failed for key" + i;
            assert store2.get("key" + i).equals("value99") : "Test 2c failed";
        }
        System.out.println("  ✓ " + numThreads + " threads wrote to different keys");
        System.out.println("✓ Test 2 passed\n");
        
        // Test 3: Concurrent writes to SAME key
        System.out.println("Test 3: Concurrent writes to same key");
        final Phase2VersionedKVStore store3 = new Phase2VersionedKVStore();
        final String sharedKey = "shared";
        
        threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    long timestamp = threadId * 1000L + j;
                    store3.put(sharedKey, "thread" + threadId + "_value" + j, timestamp);
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        assert store3.getKeyCount() == 1 : "Test 3a failed";
        assert store3.getVersionCount(sharedKey) == 1000 : "Test 3b failed (got " + 
            store3.getVersionCount(sharedKey) + ")";
        System.out.println("  ✓ " + numThreads + " threads wrote to same key (1000 versions)");
        System.out.println("✓ Test 3 passed\n");
        
        // Test 4: Concurrent reads and writes
        System.out.println("Test 4: Concurrent reads and writes");
        final Phase2VersionedKVStore store4 = new Phase2VersionedKVStore();
        store4.put("test", "v0", 0);
        
        final int numReaders = 20;
        final int numWriters = 5;
        threads = new Thread[numReaders + numWriters];
        
        // Start writers
        for (int i = 0; i < numWriters; i++) {
            final int writerId = i;
            threads[i] = new Thread(() -> {
                for (int j = 1; j <= 100; j++) {
                    store4.put("test", "writer" + writerId + "_v" + j, j * 10L);
                    try {
                        Thread.sleep(1); // Slow down to mix with reads
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threads[i].start();
        }
        
        // Start readers
        for (int i = 0; i < numReaders; i++) {
            threads[numWriters + i] = new Thread(() -> {
                for (int j = 0; j < 200; j++) {
                    String value = store4.get("test", j * 5L);
                    assert value != null : "Read should never see null after initial write";
                }
            });
            threads[numWriters + i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("  ✓ " + numReaders + " readers + " + numWriters + 
                         " writers executed concurrently");
        System.out.println("  ✓ No null reads (linearizable)");
        System.out.println("✓ Test 4 passed\n");
        
        // Test 5: Stress test - many keys, many threads
        System.out.println("Test 5: Stress test (50 threads, 100 keys each)");
        final Phase2VersionedKVStore store5 = new Phase2VersionedKVStore();
        final int stressThreads = 50;
        final int keysPerThread = 100;
        
        threads = new Thread[stressThreads];
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < stressThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < keysPerThread; j++) {
                    String key = "stress_" + threadId + "_" + j;
                    store5.put(key, "value", 100);
                    store5.get(key, 100);
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        long duration = System.currentTimeMillis() - startTime;
        int expectedKeys = stressThreads * keysPerThread;
        assert store5.getKeyCount() == expectedKeys : "Test 5 failed: expected " + 
            expectedKeys + " keys, got " + store5.getKeyCount();
        
        System.out.println("  ✓ " + stressThreads + " threads × " + keysPerThread + 
                         " keys = " + expectedKeys + " keys");
        System.out.println("  ✓ Completed in " + duration + "ms");
        System.out.println("  ✓ Throughput: ~" + (expectedKeys * 2 * 1000 / duration) + " ops/sec");
        System.out.println("✓ Test 5 passed\n");
        
        // Test 6: No deadlocks (acquire multiple keys)
        System.out.println("Test 6: No deadlocks with multiple key access");
        final Phase2VersionedKVStore store6 = new Phase2VersionedKVStore();
        store6.put("keyA", "valueA", 100);
        store6.put("keyB", "valueB", 100);
        
        threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    // Access keys in different order (potential for deadlock if not careful)
                    if (j % 2 == 0) {
                        store6.get("keyA", 100);
                        store6.get("keyB", 100);
                    } else {
                        store6.get("keyB", 100);
                        store6.get("keyA", 100);
                    }
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join(); // If deadlock occurs, this will hang
        }
        
        System.out.println("  ✓ No deadlocks detected");
        System.out.println("✓ Test 6 passed\n");
        
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ ALL PHASE 2 TESTS PASSED!");
        System.out.println("════════════════════════════════════════");
        System.out.println("\nKEY TAKEAWAYS:");
        System.out.println("  • ConcurrentHashMap for outer map (thread-safe)");
        System.out.println("  • ReadWriteLock per key (fine-grained concurrency)");
        System.out.println("  • Multiple readers can access same key concurrently");
        System.out.println("  • Writers to different keys don't block each other");
        System.out.println("  • No global lock → scales with number of cores");
        System.out.println("\nREADY FOR PHASE 3: Persistence with Thread Safety");
    }
}

