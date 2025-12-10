package com.wwb.leetcode.other.openai.versionedkvstore;

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * PHASE 2 (ALTERNATIVE): Lock-Free Multi-Threaded Consistency
 * 
 * PROBLEM STATEMENT:
 * Same as Phase2VersionedKVStore, but using lock-free concurrent collections
 * instead of manual locking.
 * 
 * KEY DIFFERENCE FROM LOCK-BASED VERSION:
 * - Uses ConcurrentSkipListMap instead of TreeMap + ReadWriteLock
 * - No VersionHistory wrapper class needed
 * - No explicit lock management
 * - Simpler code, better throughput under high contention
 * 
 * DESIGN CHOICE: ConcurrentSkipListMap
 * - Implements ConcurrentNavigableMap (thread-safe sorted map)
 * - Lock-free using CAS (Compare-And-Swap) operations
 * - Supports concurrent reads and writes
 * - Has floorEntry(), lastEntry() like TreeMap
 * 
 * TRADE-OFFS vs LOCK-BASED:
 * 
 * | Aspect | Lock-Based (TreeMap+Lock) | Lock-Free (ConcurrentSkipListMap) |
 * |--------|--------------------------|-----------------------------------|
 * | Concurrency | Lock-based | Lock-free (CAS) |
 * | Code | More complex | Simpler |
 * | Memory | Lower | ~2x overhead |
 * | Single-thread | Faster | Slightly slower |
 * | High contention | Can block | Better throughput |
 * | Deadlock risk | Yes | No |
 * 
 * WHEN TO USE THIS VERSION:
 * ✅ High write contention (many threads updating same key)
 * ✅ Simplicity preferred
 * ✅ Can afford 2x memory overhead
 * ✅ Want lock-free guarantee
 * ✅ Production code (less error-prone)
 * 
 * WHEN TO USE LOCK-BASED VERSION:
 * ✅ Read-heavy workload (99% reads, 1% writes)
 * ✅ Memory constrained
 * ✅ Single-threaded or low contention
 * ✅ Want to demonstrate locking knowledge in interview
 * 
 * TIME COMPLEXITY (same as lock-based):
 * - put: O(log V) where V = versions per key
 * - get: O(log V)
 * 
 * SPACE COMPLEXITY:
 * - O(K × V × 2) where K = keys, V = avg versions, 2 = skip list overhead
 * 
 * INTERVIEW NOTE:
 * Both implementations are valid. This one is simpler and often preferred
 * in production. The lock-based version shows deeper understanding of
 * concurrency primitives, which is valuable in interviews.
 */
public class Phase2VersionedKVStore_LockFree {
    
    // Outer: ConcurrentHashMap (thread-safe by default)
    // Inner: ConcurrentSkipListMap (lock-free sorted map)
    // No need for VersionHistory wrapper - ConcurrentSkipListMap is already thread-safe!
    // Use most general interfaces: Map and NavigableMap
    private final Map<String, NavigableMap<Long, String>> store;
    
    /**
     * Initialize an empty lock-free versioned KV store
     * 
     * INTERVIEW NOTE:
     * Following "program to interfaces" pattern (same as No981.java):
     * - Declaration: Map, NavigableMap (most general interfaces for our needs)
     * - Instantiation: ConcurrentHashMap, ConcurrentSkipListMap (concrete thread-safe types)
     * 
     * Thread-safety comes from CONCRETE TYPES, not interfaces!
     * - Map interface doesn't guarantee thread-safety, but ConcurrentHashMap does
     * - NavigableMap interface doesn't guarantee thread-safety, but ConcurrentSkipListMap does
     */
    public Phase2VersionedKVStore_LockFree() {
        this.store = new ConcurrentHashMap<>();
    }
    
    /**
     * Store a key-value pair at a specific timestamp (lock-free)
     * 
     * CONCURRENCY:
     * - ConcurrentHashMap.computeIfAbsent is atomic
     * - ConcurrentSkipListMap.put is lock-free (uses CAS)
     * - Multiple threads can write to same key concurrently
     * - No explicit locks needed!
     * 
     * Time: O(log V) where V = versions for this key
     * 
     * INTERVIEW NOTE:
     * Much simpler than lock-based version - no lock acquisition,
     * no try-finally blocks, no deadlock concerns.
     */
    public void put(String key, String value, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        // No locks needed! Both computeIfAbsent and put are thread-safe
        store.computeIfAbsent(key, k -> new ConcurrentSkipListMap<>())
             .put(timestamp, value);
    }
    
    /**
     * Retrieve value at or before the given timestamp (lock-free)
     * 
     * CONCURRENCY:
     * - ConcurrentSkipListMap.floorEntry is lock-free
     * - Returns consistent snapshot at time of call
     * - Concurrent modifications don't affect this read
     * 
     * Time: O(log V)
     * 
     * INTERVIEW NOTE:
     * floorEntry() in ConcurrentSkipListMap is atomic - it returns
     * a weakly consistent snapshot. No locks, no blocking.
     */
    public String get(String key, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        NavigableMap<Long, String> versions = store.get(key);
        if (versions == null) {
            return null;
        }
        
        // No locks needed! floorEntry is atomic in ConcurrentSkipListMap
        Map.Entry<Long, String> entry = versions.floorEntry(timestamp);
        return entry != null ? entry.getValue() : null;
    }
    
    /**
     * Retrieve the latest value for a key (lock-free)
     */
    public String get(String key) {
        validateKey(key);
        
        NavigableMap<Long, String> versions = store.get(key);
        if (versions == null) {
            return null;
        }
        
        // No locks needed! lastEntry is atomic
        Map.Entry<Long, String> entry = versions.lastEntry();
        return entry != null ? entry.getValue() : null;
    }
    
    /**
     * Get the number of keys stored (weakly consistent)
     * 
     * INTERVIEW NOTE:
     * ConcurrentHashMap.size() provides weakly consistent view.
     * May not reflect concurrent modifications, but that's acceptable.
     */
    public int getKeyCount() {
        return store.size();
    }
    
    /**
     * Get the number of versions for a specific key (weakly consistent)
     */
    public int getVersionCount(String key) {
        NavigableMap<Long, String> versions = store.get(key);
        return versions != null ? versions.size() : 0;
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
    // Test Cases (same as lock-based version)
    // ============================================================================
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Phase 2 (Lock-Free): Multi-Threaded Consistency Tests ===\n");
        
        // Test 1: Basic single-threaded (sanity check)
        System.out.println("Test 1: Single-threaded sanity check");
        Phase2VersionedKVStore_LockFree store = new Phase2VersionedKVStore_LockFree();
        store.put("user:1", "Alice", 100);
        store.put("user:1", "Alice Smith", 200);
        assert store.get("user:1", 150).equals("Alice") : "Test 1a failed";
        assert store.get("user:1").equals("Alice Smith") : "Test 1b failed";
        System.out.println("  ✓ Basic operations work");
        System.out.println("✓ Test 1 passed\n");
        
        // Test 2: Concurrent writes to DIFFERENT keys
        System.out.println("Test 2: Concurrent writes to different keys");
        final Phase2VersionedKVStore_LockFree store2 = new Phase2VersionedKVStore_LockFree();
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
        final Phase2VersionedKVStore_LockFree store3 = new Phase2VersionedKVStore_LockFree();
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
        final Phase2VersionedKVStore_LockFree store4 = new Phase2VersionedKVStore_LockFree();
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
                        Thread.sleep(1);
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
        System.out.println("  ✓ No null reads (lock-free consistency)");
        System.out.println("✓ Test 4 passed\n");
        
        // Test 5: Stress test - many keys, many threads
        System.out.println("Test 5: Stress test (50 threads, 100 keys each)");
        final Phase2VersionedKVStore_LockFree store5 = new Phase2VersionedKVStore_LockFree();
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
        final Phase2VersionedKVStore_LockFree store6 = new Phase2VersionedKVStore_LockFree();
        store6.put("keyA", "valueA", 100);
        store6.put("keyB", "valueB", 100);
        
        threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    // Access keys in different order - no deadlock possible with lock-free!
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
            t.join();
        }
        
        System.out.println("  ✓ No deadlocks (lock-free guarantee)");
        System.out.println("✓ Test 6 passed\n");
        
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ ALL PHASE 2 (LOCK-FREE) TESTS PASSED!");
        System.out.println("════════════════════════════════════════");
        System.out.println("\nKEY TAKEAWAYS:");
        System.out.println("  • ConcurrentSkipListMap is lock-free (CAS-based)");
        System.out.println("  • No explicit lock management needed");
        System.out.println("  • Simpler code than lock-based version");
        System.out.println("  • Better throughput under high contention");
        System.out.println("  • ~2x memory overhead vs TreeMap");
        System.out.println("  • Deadlock impossible (no locks!)");
        System.out.println("\nCOMPARISON WITH LOCK-BASED:");
        System.out.println("  Lock-based: More complex, lower memory, shows locking knowledge");
        System.out.println("  Lock-free: Simpler, higher throughput, production-preferred");
    }
}

