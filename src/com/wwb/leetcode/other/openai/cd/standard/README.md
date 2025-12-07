# Standard CD Implementation - Complete Guide

## 📚 Overview

This package contains the **most commonly asked** CD interview progression at OpenAI:

```
Phase 1 (15 min): Basic path navigation
Phase 2 (10 min): Tilde (~) expansion  
Phase 3 (20 min): Symlink resolution with cycle detection
```

---

## 📁 Files in This Package

### **StandardCD.java** ⭐ Main Implementation
The complete standard progression with all 3 phases.

**Quick Reference:**
```java
StandardCD cd = new StandardCD();

// Phase 1: Basic navigation
cd.phase1("/home/user", "docs");  // → "/home/user/docs"

// Phase 2: Tilde expansion
cd.phase2("/etc", "~/docs", "/home/user");  // → "/home/user/docs"

// Phase 3: Symlinks
Map<String, String> symlinks = Map.of("/link", "/target");
cd.phase3("/", "link/file", "/home", symlinks);  // → "/target/file"
```

---

### **HASHMAP_VS_TRIE.md** 📖 Deep Technical Analysis
**33-page comprehensive guide** comparing the two approaches for Phase 3.

**Contents:**
- Detailed algorithm walkthrough for both approaches
- Step-by-step execution examples
- Time/space complexity analysis
- Performance comparison with concrete numbers
- When to use each approach
- Interview discussion points
- Trade-off analysis

**Key Insight:**
```
HashMap: O(n×k) time, O(1) space, simple (~40 lines)
Trie:    O(S+n) time, O(S) space, complex (~100 lines)

Interview → Use HashMap (time constraint)
Production → Depends on scale
```

**READ THIS** if you want to deeply understand the trade-offs!

---

### **ComparisonDemo.java** 🔬 Side-by-Side Comparison
Executable demonstration showing both approaches with identical test cases.

**Features:**
- Implements both HashMap and Trie approaches
- Operation counting to show actual work done
- Performance timing
- Scalability tests with varying symlink counts
- Visual comparison output

**Run it:**
```bash
javac ComparisonDemo.java
java com.wwb.leetcode.other.openai.cd.standard.ComparisonDemo
```

**Output example:**
```
TEST: Many Symlinks (100)
Path: /home/user/docs/file.txt
Symlinks: 100

Result: /shared/documents/file.txt

PERFORMANCE COMPARISON:
Metric               | HashMap         | Trie           
--------------------------------------------------------
Operations           | 5               | 5              
Time                 | 45230           | 125640 ns      
Faster               | HashMap (2.78x)
```

---

## 🎯 Quick Start Guide

### **For Interview Prep (1 hour)**

**Step 1** (20 min): Read Phase outlines below  
**Step 2** (30 min): Implement from scratch using StandardCD.java as reference  
**Step 3** (10 min): Run tests, verify understanding  

### **For Deep Understanding (3 hours)**

**Step 1** (60 min): Study StandardCD.java thoroughly  
**Step 2** (90 min): Read HASHMAP_VS_TRIE.md  
**Step 3** (30 min): Run ComparisonDemo.java and analyze output  

---

## 📖 Phase-by-Phase Guide

### **Phase 1: Basic Path Navigation** (15 minutes)

**Goal**: Normalize paths with `.`, `..`, and handle absolute/relative paths

**Algorithm**: Stack-based
```java
1. Combine current + target to get full path
2. Split by "/"
3. For each segment:
   - Skip empty and "."
   - Pop stack for ".."
   - Push to stack for regular names
4. Join stack with "/"
```

**Example:**
```java
cd("/home/user", "../docs/./photos")

Step 1: fullPath = "/home/user/../docs/./photos"
Step 2: segments = ["", "home", "user", "..", "docs", ".", "photos"]
Step 3: Process with stack:
  "" → skip
  "home" → push → stack = [home]
  "user" → push → stack = [home, user]
  ".." → pop → stack = [home]
  "docs" → push → stack = [home, docs]
  "." → skip → stack = [home, docs]
  "photos" → push → stack = [home, docs, photos]
Step 4: result = "/home/docs/photos"
```

**Critical Edge Cases:**
```java
cd("/", "..")           → null (can't go above root)
cd("/home", "a//b///c") → "/home/a/b/c" (multiple slashes)
cd("/home", "")         → "/home" (empty target)
```

**Complexity:**
- Time: O(n) where n = total path length
- Space: O(n) for stack

---

### **Phase 2: Tilde Expansion** (10 minutes)

**Goal**: Handle `~` for home directory

**Algorithm**: Simple string replacement (before Phase 1)
```java
1. If target == "~" → replace with homeDir
2. If target starts with "~/" → replace ~ with homeDir
3. Then use Phase 1 logic
```

**Example:**
```java
cd("/etc", "~/docs", "/home/user")

Step 1: targetDir == "~/docs"
Step 2: Matches "~/..." pattern
Step 3: Replace: targetDir = "/home/user/docs"
Step 4: Use phase1("/etc", "/home/user/docs")
Step 5: Result = "/home/user/docs"
```

**Critical Edge Cases:**
```java
cd("/etc", "~", "/home/user")        → "/home/user"
cd("/etc", "~/docs", "/home/user")   → "/home/user/docs"
cd("/home", "test/~/file", "/home")  → "/home/test/~/file" (~ not at start!)
cd("/home/user", "~/..", "/home/user") → "/home"
```

**Why This Phase is Light:**
- Intentionally simple (2-3 lines of code)
- Gives breather after Phase 1
- Tests edge case thinking (~ only at start!)
- Leaves room for interviewer to add follow-ups

**Complexity:**
- Time: O(n) (dominated by Phase 1)
- Space: O(n)

---

### **Phase 3: Symlink Resolution** (20 minutes)

**Goal**: Resolve symbolic links with cycle detection

**Two Approaches:**

#### **Approach A: HashMap Iteration** (Recommended for Interview)

**Algorithm:**
```java
1. Normalize path using Phase 1
2. While not done (bounded iterations):
   a. Check if already visited (cycle detection)
   b. Find longest matching symlink prefix
   c. If found, replace prefix with target
   d. Normalize the new path
   e. If no match, we're done
```

**Longest Prefix Matching:**
```java
For each prefix of path (/a, /a/b, /a/b/c, ...):
  If prefix exists in symlinks map:
    Record as longest match
Return the longest match found
```

**Example:**
```java
path = "/home/link/conf/app.yml"
symlinks = {
  "/home/link" → "/etc",
  "/etc/conf" → "/var/config"
}

Iteration 1:
  Check prefixes: /home (no), /home/link (YES!)
  longest = "/home/link" → "/etc"
  Replace: "/etc/conf/app.yml"
  
Iteration 2:
  Check prefixes: /etc (no), /etc/conf (YES!)
  longest = "/etc/conf" → "/var/config"
  Replace: "/var/config/app.yml"
  
Iteration 3:
  Check prefixes: /var (no), /var/config (no), ...
  No match → DONE
  
Result: "/var/config/app.yml"
```

**Cycle Detection:**
```java
visited = set()
for each iteration:
  if path in visited:
    throw "Cycle detected!"
  visited.add(path)
```

**Complexity:**
- Time: O(k × n × m) where k=iterations (bounded), n=segments, m=symlinks
- Space: O(n) for visited set
- Code: ~40 lines

**Pros:**
- ✅ Simple to implement
- ✅ No preprocessing
- ✅ Interview-friendly

**Cons:**
- ❌ Not optimal for many symlinks

---

#### **Approach B: Trie Traversal** (Production Optimization)

**Algorithm:**
```java
1. Build Trie from all symlinks (one-time)
2. Normalize path using Phase 1
3. While not done:
   a. Traverse path tokens through Trie
   b. Record deepest node that's a symlink
   c. If found, replace and normalize
   d. If no match, done
```

**Trie Structure:**
```
Symlinks: {
  "/home/link" → "/etc",
  "/home/user/docs" → "/shared"
}

Trie:
        root
         |
       home
       /   \
    link   user
     |      |
   [/etc]  docs
           |
        [/shared]
```

**Traversal Example:**
```java
path = "/home/user/docs/file.txt"
tokens = ["home", "user", "docs", "file.txt"]

Traverse:
  "home" → found in trie, continue
  "user" → found in trie, continue
  "docs" → found in trie, IS SYMLINK! → target = "/shared"
  "file.txt" → not in trie, stop

Result: "/shared" + ["file.txt"] = "/shared/file.txt"
```

**Complexity:**
- Build: O(S) where S = total chars in all symlinks
- Lookup: O(n) where n = path segments
- Space: O(S) for Trie
- Code: ~100 lines (with TrieNode class)

**Pros:**
- ✅ Optimal O(n) lookup
- ✅ Efficient for repeated use
- ✅ Production-grade

**Cons:**
- ❌ Complex implementation
- ❌ Build overhead
- ❌ May not finish in 20 min interview

---

### **Which Approach to Use?**

```
┌─────────────────────────────────────────────────────────┐
│                    DECISION TREE                         │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  In an interview (time pressure)?                       │
│  ├─ YES → Use HashMap                                   │
│  └─ NO → Continue...                                    │
│                                                          │
│  Have you mastered HashMap approach?                    │
│  ├─ NO → Use HashMap (safer)                            │
│  └─ YES → Continue...                                   │
│                                                          │
│  20+ minutes left for Phase 3?                          │
│  ├─ NO → Use HashMap                                    │
│  └─ YES → Continue...                                   │
│                                                          │
│  Want to show advanced skills?                          │
│  ├─ YES → Can try Trie (risky!)                         │
│  └─ NO → Use HashMap, discuss Trie                      │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

**Recommended Strategy:**
1. Implement HashMap approach (15 min)
2. Test thoroughly (3 min)
3. If time permits, discuss Trie optimization (2 min)

**What to Say:**
```
"I'll use HashMap iteration for simplicity. This gives us O(n×m) 
time which is adequate for reasonable inputs. For production with 
many symlinks, I'd optimize with a Trie for O(n) lookup, trading 
O(S) space. The HashMap approach is clearer and faster to implement."
```

---

## 🎓 Interview Tips

### **Time Management**

```
0-2 min:   Clarify requirements
           "Should I handle permissions? Network paths?"
           
2-17 min:  Phase 1 (Basic navigation)
           - 2 min: Explain approach
           - 10 min: Implement
           - 3 min: Test edge cases
           
17-27 min: Phase 2 (Tilde)
           - 1 min: Explain (trivial)
           - 3 min: Implement
           - 1 min: Test
           
27-47 min: Phase 3 (Symlinks)
           - 3 min: Explain approach (HashMap)
           - 12 min: Implement
           - 3 min: Test
           - 2 min: Discuss Trie if time

47-50 min: Buffer for questions/fixes
```

### **What Impresses Interviewers**

✅ **Clear communication**
```
"I'll use a stack to handle . and .. naturally.
As I process each segment..."
```

✅ **Proactive edge cases**
```
"I need to check if the stack is empty before 
popping for .., otherwise we'd go above root."
```

✅ **Complexity analysis**
```
"This is O(n) time and space where n is the total path length."
```

✅ **Trade-off discussion**
```
"For Phase 3, I'm choosing HashMap for simplicity.
In production with many symlinks, a Trie would be better..."
```

✅ **Testing mindset**
```
"Let me test with: root boundary, multiple slashes,
complex navigation, and a cycle."
```

### **Common Mistakes to Avoid**

❌ **Not checking stack.isEmpty() before pop**
```java
if (segment.equals("..")) {
    stack.pop();  // WRONG! What if stack is empty?
}

// Correct:
if (segment.equals("..")) {
    if (stack.isEmpty()) return null;
    stack.pop();
}
```

❌ **Treating ~ as special anywhere**
```java
// WRONG: Expands ~ in middle of path
targetDir = targetDir.replace("~", homeDir);

// Correct: Only at start
if (targetDir.equals("~")) {
    targetDir = homeDir;
} else if (targetDir.startsWith("~/")) {
    targetDir = homeDir + targetDir.substring(1);
}
```

❌ **Forgetting cycle detection**
```java
// WRONG: Infinite loop possible
while (true) {
    path = resolveSymlink(path);
}

// Correct: Visited set + bounded iterations
Set<String> visited = new HashSet<>();
for (int i = 0; i < maxIterations; i++) {
    if (!visited.add(path)) {
        throw new RuntimeException("Cycle!");
    }
    ...
}
```

❌ **Over-engineering Phase 2**
```java
// WRONG: Too complex for Phase 2
class TildeExpander {
    private Map<String, String> users;
    public String expand(String path) { ... }
}

// Correct: Simple and direct
if (targetDir.equals("~")) {
    targetDir = homeDir;
}
```

---

## 📊 Comparison Summary

| Aspect | HashMap | Trie | Winner (Interview) |
|--------|---------|------|-------------------|
| **Implementation Time** | 15 min | 35 min | HashMap |
| **Code Lines** | ~40 | ~100 | HashMap |
| **Code Complexity** | Simple | Complex | HashMap |
| **Time (Single)** | O(n×k) | O(S+n) | Depends on m |
| **Time (Repeated)** | O(p×n×k) | O(S+p×n) | Trie |
| **Space** | O(1) | O(S) | HashMap |
| **Debuggability** | Easy | Hard | HashMap |
| **Production Ready** | Good | Better | Trie |
| **Interview Risk** | Low | High | HashMap |

**Recommendation**: Use HashMap in interviews, discuss Trie as optimization.

---

## 🚀 Running the Code

### **StandardCD.java**
```bash
cd /path/to/standard/
javac StandardCD.java
java com.wwb.leetcode.other.openai.cd.standard.StandardCD

# Expected output:
# ✓ Phase 1 tests passed!
# ✓ Phase 2 tests passed!
# ✓ Phase 3 tests passed!
# ✓ Cycle detection working!
# ✓ Chained symlinks working!
# 🎉 All tests passed!
```

### **ComparisonDemo.java**
```bash
javac ComparisonDemo.java
java com.wwb.leetcode.other.openai.cd.standard.ComparisonDemo

# Shows side-by-side comparison with performance metrics
```

---

## 📚 Additional Resources

**In parent directory:**
- `cd_question_analysis.md` - Detailed breakdown of standard progression
- `cd_interview_cheatsheet.md` - Quick reference for day-of interview
- `cd_visual_guide.md` - Flowcharts and diagrams
- `PROGRESSIONS_README.md` - Comparison with other progressions

**In this directory:**
- `HASHMAP_VS_TRIE.md` - Deep technical comparison (⭐ READ THIS!)
- `ComparisonDemo.java` - Executable comparison

---

## 💡 Key Takeaways

1. **Phase 1 is crucial** - Must be perfect before moving on
2. **Phase 2 is intentionally simple** - Don't over-engineer
3. **Phase 3 has two approaches** - Know both, use HashMap in interviews
4. **Trade-offs matter** - Showing you understand them is valuable
5. **Time management is critical** - Don't get stuck on Phase 1

---

## 🎯 Final Checklist

**Before your interview:**
- [ ] Can implement Phase 1 in < 15 minutes
- [ ] Know all critical edge cases by heart
- [ ] Understand HashMap approach for Phase 3
- [ ] Can explain Trie optimization
- [ ] Have practiced implementing from scratch 3+ times

**During the interview:**
- [ ] Clarify requirements upfront
- [ ] Explain approach before coding
- [ ] Test after each phase
- [ ] State time/space complexity
- [ ] Discuss trade-offs if time permits

---

**You're ready! Go ace that interview!** 🚀

