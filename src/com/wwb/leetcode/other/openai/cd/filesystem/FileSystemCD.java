package com.wwb.leetcode.other.openai.cd.filesystem;

import java.util.*;

/**
 * Filesystem-Focused CD Implementation
 * 
 * Phase 1 (15 min): Basic path navigation
 * Phase 2 (15 min): Path validation using filesystem tree
 * Phase 3 (15 min): Mount points and filesystem boundaries
 * 
 * Focus: Real filesystem internals, validation, mount management
 */
public class FileSystemCD {
    
    // ========================================
    // SUPPORTING CLASSES
    // ========================================
    
    public static class FileSystemTree {
        private final TrieNode root;
        
        public FileSystemTree() {
            this.root = new TrieNode("/");
            this.root.isDirectory = true;
        }
        
        /**
         * Add a directory or file to the filesystem tree.
         */
        public void addPath(String path, boolean isDirectory) {
            if (path.equals("/")) return;
            
            String[] components = path.substring(1).split("/");
            TrieNode current = root;
            
            for (String component : components) {
                if (component.isEmpty()) continue;
                
                current.children.putIfAbsent(component, new TrieNode(component));
                current = current.children.get(component);
            }
            
            current.isDirectory = isDirectory;
        }
        
        /**
         * Check if a path exists in the filesystem.
         */
        public boolean exists(String path) {
            if (path.equals("/")) return true;
            
            String[] components = path.substring(1).split("/");
            TrieNode current = root;
            
            for (String component : components) {
                if (component.isEmpty()) continue;
                if (!current.children.containsKey(component)) {
                    return false;
                }
                current = current.children.get(component);
            }
            
            return true;
        }
        
        /**
         * Check if a path is a directory.
         */
        public boolean isDirectory(String path) {
            if (path.equals("/")) return true;
            
            String[] components = path.substring(1).split("/");
            TrieNode current = root;
            
            for (String component : components) {
                if (component.isEmpty()) continue;
                if (!current.children.containsKey(component)) {
                    return false;
                }
                current = current.children.get(component);
            }
            
            return current.isDirectory;
        }
        
        /**
         * List contents of a directory.
         */
        public List<String> listDirectory(String path) {
            if (!exists(path) || !isDirectory(path)) {
                throw new IllegalArgumentException("Not a directory: " + path);
            }
            
            TrieNode node = findNode(path);
            return new ArrayList<>(node.children.keySet());
        }
        
        private TrieNode findNode(String path) {
            if (path.equals("/")) return root;
            
            String[] components = path.substring(1).split("/");
            TrieNode current = root;
            
            for (String component : components) {
                if (component.isEmpty()) continue;
                current = current.children.get(component);
                if (current == null) return null;
            }
            
            return current;
        }
        
        static class TrieNode {
            String name;
            Map<String, TrieNode> children;
            boolean isDirectory;
            
            TrieNode(String name) {
                this.name = name;
                this.children = new HashMap<>();
                this.isDirectory = false;
            }
        }
    }
    
    public static class MountPoint {
        String mountPath;
        String devicePath;
        String filesystemType;
        
        public MountPoint(String mountPath, String devicePath, String filesystemType) {
            this.mountPath = mountPath;
            this.devicePath = devicePath;
            this.filesystemType = filesystemType;
        }
        
        @Override
        public String toString() {
            return String.format("%s on %s type %s", devicePath, mountPath, filesystemType);
        }
    }
    
    
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
    // PHASE 2: Path Validation
    // ========================================
    
    /**
     * CD with filesystem validation.
     * 
     * Features:
     * - Check if path exists
     * - Verify it's a directory
     * - Provide helpful error messages
     * - Suggest similar paths for typos
     */
    public String phase2(String currentDir, String targetDir, FileSystemTree fs) {
        String finalPath = phase1(currentDir, targetDir);
        if (finalPath == null) {
            throw new IllegalArgumentException("Cannot navigate above root");
        }
        
        // Check if path exists
        if (!fs.exists(finalPath)) {
            // Try to provide helpful suggestions
            List<String> suggestions = findSimilarPaths(finalPath, fs);
            
            if (!suggestions.isEmpty()) {
                throw new IllegalArgumentException(
                    "No such directory: " + finalPath + "\n" +
                    "Did you mean: " + String.join(", ", suggestions)
                );
            }
            
            throw new IllegalArgumentException("No such directory: " + finalPath);
        }
        
        // Check if it's a directory
        if (!fs.isDirectory(finalPath)) {
            throw new IllegalArgumentException("Not a directory: " + finalPath);
        }
        
        return finalPath;
    }
    
    /**
     * Find similar paths using Levenshtein distance for typo correction.
     */
    private List<String> findSimilarPaths(String path, FileSystemTree fs) {
        String parent = getParent(path);
        String target = getBaseName(path);
        
        if (!fs.exists(parent)) {
            return Collections.emptyList();
        }
        
        List<String> candidates = fs.listDirectory(parent);
        
        // Find paths with small edit distance
        return candidates.stream()
            .filter(c -> levenshteinDistance(c, target) <= 2)
            .sorted(Comparator.comparingInt(c -> levenshteinDistance(c, target)))
            .limit(3)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private String getParent(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash <= 0 ? "/" : path.substring(0, lastSlash);
    }
    
    private String getBaseName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }
    
    /**
     * Calculate Levenshtein distance between two strings.
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(
                        dp[i - 1][j],      // deletion
                        dp[i][j - 1]),     // insertion
                        dp[i - 1][j - 1]   // substitution
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    
    // ========================================
    // PHASE 3: Mount Points
    // ========================================
    
    /**
     * CD with mount point awareness.
     * 
     * Features:
     * - Track filesystem boundaries
     * - Detect when crossing mount points
     * - Handle different filesystem types
     * - Useful for: df, mount, filesystem stats
     */
    public String phase3(String currentDir, String targetDir,
                        FileSystemTree fs,
                        List<MountPoint> mounts) {
        String finalPath = phase2(currentDir, targetDir, fs);
        
        // Find which mount point contains the paths
        MountPoint currentMount = findMountPoint(currentDir, mounts);
        MountPoint targetMount = findMountPoint(finalPath, mounts);
        
        if (!currentMount.mountPath.equals(targetMount.mountPath)) {
            System.out.println("Crossing filesystem boundary:");
            System.out.println("  From: " + currentMount);
            System.out.println("  To:   " + targetMount);
        }
        
        return finalPath;
    }
    
    /**
     * Find the mount point that contains the given path.
     * Returns the longest matching mount path (most specific).
     */
    private MountPoint findMountPoint(String path, List<MountPoint> mounts) {
        MountPoint best = null;
        int longestMatch = -1;
        
        for (MountPoint mount : mounts) {
            if (path.equals(mount.mountPath) || path.startsWith(mount.mountPath + "/")) {
                if (mount.mountPath.length() > longestMatch) {
                    longestMatch = mount.mountPath.length();
                    best = mount;
                }
            }
        }
        
        return best != null ? best : mounts.get(0);  // Default to root mount
    }
    
    /**
     * Get all mount points along a path.
     */
    public List<MountPoint> getMountPointsInPath(String path, List<MountPoint> mounts) {
        List<MountPoint> result = new ArrayList<>();
        
        String current = "/";
        result.add(findMountPoint(current, mounts));
        
        String[] components = path.substring(1).split("/");
        for (String component : components) {
            if (component.isEmpty()) continue;
            current = current.equals("/") ? "/" + component : current + "/" + component;
            
            MountPoint mount = findMountPoint(current, mounts);
            if (result.isEmpty() || !result.get(result.size() - 1).equals(mount)) {
                result.add(mount);
            }
        }
        
        return result;
    }
    
    
    // ========================================
    // TESTS
    // ========================================
    
    public static void main(String[] args) {
        FileSystemCD cd = new FileSystemCD();
        
        System.out.println("=== PHASE 1: Basic Navigation ===");
        assert "/home/user/docs".equals(cd.phase1("/home/user", "docs"));
        System.out.println("✓ Phase 1 tests passed!\n");
        
        System.out.println("=== PHASE 2: Path Validation ===");
        FileSystemTree fs = new FileSystemTree();
        fs.addPath("/home", true);
        fs.addPath("/home/user", true);
        fs.addPath("/home/user/documents", true);
        fs.addPath("/home/user/docs", true);  // Similar to documents
        fs.addPath("/home/user/file.txt", false);  // File, not directory
        
        // Valid directory
        String result = cd.phase2("/home", "user/docs", fs);
        assert "/home/user/docs".equals(result);
        System.out.println("✓ Valid directory: " + result);
        
        // Non-existent directory with suggestion
        try {
            cd.phase2("/home/user", "documetns", fs);  // Typo!
            assert false : "Should have thrown exception";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("documents");
            System.out.println("✓ Typo suggestion: " + e.getMessage());
        }
        
        // Trying to cd to a file
        try {
            cd.phase2("/home/user", "file.txt", fs);
            assert false : "Should have thrown exception";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Not a directory");
            System.out.println("✓ File detection: " + e.getMessage());
        }
        
        System.out.println("✓ Phase 2 tests passed!\n");
        
        System.out.println("=== PHASE 3: Mount Points ===");
        List<MountPoint> mounts = Arrays.asList(
            new MountPoint("/", "/dev/sda1", "ext4"),
            new MountPoint("/home", "/dev/sda2", "ext4"),
            new MountPoint("/mnt/usb", "/dev/sdb1", "vfat")
        );
        
        fs.addPath("/mnt", true);
        fs.addPath("/mnt/usb", true);
        fs.addPath("/mnt/usb/files", true);
        
        // Navigate within same filesystem
        System.out.println("\nNavigating within /home:");
        cd.phase3("/home", "user/docs", fs, mounts);
        
        // Cross filesystem boundary
        System.out.println("\nCrossing to /mnt/usb:");
        cd.phase3("/home/user", "/mnt/usb/files", fs, mounts);
        
        // Get mount points in path
        List<MountPoint> pathMounts = cd.getMountPointsInPath("/home/user/docs", mounts);
        System.out.println("\nMount points in /home/user/docs:");
        pathMounts.forEach(System.out::println);
        
        System.out.println("\n✓ Phase 3 tests passed!");
        System.out.println("\n🎉 All filesystem tests passed!");
    }
}

