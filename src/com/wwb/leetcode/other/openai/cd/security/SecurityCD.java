package com.wwb.leetcode.other.openai.cd.security;

import java.util.*;

/**
 * Security-Focused CD Implementation
 * 
 * Phase 1 (15 min): Basic path navigation
 * Phase 2 (15 min): Permission checking (read, write, execute)
 * Phase 3 (15 min): Chroot jails and security boundaries
 * 
 * Focus: System security, access control, privilege management
 */
public class SecurityCD {
    
    // ========================================
    // SUPPORTING CLASSES
    // ========================================
    
    public enum Permission {
        READ, WRITE, EXECUTE
    }
    
    public static class User {
        String username;
        String group;
        Set<String> groups;  // User can belong to multiple groups
        
        public User(String username, String group) {
            this.username = username;
            this.group = group;
            this.groups = new HashSet<>();
            this.groups.add(group);
        }
    }
    
    public static class FilePermissions {
        String owner;
        String group;
        Set<Permission> ownerPerms;
        Set<Permission> groupPerms;
        Set<Permission> otherPerms;
        
        public FilePermissions(String owner, String group) {
            this.owner = owner;
            this.group = group;
            this.ownerPerms = new HashSet<>();
            this.groupPerms = new HashSet<>();
            this.otherPerms = new HashSet<>();
        }
        
        public static FilePermissions createDefault(String owner, String group) {
            FilePermissions perms = new FilePermissions(owner, group);
            perms.ownerPerms.addAll(Arrays.asList(Permission.READ, Permission.WRITE, Permission.EXECUTE));
            perms.groupPerms.addAll(Arrays.asList(Permission.READ, Permission.EXECUTE));
            perms.otherPerms.add(Permission.EXECUTE);
            return perms;
        }
    }
    
    public static class PermissionDeniedException extends RuntimeException {
        public PermissionDeniedException(String message) {
            super(message);
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
    // PHASE 2: Permission Checking
    // ========================================
    
    /**
     * CD with permission checking.
     * 
     * Rules:
     * - Need EXECUTE permission on EVERY directory in the path
     * - Check owner, group, and other permissions in that order
     * - Throw PermissionDeniedException if access denied
     * 
     * Real Unix: cd requires execute (x) permission, not read (r)
     */
    public String phase2(String currentDir, String targetDir,
                        Map<String, FilePermissions> permissions,
                        User currentUser) {
        String finalPath = phase1(currentDir, targetDir);
        if (finalPath == null) {
            return null;
        }
        
        // Check execute permission on each directory component
        checkTraversePermissions(finalPath, permissions, currentUser);
        
        return finalPath;
    }
    
    /**
     * Check if user has execute permission on all directories in path.
     */
    private void checkTraversePermissions(String path,
                                         Map<String, FilePermissions> permissions,
                                         User user) {
        if (path.equals("/")) {
            return;  // Root is always accessible
        }
        
        String[] segments = path.substring(1).split("/");
        StringBuilder currentPath = new StringBuilder();
        
        for (String segment : segments) {
            currentPath.append("/").append(segment);
            String pathToCheck = currentPath.toString();
            
            FilePermissions perms = permissions.get(pathToCheck);
            if (perms == null) {
                // Path doesn't exist or no permissions defined
                throw new PermissionDeniedException("No permissions defined for: " + pathToCheck);
            }
            
            if (!hasPermission(perms, user, Permission.EXECUTE)) {
                throw new PermissionDeniedException(
                    "Permission denied: " + user.username + " cannot access " + pathToCheck
                );
            }
        }
    }
    
    /**
     * Check if user has a specific permission on a file/directory.
     * Checks in order: owner → group → other
     */
    private boolean hasPermission(FilePermissions perms, User user, Permission perm) {
        // Check owner permissions
        if (perms.owner.equals(user.username)) {
            return perms.ownerPerms.contains(perm);
        }
        
        // Check group permissions
        if (user.groups.contains(perms.group)) {
            return perms.groupPerms.contains(perm);
        }
        
        // Check other permissions
        return perms.otherPerms.contains(perm);
    }
    
    
    // ========================================
    // PHASE 3: Chroot Jails
    // ========================================
    
    /**
     * CD with chroot jail enforcement.
     * 
     * Chroot jail: Restrict user to a subtree of the filesystem
     * - User cannot cd outside their jail
     * - Even with .. navigation, cannot escape
     * - Paths are relative to the chroot, but internally tracked absolutely
     * 
     * Use case: Containers, FTP servers, sandboxed environments
     */
    public String phase3(String currentDir, String targetDir,
                        String chrootPath,
                        Map<String, FilePermissions> permissions,
                        User currentUser) {
        // Normalize the target path
        String finalPath = phase1(currentDir, targetDir);
        if (finalPath == null) {
            return null;
        }
        
        // Enforce chroot boundary
        if (!isWithinChroot(finalPath, chrootPath)) {
            throw new SecurityException(
                "Cannot escape chroot jail: " + chrootPath + " (attempted: " + finalPath + ")"
            );
        }
        
        // Check permissions (if provided)
        if (permissions != null && currentUser != null) {
            checkTraversePermissions(finalPath, permissions, currentUser);
        }
        
        return finalPath;
    }
    
    /**
     * Check if a path is within the chroot boundary.
     */
    private boolean isWithinChroot(String path, String chrootPath) {
        // Root chroot allows everything
        if (chrootPath.equals("/")) {
            return true;
        }
        
        // Path must start with chroot path
        return path.equals(chrootPath) || path.startsWith(chrootPath + "/");
    }
    
    /**
     * Convert absolute path to chroot-relative path.
     * 
     * Example:
     *   chrootPath = "/home/jail"
     *   absolutePath = "/home/jail/user/docs"
     *   result = "/user/docs"
     */
    public String toRelativePath(String absolutePath, String chrootPath) {
        if (!isWithinChroot(absolutePath, chrootPath)) {
            throw new SecurityException("Path outside chroot");
        }
        
        if (absolutePath.equals(chrootPath)) {
            return "/";
        }
        
        return absolutePath.substring(chrootPath.length());
    }
    
    /**
     * Convert chroot-relative path to absolute path.
     */
    public String toAbsolutePath(String relativePath, String chrootPath) {
        if (chrootPath.equals("/")) {
            return relativePath;
        }
        
        if (relativePath.equals("/")) {
            return chrootPath;
        }
        
        return chrootPath + relativePath;
    }
    
    
    // ========================================
    // TESTS
    // ========================================
    
    public static void main(String[] args) {
        SecurityCD cd = new SecurityCD();
        
        System.out.println("=== PHASE 1: Basic Navigation ===");
        assert "/home/user/docs".equals(cd.phase1("/home/user", "docs"));
        assert "/etc".equals(cd.phase1("/home/user", "/etc"));
        System.out.println("✓ Phase 1 tests passed!\n");
        
        System.out.println("=== PHASE 2: Permission Checking ===");
        User alice = new User("alice", "users");
        User bob = new User("bob", "admin");
        
        Map<String, FilePermissions> permissions = new HashMap<>();
        permissions.put("/home", FilePermissions.createDefault("root", "root"));
        permissions.put("/home/alice", FilePermissions.createDefault("alice", "users"));
        permissions.put("/home/alice/docs", FilePermissions.createDefault("alice", "users"));
        permissions.put("/home/bob", FilePermissions.createDefault("bob", "admin"));
        
        // Alice can access her own directory
        String result = cd.phase2("/home", "alice/docs", permissions, alice);
        assert "/home/alice/docs".equals(result);
        System.out.println("✓ Alice can access /home/alice/docs");
        
        // Alice cannot access Bob's directory (no execute permission for 'other')
        permissions.get("/home/bob").otherPerms.clear();  // Remove execute for others
        try {
            cd.phase2("/home", "bob", permissions, alice);
            System.out.println("✗ Should have denied permission");
            assert false;
        } catch (PermissionDeniedException e) {
            System.out.println("✓ Permission denied for Alice to access /home/bob");
        }
        System.out.println("✓ Phase 2 tests passed!\n");
        
        System.out.println("=== PHASE 3: Chroot Jails ===");
        String chrootPath = "/home/jail";
        
        // Can navigate within jail
        String withinJail = cd.phase3("/home/jail/user", "docs", chrootPath, null, null);
        assert "/home/jail/user/docs".equals(withinJail);
        System.out.println("✓ Navigation within jail: " + withinJail);
        
        // Cannot escape jail with ..
        try {
            cd.phase3("/home/jail/user", "../../../etc", chrootPath, null, null);
            System.out.println("✗ Should have prevented jail escape");
            assert false;
        } catch (SecurityException e) {
            System.out.println("✓ Prevented jail escape: " + e.getMessage());
        }
        
        // Test path conversion
        String relPath = cd.toRelativePath("/home/jail/user/docs", chrootPath);
        assert "/user/docs".equals(relPath);
        System.out.println("✓ Relative path conversion: " + relPath);
        
        String absPath = cd.toAbsolutePath("/user/docs", chrootPath);
        assert "/home/jail/user/docs".equals(absPath);
        System.out.println("✓ Absolute path conversion: " + absPath);
        
        System.out.println("\n🎉 All security tests passed!");
    }
}

