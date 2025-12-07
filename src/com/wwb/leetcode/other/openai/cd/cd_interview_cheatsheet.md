# CD Command Interview Cheatsheet 📝

**Quick reference for OpenAI interview preparation**

---

## 🎯 The Question

**"Implement a cd (change directory) command"**

**Expected duration**: 45 minutes  
**Phases**: 3 progressive phases  
**Difficulty**: Easy → Medium → Hard

---

## 📊 Phase Breakdown

| Phase | Feature | Time | Difficulty | Key Concept |
|-------|---------|------|------------|-------------|
| 1 | Basic path navigation | 15 min | Easy-Medium | Stack |
| 2 | Tilde (~) expansion | 10 min | Easy | String processing |
| 3 | Symlink resolution | 20 min | Hard | Graph traversal + cycle detection |

---

## ⚡ Quick Implementation Guide

### Phase 1: Basic Navigation (Stack Approach)

```java
public String cd(String currentDir, String targetDir) {
    // 1. Absolute or relative?
    String path = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
    
    // 2. Stack for normalization
    Stack<String> stack = new Stack<>();
    for (String seg : path.split("/")) {
        if (seg.isEmpty() || seg.equals(".")) continue;
        if (seg.equals("..")) {
            if (stack.isEmpty()) return null;  // Above root!
            stack.pop();
        } else {
            stack.push(seg);
        }
    }
    
    // 3. Build result
    return "/" + String.join("/", stack);
}
```

**Time**: O(n), **Space**: O(n)

### Phase 2: Tilde Expansion

```java
public String cd(String currentDir, String targetDir, String homeDir) {
    // Expand ~ only at the start
    if (targetDir.equals("~")) targetDir = homeDir;
    else if (targetDir.startsWith("~/")) targetDir = homeDir + targetDir.substring(1);
    
    return cdBasic(currentDir, targetDir);
}
```

### Phase 3: Symlinks (HashMap Approach)

```java
public String cd(String currentDir, String targetDir, Map<String, String> symlinks) {
    String path = cdBasic(currentDir, targetDir);
    Set<String> visited = new HashSet<>();
    
    for (int i = 0; i < symlinks.size() + 1; i++) {
        if (!visited.add(path)) throw new RuntimeException("Cycle!");
        
        String newPath = resolveLongestSymlink(path, symlinks);
        if (newPath.equals(path)) break;
        path = normalize(newPath);
    }
    return path;
}
```

---

## 🔑 Critical Edge Cases

### Must Handle:
- ✅ `cd("/", "..")` → `null` (can't go above root)
- ✅ `cd("/home", "a//b///c")` → `/home/a/b/c` (multiple slashes)
- ✅ `cd("/home/user", "")` → `/home/user` (empty target)
- ✅ `cd("/a/b", "../../c/./d/../e")` → `/c/e` (complex navigation)
- ✅ Symlink cycles → **throw exception**
- ✅ `~/path/~` → only expand first `~`

### Common Mistakes:
- ❌ Forgetting to check stack.isEmpty() before pop
- ❌ Treating `~` as special anywhere (only at start!)
- ❌ Infinite loop in symlink resolution
- ❌ Not normalizing symlink targets

---

## 🧠 Key Talking Points

### When Explaining Your Approach:

**Phase 1:**
> "I'll use a stack to handle . and .. naturally. As I process each segment, . is ignored, .. pops from stack, and regular directories are pushed. This gives us O(n) time complexity."

**Phase 2:**
> "Tilde expansion is straightforward - we only expand ~ at the beginning of the path. A ~ in the middle would be a literal directory name, like someone creating a folder called '~'."

**Phase 3:**
> "For symlinks, I'll use greedy longest prefix matching. I need to handle chained symlinks (A→B→C) and detect cycles. I'll use a visited set to detect cycles and bound iterations to symlinks.size() + 1 to prevent infinite loops."

### Performance Discussion:

**Interviewer**: "How would you optimize symlink resolution with many symlinks?"

**You**:
> "Two approaches:
> 1. **HashMap iteration** (O(m*n)): Simple, good for few symlinks
> 2. **Trie-based** (O(n)): Build Trie of symlink paths, traverse once
> 
> For production with thousands of symlinks, I'd use a Trie. For this interview, HashMap is clearer and adequate."

**Interviewer**: "What about thread safety?"

**You**:
> "Current implementation is thread-safe for reads if symlinks map is immutable. If concurrent writes are needed, we'd need synchronization around symlink map access, or use ConcurrentHashMap."

---

## 📝 Test Cases to Mention

```java
// Phase 1
cd("/home/user", "docs")         → "/home/user/docs"
cd("/home/user", "/etc")         → "/etc"
cd("/home/user", "..")           → "/home"
cd("/", "..")                    → null

// Phase 2
cd("/etc", "~", "/home/user")    → "/home/user"
cd("/etc", "~/docs", "/home")    → "/home/docs"

// Phase 3
symlinks = {"/link" → "/etc"}
cd("/", "link/passwd", symlinks) → "/etc/passwd"

// Cycle
symlinks = {"/a"→"/b", "/b"→"/a"}
cd("/", "a", symlinks)           → RuntimeException
```

---

## ⏱️ Time Management Strategy

**0-5 min**: Clarify requirements, ask questions
- "Should I handle permissions?"
- "What should happen if path doesn't exist?"
- "Are symlink targets always absolute?"

**5-20 min**: Implement Phase 1
- Write basic version
- Test with simple cases
- State complexity

**20-30 min**: Add Phase 2
- Quick tilde expansion
- Test edge cases
- Move to Phase 3

**30-45 min**: Implement Phase 3
- Explain approach first
- Implement symlink resolution
- Add cycle detection
- Test thoroughly

**45+ min**: If time permits
- Discuss optimizations (Trie)
- Talk about production concerns
- Mention alternative approaches

---

## 🎤 Opening Statement Template

> "Let me make sure I understand the requirements:
> 
> 1. **Phase 1**: Implement basic cd with absolute/relative paths, handling . and ..
> 2. **Phase 2**: Add support for ~ (home directory)
> 3. **Phase 3**: Handle symbolic links with cycle detection
> 
> I'll use a stack-based approach for path normalization, which gives us O(n) time complexity. For symlinks, I'll iteratively resolve using longest prefix matching. Should I start with Phase 1?"

---

## 🚀 What Makes You Stand Out

### Good:
- ✅ Clear explanation before coding
- ✅ Incremental testing after each phase
- ✅ Explicit complexity analysis
- ✅ Handling edge cases proactively
- ✅ Clean, readable code

### Excellent:
- 🌟 Discussing trade-offs (HashMap vs Trie)
- 🌟 Mentioning real-world details (SYMLOOP_MAX = 40)
- 🌟 Asking clarifying questions
- 🌟 Suggesting improvements ("We could cache normalized paths")
- 🌟 Writing test cases as you go

---

## 🎯 Success Criteria (From Interview Reports)

**Strong Hire** (What you need):
- ✅ Complete Phase 1 & 2 perfectly
- ✅ Working Phase 3 with correct cycle detection
- ✅ All test cases passing
- ✅ Clean, maintainable code
- ✅ Good communication

**Hire** (Minimum):
- ✅ Complete Phase 1 & 2
- ✅ Partial Phase 3 with correct approach
- ✅ Can explain remaining implementation

**What causes failure**:
- ❌ Not completing Phase 1 correctly
- ❌ Major bugs that don't pass test cases
- ❌ Infinite loops with no cycle detection
- ❌ Can't explain complexity or approach

---

## 💡 Interview Tips

### DO:
1. **Think out loud**: "I'm using a stack because..."
2. **Test incrementally**: Run tests after each phase
3. **Ask questions**: "Are symlink paths always absolute in the map?"
4. **State assumptions**: "I'm assuming targetDir is never null"
5. **Mention trade-offs**: "HashMap is simpler but Trie is faster"

### DON'T:
1. ❌ Silent coding for 10 minutes
2. ❌ Skipping Phase 1 to jump to symlinks
3. ❌ Ignoring test failures
4. ❌ Over-engineering (don't build Trie unless discussed)
5. ❌ Forgetting edge cases

---

## 📚 Related OpenAI Questions

Based on your experience notes, other questions follow similar patterns:

| Question | Phase 1 | Phase 2 | Phase 3 |
|----------|---------|---------|---------|
| **KV Store** | Get/Set | Versioning | Multi-file |
| **In-Memory DB** | CREATE/INSERT | SELECT with WHERE | JOIN/ORDER BY |
| **IP Iterator** | Forward iteration | Backward | CIDR blocks |
| **Node Cluster** | Count nodes | Tree topology | Handle failures |

**Common pattern**: Start simple → Add features → Handle complexity

---

## ✨ Final Checklist

Before interview:
- [ ] Can implement Phase 1 in 15 minutes
- [ ] Know all edge cases by heart
- [ ] Can explain HashMap vs Trie trade-off
- [ ] Have mental list of test cases ready
- [ ] Can detect and explain symlink cycles
- [ ] Know time/space complexity for each phase

During interview:
- [ ] Asked clarifying questions
- [ ] Explained approach before coding
- [ ] Tested after each phase
- [ ] Stated complexity
- [ ] Handled edge cases
- [ ] Communicated clearly

---

## 🎓 Key Insights from Interview Reports

1. **Time pressure is real**: Many candidates don't finish Phase 3
   - → Prioritize getting Phase 1 & 2 perfect

2. **Clean code matters more than optimization**
   - → Don't prematurely optimize with Trie

3. **Communication is highly valued**
   - → Explaining trade-offs can compensate for incomplete code

4. **They test incrementally**
   - → Make sure each phase works before moving on

5. **Most offers given with Phase 1 & 2 + partial Phase 3**
   - → Don't panic if you don't complete everything

---

## 📞 Last-Minute Review (5 minutes before interview)

1. **Phase 1**: Stack, handle `.` and `..`, check empty stack before pop
2. **Phase 2**: Only expand `~` at the start
3. **Phase 3**: Longest prefix, visited set for cycles, bounded iterations
4. **Edge cases**: Root boundary, multiple slashes, cycles
5. **Complexity**: All O(n) except Phase 3 which is O(k*m*n) but bounded

**Deep breath. You got this!** 💪

---

*Good luck with your OpenAI interview! Remember: clear communication and incremental progress beats rushing to a perfect solution.*

