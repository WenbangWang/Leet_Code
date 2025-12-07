package com.wwb.leetcode.other.openai.cd;

import java.util.*;

/**
 * OpenAI Interview Question: Implement CD Command
 * 
 * This file demonstrates a progressive implementation approach suitable for interviews.
 * Each phase builds upon the previous one.
 * 
 * Time allocation (45 minutes total):
 * - Phase 1: Basic path navigation (15 min)
 * - Phase 2: Tilde expansion (10 min)
 * - Phase 3: Symlink resolution (20 min)
 */
public class CDCommandInterview {
    
    // ========================================
    // PHASE 1: Basic Path Navigation
    // ========================================
    
    /**
     * Basic cd implementation handling:
     * - Absolute paths (start with "/")
     * - Relative paths
     * - "." (current directory)
     * - ".." (parent directory)
     * 
     * Time Complexity: O(n) where n = total path length
     * Space Complexity: O(n) for the stack
     * 
     * @param currentDir current absolute directory path
     * @param targetDir target directory (relative or absolute)
     * @return final absolute path, or null if trying to go above root
     */
    public String cdBasic(String currentDir, String targetDir) {
        // Step 1: Determine if target is absolute or relative
        String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
        
        // Step 2: Normalize path using stack to handle . and ..
        Stack<String> stack = new Stack<>();
        String[] segments = fullPath.split("/");
        
        for (String segment : segments) {
            if (segment.isEmpty() || segment.equals(".")) {
                // Skip empty segments (from multiple slashes) and current directory
                continue;
            } else if (segment.equals("..")) {
                // Parent directory
                if (stack.isEmpty()) {
                    // Trying to go above root
                    return null;
                }
                stack.pop();
            } else {
                // Regular directory name
                stack.push(segment);
            }
        }
        
        // Step 3: Build the canonical path
        if (stack.isEmpty()) {
            return "/";  // Root directory
        }
        return "/" + String.join("/", stack);
    }
    
    
    // ========================================
    // PHASE 2: Home Directory Expansion
    // ========================================
    
    /**
     * Extended cd with tilde (~) expansion:
     * - "~" expands to home directory
     * - "~/path" expands to home/path
     * - "~" only expands at the beginning of the path
     * 
     * @param currentDir current absolute directory path
     * @param targetDir target directory (may start with ~)
     * @param homeDir user's home directory (e.g., "/home/user")
     * @return final absolute path, or null if invalid
     */
    public String cdWithHome(String currentDir, String targetDir, String homeDir) {
        // Step 1: Handle tilde expansion (only at the start)
        if (targetDir.equals("~")) {
            targetDir = homeDir;
        } else if (targetDir.startsWith("~/")) {
            // Replace ~ with homeDir, keeping the trailing /
            targetDir = homeDir + targetDir.substring(1);
        }
        // Note: "~" in the middle of path is treated as a literal directory name
        
        // Step 2: Use basic cd logic
        return cdBasic(currentDir, targetDir);
    }
    
    
    // ========================================
    // PHASE 3: Symbolic Link Resolution
    // ========================================
    
    /**
     * Full cd implementation with symlink resolution:
     * - Resolves symbolic links iteratively
     * - Handles chained symlinks (A→B→C)
     * - Detects and prevents infinite cycles
     * - Uses greedy longest prefix matching
     * 
     * Approach: Iterative HashMap-based resolution
     * (Simpler to implement in interview than Trie, adequate for most cases)
     * 
     * Time Complexity: O(k * m * n) where:
     *   k = number of symlink resolution iterations (bounded)
     *   m = number of symlinks
     *   n = path length
     * 
     * @param currentDir current absolute directory path
     * @param targetDir target directory
     * @param symlinks map of symlink paths to their targets
     * @return final absolute path after resolving all symlinks
     * @throws RuntimeException if symlink cycle detected
     */
    public String cdWithSymlinks(String currentDir, String targetDir, Map<String, String> symlinks) {
        // Step 1: Get the normalized path using basic cd
        String path = cdBasic(currentDir, targetDir);
        if (path == null) {
            return null;
        }
        
        // Step 2: Iteratively resolve symlinks
        Set<String> visited = new HashSet<>();
        
        // Limit iterations to prevent infinite loops
        // Real systems use SYMLOOP_MAX (typically 40), we use symlinks.size() + 1
        int maxIterations = symlinks.size() + 1;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Cycle detection: if we've seen this path before, we're in a loop
            if (!visited.add(path)) {
                throw new RuntimeException("Symlink cycle detected at path: " + path);
            }
            
            // Try to resolve the longest matching symlink prefix
            String newPath = resolveLongestSymlink(path, symlinks);
            
            // If no symlink was resolved, we're done
            if (newPath.equals(path)) {
                break;
            }
            
            // Normalize the new path (symlink target might contain . or ..)
            path = normalizePath(newPath);
        }
        
        return path;
    }
    
    /**
     * Find and resolve the longest matching symlink prefix in the given path.
     * 
     * Example:
     *   path = "/home/user/docs/file.txt"
     *   symlinks = {"/home/user/docs" -> "/shared/documents"}
     *   result = "/shared/documents/file.txt"
     * 
     * @param path the current path
     * @param symlinks map of symlink paths to targets
     * @return new path with longest symlink resolved, or original path if no match
     */
    private String resolveLongestSymlink(String path, Map<String, String> symlinks) {
        String[] segments = path.substring(1).split("/");  // Remove leading "/" and split
        StringBuilder currentPath = new StringBuilder();
        
        String longestMatch = null;
        String longestTarget = null;
        int longestMatchIndex = -1;
        
        // Try progressively longer prefixes
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].isEmpty()) {
                continue;
            }
            
            currentPath.append("/").append(segments[i]);
            String candidate = currentPath.toString();
            
            if (symlinks.containsKey(candidate)) {
                // Found a matching symlink, record it
                longestMatch = candidate;
                longestTarget = symlinks.get(candidate);
                longestMatchIndex = i;
            }
        }
        
        // If we found a symlink, replace the prefix with its target
        if (longestMatch != null) {
            // Build the remainder of the path after the symlink
            List<String> remainingSegments = new ArrayList<>();
            for (int i = longestMatchIndex + 1; i < segments.length; i++) {
                if (!segments[i].isEmpty()) {
                    remainingSegments.add(segments[i]);
                }
            }
            
            if (remainingSegments.isEmpty()) {
                return longestTarget;
            } else {
                return longestTarget + "/" + String.join("/", remainingSegments);
            }
        }
        
        // No symlink found, return original path
        return path;
    }
    
    /**
     * Normalize a path by resolving . and .. without changing current directory context.
     * Similar to cdBasic but assumes input is already an absolute path.
     * 
     * @param path absolute path (may contain . or ..)
     * @return normalized absolute path
     */
    private String normalizePath(String path) {
        Stack<String> stack = new Stack<>();
        String[] segments = path.split("/");
        
        for (String segment : segments) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            } else if (segment.equals("..")) {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
                // If stack is empty, we're at root, can't go higher
            } else {
                stack.push(segment);
            }
        }
        
        return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
    }
    
    
    // ========================================
    // ALTERNATIVE: Trie-based Symlink Resolution
    // ========================================
    
    /**
     * Alternative implementation using Trie for symlink resolution.
     * 
     * Advantages:
     * - O(n) lookup where n = path length (vs O(m*n) for HashMap iteration)
     * - More efficient with many symlinks
     * - Natural prefix matching
     * 
     * Trade-offs:
     * - More complex to implement
     * - Additional space for Trie structure
     * - May be overkill for small symlink maps
     * 
     * In interview: Implement HashMap version first, then discuss this as optimization.
     */
    public String cdWithSymlinksOptimized(String currentDir, String targetDir, Map<String, String> symlinks) {
        String path = cdBasic(currentDir, targetDir);
        if (path == null) {
            return null;
        }
        
        // Build Trie from symlinks (one-time cost if reusing)
        TrieNode root = TrieNode.buildTrie(symlinks);
        
        Set<String> visited = new HashSet<>();
        List<String> pathTokens = Arrays.stream(path.split("/"))
                                       .filter(s -> !s.isEmpty())
                                       .toList();
        
        int maxDepth = symlinks.size() + 1;
        
        for (int step = 0; step < maxDepth; step++) {
            String key = "/" + String.join("/", pathTokens);
            
            if (!visited.add(key)) {
                throw new RuntimeException("Symlink cycle detected");
            }
            
            List<String> newPathTokens = TrieNode.symlinkToPath(pathTokens, root);
            
            if (newPathTokens == null) {
                // No more symlinks to resolve
                break;
            }
            
            pathTokens = newPathTokens;
        }
        
        return "/" + String.join("/", pathTokens);
    }
    
    
    // ========================================
    // COMPREHENSIVE VERSION (All Phases Combined)
    // ========================================
    
    /**
     * Complete cd implementation with all features.
     * This is what you'd end up with after completing all interview phases.
     */
    public String cd(String currentDir, String targetDir, String homeDir, Map<String, String> symlinks) {
        // Phase 2: Handle tilde expansion
        if (targetDir.equals("~")) {
            targetDir = homeDir;
        } else if (targetDir.startsWith("~/")) {
            targetDir = homeDir + targetDir.substring(1);
        }
        
        // Phase 1 + 3: Basic path resolution with symlink handling
        if (symlinks == null || symlinks.isEmpty()) {
            return cdBasic(currentDir, targetDir);
        } else {
            return cdWithSymlinks(currentDir, targetDir, symlinks);
        }
    }
    
    
    // ========================================
    // TEST CASES
    // ========================================
    
    public static void main(String[] args) {
        CDCommandInterview cd = new CDCommandInterview();
        
        System.out.println("=== Phase 1: Basic Tests ===");
        assert "/home/user/documents".equals(cd.cdBasic("/home/user", "documents"));
        assert "/etc".equals(cd.cdBasic("/home/user", "/etc"));
        assert "/home/user".equals(cd.cdBasic("/home/user", "."));
        assert "/home".equals(cd.cdBasic("/home/user", ".."));
        assert "/".equals(cd.cdBasic("/home/user", "../.."));
        assert cd.cdBasic("/", "..") == null;  // Can't go above root
        assert "/home/user/pics".equals(cd.cdBasic("/home/user", "./docs/../pics"));
        assert "/c/e".equals(cd.cdBasic("/a/b", "../../c/./d/../e"));
        assert "/home/user/documents/file".equals(cd.cdBasic("/home", "user//documents///file"));
        assert "/home".equals(cd.cdBasic("/home", ""));
        System.out.println("✓ All Phase 1 tests passed!");
        
        System.out.println("\n=== Phase 2: Tilde Tests ===");
        assert "/home/user".equals(cd.cdWithHome("/etc", "~", "/home/user"));
        assert "/home/user/docs".equals(cd.cdWithHome("/etc", "~/docs", "/home/user"));
        assert "/home".equals(cd.cdWithHome("/home/user", "~/..", "/home/user"));
        assert "/home/test/~/file".equals(cd.cdWithHome("/home", "test/~/file", "/home/user"));
        System.out.println("✓ All Phase 2 tests passed!");
        
        System.out.println("\n=== Phase 3: Symlink Tests ===");
        Map<String, String> symlinks = new HashMap<>();
        symlinks.put("/home/link", "/etc");
        symlinks.put("/etc/conf", "/var/config");
        symlinks.put("/a/b", "/c/d");
        
        assert "/etc/passwd".equals(cd.cdWithSymlinks("/home", "link/passwd", symlinks));
        assert "/var/config/app".equals(cd.cdWithSymlinks("/home", "link/conf/app", symlinks));
        assert "/c/d".equals(cd.cdWithSymlinks("/", "a/b", symlinks));
        System.out.println("✓ All Phase 3 tests passed!");
        
        System.out.println("\n=== Cycle Detection Test ===");
        Map<String, String> cycleLinks = new HashMap<>();
        cycleLinks.put("/a", "/b");
        cycleLinks.put("/b", "/a");
        
        try {
            cd.cdWithSymlinks("/", "a", cycleLinks);
            System.out.println("✗ Should have thrown exception for cycle");
            assert false;
        } catch (RuntimeException e) {
            System.out.println("✓ Cycle detection working: " + e.getMessage());
        }
        
        System.out.println("\n=== Chained Symlink Test ===");
        Map<String, String> chainedLinks = new HashMap<>();
        chainedLinks.put("/link1", "/link2");
        chainedLinks.put("/link2", "/link3");
        chainedLinks.put("/link3", "/final");
        
        assert "/final/file.txt".equals(cd.cdWithSymlinks("/", "link1/file.txt", chainedLinks));
        System.out.println("✓ Chained symlink resolution working!");
        
        System.out.println("\n=== Trie-based Implementation Test ===");
        assert "/etc/passwd".equals(cd.cdWithSymlinksOptimized("/home", "link/passwd", symlinks));
        assert "/var/config/app".equals(cd.cdWithSymlinksOptimized("/home", "link/conf/app", symlinks));
        System.out.println("✓ Trie-based implementation working!");
        
        System.out.println("\n🎉 All tests passed!");
    }
}

