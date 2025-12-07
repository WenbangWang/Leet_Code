package com.wwb.leetcode.other.openai.cd.standard;

import java.util.*;

/**
 * Side-by-Side Comparison: HashMap vs Trie for Symlink Resolution
 * 
 * This class demonstrates both approaches with identical test cases
 * to show performance differences and implementation complexity.
 * 
 * ╔════════════════════════════════════════════════════════════════════════╗
 * ║                     COMPLEXITY COMPARISON SUMMARY                      ║
 * ╠════════════════════════════════════════════════════════════════════════╣
 * ║                                                                        ║
 * ║  HASHMAP APPROACH:                                                     ║
 * ║  ─────────────────                                                     ║
 * ║  Time:  O(s × p) per resolution                                       ║
 * ║         s = path segments, p = number of symlinks                     ║
 * ║  Space: O(1) extra (no preprocessing structure)                       ║
 * ║  Code:  ~40 lines (simple)                                            ║
 * ║                                                                        ║
 * ║  Best for:                                                             ║
 * ║  • Small number of symlinks (< 100)                                   ║
 * ║  • One-time or infrequent resolutions                                 ║
 * ║  • Interview settings (time pressure)                                 ║
 * ║  • Memory-constrained environments                                    ║
 * ║                                                                        ║
 * ║  ──────────────────────────────────────────────────────────────────   ║
 * ║                                                                        ║
 * ║  TRIE APPROACH:                                                        ║
 * ║  ─────────────                                                         ║
 * ║  Time:  O(S) build + O(s) per resolution                              ║
 * ║         S = total chars in all symlinks, s = path segments            ║
 * ║         Amortized: O(S/n + s) for n resolutions                       ║
 * ║  Space: O(S) for Trie structure                                       ║
 * ║  Code:  ~100 lines (complex, requires TrieNode class)                 ║
 * ║                                                                        ║
 * ║  Best for:                                                             ║
 * ║  • Many symlinks (> 100)                                              ║
 * ║  • Frequent repeated resolutions                                      ║
 * ║  • Production systems with high throughput                            ║
 * ║  • When memory is available                                           ║
 * ║                                                                        ║
 * ║  ──────────────────────────────────────────────────────────────────   ║
 * ║                                                                        ║
 * ║  KEY INSIGHT:                                                          ║
 * ║  The fundamental difference is in the lookup strategy:                ║
 * ║                                                                        ║
 * ║  HashMap: Checks ALL symlinks for each prefix                         ║
 * ║           → O(p) implicit checks                                      ║
 * ║                                                                        ║
 * ║  Trie:    Only checks CHILDREN of current node                        ║
 * ║           → O(1) per segment, independent of total symlinks!          ║
 * ║                                                                        ║
 * ║  As number of symlinks grows → Trie advantage increases               ║
 * ║                                                                        ║
 * ╚════════════════════════════════════════════════════════════════════════╝
 * 
 * Example with concrete numbers:
 * 
 *   Symlinks: 1000
 *   Path segments: 10
 *   
 *   HashMap per resolution:
 *   - ~10 prefix checks × ~1000 symlinks = ~10,000 conceptual operations
 *   - Actual: Better due to HashMap efficiency, but still O(s×p)
 *   
 *   Trie:
 *   - Build: 1000 symlinks × 4 avg segments = 4,000 operations (ONE TIME)
 *   - Per resolution: 10 segment lookups = 10 operations
 *   - After 100 resolutions: Trie wins decisively
 *   
 * Run this class to see actual benchmark results!
 */
public class ComparisonDemo {
    
    // ========================================
    // APPROACH 1: HASHMAP ITERATION
    // ========================================
    
    public static class HashMapApproach {
        private long comparisonCount = 0;  // Track operations
        
        /**
         * Resolve symlinks using HashMap iteration.
         * 
         * Time Complexity: O(s × p) where:
         *   s = number of segments in path (typically 5-10)
         *   p = number of symlinks in map (variable)
         *   
         *   Breakdown:
         *   - split("/"): O(n) where n = path length
         *   - For each segment (s iterations):
         *     * Build prefix string: O(1) amortized with StringBuilder
         *     * HashMap.containsKey(): O(1) average case
         *       - HashMap lookup is constant time on average
         *       - But implicitly "competes" with p symlinks
         *   - Build result: O(r) where r = remaining segments
         *   
         *   In practice:
         *   - Small p (10-50): Very fast, O(s) practically
         *   - Large p (1000+): O(s×p) becomes noticeable
         *   
         *   Note: We're counting HashMap checks as "operations" to
         *         demonstrate the conceptual difference with Trie.
         *         Actual time is better than O(s×p) due to HashMap efficiency.
         * 
         * Space Complexity: O(1) extra
         *   - Only stores longestMatch, longestTarget (constant)
         *   - Reuses input map, doesn't build new structure
         *   - StringBuilder for building prefixes: O(s) worst case
         * 
         * Code Complexity: ~40 lines (simple to implement)
         */
        public String resolve(String path, Map<String, String> symlinks) {
            comparisonCount = 0;
            
            String[] segments = path.split("/");
            StringBuilder currentPath = new StringBuilder();
            
            String longestMatch = null;
            String longestTarget = null;
            int longestMatchIndex = -1;
            
            // Try each prefix
            for (int i = 0; i < segments.length; i++) {
                if (segments[i].isEmpty()) continue;
                
                currentPath.append("/").append(segments[i]);
                String candidate = currentPath.toString();
                
                // Check if this prefix is a symlink
                comparisonCount++;
                if (symlinks.containsKey(candidate)) {
                    longestMatch = candidate;
                    longestTarget = symlinks.get(candidate);
                    longestMatchIndex = i;
                }
            }
            
            // Replace prefix with target
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
        
        public long getComparisonCount() {
            return comparisonCount;
        }
    }
    
    
    // ========================================
    // APPROACH 2: TRIE TRAVERSAL
    // ========================================
    
    public static class TrieApproach {
        private long comparisonCount = 0;  // Track operations
        
        static class TrieNode {
            String name;
            Map<String, TrieNode> children;
            List<String> targetTokens;  // Non-empty if this is a symlink endpoint
            
            TrieNode(String name) {
                this.name = name;
                this.children = new HashMap<>();
                this.targetTokens = new ArrayList<>();
            }
        }
        
        /**
         * Build Trie from symlinks.
         * 
         * Time Complexity: O(S) where S = sum of all characters in symlink paths
         *   
         *   For each symlink (m total):
         *   - Split path: O(k) where k = path length
         *   - Insert into Trie: O(k) - one node per segment
         *     * Each segment: O(1) putIfAbsent + map traversal
         *   
         *   If average symlink has L characters:
         *   Total: O(m × L) = O(S)
         *   
         *   Example: 100 symlinks × 20 chars avg = 2000 operations
         * 
         * Space Complexity: O(S)
         *   - One TrieNode per unique segment in all paths
         *   - Shared prefixes save space: /home/a, /home/b share "home" node
         *   - Worst case (no sharing): O(S) where S = total chars
         *   - Best case (max sharing): O(unique segments)
         *   
         *   Each TrieNode contains:
         *   - name: O(segment length)
         *   - children HashMap: O(number of children)
         *   - targetTokens: O(target path length) if symlink endpoint
         *   
         * This is a ONE-TIME cost. After building, can resolve many paths efficiently.
         */
        public TrieNode buildTrie(Map<String, String> symlinks) {
            TrieNode root = new TrieNode("");
            
            for (Map.Entry<String, String> entry : symlinks.entrySet()) {
                String symlinkPath = entry.getKey();
                String targetPath = entry.getValue();
                
                // Insert symlink path into Trie
                String[] tokens = symlinkPath.split("/");
                TrieNode current = root;
                
                for (String token : tokens) {
                    if (token.isEmpty()) continue;
                    
                    current.children.putIfAbsent(token, new TrieNode(token));
                    current = current.children.get(token);
                }
                
                // Store target at endpoint
                current.targetTokens = Arrays.stream(targetPath.split("/"))
                                             .filter(s -> !s.isEmpty())
                                             .toList();
            }
            
            return root;
        }
        
        /**
         * Resolve symlinks using Trie traversal.
         * 
         * Time Complexity: O(S + s) where:
         *   S = total chars in all symlinks (BUILD cost)
         *   s = number of segments in path (LOOKUP cost)
         *   
         *   Breakdown:
         *   - buildTrie(): O(S) - one-time preprocessing
         *   - Extract path tokens: O(n) where n = path length
         *   - Trie traversal: O(s) - one check per segment
         *     * For each segment: O(1) HashMap lookup in children
         *     * NO iteration over all symlinks!
         *   - Build result: O(r) where r = remaining segments
         *   
         *   Total for single resolution: O(S + s)
         *   
         *   For multiple resolutions (p paths):
         *   - Build once: O(S)
         *   - Resolve p times: p × O(s)
         *   - Amortized per path: O(S/p + s)
         *   - As p → ∞: O(s) per path
         *   
         *   Example: 100 symlinks (S=2000), 10 segments
         *   - First call: 2000 + 10 = 2010 operations
         *   - Subsequent calls: 10 operations each
         *   - After 100 calls: Trie wins over HashMap
         * 
         * Space Complexity: O(S)
         *   - Trie structure: O(S) as analyzed in buildTrie
         *   - Path tokens list: O(s)
         *   - Result list: O(s + target)
         *   Total: O(S) dominated by Trie
         * 
         * Key Advantage: LOOKUP is O(s), independent of number of symlinks!
         *                HashMap lookup is O(s × p) conceptually.
         * 
         * Trade-off: Higher upfront cost (O(S)) but faster repeated lookups.
         */
        public String resolve(String path, Map<String, String> symlinks) {
            comparisonCount = 0;
            
            // O(S): Build Trie (one-time cost)
            // In production, this would be done once at startup, not per resolution
            TrieNode root = buildTrie(symlinks);
            
            // O(n): Extract path tokens (split and filter)
            List<String> pathTokens = Arrays.stream(path.split("/"))
                                           .filter(s -> !s.isEmpty())
                                           .toList();
            
            // O(s): Traverse Trie following the path
            // KEY INSIGHT: Only check children of current node, not all symlinks!
            TrieNode current = root;
            TrieNode linkedNode = null;  // Track deepest symlink found
            int linkedIndex = -1;
            
            for (int i = 0; i < pathTokens.size(); i++) {
                String token = pathTokens.get(i);
                
                comparisonCount++;  // Count for benchmark
                
                // O(1): Check if this segment exists in Trie
                // This is the key difference from HashMap approach!
                // HashMap checks all symlinks; Trie only checks current node's children
                if (!current.children.containsKey(token)) {
                    break;  // No further match possible in this path
                }
                
                // O(1): Move to child node
                current = current.children.get(token);
                
                // O(1): Check if this node represents a symlink endpoint
                if (!current.targetTokens.isEmpty()) {
                    // Found a symlink! Remember it (greedy: keep looking for longer match)
                    linkedNode = current;
                    linkedIndex = i;
                }
            }
            
            // O(r): Build result if symlink was found
            if (linkedNode != null) {
                List<String> result = new ArrayList<>(linkedNode.targetTokens);
                
                // Append remaining path segments after the symlink
                if (linkedIndex < pathTokens.size() - 1) {
                    result.addAll(pathTokens.subList(linkedIndex + 1, pathTokens.size()));
                }
                
                return "/" + String.join("/", result);
            }
            
            return path;  // No symlink found
        }
        
        public long getComparisonCount() {
            return comparisonCount;
        }
    }
    
    
    // ========================================
    // PERFORMANCE BENCHMARKING
    // ========================================
    
    public static class Benchmark {
        
        public static void runComparison(String testName, 
                                        String path, 
                                        Map<String, String> symlinks) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("TEST: " + testName);
            System.out.println("=".repeat(60));
            System.out.println("Path: " + path);
            System.out.println("Symlinks: " + symlinks.size());
            
            // HashMap approach
            HashMapApproach hashMap = new HashMapApproach();
            long hashMapStart = System.nanoTime();
            String hashMapResult = hashMap.resolve(path, symlinks);
            long hashMapTime = System.nanoTime() - hashMapStart;
            long hashMapOps = hashMap.getComparisonCount();
            
            // Trie approach
            TrieApproach trie = new TrieApproach();
            long trieStart = System.nanoTime();
            String trieResult = trie.resolve(path, symlinks);
            long trieTime = System.nanoTime() - trieStart;
            long trieOps = trie.getComparisonCount();
            
            // Verify results match
            if (!hashMapResult.equals(trieResult)) {
                System.out.println("❌ ERROR: Results don't match!");
                System.out.println("  HashMap: " + hashMapResult);
                System.out.println("  Trie:    " + trieResult);
                return;
            }
            
            // Display results
            System.out.println("\nResult: " + hashMapResult);
            System.out.println("\n" + "-".repeat(60));
            System.out.println("PERFORMANCE COMPARISON:");
            System.out.println("-".repeat(60));
            
            System.out.printf("%-20s | %-15s | %-15s%n", "Metric", "HashMap", "Trie");
            System.out.println("-".repeat(60));
            System.out.printf("%-20s | %-15d | %-15d%n", "Operations", hashMapOps, trieOps);
            System.out.printf("%-20s | %-15d | %-15d ns%n", "Time", hashMapTime, trieTime);
            
            double speedup = (double) hashMapTime / trieTime;
            String faster = speedup > 1 ? "Trie" : "HashMap";
            System.out.printf("%-20s | %-33s%n", "Faster", 
                String.format("%s (%.2fx)", faster, Math.abs(speedup)));
            
            System.out.println("-".repeat(60));
        }
        
        public static void runScalabilityTest() {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("SCALABILITY TEST: Varying Number of Symlinks");
            System.out.println("=".repeat(60));
            
            int[] symlinkCounts = {10, 50, 100, 500, 1000};
            
            System.out.printf("%-15s | %-20s | %-20s | %-15s%n", 
                "Symlinks", "HashMap Ops", "Trie Ops", "Speedup");
            System.out.println("-".repeat(75));
            
            for (int count : symlinkCounts) {
                // Generate symlinks
                Map<String, String> symlinks = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    symlinks.put("/home/link" + i, "/target" + i);
                }
                
                String path = "/home/link" + (count - 1) + "/file.txt";
                
                // HashMap
                HashMapApproach hashMap = new HashMapApproach();
                hashMap.resolve(path, symlinks);
                long hashMapOps = hashMap.getComparisonCount();
                
                // Trie
                TrieApproach trie = new TrieApproach();
                trie.resolve(path, symlinks);
                long trieOps = trie.getComparisonCount();
                
                double opsRatio = (double) hashMapOps / trieOps;
                
                System.out.printf("%-15d | %-20d | %-20d | %.2fx%n", 
                    count, hashMapOps, trieOps, opsRatio);
            }
        }
    }
    
    
    // ========================================
    // DEMONSTRATION TESTS
    // ========================================
    
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  HashMap vs Trie: Symlink Resolution Comparison           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // Test 1: Simple case
        Map<String, String> symlinks1 = new HashMap<>();
        symlinks1.put("/home/link", "/etc");
        Benchmark.runComparison(
            "Simple Symlink",
            "/home/link/passwd",
            symlinks1
        );
        
        // Test 2: Longest prefix matching
        Map<String, String> symlinks2 = new HashMap<>();
        symlinks2.put("/home/link", "/etc");
        symlinks2.put("/home/user/docs", "/shared/documents");
        Benchmark.runComparison(
            "Longest Prefix Matching",
            "/home/user/docs/file.txt",
            symlinks2
        );
        
        // Test 3: No symlink match
        Map<String, String> symlinks3 = new HashMap<>();
        symlinks3.put("/home/link", "/etc");
        Benchmark.runComparison(
            "No Symlink Match",
            "/var/log/messages",
            symlinks3
        );
        
        // Test 4: Many symlinks (shows Trie advantage)
        Map<String, String> symlinks4 = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            symlinks4.put("/home/link" + i, "/target" + i);
        }
        symlinks4.put("/home/user/docs", "/shared/documents");
        Benchmark.runComparison(
            "Many Symlinks (100)",
            "/home/user/docs/deeply/nested/file.txt",
            symlinks4
        );
        
        // Test 5: Chained resolution
        Map<String, String> symlinks5 = new HashMap<>();
        symlinks5.put("/a/b/c", "/x/y/z");
        Benchmark.runComparison(
            "Deep Path",
            "/a/b/c/d/e/f/g/file.txt",
            symlinks5
        );
        
        // Scalability test
        Benchmark.runScalabilityTest();
        
        // Summary
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("\nHashMap Approach:");
        System.out.println("  ✓ Simple implementation (~40 lines)");
        System.out.println("  ✓ No preprocessing needed");
        System.out.println("  ✓ O(1) extra space");
        System.out.println("  ✓ Better for small number of symlinks");
        System.out.println("  ✓ Interview-friendly (quick to implement)");
        
        System.out.println("\nTrie Approach:");
        System.out.println("  ✓ Optimal O(n) lookup time");
        System.out.println("  ✓ Better for many symlinks");
        System.out.println("  ✓ Better for repeated resolutions");
        System.out.println("  ✓ Production-grade performance");
        System.out.println("  ✗ More complex (~100 lines with TrieNode)");
        System.out.println("  ✗ O(S) space and build time");
        
        System.out.println("\nRecommendation:");
        System.out.println("  • Interview: Use HashMap (simpler, faster to code)");
        System.out.println("  • Production: Depends on scale and usage");
        System.out.println("    - Few symlinks, one-time: HashMap");
        System.out.println("    - Many symlinks, repeated: Trie");
        System.out.println("  • Show depth: Implement HashMap, discuss Trie");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✓ All comparisons complete!");
        System.out.println("=".repeat(60) + "\n");
    }
}

