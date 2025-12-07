# CD Command Interview Progressions - Implementation Guide

## 📚 Overview

This package contains **6 complete implementations** of the CD command interview question, each focusing on different aspects and phase progressions. All implementations are production-ready with comprehensive tests.

---

## 🗂️ Package Structure

```
cd/
├── standard/          ⭐ Most common in interviews
│   └── StandardCD.java
│       Phase 1: Basic path navigation (., .., abs/rel)
│       Phase 2: Tilde (~) expansion
│       Phase 3: Symlink resolution + cycle detection
│
├── security/          🔒 System security focus
│   └── SecurityCD.java
│       Phase 1: Basic path navigation
│       Phase 2: Permission checking (owner/group/other)
│       Phase 3: Chroot jails and security boundaries
│
├── performance/       ⚡ Optimization focus
│   └── PerformanceCD.java
│       Phase 1: Basic path navigation
│       Phase 2: LRU caching for path resolutions
│       Phase 3: Concurrent access with thread safety
│
├── filesystem/        💾 Real filesystem internals
│   └── FileSystemCD.java
│       Phase 1: Basic path navigation
│       Phase 2: Path validation with filesystem tree
│       Phase 3: Mount points and filesystem boundaries
│
├── features/          ✨ Shell features focus
│   └── FeaturesCD.java
│       Phase 1: Basic path navigation
│       Phase 2: History (cd -, pushd/popd) + env vars
│       Phase 3: Wildcard pattern matching (*, ?, [])
│
└── crossplatform/     🌐 Cross-platform compatibility
    └── CrossPlatformCD.java
        Phase 1: Unix path navigation
        Phase 2: Windows paths (C:\, drive letters)
        Phase 3: Network paths (UNC, URLs, remote FS)
```

---

## 🎯 Which Progression to Use?

### **For Standard Interview Prep:**
→ **`standard/StandardCD.java`**
- Most commonly reported in actual OpenAI interviews
- Classic progression everyone should know
- Good balance of difficulty
- **Study this first!**

### **If Interviewer Mentions:**

| Keyword | Use Progression | Why |
|---------|----------------|-----|
| "security", "permissions" | `security/` | System access control focus |
| "performance", "scale" | `performance/` | Optimization, caching, concurrency |
| "validation", "filesystem" | `filesystem/` | Real FS internals, mount points |
| "user experience", "shell" | `features/` | Practical shell features |
| "Windows", "cross-platform" | `crossplatform/` | Multi-OS support |

### **For Deep Understanding:**
→ **Study all 6 progressions**
- Shows breadth and adaptability
- Understand trade-offs between approaches
- Can pivot when interviewer changes direction

---

## 📖 Detailed Progression Comparison

### 1. Standard Progression (Path → Tilde → Symlinks)

**File**: `standard/StandardCD.java`

**When to use**: Default choice for interviews

**Key features**:
- ✅ Stack-based path normalization
- ✅ Tilde expansion (`~` and `~/path`)
- ✅ Symlink resolution with cycle detection
- ✅ HashMap approach (O(k*m*n))
- ✅ Greedy longest prefix matching

**Example phase 3**:
```java
Map<String, String> symlinks = Map.of(
    "/home/link", "/etc",
    "/etc/conf", "/var/config"
);

cd.phase3("/home", "link/conf/app.yml", "/home/user", symlinks);
// Result: /var/config/app.yml
// (resolved /home/link → /etc, then /etc/conf → /var/config)
```

**Interview tips**:
- Can complete Phase 1 in 10-12 minutes if practiced
- Phase 2 is trivial (2-3 lines)
- Spend most time on Phase 3 symlink logic
- Discuss Trie optimization if time permits

---

### 2. Security Progression (Path → Permissions → Chroot)

**File**: `security/SecurityCD.java`

**When to use**: 
- Interviewer mentions "production", "security", "containers"
- System engineer role
- Backend/infrastructure focus

**Key features**:
- ✅ Unix-style permissions (owner/group/other)
- ✅ Execute permission checking on each directory
- ✅ Chroot jail enforcement
- ✅ Path escaping prevention
- ✅ User and group management

**Example phase 2**:
```java
User alice = new User("alice", "users");
FilePermissions perms = FilePermissions.createDefault("alice", "users");
permissions.put("/home/alice/docs", perms);

// Alice can access her own directory
cd.phase2("/home", "alice/docs", permissions, alice);

// Bob cannot access Alice's directory (no permission)
cd.phase2("/home", "alice/docs", permissions, bob);
// → PermissionDeniedException
```

**Example phase 3**:
```java
String chrootPath = "/home/jail";

// Can navigate within jail
cd.phase3("/home/jail/user", "docs", chrootPath, ...);
// Result: /home/jail/user/docs ✓

// Cannot escape jail
cd.phase3("/home/jail/user", "../../..", chrootPath, ...);
// → SecurityException: Cannot escape chroot jail
```

**Interview tips**:
- Mention real-world use cases: Docker, chroot, FTP servers
- Discuss permission bits: rwxrwxrwx
- Talk about setuid, setgid (advanced)
- Connect to your experience with containerization

---

### 3. Performance Progression (Path → Caching → Concurrency)

**File**: `performance/PerformanceCD.java`

**When to use**:
- Interviewer mentions "millions of requests", "performance"
- Senior engineer role
- System design mindset expected

**Key features**:
- ✅ LRU cache for path resolutions
- ✅ Cache hit rate tracking
- ✅ Thread-safe concurrent access
- ✅ Read-write locks
- ✅ Copy-on-write for symlink updates
- ✅ ConcurrentHashMap for cache

**Example phase 2**:
```java
CachedCD cd = new CachedCD(100);  // Cache size

cd.cd("/home/user", "docs");  // Cache miss
cd.cd("/home/user", "docs");  // Cache hit!

System.out.println(cd.getStats());
// → Cache stats: hits=1, misses=1, hit rate=50.00%
```

**Example phase 3**:
```java
ConcurrentCD cd = new ConcurrentCD();

// 10 threads reading concurrently
for (Thread t : readers) {
    t.start(() -> {
        String result = cd.cd("/home", "link/file");
    });
}

// Safe concurrent write
cd.addSymlink("/link2", "/target2");  // Copy-on-write
```

**Interview tips**:
- Discuss LRU vs LFU vs TTL caching strategies
- Talk about cache invalidation (hardest problem!)
- Mention ReadWriteLock vs synchronized trade-offs
- Consider distributed caching (Redis) for scale

---

### 4. Filesystem Progression (Path → Validation → Mount Points)

**File**: `filesystem/FileSystemCD.java`

**When to use**:
- Interviewer mentions "real filesystem", "inodes"
- Systems programming background expected
- Linux kernel knowledge valued

**Key features**:
- ✅ Filesystem tree (Trie-based)
- ✅ Path existence validation
- ✅ Typo correction with Levenshtein distance
- ✅ Mount point detection
- ✅ Filesystem boundary crossing
- ✅ Directory listing

**Example phase 2**:
```java
FileSystemTree fs = new FileSystemTree();
fs.addPath("/home/user/documents", true);
fs.addPath("/home/user/docs", true);

// Valid path
cd.phase2("/home", "user/docs", fs);
// → "/home/user/docs"

// Typo with suggestion
cd.phase2("/home/user", "documetns", fs);
// → No such directory: /home/user/documetns
//    Did you mean: documents, docs
```

**Example phase 3**:
```java
List<MountPoint> mounts = Arrays.asList(
    new MountPoint("/", "/dev/sda1", "ext4"),
    new MountPoint("/home", "/dev/sda2", "ext4"),
    new MountPoint("/mnt/usb", "/dev/sdb1", "vfat")
);

cd.phase3("/home/user", "/mnt/usb/files", fs, mounts);
// Output:
// Crossing filesystem boundary:
//   From: /dev/sda2 on /home type ext4
//   To:   /dev/sdb1 on /mnt/usb type vfat
```

**Interview tips**:
- Discuss inodes and hard links
- Mention `/proc/mounts` and `df -h`
- Talk about filesystem types (ext4, NTFS, APFS)
- Connect to experience with system administration

---

### 5. Features Progression (Path → History/Env → Wildcards)

**File**: `features/FeaturesCD.java`

**When to use**:
- Interviewer mentions "user experience", "shell features"
- Product engineer role
- Practical functionality valued

**Key features**:
- ✅ `cd -` (previous directory)
- ✅ `pushd/popd` (directory stack)
- ✅ Environment variable expansion (`$VAR`, `${VAR}`)
- ✅ History tracking
- ✅ Frequent directories analytics
- ✅ Wildcard matching (`*`, `?`, `[abc]`, `{a,b}`)

**Example phase 2**:
```java
CDWithHistory cd = new CDWithHistory("/home/user", envVars);

cd.cd("/etc");
cd.cd("/var");
cd.cd("-");  // Back to /etc

cd.pushd("/tmp");
cd.pushd("/opt");
cd.popd();  // Back to /tmp

cd.cd("$PROJECT/src");  // Expands $PROJECT env var
cd.cd("${HOME}/docs");   // Expands ${HOME}

List<String> frequent = cd.getFrequentDirs(5);
// → Most visited directories
```

**Example phase 3**:
```java
Set<String> validPaths = Set.of(
    "/home/user1", "/home/user2", "/home/user123",
    "/home/alice", "/home/bob"
);

CDWithWildcards cd = new CDWithWildcards(validPaths);

cd.cd("/home", "user*");
// → [/home/user1, /home/user2, /home/user123]

cd.cd("/home", "[ab]*");
// → [/home/alice, /home/bob]

cd.cd("/home", "{alice,bob}");
// → [/home/alice, /home/bob]
```

**Interview tips**:
- Relate to actual shell experience (bash, zsh)
- Mention tools like `autojump`, `z` for directory jumping
- Discuss regex vs glob patterns
- Talk about auto-completion and fuzzy matching

---

### 6. Cross-Platform Progression (Path → Windows → Network)

**File**: `crossplatform/CrossPlatformCD.java`

**When to use**:
- Interviewer mentions "Windows", "cross-platform"
- Desktop application development
- Electron/VSCode-like tools

**Key features**:
- ✅ Windows paths (`C:\Users\alice`)
- ✅ Drive letters and backslashes
- ✅ UNC paths (`\\server\share`)
- ✅ URL paths (`ssh://host/path`, `s3://bucket/key`)
- ✅ Path type detection
- ✅ Format conversion (Windows ↔ Unix)
- ✅ WSL-style mount points (`/mnt/c/`)

**Example phase 2**:
```java
// Windows absolute path
cd.phase2Windows("C:\\Users\\alice", "D:\\Documents");
// → "D:\\Documents"

// Windows relative with ..
cd.phase2Windows("C:\\Users\\alice", "..\\..\\Windows");
// → "C:\\Windows"

// Mixed slashes
cd.normalizeWindowsPath("C:/Users/alice/../bob/./docs");
// → "C:\\Users\\bob\\docs"

// Windows to Unix (WSL style)
cd.windowsToUnix("C:\\Users\\alice\\documents");
// → "/mnt/c/Users/alice/documents"
```

**Example phase 3**:
```java
// UNC path
PathInfo info = cd.phase3Network("/", "\\\\server\\share\\folder");
// → type=UNC, host=server, path=\\\\server\\share\\folder

// SSH URL
info = cd.phase3Network("/", "ssh://user@host/home/user/../docs");
// → type=URL, scheme=ssh, host=host, path=ssh://user@host/home/docs

// S3 URL
info = cd.phase3Network("/", "s3://bucket/data/./archive");
// → type=URL, scheme=s3, path=s3://bucket/data/archive

// HDFS URL
info = cd.phase3Network("/", "hdfs://namenode:9000/user/data");
// → type=URL, scheme=hdfs, host=namenode
```

**Interview tips**:
- Discuss case sensitivity differences (Windows vs Unix)
- Mention path length limits (260 on Windows, 4096 on Linux)
- Talk about `/` vs `\` separator issues
- Relate to VSCode, Git, or other cross-platform tools

---

## 🎓 How to Study These Progressions

### **Week 1: Master Standard**
```
Day 1-2: Implement standard/ from scratch 3 times
Day 3-4: Optimize and add tests
Day 5-7: Can complete in 30 minutes confidently
```

### **Week 2: Explore Variations**
```
Day 1-2: Study security/ and performance/
Day 3-4: Study filesystem/ and features/
Day 5-7: Study crossplatform/, review all
```

### **Week 3: Integration**
```
Day 1-3: Combine features from different progressions
Day 4-5: Practice pivoting between progressions
Day 6-7: Mock interviews with random progression
```

---

## 💡 Interview Strategies

### **Reading Interviewer Signals**

**At the start:**
```
Interviewer: "Let's implement a cd command."
You: "Great! Should I focus on core path navigation first,
      or are there specific features like permissions, 
      performance, or cross-platform support you'd like to see?"
```

**This shows**:
- ✅ You know there are multiple approaches
- ✅ You're thinking about requirements
- ✅ You're collaborative

### **Adapting Mid-Interview**

**Scenario**: You completed standard Phase 1-2, then:

```
Interviewer: "Now let's add permission checking."
You: [Internally: Not symlinks! Security focus.]
     "Sure! I'll add execute permission validation.
      Should I follow the Unix model with owner/group/other?"
```

**This shows**:
- ✅ You can pivot quickly
- ✅ You know domain knowledge (Unix permissions)
- ✅ You're still asking clarifying questions

### **Combining Progressions**

**Advanced**: Interviewer asks for multiple features

```
"Implement cd with symlinks AND caching."
→ Combine standard/phase3 + performance/phase2

"Implement cd with Windows support AND permissions."
→ Combine crossplatform/phase2 + security/phase2

"Implement cd with validation AND suggestions."
→ Use filesystem/phase2 (already includes both)
```

---

## 📊 Complexity Quick Reference

| Progression | Phase 1 | Phase 2 | Phase 3 |
|-------------|---------|---------|---------|
| **Standard** | O(n) | O(n) | O(k*m*n) |
| **Security** | O(n) | O(n*p) | O(n*p) |
| **Performance** | O(n) | O(1) cache | O(k*n) |
| **Filesystem** | O(n) | O(n+L) | O(n*m) |
| **Features** | O(n) | O(n) | O(n*p) |
| **Cross-platform** | O(n) | O(n) | O(n) |

**Legend**:
- n = path length
- k = symlink resolution iterations
- m = number of symlinks/mounts
- p = number of permissions/paths to check
- L = Levenshtein distance computation

---

## 🚀 Running the Tests

Each implementation has a `main()` method with comprehensive tests:

```bash
# Standard progression
javac standard/StandardCD.java && java standard.StandardCD

# Security progression  
javac security/SecurityCD.java && java security.SecurityCD

# Performance progression
javac performance/PerformanceCD.java && java performance.PerformanceCD

# Filesystem progression
javac filesystem/FileSystemCD.java && java filesystem.FileSystemCD

# Features progression
javac features/FeaturesCD.java && java features.FeaturesCD

# Cross-platform progression
javac crossplatform/CrossPlatformCD.java && java crossplatform.CrossPlatformCD
```

All tests should print:
```
✓ Phase 1 tests passed!
✓ Phase 2 tests passed!
✓ Phase 3 tests passed!
🎉 All tests passed!
```

---

## 🎯 Final Recommendation

**For interview prep**:
1. ✅ **Master `standard/`** - Can implement in sleep
2. ✅ **Understand `performance/`** - Shows senior thinking
3. ✅ **Browse others** - Know they exist, key features

**Day before interview**:
- Re-implement `standard/` from scratch (20 min)
- Review key features from `performance/` and `security/`
- Read this README one more time

**Day of interview (10 min before)**:
- Review `standard/` phase outline
- Remember: Phase 1 stack, Phase 2 tilde, Phase 3 symlinks
- Take a deep breath - you got this! 💪

---

## 📚 Additional Resources

- `cd_question_analysis.md` - Deep dive on standard progression
- `cd_interview_cheatsheet.md` - Quick reference for day-of
- `cd_implementation_comparison.md` - HashMap vs Trie analysis
- `cd_followup_questions.md` - Advanced topics and extensions
- `cd_phase_variations.md` - Creative alternatives and hybrids
- `cd_visual_guide.md` - Flowcharts and diagrams

---

**Created for comprehensive OpenAI interview preparation**  
**All 6 progressions are production-ready with full test coverage**

Good luck! 🎉

