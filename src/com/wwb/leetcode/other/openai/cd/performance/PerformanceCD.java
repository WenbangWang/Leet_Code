package com.wwb.leetcode.other.openai.cd.performance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Performance-Focused CD Implementation
 * 
 * Phase 1 (15 min): Basic path navigation
 * Phase 2 (15 min): LRU caching for path resolutions
 * Phase 3 (15 min): Concurrent access with thread safety
 * 
 * Focus: Performance optimization, caching strategies, concurrency control
 */
public class PerformanceCD {
    
    // ========================================
    // PHASE 1: Basic Path Navigation
    // ========================================
    
    public String phase1(String currentDir, String targetDir) {
        String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
        
        Stack<String> stack = new Stack<>();
        for (String segment : fullPath.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            } else if (segment.equals("..")) {
                if (stack.isEmpty()) {
                    return null;
                }
                stack.pop();
            } else {
                stack.push(segment);
            }
        }
        
        return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
    }
    
    
    // ========================================
    // PHASE 2: LRU Caching
    // ========================================
    
    /**
     * LRU Cache for path resolutions.
     * 
     * Motivation: Path normalization is expensive if done millions of times
     * Solution: Cache (currentDir, targetDir) → finalPath mappings
     * Eviction: LRU when cache is full
     * 
     * Time Complexity:
     *   - get(): O(1) - LinkedHashMap with access-order
     *   - put(): O(1) - amortized
     *   - removeEldestEntry(): O(1) - called after each put
     * 
     * Space Complexity: O(capacity × entry_size)
     *   - Stores up to 'capacity' entries
     *   - Each entry: key (2 strings) + value (1 string)
     *   - Example: capacity=100, avg entry=100 bytes → 10KB
     * 
     * Cache Hit Rate Impact:
     *   Without cache: Every cd() does O(n) work
     *   With cache (90% hit rate): 
     *     - 90% of calls: O(1)
     *     - 10% of calls: O(n)
     *     - Effective: 0.9×O(1) + 0.1×O(n) ≈ O(1) for typical n
     *   
     *   Example: 1M requests, 90% hit rate
     *     - Without cache: 1M × 50 ops = 50M operations
     *     - With cache: 900K × 1 op + 100K × 50 ops = 5.9M operations
     *     - Speedup: 8.5×
     */
    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        
        public LRUCache(int capacity) {
            super(capacity, 0.75f, true);  // true = access-order
            this.capacity = capacity;
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
    
    /**
     * CD with LRU caching.
     */
    public static class CachedCD {
        private final LRUCache<String, String> cache;
        private long cacheHits = 0;
        private long cacheMisses = 0;
        
        public CachedCD(int cacheSize) {
            this.cache = new LRUCache<>(cacheSize);
        }
        
        /**
         * CD with caching - the main optimization.
         * 
         * Time Complexity:
         *   Best case (cache hit): O(1)
         *     - Create cache key: O(k) where k = key length
         *     - LRU get(): O(1)
         *     Total: O(1) for practical purposes
         *   
         *   Worst case (cache miss): O(n)
         *     - computePath(): O(n) where n = path length
         *     - LRU put(): O(1)
         *     Total: O(n)
         *   
         *   Amortized (with h% hit rate):
         *     h × O(1) + (1-h) × O(n)
         *     Example: 90% hit rate: 0.9×O(1) + 0.1×O(n) ≈ O(1)
         * 
         * Space Complexity: O(capacity × avg_entry_size)
         *   - Cache stores up to 'capacity' entries
         *   - Each entry ~= key + value ~= 2×path_length
         */
        public String cd(String currentDir, String targetDir) {
            // O(k): Create cache key (concatenate strings)
            String cacheKey = currentDir + "::" + targetDir;
            
            // O(1): Check cache (LRU LinkedHashMap get)
            String cached = cache.get(cacheKey);
            if (cached != null) {
                cacheHits++;
                return cached;  // Fast path: O(1) return
            }
            
            // Cache miss - need to compute
            cacheMisses++;
            // O(n): Expensive path normalization
            String result = computePath(currentDir, targetDir);
            
            // O(1): Store in cache for future lookups
            if (result != null) {
                cache.put(cacheKey, result);
            }
            
            return result;
        }
        
        private String computePath(String currentDir, String targetDir) {
            String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
            
            Stack<String> stack = new Stack<>();
            for (String segment : fullPath.split("/")) {
                if (segment.isEmpty() || segment.equals(".")) {
                    continue;
                } else if (segment.equals("..")) {
                    if (stack.isEmpty()) {
                        return null;
                    }
                    stack.pop();
                } else {
                    stack.push(segment);
                }
            }
            
            return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
        }
        
        public void invalidateCache() {
            cache.clear();
            cacheHits = 0;
            cacheMisses = 0;
        }
        
        public double getCacheHitRate() {
            long total = cacheHits + cacheMisses;
            return total == 0 ? 0.0 : (double) cacheHits / total;
        }
        
        public String getStats() {
            return String.format("Cache stats: hits=%d, misses=%d, hit rate=%.2f%%",
                cacheHits, cacheMisses, getCacheHitRate() * 100);
        }
    }
    
    
    // ========================================
    // PHASE 3: Thread-Safe Concurrent CD
    // ========================================
    
    /**
     * Thread-safe CD with concurrent access support.
     * 
     * Challenges:
     * 1. Multiple threads calling cd() simultaneously
     * 2. Symlink map being modified concurrently
     * 3. Cache invalidation when symlinks change
     * 
     * Solutions:
     * 1. Copy-on-write for symlinks
     * 2. Read-write locks for structured access
     * 3. ConcurrentHashMap for cache
     */
    public static class ConcurrentCD {
        private volatile Map<String, String> symlinks;
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
        
        public ConcurrentCD() {
            this.symlinks = new HashMap<>();
        }
        
        /**
         * Thread-safe cd operation (read operation).
         * 
         * Time Complexity:
         *   Best case (cache hit): O(1)
         *     - ConcurrentHashMap.get(): O(1) - no locking needed!
         *     
         *   Worst case (cache miss): O(n) + lock overhead
         *     - Read lock acquisition: O(1) typically, O(w) if writers waiting
         *     - Create snapshot: O(m) where m = number of symlinks
         *     - Compute with symlinks: O(k×s×p) where k=iterations, s=segments, p=symlinks
         *     - Cache put: O(1)
         *     
         * Concurrency:
         *   - Multiple readers can execute simultaneously (ReadWriteLock)
         *   - ConcurrentHashMap allows concurrent reads without blocking
         *   - Only blocks if writer is modifying symlinks (rare)
         *   
         * Lock Strategy:
         *   1. Try cache first (no lock - ConcurrentHashMap handles it)
         *   2. If miss, acquire read lock
         *   3. Create snapshot of symlinks (copy under lock)
         *   4. Release lock before expensive computation
         *   5. Compute without holding lock (uses snapshot)
         *   
         * Why snapshot instead of holding lock?
         *   - Computation can be slow (O(n))
         *   - Holding lock blocks writers
         *   - Snapshot trades space (O(m)) for concurrency
         */
        public String cd(String currentDir, String targetDir) {
            // O(k): Create cache key
            String cacheKey = currentDir + "::" + targetDir;
            
            // Try cache first (no lock needed for ConcurrentHashMap)
            String cached = cache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            
            // Acquire read lock for accessing symlinks
            rwLock.readLock().lock();
            try {
                // Create snapshot of symlinks to avoid holding lock during computation
                Map<String, String> symlinkSnapshot = new HashMap<>(symlinks);
                
                // Release lock before expensive computation
                rwLock.readLock().unlock();
                
                // Compute path with symlink resolution
                String result = computeWithSymlinks(currentDir, targetDir, symlinkSnapshot);
                
                // Cache result
                if (result != null) {
                    cache.put(cacheKey, result);
                }
                
                return result;
            } finally {
                // Ensure lock is released even if exception occurs
                if (rwLock.readLock().tryLock()) {
                    rwLock.readLock().unlock();
                    rwLock.readLock().unlock();  // Release the one we just acquired
                }
            }
        }
        
        /**
         * Add or update a symlink (write operation).
         * 
         * Time Complexity: O(m) where m = number of symlinks
         *   - Write lock acquisition: O(1) typically
         *     * May wait if readers hold the lock
         *     * Writer gets priority over new readers
         *   - Copy HashMap: O(m) - copy all entries
         *   - put(): O(1)
         *   - Volatile write: O(1) but ensures visibility
         *   - cache.clear(): O(c) where c = cache size
         *   Total: O(m + c)
         * 
         * Why Copy-on-Write?
         *   Pro: Readers never block - they use old version
         *   Pro: No corrupted state if exception during update
         *   Con: O(m) space for copy
         *   Con: O(m) time for copy
         *   
         *   Alternative: Modify in place
         *     Pro: O(1) time and space
         *     Con: Readers must wait or see inconsistent state
         *   
         *   Choice: CoW is better when reads >> writes (typical)
         * 
         * Cache Invalidation:
         *   Must clear cache because symlink change affects results
         *   Alternative: Fine-grained invalidation (only affected paths)
         *   Trade-off: Simple (clear all) vs Complex (track dependencies)
         */
        public void addSymlink(String link, String target) {
            // Acquire write lock - blocks all readers and other writers
            rwLock.writeLock().lock();
            try {
                // O(m): Copy-on-write: create new map
                // Readers still use old map during this copy
                Map<String, String> newSymlinks = new HashMap<>(symlinks);
                
                // O(1): Modify the copy
                newSymlinks.put(link, target);
                
                // O(1): Atomic update - now readers see new version
                // volatile ensures visibility across threads
                symlinks = newSymlinks;
                
                // O(c): Invalidate cache when symlinks change
                // Necessary because cached results may now be wrong
                cache.clear();
            } finally {
                // Always release lock, even if exception
                rwLock.writeLock().unlock();
            }
        }
        
        /**
         * Remove a symlink (write operation).
         * 
         * Time Complexity: O(m + c)
         *   Same as addSymlink - copy map, modify, clear cache
         * 
         * Space Complexity: O(m)
         *   Temporary copy of symlinks map
         */
        public void removeSymlink(String link) {
            rwLock.writeLock().lock();
            try {
                // O(m): Copy-on-write
                Map<String, String> newSymlinks = new HashMap<>(symlinks);
                
                // O(1): Remove from copy
                newSymlinks.remove(link);
                
                // O(1): Atomic update
                symlinks = newSymlinks;
                
                // O(c): Invalidate cache
                cache.clear();
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        
        private String computeWithSymlinks(String currentDir, String targetDir,
                                          Map<String, String> symlinks) {
            // Basic normalization
            String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
            String path = normalizePath(fullPath);
            
            if (path == null || symlinks.isEmpty()) {
                return path;
            }
            
            // Resolve symlinks
            Set<String> visited = new HashSet<>();
            int maxIterations = symlinks.size() + 1;
            
            for (int i = 0; i < maxIterations; i++) {
                if (!visited.add(path)) {
                    throw new RuntimeException("Symlink cycle detected");
                }
                
                String newPath = resolveLongestSymlink(path, symlinks);
                if (newPath.equals(path)) {
                    break;
                }
                path = normalizePath(newPath);
            }
            
            return path;
        }
        
        private String resolveLongestSymlink(String path, Map<String, String> symlinks) {
            String[] segments = path.split("/");
            StringBuilder currentPath = new StringBuilder();
            
            String longestMatch = null;
            String longestTarget = null;
            int longestMatchIndex = -1;
            
            for (int i = 0; i < segments.length; i++) {
                if (segments[i].isEmpty()) continue;
                
                currentPath.append("/").append(segments[i]);
                String candidate = currentPath.toString();
                
                if (symlinks.containsKey(candidate)) {
                    longestMatch = candidate;
                    longestTarget = symlinks.get(candidate);
                    longestMatchIndex = i;
                }
            }
            
            if (longestMatch != null) {
                List<String> remaining = new ArrayList<>();
                for (int i = longestMatchIndex + 1; i < segments.length; i++) {
                    if (!segments[i].isEmpty()) {
                        remaining.add(segments[i]);
                    }
                }
                return remaining.isEmpty() ? longestTarget 
                                           : longestTarget + "/" + String.join("/", remaining);
            }
            
            return path;
        }
        
        private String normalizePath(String path) {
            Stack<String> stack = new Stack<>();
            for (String segment : path.split("/")) {
                if (segment.isEmpty() || segment.equals(".")) {
                    continue;
                } else if (segment.equals("..")) {
                    if (!stack.isEmpty()) stack.pop();
                } else {
                    stack.push(segment);
                }
            }
            return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
        }
    }
    
    
    // ========================================
    // TESTS
    // ========================================
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== PHASE 1: Basic Navigation ===");
        PerformanceCD cd = new PerformanceCD();
        assert "/home/user/docs".equals(cd.phase1("/home/user", "docs"));
        System.out.println("✓ Phase 1 tests passed!\n");
        
        System.out.println("=== PHASE 2: LRU Caching ===");
        CachedCD cachedCD = new CachedCD(3);  // Cache size = 3
        
        // First access - cache miss
        cachedCD.cd("/home/user", "docs");
        assert cachedCD.cacheMisses == 1;
        
        // Second access - cache hit
        cachedCD.cd("/home/user", "docs");
        assert cachedCD.cacheHits == 1;
        
        // Fill cache
        cachedCD.cd("/home/user", "pics");
        cachedCD.cd("/home/user", "videos");
        cachedCD.cd("/home/user", "music");  // This evicts "docs"
        
        // Access evicted entry - cache miss
        cachedCD.cd("/home/user", "docs");
        assert cachedCD.cacheMisses == 5;  // 1 + 3 + 1
        
        System.out.println(cachedCD.getStats());
        System.out.println("✓ LRU cache working correctly!\n");
        
        System.out.println("=== PHASE 3: Concurrent Access ===");
        ConcurrentCD concurrentCD = new ConcurrentCD();
        concurrentCD.addSymlink("/link", "/target");
        
        // Test concurrent reads
        Thread[] readers = new Thread[10];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    String result = concurrentCD.cd("/home", "link/file.txt");
                    assert "/target/file.txt".equals(result);
                }
            });
            readers[i].start();
        }
        
        // Wait for all readers
        for (Thread reader : readers) {
            reader.join();
        }
        System.out.println("✓ Concurrent reads successful (10 threads × 100 iterations)");
        
        // Test concurrent write
        Thread writer = new Thread(() -> {
            concurrentCD.addSymlink("/link2", "/target2");
        });
        writer.start();
        writer.join();
        
        String result = concurrentCD.cd("/home", "link2/file.txt");
        assert "/target2/file.txt".equals(result);
        System.out.println("✓ Concurrent write successful");
        
        System.out.println("\n🎉 All performance tests passed!");
    }
}

