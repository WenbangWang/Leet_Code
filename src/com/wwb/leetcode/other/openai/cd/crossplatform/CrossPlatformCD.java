package com.wwb.leetcode.other.openai.cd.crossplatform;

import java.net.URI;
import java.util.*;

/**
 * Cross-Platform CD Implementation
 *
 * Phase 1 (15 min): Basic Unix path navigation
 * Phase 2 (15 min): Windows path support (C:\, drive letters)
 * Phase 3 (15 min): Network paths (UNC, URLs, remote filesystems)
 *
 * Focus: Cross-platform compatibility, Windows, network filesystems
 */
public class CrossPlatformCD {

    // ========================================
    // SUPPORTING CLASSES
    // ========================================

    public enum PathType {
        UNIX,           // /home/user
        WINDOWS,        // C:\Users\\user
        UNC,            // \\server\share
        URL             // ssh://host/path, s3://bucket/path
    }

    public static class PathInfo {
        PathType type;
        String normalizedPath;
        String scheme;     // For URLs: ssh, ftp, s3, etc.
        String host;       // For UNC/URLs
        String drive;      // For Windows: C, D, etc.

        @Override
        public String toString() {
            return String.format("%s: %s", type, normalizedPath);
        }
    }


    // ========================================
    // PHASE 1: Basic Unix Path Navigation
    // ========================================

    public String phase1Unix(String currentDir, String targetDir) {
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
    // PHASE 2: Windows Path Support
    // ========================================

    /**
     * CD with Windows path support.
     *
     * Handles:
     * - Drive letters: C:\, D:\
     * - Backslashes: \Users\\user
     * - Mixed slashes: C:/Users/user
     * - Case insensitivity (optional)
     */
    public String phase2Windows(String currentDir, String targetDir) {
        PathType currentType = detectPathType(currentDir);
        PathType targetType = detectPathType(targetDir);

        // Windows absolute path
        if (targetDir.matches("^[A-Za-z]:[/\\\\].*")) {
            return normalizeWindowsPath(targetDir);
        }

        // Unix absolute path
        if (targetDir.startsWith("/")) {
            return normalizeUnixPath(targetDir);
        }

        // Relative path - use current directory's type
        if (currentType == PathType.WINDOWS) {
            return normalizeWindowsPath(currentDir + "\\" + targetDir);
        } else {
            return normalizeUnixPath(currentDir + "/" + targetDir);
        }
    }

    /**
     * Detect path type from format.
     */
    private PathType detectPathType(String path) {
        if (path.matches("^[A-Za-z]:[/\\\\].*")) {
            return PathType.WINDOWS;
        }
        if (path.startsWith("\\\\")) {
            return PathType.UNC;
        }
        if (path.contains("://")) {
            return PathType.URL;
        }
        return PathType.UNIX;
    }

    /**
     * Normalize Windows path.
     *
     * Examples:
     *   C:\Users\..\..\Windows → C:\Windows
     *   D:/Documents/./file → D:\Documents\file
     */
    private String normalizeWindowsPath(String path) {
        // Convert forward slashes to backslashes
        path = path.replace('/', '\\');

        String[] parts = path.split("\\\\");
        if (parts.length == 0) return null;

        Stack<String> stack = new Stack<>();

        // First part is drive letter (C:)
        stack.push(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            if (part.isEmpty() || part.equals(".")) {
                continue;
            } else if (part.equals("..")) {
                // Can't go above drive letter
                if (stack.size() > 1) {
                    stack.pop();
                }
            } else {
                stack.push(part);
            }
        }

        return String.join("\\", stack);
    }

    /**
     * Normalize Unix path.
     */
    private String normalizeUnixPath(String path) {
        Stack<String> stack = new Stack<>();

        for (String segment : path.split("/")) {
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

    /**
     * Convert Windows path to Unix (WSL style).
     *
     * Examples:
     *   C:\Users\\user → /mnt/c/Users/user
     *   D:\Documents → /mnt/d/Documents
     */
    public String windowsToUnix(String windowsPath) {
        if (!windowsPath.matches("^[A-Za-z]:[/\\\\].*")) {
            return windowsPath;  // Not a Windows path
        }

        char drive = Character.toLowerCase(windowsPath.charAt(0));
        String path = windowsPath.substring(2).replace('\\', '/');

        return "/mnt/" + drive + path;
    }

    /**
     * Convert Unix path to Windows (if mounted).
     */
    public String unixToWindows(String unixPath, char defaultDrive) {
        if (unixPath.startsWith("/mnt/")) {
            // WSL mount: /mnt/c/Users → C:\Users
            char drive = unixPath.charAt(5);
            String path = unixPath.substring(6).replace('/', '\\');
            return Character.toUpperCase(drive) + ":" + path;
        }

        // Convert to default drive
        return defaultDrive + ":" + unixPath.replace('/', '\\');
    }


    // ========================================
    // PHASE 3: Network Paths
    // ========================================

    /**
     * CD with network path support.
     *
     * Handles:
     * - UNC paths: \\server\share\path
     * - SSH: ssh://user@host/path
     * - FTP: ftp://host/path
     * - S3: s3://bucket/path
     * - HDFS: hdfs://namenode/path
     */
    public PathInfo phase3Network(String currentDir, String targetDir) {
        PathType type = detectPathType(targetDir);

        switch (type) {
            case UNC:
                return parseUNCPath(targetDir);
            case URL:
                return parseURLPath(targetDir);
            case WINDOWS:
                return parseWindowsPath(targetDir);
            case UNIX:
            default:
                PathInfo info = new PathInfo();
                info.type = PathType.UNIX;
                info.normalizedPath = phase1Unix(currentDir, targetDir);
                return info;
        }
    }

    /**
     * Parse UNC path: \\server\share\path
     */
    private PathInfo parseUNCPath(String uncPath) {
        PathInfo info = new PathInfo();
        info.type = PathType.UNC;

        // Remove leading \\
        String path = uncPath.substring(2);
        String[] parts = path.split("\\\\");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid UNC path: " + uncPath);
        }

        info.host = parts[0];
        String share = parts[1];

        // Normalize remaining path
        Stack<String> stack = new Stack<>();
        stack.push(parts[0]);
        stack.push(share);

        for (int i = 2; i < parts.length; i++) {
            if (parts[i].isEmpty() || parts[i].equals(".")) {
                continue;
            } else if (parts[i].equals("..")) {
                if (stack.size() > 2) {  // Can't go above share
                    stack.pop();
                }
            } else {
                stack.push(parts[i]);
            }
        }

        info.normalizedPath = "\\\\" + String.join("\\", stack);
        return info;
    }

    /**
     * Parse URL path: scheme://host/path
     */
    private PathInfo parseURLPath(String urlPath) {
        PathInfo info = new PathInfo();
        info.type = PathType.URL;

        try {
            URI uri = new URI(urlPath);
            info.scheme = uri.getScheme();
            info.host = uri.getHost();

            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }

            // Normalize the path component
            String normalized = normalizeUnixPath(path);

            // Reconstruct URL
            StringBuilder sb = new StringBuilder();
            sb.append(info.scheme).append("://");

            if (uri.getUserInfo() != null) {
                sb.append(uri.getUserInfo()).append("@");
            }

            sb.append(info.host);

            if (uri.getPort() != -1) {
                sb.append(":").append(uri.getPort());
            }

            sb.append(normalized);

            if (uri.getQuery() != null) {
                sb.append("?").append(uri.getQuery());
            }

            info.normalizedPath = sb.toString();
            return info;

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + urlPath, e);
        }
    }

    /**
     * Parse Windows path.
     */
    private PathInfo parseWindowsPath(String windowsPath) {
        PathInfo info = new PathInfo();
        info.type = PathType.WINDOWS;
        info.drive = String.valueOf(windowsPath.charAt(0));
        info.normalizedPath = normalizeWindowsPath(windowsPath);
        return info;
    }

    /**
     * Convert between path formats.
     */
    public String convertPath(String path, PathType targetType) {
        PathType sourceType = detectPathType(path);

        if (sourceType == targetType) {
            return path;
        }

        // Convert Unix to Windows
        if (sourceType == PathType.UNIX && targetType == PathType.WINDOWS) {
            return unixToWindows(path, 'C');
        }

        // Convert Windows to Unix
        if (sourceType == PathType.WINDOWS && targetType == PathType.UNIX) {
            return windowsToUnix(path);
        }

        // Convert UNC to Unix (network mount)
        if (sourceType == PathType.UNC && targetType == PathType.UNIX) {
            PathInfo info = parseUNCPath(path);
            return "/net/" + info.host + "/" + path.substring(2 + info.host.length() + 1).replace('\\', '/');
        }

        throw new UnsupportedOperationException(
            "Conversion from " + sourceType + " to " + targetType + " not implemented"
        );
    }


    // ========================================
    // TESTS
    // ========================================

    public static void main(String[] args) {
        CrossPlatformCD cd = new CrossPlatformCD();

        System.out.println("=== PHASE 1: Unix Paths ===");
        assert "/home/user/docs".equals(cd.phase1Unix("/home/user", "docs"));
        assert "/etc".equals(cd.phase1Unix("/home/user", "/etc"));
        System.out.println("✓ Phase 1 tests passed!\n");

        System.out.println("=== PHASE 2: Windows Paths ===");

        // Windows absolute path
        String result = cd.phase2Windows("C:\\Users\\alice", "D:\\Documents");
        assert "D:\\Documents".equals(result);
        System.out.println("✓ Windows absolute: " + result);

        // Windows relative path with ..
        result = cd.phase2Windows("C:\\Users\\alice", "..\\..\\Windows");
        assert "C:\\Windows".equals(result);
        System.out.println("✓ Windows relative: " + result);

        // Mixed slashes
        result = cd.normalizeWindowsPath("C:/Users/alice/../bob/./documents");
        assert "C:\\Users\\bob\\documents".equals(result);
        System.out.println("✓ Mixed slashes: " + result);

        // Windows to Unix conversion
        result = cd.windowsToUnix("C:\\Users\\alice\\documents");
        assert "/mnt/c/Users/alice/documents".equals(result);
        System.out.println("✓ Windows to Unix: " + result);

        // Unix to Windows conversion
        result = cd.unixToWindows("/mnt/d/Projects", 'C');
        assert "D:\\Projects".equals(result);
        System.out.println("✓ Unix to Windows: " + result);

        System.out.println("✓ Phase 2 tests passed!\n");

        System.out.println("=== PHASE 3: Network Paths ===");

        // UNC path
        PathInfo info = cd.phase3Network("/", "\\\\server\\share\\folder\\file");
        assert PathType.UNC.equals(info.type);
        assert "server".equals(info.host);
        System.out.println("✓ UNC path: " + info);

        // SSH URL
        info = cd.phase3Network("/", "ssh://user@host.com/home/user/../documents");
        assert PathType.URL.equals(info.type);
        assert "ssh".equals(info.scheme);
        assert "host.com".equals(info.host);
        assert info.normalizedPath.contains("/home/documents");
        System.out.println("✓ SSH URL: " + info);

        // S3 URL
        info = cd.phase3Network("/", "s3://my-bucket/data/./files/../archive");
        assert PathType.URL.equals(info.type);
        assert "s3".equals(info.scheme);
        assert info.normalizedPath.contains("/data/archive");
        System.out.println("✓ S3 URL: " + info);

        // HDFS URL with port
        info = cd.phase3Network("/", "hdfs://namenode:9000/user/hadoop/data");
        assert PathType.URL.equals(info.type);
        assert "hdfs".equals(info.scheme);
        System.out.println("✓ HDFS URL: " + info);

        // Path conversion
        result = cd.convertPath("/home/user/docs", PathType.WINDOWS);
        assert result.contains(":\\");
        System.out.println("✓ Unix to Windows conversion: " + result);

        result = cd.convertPath("C:\\Users\\alice", PathType.UNIX);
        assert "/mnt/c/Users/alice".equals(result);
        System.out.println("✓ Windows to Unix conversion: " + result);

        System.out.println("✓ Phase 3 tests passed!");
        System.out.println("\n🎉 All cross-platform tests passed!");
    }
}

