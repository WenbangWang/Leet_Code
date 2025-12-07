# CD Command: Follow-up Questions & Advanced Topics

## Overview

After completing the basic 3-phase CD implementation, interviewers often ask follow-up questions to explore:
- System design thinking
- Real-world constraints
- Optimization skills
- Edge case handling
- Concurrency awareness

This document covers common follow-ups based on actual OpenAI interview reports.

---

## Category 1: Extended Functionality

### Q1: "What if we need to support environment variables?"

**Example**: `cd("/home", "$HOME/documents")`

**Answer:**

```java
public String cd(String currentDir, String targetDir, 
                 String homeDir, Map<String, String> symlinks,
                 Map<String, String> envVars) {
    
    // 1. Expand environment variables first
    targetDir = expandEnvVars(targetDir, envVars);
    
    // 2. Then handle tilde expansion
    if (targetDir.equals("~")) {
        targetDir = homeDir;
    } else if (targetDir.startsWith("~/")) {
        targetDir = homeDir + targetDir.substring(1);
    }
    
    // 3. Basic cd + symlink resolution
    return cdWithSymlinks(currentDir, targetDir, symlinks);
}

private String expandEnvVars(String path, Map<String, String> envVars) {
    // Handle $VAR or ${VAR} syntax
    for (Map.Entry<String, String> entry : envVars.entrySet()) {
        String var = entry.getKey();
        String value = entry.getValue();
        
        path = path.replace("$" + var, value);
        path = path.replace("${" + var + "}", value);
    }
    return path;
}
```

**Discussion Points:**
- Order matters: env vars → tilde → symlinks
- Handle both `$VAR` and `${VAR}` syntax
- What if env var doesn't exist? (keep literal, throw error, or use default)
- Escaping: `\$VAR` should not expand

---

### Q2: "Support wildcard expansion (globbing)?"

**Example**: `cd("/home", "user*")` with directories `user1`, `user2`

**Answer:**

This changes the return type fundamentally:

```java
public List<String> cdWithGlob(String currentDir, String targetDir) {
    // 1. Check if targetDir contains wildcards
    if (!targetDir.contains("*") && !targetDir.contains("?")) {
        return Collections.singletonList(cd(currentDir, targetDir));
    }
    
    // 2. Need file system access to enumerate matching directories
    // This requires a new parameter: Set<String> validPaths
    
    List<String> results = new ArrayList<>();
    String pattern = convertGlobToRegex(targetDir);
    
    for (String validPath : validPaths) {
        if (validPath.matches(pattern)) {
            String result = cd(currentDir, validPath);
            if (result != null) {
                results.add(result);
            }
        }
    }
    
    return results;
}
```

**Key Insight:**
> "Wildcards require file system access, which changes the problem fundamentally. The original cd is a pure path manipulation problem. With wildcards, we need to query actual directories."

---

### Q3: "What about permission checking?"

**Example**: User can't cd into `/root` without permissions

**Answer:**

```java
public String cd(String currentDir, String targetDir,
                 Map<String, Set<Permission>> permissions,
                 String currentUser) {
    
    String finalPath = cdBasic(currentDir, targetDir);
    if (finalPath == null) {
        return null;
    }
    
    // Check if user has execute permission on each directory in path
    if (!hasTraversePermission(finalPath, permissions, currentUser)) {
        throw new PermissionDeniedException(
            "Permission denied: " + finalPath
        );
    }
    
    return finalPath;
}

private boolean hasTraversePermission(String path, 
                                     Map<String, Set<Permission>> perms,
                                     String user) {
    // Check each directory component
    String[] segments = path.split("/");
    StringBuilder current = new StringBuilder();
    
    for (String seg : segments) {
        if (seg.isEmpty()) continue;
        current.append("/").append(seg);
        
        if (!perms.getOrDefault(current.toString(), Set.of())
                  .contains(Permission.EXECUTE)) {
            return false;
        }
    }
    return true;
}
```

**Discussion:**
- Unix requires EXECUTE permission to traverse a directory
- Need to check ALL directories in the path, not just the final one
- Different from READ permission (listing contents)

---

## Category 2: File System Integration

### Q4: "How would you verify the final path exists?"

**Answer:**

```java
public String cd(String currentDir, String targetDir, 
                 Set<String> validPaths) {
    
    String finalPath = cdBasic(currentDir, targetDir);
    if (finalPath == null) {
        return null;
    }
    
    if (!validPaths.contains(finalPath)) {
        throw new IllegalArgumentException(
            "No such directory: " + finalPath
        );
    }
    
    return finalPath;
}
```

**Follow-up: "What if validPaths is huge (millions of paths)?"**

```java
// Use Trie for efficient prefix checking
public class FileSystemTrie {
    private TrieNode root = new TrieNode();
    
    public void addPath(String path) {
        String[] segments = path.split("/");
        TrieNode current = root;
        
        for (String seg : segments) {
            if (seg.isEmpty()) continue;
            current.children.putIfAbsent(seg, new TrieNode());
            current = current.children.get(seg);
        }
        current.isDirectory = true;
    }
    
    public boolean exists(String path) {
        String[] segments = path.split("/");
        TrieNode current = root;
        
        for (String seg : segments) {
            if (seg.isEmpty()) continue;
            if (!current.children.containsKey(seg)) {
                return false;
            }
            current = current.children.get(seg);
        }
        return current.isDirectory;
    }
}
```

---

### Q5: "What if we need to track actual inodes (real Linux file system)?"

**Answer:**

> "In a real Linux file system:
> 
> 1. **Inodes**: Each file/directory has a unique inode number
> 2. **Directory entries**: Map names to inode numbers
> 3. **Hard links**: Multiple names → same inode
> 4. **Symlinks**: Special file type containing target path
> 
> Our current implementation models the path layer, not the inode layer. For a full implementation:

```java
class Inode {
    int inodeNumber;
    FileType type;  // DIRECTORY, REGULAR_FILE, SYMLINK
    String symlinkTarget;  // if type == SYMLINK
    Map<String, Integer> entries;  // if type == DIRECTORY (name → inode)
}

class FileSystem {
    Map<Integer, Inode> inodes;  // inode number → inode
    int rootInodeNumber = 0;
    
    public int resolvePath(String path) {
        int currentInode = rootInodeNumber;
        String[] segments = path.split("/");
        
        for (String seg : segments) {
            if (seg.isEmpty()) continue;
            
            Inode current = inodes.get(currentInode);
            
            if (current.type == FileType.SYMLINK) {
                // Follow symlink
                currentInode = resolvePath(current.symlinkTarget);
                current = inodes.get(currentInode);
            }
            
            if (current.type != FileType.DIRECTORY) {
                throw new NotADirectoryException();
            }
            
            if (!current.entries.containsKey(seg)) {
                throw new NoSuchFileException();
            }
            
            currentInode = current.entries.get(seg);
        }
        
        return currentInode;
    }
}
```

---

## Category 3: Performance & Optimization

### Q6: "How would you cache normalized paths?"

**Answer:**

```java
public class CachedCDCommand {
    private final Map<String, String> pathCache;
    private final int maxCacheSize;
    
    public CachedCDCommand(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        // LRU cache
        this.pathCache = new LinkedHashMap<>(maxCacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > maxCacheSize;
            }
        };
    }
    
    public String cd(String currentDir, String targetDir) {
        String cacheKey = currentDir + "||" + targetDir;
        
        // Check cache
        if (pathCache.containsKey(cacheKey)) {
            return pathCache.get(cacheKey);
        }
        
        // Compute
        String result = cdBasic(currentDir, targetDir);
        
        // Cache result
        if (result != null) {
            pathCache.put(cacheKey, result);
        }
        
        return result;
    }
    
    public void invalidateCache() {
        pathCache.clear();
    }
}
```

**Discussion:**
- When to invalidate? If symlinks change, must clear cache
- Cache key: combine currentDir + targetDir
- LRU eviction to bound memory
- Thread safety: use ConcurrentHashMap or synchronize

---

### Q7: "What if cd is called millions of times per second?"

**Answer:**

**Optimization strategies:**

1. **Immutable String Optimization:**
```java
// Bad: Creates many intermediate strings
String path = currentDir + "/" + targetDir;

// Better: Use StringBuilder
StringBuilder sb = new StringBuilder(currentDir.length() + targetDir.length() + 1);
sb.append(currentDir).append("/").append(targetDir);
String path = sb.toString();
```

2. **Avoid String.split():**
```java
// Bad: Creates array, allocates memory
String[] segments = path.split("/");

// Better: Manual tokenization
List<String> segments = new ArrayList<>();
int start = 0;
for (int i = 0; i < path.length(); i++) {
    if (path.charAt(i) == '/') {
        if (i > start) {
            segments.add(path.substring(start, i));
        }
        start = i + 1;
    }
}
if (start < path.length()) {
    segments.add(path.substring(start));
}
```

3. **Object Pooling:**
```java
// Reuse Stack objects
private ThreadLocal<Stack<String>> stackPool = 
    ThreadLocal.withInitial(Stack::new);

public String cd(String currentDir, String targetDir) {
    Stack<String> stack = stackPool.get();
    stack.clear();  // Reuse instead of allocate
    
    // ... use stack ...
    
    String result = "/" + String.join("/", stack);
    // Don't clear stack yet - keep for next call
    return result;
}
```

4. **Precompute Common Paths:**
```java
// If certain paths are accessed frequently
private static final Map<String, String> COMMON_PATHS = Map.of(
    "~", "/home/user",
    "~/documents", "/home/user/documents",
    "~/downloads", "/home/user/downloads"
);

public String cd(String currentDir, String targetDir) {
    // Fast path for common cases
    if (COMMON_PATHS.containsKey(targetDir)) {
        return COMMON_PATHS.get(targetDir);
    }
    
    // General case
    return cdBasic(currentDir, targetDir);
}
```

---

## Category 4: Concurrency

### Q8: "Is your implementation thread-safe?"

**Answer:**

**Current implementation analysis:**

```java
public String cd(String currentDir, String targetDir, Map<String, String> symlinks) {
    // Local variables - thread-safe ✓
    String path = cdBasic(currentDir, targetDir);
    
    // New Stack per call - thread-safe ✓
    Stack<String> stack = new Stack<>();
    
    // New HashSet per call - thread-safe ✓
    Set<String> visited = new HashSet<>();
    
    // Reading from symlinks Map
    // Thread-safe IF symlinks is immutable ✓
    // NOT thread-safe if symlinks can be modified ✗
}
```

**Conclusion:**
> "The implementation is thread-safe for concurrent reads, assuming the symlinks map is either immutable or externally synchronized. If the symlinks map can be modified concurrently, we need additional synchronization."

**For concurrent writes:**

```java
public class ThreadSafeCDCommand {
    private final ConcurrentHashMap<String, String> symlinks;
    
    public void updateSymlink(String link, String target) {
        symlinks.put(link, target);
        // Invalidate any caches
    }
    
    public String cd(String currentDir, String targetDir) {
        // Create snapshot to prevent concurrent modification
        Map<String, String> snapshot = new HashMap<>(symlinks);
        return cdBasic(currentDir, targetDir, snapshot);
    }
}
```

---

### Q9: "What if multiple threads are modifying symlinks?"

**Answer:**

**Approach 1: Read-Write Lock**

```java
public class ThreadSafeCDCommand {
    private final Map<String, String> symlinks = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    
    public String cd(String currentDir, String targetDir) {
        readLock.lock();
        try {
            return cdBasic(currentDir, targetDir, symlinks);
        } finally {
            readLock.unlock();
        }
    }
    
    public void addSymlink(String link, String target) {
        writeLock.lock();
        try {
            symlinks.put(link, target);
        } finally {
            writeLock.unlock();
        }
    }
}
```

**Approach 2: Copy-on-Write**

```java
public class ThreadSafeCDCommand {
    private volatile Map<String, String> symlinks = new HashMap<>();
    
    public String cd(String currentDir, String targetDir) {
        // Read current version (no lock needed due to volatile)
        Map<String, String> currentSymlinks = symlinks;
        return cdBasic(currentDir, targetDir, currentSymlinks);
    }
    
    public synchronized void addSymlink(String link, String target) {
        // Create new map with updated value
        Map<String, String> newSymlinks = new HashMap<>(symlinks);
        newSymlinks.put(link, target);
        symlinks = newSymlinks;  // Atomic update
    }
}
```

**Trade-offs:**
- Read-Write Lock: Better for frequent writes, readers wait
- Copy-on-Write: Better for frequent reads, writes are expensive

---

## Category 5: Edge Cases & Error Handling

### Q10: "What should happen with null inputs?"

**Answer:**

```java
public String cd(String currentDir, String targetDir) {
    // Validate inputs
    if (currentDir == null || targetDir == null) {
        throw new IllegalArgumentException("Directories cannot be null");
    }
    
    if (!currentDir.startsWith("/")) {
        throw new IllegalArgumentException(
            "Current directory must be absolute: " + currentDir
        );
    }
    
    if (currentDir.isEmpty()) {
        throw new IllegalArgumentException("Current directory cannot be empty");
    }
    
    // Handle empty target as current directory
    if (targetDir.isEmpty()) {
        return currentDir;
    }
    
    // Continue with normal logic...
}
```

---

### Q11: "What about very long paths (> 4096 characters, PATH_MAX)?"

**Answer:**

```java
private static final int PATH_MAX = 4096;

public String cd(String currentDir, String targetDir) {
    String result = cdBasic(currentDir, targetDir);
    
    if (result != null && result.length() > PATH_MAX) {
        throw new PathTooLongException(
            "Path exceeds maximum length: " + result.length() + " > " + PATH_MAX
        );
    }
    
    return result;
}
```

**Real-world context:**
- Linux: PATH_MAX = 4096
- Windows: MAX_PATH = 260 (legacy), 32,767 (extended)
- macOS: PATH_MAX = 1024

---

### Q12: "Handle relative symlink targets?"

**Current assumption**: All symlink targets are absolute paths

**Extended implementation:**

```java
private String resolveLongestSymlink(String path, Map<String, String> symlinks) {
    // ... find longest match ...
    
    if (longestMatch != null) {
        String target = longestTarget;
        
        // If target is relative, resolve relative to symlink's directory
        if (!target.startsWith("/")) {
            String symlinkDir = longestMatch.substring(0, 
                longestMatch.lastIndexOf('/'));
            if (symlinkDir.isEmpty()) {
                symlinkDir = "/";
            }
            target = symlinkDir + "/" + target;
        }
        
        // Normalize the target (might contain .. or .)
        target = normalizePath(target);
        
        // ... append remaining segments ...
    }
}
```

**Example:**
```
Symlink: /home/user/link → ../shared
Path: /home/user/link/file.txt
Resolved: /home/user/../shared/file.txt = /home/shared/file.txt
```

---

## Category 6: System Design Integration

### Q13: "How would you design a complete file system service?"

**Answer:**

```
┌─────────────────────────────────────────┐
│          File System Service            │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────┐    ┌──────────────┐  │
│  │ Path Parser │───▶│ CD Command   │  │
│  └─────────────┘    └──────────────┘  │
│         │                   │          │
│         ▼                   ▼          │
│  ┌─────────────────────────────────┐  │
│  │    Permission Checker           │  │
│  └─────────────────────────────────┘  │
│         │                   │          │
│         ▼                   ▼          │
│  ┌─────────────┐    ┌──────────────┐  │
│  │ Inode Cache │    │ Symlink Map  │  │
│  └─────────────┘    └──────────────┘  │
│         │                   │          │
│         ▼                   ▼          │
│  ┌─────────────────────────────────┐  │
│  │    Storage Backend              │  │
│  │  (Disk / Database / S3)         │  │
│  └─────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

**Components:**

1. **API Layer**: REST endpoints
   ```
   POST /cd
   {
     "currentDir": "/home/user",
     "targetDir": "documents"
   }
   ```

2. **Business Logic**: CD implementation

3. **Caching Layer**: LRU cache for paths

4. **Storage Layer**: Persistent storage for directory structure

5. **Monitoring**: Track performance metrics
   - Average resolution time
   - Cache hit rate
   - Symlink resolution depth
   - Error rates

---

## Category 7: Testing

### Q14: "How would you test this implementation?"

**Answer:**

**Unit Tests:**

```java
@Test
public void testBasicRelativePath() {
    assertEquals("/home/user/documents", 
                 cd.cd("/home/user", "documents"));
}

@Test
public void testAbsolutePath() {
    assertEquals("/etc", 
                 cd.cd("/home/user", "/etc"));
}

@Test
public void testParentDirectory() {
    assertEquals("/home", 
                 cd.cd("/home/user", ".."));
}

@Test
public void testRootBoundary() {
    assertNull(cd.cd("/", ".."));
}

@Test
public void testComplexNavigation() {
    assertEquals("/c/e", 
                 cd.cd("/a/b", "../../c/./d/../e"));
}

@Test
public void testSymlinkResolution() {
    Map<String, String> symlinks = Map.of("/link", "/target");
    assertEquals("/target/file.txt", 
                 cd.cd("/", "link/file.txt", symlinks));
}

@Test(expected = RuntimeException.class)
public void testSymlinkCycle() {
    Map<String, String> symlinks = Map.of(
        "/a", "/b",
        "/b", "/a"
    );
    cd.cd("/", "a", symlinks);
}
```

**Property-Based Tests:**

```java
@Property
public void cdIsIdempotent(@ForAll String dir) {
    // cd to same directory twice = same result
    String result1 = cd.cd(dir, ".");
    String result2 = cd.cd(result1, ".");
    assertEquals(result1, result2);
}

@Property
public void cdParentThenChild(@ForAll String dir, @ForAll String child) {
    // cd .. then child should return to original (if possible)
    String original = cd.cd("/", dir + "/" + child);
    if (original != null) {
        String parent = cd.cd(original, "..");
        String back = cd.cd(parent, child);
        assertEquals(original, back);
    }
}
```

**Integration Tests:**

```java
@Test
public void testWithActualFileSystem() throws IOException {
    Path tempDir = Files.createTempDirectory("cdtest");
    Path subDir = Files.createDirectory(tempDir.resolve("sub"));
    
    String current = tempDir.toString();
    String result = cd.cd(current, "sub");
    
    assertEquals(subDir.toString(), result);
    assertTrue(Files.exists(Paths.get(result)));
}
```

---

## Summary: What Interviewers Look For

### ✅ Strong Signals:
1. **Systematic thinking**: Handle edge cases methodically
2. **Trade-off awareness**: Explain HashMap vs Trie, caching vs simplicity
3. **Real-world knowledge**: Mention PATH_MAX, SYMLOOP_MAX, permissions
4. **Extensibility**: Code is easy to extend for new features
5. **Testing mindset**: Discuss how you'd validate the implementation

### ❌ Red Flags:
1. No consideration for thread safety
2. Ignoring edge cases (null inputs, empty paths)
3. Can't explain time/space complexity
4. Over-engineering without justification
5. No awareness of real file system concepts

---

## Final Tips

1. **Always start simple**: Get basic version working first
2. **Ask clarifying questions**: "Are symlinks always absolute?"
3. **Explain as you code**: "I'm using a stack because..."
4. **Consider trade-offs**: "This is O(m*n) but simpler than O(n) with Trie"
5. **Think about production**: "In production, I'd add caching..."

Good luck! 🚀

