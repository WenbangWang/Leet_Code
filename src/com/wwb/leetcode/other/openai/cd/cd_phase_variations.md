# CD Command: Creative Phase Variations & Alternative Progressions

## Overview

This document explores **creative alternatives** to the standard 3-phase CD implementation. Different interviewers may emphasize different aspects, and understanding multiple progression paths shows depth and adaptability.

---

## 🎨 Alternative Phase Progressions

### **Variation A: Path Focus (Standard)**
```
Phase 1: Basic path navigation (., .., absolute/relative)
Phase 2: Special characters (~, environment variables)
Phase 3: Symlinks with cycle detection
```
**Focus**: String manipulation → Features → Graph algorithms

---

### **Variation B: Security & Permissions Focus**
```
Phase 1: Basic path navigation
Phase 2: Permission checking (read, write, execute)
Phase 3: User/group permissions and chroot jails
Phase 4: Security contexts (SELinux, AppArmor)
```
**Focus**: System security, access control

**Phase 2 Example:**
```java
enum Permission { READ, WRITE, EXECUTE }

class FilePermissions {
    String owner;
    String group;
    Set<Permission> ownerPerms;
    Set<Permission> groupPerms;
    Set<Permission> otherPerms;
}

public String cd(String currentDir, String targetDir, 
                 Map<String, FilePermissions> permissions,
                 User currentUser) {
    String finalPath = normalize(currentDir, targetDir);
    
    // Check execute permission on each directory
    for (String component : getPathComponents(finalPath)) {
        if (!hasExecutePermission(component, permissions, currentUser)) {
            throw new PermissionDeniedException(component);
        }
    }
    return finalPath;
}
```

**Phase 3 Example: Chroot Jails**
```java
public String cd(String currentDir, String targetDir, String chrootPath) {
    String finalPath = normalize(currentDir, targetDir);
    
    // Cannot escape chroot
    if (!finalPath.startsWith(chrootPath)) {
        throw new SecurityException("Cannot escape chroot: " + chrootPath);
    }
    
    return finalPath;
}
```

---

### **Variation C: Performance & Caching Focus**
```
Phase 1: Basic path navigation
Phase 2: Implement LRU cache for path resolutions
Phase 3: Handle concurrent access with read-write locks
Phase 4: Distributed caching across multiple servers
```
**Focus**: Performance optimization, concurrency

**Phase 2 Example:**
```java
public class CachedCDCommand {
    private final LRUCache<String, String> pathCache;
    
    public CachedCDCommand(int cacheSize) {
        this.pathCache = new LRUCache<>(cacheSize);
    }
    
    public String cd(String currentDir, String targetDir) {
        String cacheKey = currentDir + "::" + targetDir;
        
        return pathCache.computeIfAbsent(cacheKey, k -> {
            return normalizePathSlow(currentDir, targetDir);
        });
    }
    
    // Follow-up: What invalidates the cache?
    // Answer: Symlink changes, filesystem modifications
}
```

**Phase 3 Example:**
```java
public class ConcurrentCDCommand {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile Map<String, String> symlinks;
    
    public String cd(String currentDir, String targetDir) {
        lock.readLock().lock();
        try {
            return resolveWithSymlinks(currentDir, targetDir, symlinks);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void updateSymlink(String link, String target) {
        lock.writeLock().lock();
        try {
            Map<String, String> newSymlinks = new HashMap<>(symlinks);
            newSymlinks.put(link, target);
            symlinks = newSymlinks;  // Copy-on-write
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

---

### **Variation D: File System Features Focus**
```
Phase 1: Basic path navigation
Phase 2: Hard links (inode-based resolution)
Phase 3: Mount points and filesystem boundaries
Phase 4: Network filesystems (NFS, SMB paths)
```
**Focus**: Real file system internals

**Phase 2 Example: Hard Links**
```java
class Inode {
    int inodeNumber;
    FileType type;
    Map<String, Integer> entries;  // name → inode for directories
}

class FileSystem {
    Map<Integer, Inode> inodes;
    Map<String, Integer> pathToInode;  // cache
    
    public int resolvePath(String path) {
        // Multiple paths can point to same inode
        if (pathToInode.containsKey(path)) {
            return pathToInode.get(path);
        }
        
        // Walk the path through directory inodes
        int currentInode = ROOT_INODE;
        for (String component : path.split("/")) {
            Inode dir = inodes.get(currentInode);
            currentInode = dir.entries.get(component);
        }
        
        pathToInode.put(path, currentInode);
        return currentInode;
    }
}
```

**Phase 3 Example: Mount Points**
```java
class MountPoint {
    String mountPath;
    String devicePath;
    String filesystemType;
}

public String cd(String currentDir, String targetDir,
                 List<MountPoint> mounts) {
    String finalPath = normalize(currentDir, targetDir);
    
    // Check if crossing mount boundaries
    MountPoint currentMount = findMount(currentDir, mounts);
    MountPoint targetMount = findMount(finalPath, mounts);
    
    if (!currentMount.equals(targetMount)) {
        // Crossing filesystem boundary
        System.out.println("Crossing mount: " + currentMount.mountPath 
                         + " → " + targetMount.mountPath);
    }
    
    return finalPath;
}
```

---

### **Variation E: Network & Remote Paths**
```
Phase 1: Basic path navigation
Phase 2: UNC paths (Windows) and network shares
Phase 3: URL-style paths (ftp://, ssh://, s3://)
Phase 4: Distributed filesystem (HDFS, GFS)
```
**Focus**: Remote and distributed systems

**Phase 2 Example:**
```java
public String cd(String currentDir, String targetDir) {
    // Handle UNC paths: \\server\share\path
    if (targetDir.startsWith("\\\\")) {
        return resolveUNCPath(targetDir);
    }
    
    // Handle URLs: ssh://user@host/path
    if (targetDir.contains("://")) {
        return resolveURLPath(targetDir);
    }
    
    return normalizeLocalPath(currentDir, targetDir);
}

private String resolveUNCPath(String uncPath) {
    // \\server\share\folder\file
    String[] parts = uncPath.split("\\\\");
    String server = parts[2];
    String share = parts[3];
    String path = String.join("/", Arrays.copyOfRange(parts, 4, parts.length));
    
    return String.format("//%s/%s/%s", server, share, path);
}
```

**Phase 3 Example:**
```java
public String cd(String currentDir, String targetDir) {
    URI uri = parseURI(targetDir);
    
    switch (uri.getScheme()) {
        case "file":
            return normalizeLocalPath(uri.getPath());
        case "ssh":
        case "sftp":
            return resolveSSHPath(uri);
        case "s3":
            return resolveS3Path(uri);
        case "hdfs":
            return resolveHDFSPath(uri);
        default:
            throw new UnsupportedOperationException("Unknown scheme: " + uri.getScheme());
    }
}
```

---

### **Variation F: History & State Management**
```
Phase 1: Basic path navigation
Phase 2: Implement cd history (cd -, cd .., pushd/popd)
Phase 3: Session management and working directory stack
Phase 4: Persistent history across sessions
```
**Focus**: Stateful shell features

**Phase 2 Example:**
```java
public class CDWithHistory {
    private Stack<String> dirStack = new Stack<>();
    private String previousDir = null;
    private String currentDir = "/";
    
    public String cd(String targetDir) {
        if (targetDir.equals("-")) {
            // cd - goes to previous directory
            if (previousDir == null) {
                throw new IllegalStateException("No previous directory");
            }
            String temp = currentDir;
            currentDir = previousDir;
            previousDir = temp;
            return currentDir;
        }
        
        String newDir = normalize(currentDir, targetDir);
        previousDir = currentDir;
        currentDir = newDir;
        return currentDir;
    }
    
    public void pushd(String targetDir) {
        dirStack.push(currentDir);
        currentDir = normalize(currentDir, targetDir);
    }
    
    public String popd() {
        if (dirStack.isEmpty()) {
            throw new IllegalStateException("Directory stack empty");
        }
        currentDir = dirStack.pop();
        return currentDir;
    }
    
    public List<String> dirs() {
        List<String> result = new ArrayList<>();
        result.add(currentDir);
        result.addAll(dirStack);
        return result;
    }
}
```

**Phase 3 Example: Persistent History**
```java
public class PersistentCDHistory {
    private static final String HISTORY_FILE = ".cd_history";
    private final Deque<String> history = new ArrayDeque<>(100);
    
    public void recordVisit(String directory) {
        history.addFirst(directory);
        if (history.size() > 100) {
            history.removeLast();
        }
        persistHistory();
    }
    
    public String cdByNumber(int n) {
        // cd !42 goes to 42nd directory in history
        if (n < 0 || n >= history.size()) {
            throw new IllegalArgumentException("Invalid history index: " + n);
        }
        return new ArrayList<>(history).get(n);
    }
    
    public List<String> frequentDirs() {
        // Return most frequently visited directories
        return history.stream()
            .collect(Collectors.groupingBy(d -> d, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
```

---

### **Variation G: Validation & Error Handling**
```
Phase 1: Basic path navigation
Phase 2: Validate paths exist using a filesystem tree
Phase 3: Handle errors gracefully (not found, permission denied)
Phase 4: Provide helpful suggestions for typos
```
**Focus**: User experience, robustness

**Phase 2 Example:**
```java
class FileSystemTree {
    TrieNode root;
    
    public boolean exists(String path) {
        String[] components = path.split("/");
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
    
    public List<String> listDirectory(String path) {
        TrieNode node = findNode(path);
        return new ArrayList<>(node.children.keySet());
    }
}

public String cd(String currentDir, String targetDir, FileSystemTree fs) {
    String finalPath = normalize(currentDir, targetDir);
    
    if (!fs.exists(finalPath)) {
        throw new FileNotFoundException("No such directory: " + finalPath);
    }
    
    return finalPath;
}
```

**Phase 4 Example: Smart Suggestions**
```java
public String cdWithSuggestions(String currentDir, String targetDir, FileSystemTree fs) {
    String finalPath = normalize(currentDir, targetDir);
    
    if (!fs.exists(finalPath)) {
        // Find similar directories
        List<String> suggestions = findSimilarPaths(finalPath, fs);
        
        if (!suggestions.isEmpty()) {
            throw new FileNotFoundException(
                "No such directory: " + finalPath + "\n" +
                "Did you mean: " + String.join(", ", suggestions)
            );
        }
        throw new FileNotFoundException("No such directory: " + finalPath);
    }
    
    return finalPath;
}

private List<String> findSimilarPaths(String path, FileSystemTree fs) {
    String parent = getParent(path);
    String target = getBaseName(path);
    
    List<String> candidates = fs.listDirectory(parent);
    
    // Use Levenshtein distance to find similar names
    return candidates.stream()
        .filter(c -> levenshteinDistance(c, target) <= 2)
        .sorted(Comparator.comparingInt(c -> levenshteinDistance(c, target)))
        .limit(3)
        .collect(Collectors.toList());
}
```

---

### **Variation H: Pattern Matching & Wildcards**
```
Phase 1: Basic path navigation
Phase 2: Single directory wildcards (*, ?)
Phase 3: Recursive wildcards (**/pattern)
Phase 4: Regular expressions and advanced patterns
```
**Focus**: Pattern matching algorithms

**Phase 2 Example:**
```java
public List<String> cdWithWildcard(String currentDir, String pattern, FileSystemTree fs) {
    // cd /home/user*/documents
    
    if (!containsWildcard(pattern)) {
        return Collections.singletonList(cd(currentDir, pattern));
    }
    
    String[] components = pattern.split("/");
    List<String> currentPaths = Collections.singletonList(
        pattern.startsWith("/") ? "/" : currentDir
    );
    
    for (String component : components) {
        if (component.isEmpty()) continue;
        
        List<String> nextPaths = new ArrayList<>();
        for (String path : currentPaths) {
            if (isWildcard(component)) {
                // Expand wildcard
                List<String> matches = matchPattern(component, fs.listDirectory(path));
                for (String match : matches) {
                    nextPaths.add(path + "/" + match);
                }
            } else {
                nextPaths.add(path + "/" + component);
            }
        }
        currentPaths = nextPaths;
    }
    
    return currentPaths;
}

private boolean matchesPattern(String text, String pattern) {
    // * matches any sequence, ? matches single char
    return text.matches(pattern.replace("*", ".*").replace("?", "."));
}
```

---

### **Variation I: Transaction & Rollback**
```
Phase 1: Basic path navigation
Phase 2: Implement transaction log
Phase 3: Rollback/undo functionality
Phase 4: Multi-step atomic operations
```
**Focus**: Transaction management, state consistency

**Phase 2 Example:**
```java
public class TransactionalCD {
    private String currentDir = "/";
    private final List<CDOperation> transactionLog = new ArrayList<>();
    
    static class CDOperation {
        String from;
        String to;
        long timestamp;
    }
    
    public String cd(String targetDir) {
        String previousDir = currentDir;
        String newDir = normalize(currentDir, targetDir);
        
        CDOperation op = new CDOperation();
        op.from = previousDir;
        op.to = newDir;
        op.timestamp = System.currentTimeMillis();
        
        transactionLog.add(op);
        currentDir = newDir;
        
        return currentDir;
    }
    
    public String undo() {
        if (transactionLog.isEmpty()) {
            throw new IllegalStateException("Nothing to undo");
        }
        
        CDOperation lastOp = transactionLog.remove(transactionLog.size() - 1);
        currentDir = lastOp.from;
        return currentDir;
    }
    
    public void beginTransaction() {
        transactionLog.add(new CDOperation());  // marker
    }
    
    public void rollback() {
        // Rollback to last transaction marker
        while (!transactionLog.isEmpty()) {
            CDOperation op = transactionLog.remove(transactionLog.size() - 1);
            if (op.from == null) break;  // marker
            currentDir = op.from;
        }
    }
}
```

---

### **Variation J: Cross-Platform Compatibility**
```
Phase 1: Basic Unix path navigation
Phase 2: Windows path support (C:\, drive letters)
Phase 3: Normalize between Unix and Windows formats
Phase 4: Handle case sensitivity differences
```
**Focus**: Cross-platform development

**Phase 2 Example:**
```java
public class CrossPlatformCD {
    enum PathType { UNIX, WINDOWS, UNC }
    
    public String cd(String currentDir, String targetDir) {
        PathType currentType = detectPathType(currentDir);
        PathType targetType = detectPathType(targetDir);
        
        // Handle Windows absolute paths
        if (targetDir.matches("[A-Z]:\\\\.*")) {
            return normalizeWindowsPath(targetDir);
        }
        
        // Handle Unix absolute paths
        if (targetDir.startsWith("/")) {
            return normalizeUnixPath(targetDir);
        }
        
        // Relative path - follow current type
        if (currentType == PathType.WINDOWS) {
            return normalizeWindowsPath(currentDir + "\\" + targetDir);
        } else {
            return normalizeUnixPath(currentDir + "/" + targetDir);
        }
    }
    
    private String normalizeWindowsPath(String path) {
        // C:\Users\..\..\Windows → C:\Windows
        String[] parts = path.split("\\\\");
        Stack<String> stack = new Stack<>();
        
        stack.push(parts[0]);  // Drive letter C:
        
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].equals("..")) {
                if (stack.size() > 1) stack.pop();
            } else if (!parts[i].equals(".") && !parts[i].isEmpty()) {
                stack.push(parts[i]);
            }
        }
        
        return String.join("\\", stack);
    }
    
    public String convertToUnix(String windowsPath) {
        // C:\Users\file → /mnt/c/Users/file (WSL style)
        if (windowsPath.matches("[A-Z]:\\\\.*")) {
            char drive = Character.toLowerCase(windowsPath.charAt(0));
            String path = windowsPath.substring(3).replace("\\", "/");
            return "/mnt/" + drive + "/" + path;
        }
        return windowsPath;
    }
}
```

---

## 🎯 Creative Hybrid Progressions

### **Progression 1: Practical Shell Features**
```
Phase 1: Basic navigation
Phase 2: Tilde + environment variables
Phase 3: History (cd -, pushd/popd)
Phase 4: Auto-completion suggestions
```

### **Progression 2: System Engineer Focus**
```
Phase 1: Basic navigation
Phase 2: Symlinks
Phase 3: Permissions
Phase 4: Chroot/containers
```

### **Progression 3: Performance Engineer Focus**
```
Phase 1: Basic navigation
Phase 2: Simple caching
Phase 3: Concurrent access
Phase 4: Distributed consensus
```

### **Progression 4: Product Engineer Focus**
```
Phase 1: Basic navigation
Phase 2: Validation with helpful errors
Phase 3: Smart suggestions for typos
Phase 4: Usage analytics and recommendations
```

---

## 🧠 Interview Adaptation Strategies

### **Read the Interviewer's Signals**

**If they mention "performance" or "scale":**
→ Expect caching, concurrency, optimization phases

**If they mention "security" or "production":**
→ Expect permissions, validation, error handling phases

**If they mention "user experience":**
→ Expect suggestions, history, helpful features phases

**If they mention "distributed systems":**
→ Expect network paths, consistency, remote filesystem phases

---

### **Flexible Implementation Strategy**

**Start with solid Phase 1:**
```java
// This foundation works for ALL variations
public String cdBasic(String currentDir, String targetDir) {
    String path = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
    
    Stack<String> stack = new Stack<>();
    for (String seg : path.split("/")) {
        if (seg.isEmpty() || seg.equals(".")) continue;
        if (seg.equals("..")) {
            if (stack.isEmpty()) return null;
            stack.pop();
        } else {
            stack.push(seg);
        }
    }
    
    return "/" + String.join("/", stack);
}
```

**Then adapt based on Phase 2 direction:**
- Symlinks? → Add resolver
- Permissions? → Add checker
- Caching? → Add LRU cache
- History? → Add stack
- Validation? → Add filesystem tree
- Windows? → Add path type detection

---

## 💡 Creative Follow-up Questions

### **Unexpected but Logical**

1. **"How would you implement cdpath (search multiple directories)?"**
```java
public String cd(String currentDir, String targetDir, List<String> cdpath) {
    if (targetDir.startsWith("/")) {
        return normalize(targetDir);
    }
    
    // Try current directory first
    String result = cdBasic(currentDir, targetDir);
    if (exists(result)) return result;
    
    // Search cdpath
    for (String searchDir : cdpath) {
        result = cdBasic(searchDir, targetDir);
        if (exists(result)) return result;
    }
    
    throw new FileNotFoundException("Not found in current dir or CDPATH");
}
```

2. **"Implement auto-correction for typos"**
```java
public String cdWithCorrection(String currentDir, String targetDir, FileSystemTree fs) {
    String path = normalize(currentDir, targetDir);
    
    if (fs.exists(path)) {
        return path;
    }
    
    // Try common typos
    List<String> alternatives = Arrays.asList(
        path,  // original
        path.toLowerCase(),  // case
        fixDoubleSlash(path),  // //
        removeTrailingSlash(path),
        fixCommonTypos(path)  // documetns → documents
    );
    
    for (String alt : alternatives) {
        if (fs.exists(alt)) {
            System.out.println("Auto-corrected to: " + alt);
            return alt;
        }
    }
    
    throw new FileNotFoundException(path);
}
```

3. **"Handle symbolic links that are relative paths"**
```java
// Symlink: /home/user/link → ../shared (relative!)
private String resolveRelativeSymlink(String symlinkPath, String target) {
    if (target.startsWith("/")) {
        return target;  // absolute
    }
    
    // Resolve relative to symlink's directory
    String symlinkDir = symlinkPath.substring(0, symlinkPath.lastIndexOf('/'));
    if (symlinkDir.isEmpty()) symlinkDir = "/";
    
    return normalize(symlinkDir, target);
}
```

---

## 🎓 Meta-Learning: What Makes a Good Phase?

### **Characteristics of Well-Designed Phases:**

1. **Independence**: Can complete without knowing next phase exists
2. **Progressive complexity**: Each phase is noticeably harder
3. **Natural extension**: Feels like a logical next step
4. **Time-bounded**: Reasonable to complete in allotted time
5. **Discussion-rich**: Creates opportunities for trade-off discussion

### **Anti-patterns (Bad Phase Design):**

❌ **Phase leap**: Phase 1 is easy, Phase 2 is impossibly hard
❌ **Orthogonal**: Phases don't build on each other
❌ **Trivial**: Phase adds no complexity or learning
❌ **Open-ended**: No clear completion criteria

---

## 🚀 Practice Recommendation

**For your interview prep:**

1. **Master the standard progression** (path → tilde → symlinks)
2. **Pick 2-3 variations** that interest you
3. **Practice pivoting** when interviewer changes direction

**Example pivot:**
```
You: [Completes Phase 1]
Interviewer: "Great! Now add permission checking"
You: [Internally: Not symlinks? Okay, security focus]
      "Sure! I'll add execute permission validation on each path component..."
```

Being able to adapt shows **real engineering maturity**.

---

## 📚 Summary

The CD command is **infinitely extensible**. Key insight:

> **The core algorithm (stack-based normalization) is the same.**
> **What changes is what you layer on top of it.**

Layers can be:
- Security (permissions, chroot)
- Performance (caching, concurrency)  
- Features (history, wildcards)
- Compatibility (Windows, network)
- UX (suggestions, validation)

**Interview strategy**: Start solid, adapt quickly, discuss trade-offs.

Good luck! 🎉

