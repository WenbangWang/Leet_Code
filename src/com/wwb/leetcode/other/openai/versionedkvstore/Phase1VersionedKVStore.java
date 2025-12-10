package com.wwb.leetcode.other.openai.versionedkvstore;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * PHASE 1: Basic Versioned KV Store (15-20 minutes)
 * 
 * PROBLEM STATEMENT:
 * Design an in-memory versioned key-value store that maintains history of all updates.
 * Support time-travel queries to retrieve values at any point in time.
 * 
 * API REQUIREMENTS:
 * - put(key, value, timestamp): Store a key-value pair at a specific timestamp
 * - get(key, timestamp): Retrieve value at or before the given timestamp
 * - get(key): Retrieve the latest value for the key
 * 
 * CLARIFYING QUESTIONS TO ASK:
 * 1. "Are timestamps guaranteed to be monotonically increasing?" 
 *    → No, out-of-order updates are possible
 * 2. "What if the same key is updated at the same timestamp?" 
 *    → Latest write wins (overwrite)
 * 3. "Should we handle null/empty keys?" 
 *    → Validate inputs, throw exception
 * 4. "What's the expected scale?" 
 *    → Single-machine, in-memory for Phase 1
 * 5. "What if we query a timestamp before any writes?" 
 *    → Return null
 * 6. "Do we need to track who made the update?" 
 *    → Not in Phase 1
 * 
 * KEY INSIGHTS:
 * - Use Map<String, NavigableMap<Long, String>> for efficient time-travel
 * - TreeMap provides O(log V) lookups with floorEntry() for "at or before" semantics
 * - Each key maintains independent version history
 * - NavigableMap automatically handles out-of-order inserts
 * 
 * TIME COMPLEXITY:
 * - put(key, value, timestamp): O(log V) where V = versions per key
 * - get(key, timestamp): O(log V) using floorEntry()
 * - get(key): O(log V) using lastEntry()
 * 
 * SPACE COMPLEXITY:
 * - O(K × V) where K = number of keys, V = average versions per key
 * 
 * COMMON PITFALLS:
 * - Using List instead of TreeMap → O(n) lookups
 * - Not handling out-of-order timestamps
 * - Forgetting edge cases (no history, future queries)
 * - Not validating inputs
 * 
 * EXTENSIONS FOR LATER PHASES:
 * - Phase 2: Persistence with multi-file serialization
 * - Phase 3: Multi-threaded consistency
 * - Phase 4: Distributed versioning or garbage collection
 */
public class Phase1VersionedKVStore {
    
    // Map from key to its version history (timestamp → value)
    // Use interface types for declarations (Map, NavigableMap)
    private final Map<String, NavigableMap<Long, String>> store;
    
    /**
     * Initialize an empty versioned KV store
     */
    public Phase1VersionedKVStore() {
        // Use concrete types for instantiation (HashMap, TreeMap)
        this.store = new HashMap<>();
    }
    
    /**
     * Store a key-value pair at a specific timestamp
     * 
     * @param key The key (must not be null or empty)
     * @param value The value (can be null)
     * @param timestamp The timestamp (must be non-negative)
     * @throws IllegalArgumentException if key is invalid or timestamp is negative
     * 
     * Time: O(log V) where V = versions for this key
     * Space: O(1) amortized
     * 
     * INTERVIEW NOTE:
     * TreeMap automatically maintains sorted order, so out-of-order
     * inserts are handled correctly without additional logic.
     */
    public void put(String key, String value, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        // computeIfAbsent creates a new TreeMap if key doesn't exist
        // TreeMap maintains timestamps in sorted order
        store.computeIfAbsent(key, k -> new TreeMap<>()).put(timestamp, value);
    }
    
    /**
     * Retrieve value at or before the given timestamp
     * 
     * @param key The key to look up
     * @param timestamp The query timestamp
     * @return Value at or before timestamp, or null if no such version exists
     * @throws IllegalArgumentException if key is invalid or timestamp is negative
     * 
     * Time: O(log V) where V = versions for this key
     * Space: O(1)
     * 
     * INTERVIEW NOTE:
     * floorEntry() is the key method - it finds the greatest entry
     * whose key is <= the given timestamp. This is exactly "at or before" semantics.
     */
    public String get(String key, long timestamp) {
        validateKey(key);
        validateTimestamp(timestamp);
        
        NavigableMap<Long, String> versions = store.get(key);
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        
        // floorEntry finds the largest timestamp <= query timestamp
        Map.Entry<Long, String> entry = versions.floorEntry(timestamp);
        return entry != null ? entry.getValue() : null;
    }
    
    /**
     * Retrieve the latest value for a key
     * 
     * @param key The key to look up
     * @return The most recent value, or null if key doesn't exist
     * @throws IllegalArgumentException if key is invalid
     * 
     * Time: O(log V) where V = versions for this key
     * Space: O(1)
     * 
     * INTERVIEW NOTE:
     * lastEntry() gets the entry with the largest timestamp.
     * Alternative: could cache "latest" separately for O(1) access if this is hot path.
     */
    public String get(String key) {
        validateKey(key);
        
        NavigableMap<Long, String> versions = store.get(key);
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        
        Map.Entry<Long, String> entry = versions.lastEntry();
        return entry != null ? entry.getValue() : null;
    }
    
    /**
     * Get the number of keys stored (useful for testing)
     */
    public int getKeyCount() {
        return store.size();
    }
    
    /**
     * Get the number of versions for a specific key (useful for testing)
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
    // Test Cases
    // ============================================================================
    
    public static void main(String[] args) {
        System.out.println("=== Phase 1: Basic Versioned KV Store Tests ===\n");
        
        Phase1VersionedKVStore store = new Phase1VersionedKVStore();
        
        // Test 1: Basic put and get
        System.out.println("Test 1: Basic put and get");
        store.put("user:1", "Alice", 100);
        assert store.get("user:1", 100).equals("Alice") : "Test 1a failed";
        assert store.get("user:1").equals("Alice") : "Test 1b failed";
        System.out.println("  put('user:1', 'Alice', 100)");
        System.out.println("  get('user:1', 100) = " + store.get("user:1", 100));
        System.out.println("  get('user:1') = " + store.get("user:1"));
        System.out.println("✓ Test 1 passed\n");
        
        // Test 2: Time-travel query (at or before semantics)
        System.out.println("Test 2: Time-travel query");
        store.put("user:1", "Alice Smith", 200);
        store.put("user:1", "Alice Johnson", 300);
        assert store.get("user:1", 150).equals("Alice") : "Test 2a failed";
        assert store.get("user:1", 250).equals("Alice Smith") : "Test 2b failed";
        assert store.get("user:1", 300).equals("Alice Johnson") : "Test 2c failed";
        assert store.get("user:1", 350).equals("Alice Johnson") : "Test 2d failed";
        System.out.println("  put('user:1', 'Alice Smith', 200)");
        System.out.println("  put('user:1', 'Alice Johnson', 300)");
        System.out.println("  get('user:1', 150) = " + store.get("user:1", 150));
        System.out.println("  get('user:1', 250) = " + store.get("user:1", 250));
        System.out.println("  get('user:1', 300) = " + store.get("user:1", 300));
        System.out.println("  get('user:1', 350) = " + store.get("user:1", 350));
        System.out.println("✓ Test 2 passed\n");
        
        // Test 3: Query before any writes
        System.out.println("Test 3: Query before any writes");
        assert store.get("user:1", 50) == null : "Test 3a failed";
        assert store.get("nonexistent") == null : "Test 3b failed";
        System.out.println("  get('user:1', 50) = " + store.get("user:1", 50));
        System.out.println("  get('nonexistent') = " + store.get("nonexistent"));
        System.out.println("✓ Test 3 passed\n");
        
        // Test 4: Out-of-order timestamps
        System.out.println("Test 4: Out-of-order timestamps");
        store = new Phase1VersionedKVStore();
        store.put("config", "v3", 300);
        store.put("config", "v1", 100);
        store.put("config", "v2", 200);
        assert store.get("config", 150).equals("v1") : "Test 4a failed";
        assert store.get("config", 250).equals("v2") : "Test 4b failed";
        assert store.get("config", 350).equals("v3") : "Test 4c failed";
        System.out.println("  put('config', 'v3', 300) [out of order]");
        System.out.println("  put('config', 'v1', 100)");
        System.out.println("  put('config', 'v2', 200)");
        System.out.println("  get('config', 150) = " + store.get("config", 150));
        System.out.println("  get('config', 250) = " + store.get("config", 250));
        System.out.println("  get('config', 350) = " + store.get("config", 350));
        System.out.println("✓ Test 4 passed\n");
        
        // Test 5: Same timestamp overwrite
        System.out.println("Test 5: Same timestamp overwrite");
        store = new Phase1VersionedKVStore();
        store.put("key", "value1", 100);
        store.put("key", "value2", 100);
        assert store.get("key", 100).equals("value2") : "Test 5 failed";
        assert store.getVersionCount("key") == 1 : "Test 5 version count failed";
        System.out.println("  put('key', 'value1', 100)");
        System.out.println("  put('key', 'value2', 100) [overwrite]");
        System.out.println("  get('key', 100) = " + store.get("key", 100));
        System.out.println("  version count = " + store.getVersionCount("key"));
        System.out.println("✓ Test 5 passed\n");
        
        // Test 6: Multiple keys independence
        System.out.println("Test 6: Multiple keys independence");
        store = new Phase1VersionedKVStore();
        store.put("user:1", "Alice", 100);
        store.put("user:2", "Bob", 100);
        store.put("user:1", "Alice2", 200);
        assert store.get("user:1", 150).equals("Alice") : "Test 6a failed";
        assert store.get("user:2", 150).equals("Bob") : "Test 6b failed";
        assert store.get("user:1").equals("Alice2") : "Test 6c failed";
        assert store.get("user:2").equals("Bob") : "Test 6d failed";
        System.out.println("  put('user:1', 'Alice', 100)");
        System.out.println("  put('user:2', 'Bob', 100)");
        System.out.println("  put('user:1', 'Alice2', 200)");
        System.out.println("  get('user:1', 150) = " + store.get("user:1", 150));
        System.out.println("  get('user:2', 150) = " + store.get("user:2", 150));
        System.out.println("✓ Test 6 passed\n");
        
        // Test 7: Null value handling
        System.out.println("Test 7: Null value handling");
        store = new Phase1VersionedKVStore();
        store.put("key", null, 100);
        assert store.get("key", 100) == null : "Test 7 failed";
        System.out.println("  put('key', null, 100)");
        System.out.println("  get('key', 100) = " + store.get("key", 100));
        System.out.println("✓ Test 7 passed\n");
        
        // Test 8: Input validation
        System.out.println("Test 8: Input validation");
        store = new Phase1VersionedKVStore();
        boolean caught1 = false, caught2 = false, caught3 = false;
        try {
            store.put(null, "value", 100);
        } catch (IllegalArgumentException e) {
            caught1 = true;
            System.out.println("  ✓ Caught null key: " + e.getMessage());
        }
        try {
            store.put("", "value", 100);
        } catch (IllegalArgumentException e) {
            caught2 = true;
            System.out.println("  ✓ Caught empty key: " + e.getMessage());
        }
        try {
            store.put("key", "value", -1);
        } catch (IllegalArgumentException e) {
            caught3 = true;
            System.out.println("  ✓ Caught negative timestamp: " + e.getMessage());
        }
        assert caught1 && caught2 && caught3 : "Test 8 failed";
        System.out.println("✓ Test 8 passed\n");
        
        // Test 9: Large version history
        System.out.println("Test 9: Large version history");
        store = new Phase1VersionedKVStore();
        for (int i = 0; i < 1000; i++) {
            store.put("key", "v" + i, i * 10);
        }
        assert store.getVersionCount("key") == 1000 : "Test 9a failed";
        assert store.get("key", 5555).equals("v555") : "Test 9b failed";
        assert store.get("key", 9999).equals("v999") : "Test 9c failed";
        System.out.println("  Added 1000 versions");
        System.out.println("  version count = " + store.getVersionCount("key"));
        System.out.println("  get('key', 5555) = " + store.get("key", 5555));
        System.out.println("  get('key', 9999) = " + store.get("key", 9999));
        System.out.println("✓ Test 9 passed\n");
        
        // Test 10: Edge case - timestamp at exact boundaries
        System.out.println("Test 10: Exact timestamp boundaries");
        store = new Phase1VersionedKVStore();
        store.put("key", "v1", 100);
        store.put("key", "v2", 200);
        assert store.get("key", 99) == null : "Test 10a failed";
        assert store.get("key", 100).equals("v1") : "Test 10b failed";
        assert store.get("key", 199).equals("v1") : "Test 10c failed";
        assert store.get("key", 200).equals("v2") : "Test 10d failed";
        System.out.println("  put('key', 'v1', 100)");
        System.out.println("  put('key', 'v2', 200)");
        System.out.println("  get('key', 99) = " + store.get("key", 99));
        System.out.println("  get('key', 100) = " + store.get("key", 100));
        System.out.println("  get('key', 199) = " + store.get("key", 199));
        System.out.println("  get('key', 200) = " + store.get("key", 200));
        System.out.println("✓ Test 10 passed\n");
        
        System.out.println("════════════════════════════════════════");
        System.out.println("✅ ALL PHASE 1 TESTS PASSED!");
        System.out.println("════════════════════════════════════════");
        System.out.println("\nKEY TAKEAWAYS:");
        System.out.println("  • TreeMap provides O(log n) time-travel queries");
        System.out.println("  • floorEntry() implements 'at or before' semantics");
        System.out.println("  • Out-of-order inserts handled automatically");
        System.out.println("  • Each key maintains independent version history");
        System.out.println("\nREADY FOR PHASE 2: Persistence & Serialization");
    }
}

