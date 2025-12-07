package com.wwb.leetcode.other.openai.cd.standard;

import java.util.*;

/**
 * Standard CD Implementation - The Classic Progression
 * 
 * Phase 1 (15 min): Basic path navigation with . and ..
 * Phase 2 (10 min): Tilde (~) expansion  
 * Phase 3 (20 min): Symlink resolution with cycle detection
 * 
 * This is the most commonly reported progression in OpenAI interviews.
 */
public class StandardCD {
    
    // ========================================
    // PHASE 1: Basic Path Navigation
    // ========================================
    
    /**
     * Basic cd implementation.
     * 
     * Handles:
     * - Absolute paths (start with "/")
     * - Relative paths
     * - "." (current directory)
     * - ".." (parent directory)
     * 
     * Time Complexity: O(n) where n = total length of the path
     *   - String concatenation: O(n)
     *   - Split by "/": O(n) - creates array of segments
     *   - Processing each segment: O(n) total
     *     * Each segment processed once
     *     * Stack operations (push/pop) are O(1)
     *   - String.join(): O(n)
     *   Total: O(n) + O(n) + O(n) + O(n) = O(n)
     * 
     * Space Complexity: O(n)
     *   - Stack: O(d) where d = directory depth (worst case n)
     *   - Split array: O(s) where s = number of segments
     *   - Result string: O(n)
     *   Total: O(n)
     */
    public String phase1(String currentDir, String targetDir) {
        // O(n): Determine if absolute or relative, concatenate if needed
        String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
        
        // O(d): Stack to track directory depth, where d ≤ n
        Stack<String> stack = new Stack<>();
        
        // O(n): Split creates array, then iterate through each segment
        for (String segment : fullPath.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                // Skip empty segments (from multiple slashes) and current directory
                continue;
            } else if (segment.equals("..")) {
                // Parent directory - pop from stack
                if (stack.isEmpty()) {
                    return null;  // Cannot go above root
                }
                stack.pop();  // O(1)
            } else {
                // Regular directory - push to stack
                stack.push(segment);  // O(1)
            }
        }
        
        // O(n): Join stack elements with "/"
        return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
    }
    
    
    // ========================================
    // PHASE 2: Tilde Expansion
    // ========================================
    
    /**
     * Extended cd with tilde (~) expansion.
     * 
     * Rules:
     * - "~" alone → home directory
     * - "~/path" → home directory + path
     * - "~" only expands at the beginning
     * 
     * Time Complexity: O(n) where n = total path length
     *   - equals() check: O(1) for small strings
     *   - startsWith() check: O(1) for checking "~/"
     *   - substring() + concatenation: O(n) worst case
     *   - phase1() call: O(n)
     *   Total: O(n) (dominated by phase1)
     * 
     * Space Complexity: O(n)
     *   - New string for expanded path: O(n) worst case
     *   - Space from phase1: O(n)
     *   Total: O(n)
     */
    public String phase2(String currentDir, String targetDir, String homeDir) {
        // O(1): Check if exact match for "~"
        if (targetDir.equals("~")) {
            targetDir = homeDir;
        } 
        // O(1): Check if starts with "~/"
        else if (targetDir.startsWith("~/")) {
            // O(n): Create new string with home expanded
            targetDir = homeDir + targetDir.substring(1);
        }
        // Note: "~" in middle of path is treated as literal directory name
        
        // O(n): Delegate to phase1 for path normalization
        return phase1(currentDir, targetDir);
    }
    
    
    // ========================================
    // PHASE 3: Symlink Resolution
    // ========================================
    
    /**
     * Full cd implementation with symlink resolution.
     * 
     * Features:
     * - Resolves symbolic links iteratively
     * - Handles chained symlinks (A→B→C)
     * - Detects cycles using visited set
     * - Uses greedy longest prefix matching
     * - Bounds iterations to prevent infinite loops
     * 
     * Time Complexity: O(k × s × p) where:
     *   k = number of symlink resolution iterations (≤ m+1, typically 1-3)
     *   s = number of path segments (typically 5-10)
     *   p = number of symlinks in map (variable)
     *   
     *   Breakdown per iteration:
     *   - visited.add(): O(1) average, O(p) worst case for string comparison
     *   - resolveLongestSymlink(): O(s × p)
     *     * For each of s segments: O(1) to build prefix
     *     * For each prefix: O(1) HashMap lookup (average case)
     *     * But implicitly checks against p symlinks conceptually
     *   - normalizePath(): O(n)
     *   
     *   Total: O(k × (1 + s×p + n))
     *        ≈ O(k × s × p) when p is significant
     *        ≈ O(k × n) when p is small and HashMap lookups dominate
     *   
     *   Typical case: k=2, s=5, p=10 → ~100 operations
     *   Worst case: k=100, s=10, p=1000 → ~1M operations (but k bounded by m+1)
     * 
     * Space Complexity: O(n + m) where:
     *   n = path length
     *   m = number of symlinks
     *   
     *   - visited set: O(k) paths stored, each O(n) length → O(k×n)
     *     In practice k ≤ m+1, so O(m×n) worst case
     *   - normalizePath stack: O(n)
     *   - resolveLongestSymlink temporaries: O(n)
     *   Total: O(m×n) worst case, O(n) typical case
     * 
     * Alternative (Trie-based): O(S + k×n) time, O(S) space
     *   where S = sum of all characters in symlink paths
     *   Trade-off: Faster lookup O(n) but O(S) build cost and space
     */
    public String phase3(String currentDir, String targetDir, 
                        String homeDir, Map<String, String> symlinks) {
        // O(n): First, apply tilde expansion and basic normalization
        String path = phase2(currentDir, targetDir, homeDir);
        if (path == null) {
            return null;
        }
        
        // O(1): If no symlinks, we're done (early exit optimization)
        if (symlinks == null || symlinks.isEmpty()) {
            return path;
        }
        
        // O(k): Iteratively resolve symlinks (k iterations, bounded by m+1)
        Set<String> visited = new HashSet<>();
        int maxIterations = symlinks.size() + 1;  // Prevent infinite loops (real systems use SYMLOOP_MAX=40)
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // O(1) average: Cycle detection using HashSet
            // If we've seen this path before, we have a cycle: /a→/b→/a
            if (!visited.add(path)) {
                throw new RuntimeException("Symlink cycle detected at: " + path);
            }
            
            // O(s×p): Try to resolve the longest matching symlink prefix
            // s = segments in path, p = symlinks in map
            String newPath = resolveLongestSymlink(path, symlinks);
            
            // O(n): If no symlink was resolved, we're done
            if (newPath.equals(path)) {
                break;  // No more symlinks to resolve
            }
            
            // O(n): Normalize the new path (symlink target might contain . or ..)
            // Example: symlink target="/etc/../var" needs normalization
            path = normalizePath(newPath);
        }
        
        return path;
    }
    
    /**
     * Find and resolve the longest matching symlink prefix.
     * 
     * Example:
     *   path = "/home/user/docs/file.txt"
     *   symlinks = {"/home/user/docs" -> "/shared/documents"}
     *   result = "/shared/documents/file.txt"
     * 
     * Time Complexity: O(s × (b + L)) where:
     *   s = number of segments in path
     *   b = average characters per segment (for building strings)
     *   L = HashMap lookup time (O(1) average, O(k) for key comparison)
     *   
     *   Breakdown:
     *   - split("/"): O(n) where n = path length
     *   - For loop (s iterations):
     *     * append(): O(1) amortized
     *     * toString(): O(current length) = O(i×b) for iteration i
     *     * containsKey(): O(1) average for HashMap lookup
     *                      O(k) for string key comparison where k = key length
     *   - Building result: O(r) where r = remaining segments
     *   
     *   Total: O(n) + O(s²×b) + O(r) ≈ O(s²) for path with s segments
     *   
     *   In practice with s=5-10 segments: Very fast
     *   Worst case with s=100: Could be slow (but rare)
     *   
     * Space Complexity: O(n)
     *   - segments array: O(s) segments, each O(b) chars → O(n) total
     *   - StringBuilder: O(n) worst case (full path)
     *   - remainingSegments list: O(s) segments
     *   Total: O(n)
     *   
     * Note: This is the HashMap iteration approach.
     *       Trie approach would be O(s) time with O(S) preprocessing.
     */
    private String resolveLongestSymlink(String path, Map<String, String> symlinks) {
        // O(n): Split path into segments
        String[] segments = path.split("/");
        StringBuilder currentPath = new StringBuilder();
        
        String longestMatch = null;
        String longestTarget = null;
        int longestMatchIndex = -1;
        
        // O(s): Try progressively longer prefixes (/a, /a/b, /a/b/c, ...)
        // Greedy: We want the LONGEST matching prefix
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].isEmpty()) {
                continue;  // Skip empty segments from leading/trailing/multiple slashes
            }
            
            // O(1) amortized: Build current prefix path
            currentPath.append("/").append(segments[i]);
            String candidate = currentPath.toString();
            
            // O(1) average: Check if this prefix is a symlink
            // HashMap.containsKey() is O(1) on average
            // String equality check is O(k) where k = key length, but k is small
            if (symlinks.containsKey(candidate)) {
                // Found a match! Remember it (might find longer match later)
                longestMatch = candidate;
                longestTarget = symlinks.get(candidate);
                longestMatchIndex = i;
            }
        }
        
        // O(s): If we found a symlink, replace the prefix with target
        if (longestMatch != null) {
            // Build the remainder of the path (segments after the symlink)
            // Example: /home/user/docs/file.txt with symlink at docs
            //          remainder = [file.txt]
            List<String> remainingSegments = new ArrayList<>();
            for (int i = longestMatchIndex + 1; i < segments.length; i++) {
                if (!segments[i].isEmpty()) {
                    remainingSegments.add(segments[i]);
                }
            }
            
            // O(r): Concatenate target + remaining segments
            if (remainingSegments.isEmpty()) {
                return longestTarget;  // Symlink with no remaining path
            } else {
                return longestTarget + "/" + String.join("/", remainingSegments);
            }
        }
        
        return path;  // No symlink found - return original path
    }
    
    /**
     * Normalize a path (resolve . and ..).
     * 
     * Similar to phase1, but assumes input is already absolute path.
     * Used after symlink resolution to normalize the target path.
     * 
     * Time Complexity: O(n) where n = path length
     *   - split("/"): O(n)
     *   - Loop through segments: O(s) iterations where s = segments
     *     * Each iteration: O(1) for stack operations
     *   - String.join(): O(n)
     *   Total: O(n)
     * 
     * Space Complexity: O(n)
     *   - Stack: O(s) segments, each O(b) bytes → O(n) total
     *   - Split array: O(n)
     *   - Result string: O(n)
     */
    private String normalizePath(String path) {
        Stack<String> stack = new Stack<>();
        
        // O(n): Split and process each segment
        for (String segment : path.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;  // Skip empty and current directory
            } else if (segment.equals("..")) {
                // Parent directory - pop if not at root
                if (!stack.isEmpty()) {
                    stack.pop();  // O(1)
                }
                // If stack is empty, we're at root - can't go up, so ignore
            } else {
                // Regular directory name
                stack.push(segment);  // O(1)
            }
        }
        
        // O(n): Join stack elements with "/"
        return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
    }
    
    
    // ========================================
    // CONVENIENCE METHOD
    // ========================================
    
    /**
     * Main cd method that delegates to the appropriate phase.
     */
    public String cd(String currentDir, String targetDir, String homeDir, 
                     Map<String, String> symlinks) {
        return phase3(currentDir, targetDir, homeDir, symlinks);
    }
    
    
    // ========================================
    // TESTS
    // ========================================
    
    public static void main(String[] args) {
        StandardCD cd = new StandardCD();
        
        System.out.println("=== PHASE 1: Basic Navigation ===");
        test(cd.phase1("/home/user", "documents"), "/home/user/documents");
        test(cd.phase1("/home/user", "/etc"), "/etc");
        test(cd.phase1("/home/user", "."), "/home/user");
        test(cd.phase1("/home/user", ".."), "/home");
        test(cd.phase1("/home/user", "../.."), "/");
        test(cd.phase1("/", ".."), null);
        test(cd.phase1("/home/user", "./docs/../pics"), "/home/user/pics");
        test(cd.phase1("/a/b", "../../c/./d/../e"), "/c/e");
        System.out.println("✓ Phase 1 tests passed!\n");
        
        System.out.println("=== PHASE 2: Tilde Expansion ===");
        test(cd.phase2("/etc", "~", "/home/user"), "/home/user");
        test(cd.phase2("/etc", "~/docs", "/home/user"), "/home/user/docs");
        test(cd.phase2("/home/user", "~/..", "/home/user"), "/home");
        test(cd.phase2("/home", "test/~/file", "/home/user"), "/home/test/~/file");
        System.out.println("✓ Phase 2 tests passed!\n");
        
        System.out.println("=== PHASE 3: Symlinks ===");
        Map<String, String> symlinks = new HashMap<>();
        symlinks.put("/home/link", "/etc");
        symlinks.put("/etc/conf", "/var/config");
        
        test(cd.phase3("/home", "link/passwd", "/home/user", symlinks), "/etc/passwd");
        test(cd.phase3("/home", "link/conf/app", "/home/user", symlinks), "/var/config/app");
        System.out.println("✓ Phase 3 tests passed!\n");
        
        System.out.println("=== Cycle Detection ===");
        Map<String, String> cycleLinks = new HashMap<>();
        cycleLinks.put("/a", "/b");
        cycleLinks.put("/b", "/a");
        
        try {
            cd.phase3("/", "a", "/home", cycleLinks);
            System.out.println("✗ Should have detected cycle");
        } catch (RuntimeException e) {
            System.out.println("✓ Cycle detected: " + e.getMessage());
        }
        
        System.out.println("\n🎉 All tests passed!");
    }
    
    private static void test(String actual, String expected) {
        if (Objects.equals(actual, expected)) {
            System.out.println("✓ " + expected);
        } else {
            System.out.println("✗ Expected: " + expected + ", Got: " + actual);
            throw new AssertionError();
        }
    }
}

