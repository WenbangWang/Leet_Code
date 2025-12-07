# HashMap vs Trie for Symlink Resolution - Deep Technical Analysis

## 🎯 The Problem

Given a path like `/home/user/docs/file.txt` and symlinks:
```
/home/link → /etc
/home/user/docs → /shared/documents
```

We need to find the **longest matching prefix** and resolve it.

**Answer**: `/shared/documents/file.txt` (matched `/home/user/docs`)

---

## 🔀 Two Approaches

### **Approach 1: HashMap Iteration**
```java
// Iterate through each prefix and check if it's in the map
for each prefix of path:
    if symlinks.containsKey(prefix):
        record as potential match
```

### **Approach 2: Trie Traversal**
```java
// Build a Trie from symlinks, traverse path through Trie
Build Trie once from all symlinks
Traverse path through Trie in one pass
```

---

## 📊 Detailed Comparison

| Aspect | HashMap Iteration | Trie Traversal |
|--------|------------------|----------------|
| **Build Time** | O(1) - no build needed | O(S) where S = total chars in all symlinks |
| **Lookup Time** | O(n × m) | O(n) |
| **Space** | O(1) extra | O(S) for Trie structure |
| **Code Complexity** | Simple (~40 lines) | Complex (~100 lines with TrieNode) |
| **Best For** | Few symlinks, one-time use | Many symlinks, repeated lookups |
| **Implementation Time** | 15-20 minutes | 30-40 minutes |

**Where**:
- n = number of segments in path (typically 5-10)
- m = number of symlinks (variable)
- S = sum of all characters in symlink paths

---

## 🔍 HashMap Approach - Detailed Analysis

### **Algorithm**

```java
private String resolveLongestSymlink(String path, Map<String, String> symlinks) {
    String[] segments = path.split("/");
    StringBuilder currentPath = new StringBuilder();
    
    String longestMatch = null;
    String longestTarget = null;
    int longestMatchIndex = -1;
    
    // Try progressively longer prefixes
    for (int i = 0; i < segments.length; i++) {
        if (segments[i].isEmpty()) continue;
        
        currentPath.append("/").append(segments[i]);
        String candidate = currentPath.toString();
        
        // Check if this prefix is a symlink
        if (symlinks.containsKey(candidate)) {
            longestMatch = candidate;
            longestTarget = symlinks.get(candidate);
            longestMatchIndex = i;
        }
    }
    
    // If found, replace prefix with target
    if (longestMatch != null) {
        List<String> remaining = Arrays.asList(segments)
            .subList(longestMatchIndex + 1, segments.length);
        return remaining.isEmpty() ? longestTarget 
                                   : longestTarget + "/" + String.join("/", remaining);
    }
    
    return path;  // No symlink found
}
```

### **Step-by-Step Example**

**Given**:
```
path = "/home/user/docs/file.txt"
symlinks = {
    "/home/link" → "/etc",
    "/home/user/docs" → "/shared/documents"
}
```

**Execution**:

```
Iteration 1: currentPath = "/home"
  Check symlinks.containsKey("/home") → false
  
Iteration 2: currentPath = "/home/user"
  Check symlinks.containsKey("/home/user") → false
  
Iteration 3: currentPath = "/home/user/docs"
  Check symlinks.containsKey("/home/user/docs") → TRUE ✓
  longestMatch = "/home/user/docs"
  longestTarget = "/shared/documents"
  longestMatchIndex = 2
  
Iteration 4: currentPath = "/home/user/docs/file.txt"
  Check symlinks.containsKey("/home/user/docs/file.txt") → false
  (but we keep longestMatch from iteration 3)

Result: 
  remaining = ["file.txt"]
  return "/shared/documents" + "/" + "file.txt"
       = "/shared/documents/file.txt"
```

### **Time Complexity Analysis**

**For a single resolution**:
```
segments.length = n (typically 5-10)
For each segment:
  - Build string: O(1) amortized
  - HashMap lookup: O(1) average case
Total: O(n)
```

**Wait, that's not O(n × m)?**

Actually, HashMap lookup is O(1) **on average**, but:
1. String comparison in HashMap uses `hashCode()` which is O(k) where k = key length
2. In worst case, hash collisions can make it O(m)
3. More accurately: **O(n × k) where k = average symlink path length**

**But the real cost is iteration count**:
- We check HashMap **n times** (once per prefix)
- Each check is O(1) for lookup, O(k) for string comparison
- Total: **O(n × k)** where k is average key length

**Why do people say O(n × m)?**

In practice, when comparing approaches:
- HashMap: We iterate n prefixes, each checks against m symlinks conceptually
- Trie: We iterate n segments, each checks children (constant)

The comparison is **relative**, not absolute.

### **Pros**

✅ **Simple to implement**
```java
// Easy to understand
for each prefix:
    if prefix in symlinks:
        remember it
```

✅ **No preprocessing**
```java
// Just use the Map directly
resolve(path, symlinks);  // No build step
```

✅ **Memory efficient**
```java
// O(1) extra space beyond input
// Only stores longestMatch, longestTarget temporarily
```

✅ **Interview-friendly**
```java
// Can implement in 15 minutes
// Easy to explain
// Easy to debug
```

### **Cons**

❌ **Slower with many symlinks**
```java
// If you have 1000 symlinks and path has 10 segments
// Worst case: checking each prefix against keys
// String comparison overhead
```

❌ **Repeated string building**
```java
// currentPath.append() called n times
// Creates intermediate strings
```

❌ **Not reusable**
```java
// If resolving many paths with same symlinks
// No optimization across calls
```

---

## 🌲 Trie Approach - Detailed Analysis

### **Data Structure**

```java
class TrieNode {
    String name;                      // Directory name
    Map<String, TrieNode> children;   // Child directories
    List<String> pathTokens;          // If this is a symlink endpoint
    
    TrieNode(String name) {
        this.name = name;
        this.children = new HashMap<>();
        this.pathTokens = new ArrayList<>();
    }
}
```

### **Building the Trie**

```java
public static TrieNode buildTrie(Map<String, String> symlinks) {
    TrieNode root = new TrieNode("");
    
    for (Map.Entry<String, String> entry : symlinks.entrySet()) {
        String symlinkPath = entry.getKey();    // e.g., "/home/user/docs"
        String targetPath = entry.getValue();   // e.g., "/shared/documents"
        
        // Insert symlink path into Trie
        String[] tokens = symlinkPath.split("/");
        TrieNode current = root;
        
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            
            current.children.putIfAbsent(token, new TrieNode(token));
            current = current.children.get(token);
        }
        
        // Mark endpoint and store target
        current.pathTokens = Arrays.stream(targetPath.split("/"))
                                   .filter(x -> !x.isEmpty())
                                   .toList();
    }
    
    return root;
}
```

### **Visual Example**

**Given symlinks**:
```
/home/link → /etc
/home/user/docs → /shared/documents
/home/user/pics → /shared/photos
```

**Resulting Trie**:
```
                    root
                     │
                   home
                  /    \
               link    user
                |      /   \
            [/etc]  docs   pics
                     |      |
            [/shared/ [/shared/
             documents] photos]
```

**Legend**: `[target]` = symlink endpoint with target path

### **Resolution Algorithm**

```java
public static List<String> symlinkToPath(List<String> pathTokens, TrieNode root) {
    TrieNode current = root;
    TrieNode linkedNode = null;
    int index = -1;
    
    // Traverse Trie following path
    for (int i = 0; i < pathTokens.size(); i++) {
        String token = pathTokens.get(i);
        
        if (!current.children.containsKey(token)) {
            break;  // No further matching in Trie
        }
        
        current = current.children.get(token);
        
        // Check if this node is a symlink endpoint
        if (!current.pathTokens.isEmpty()) {
            linkedNode = current;
            index = i;
        }
    }
    
    if (linkedNode == null) {
        return null;  // No symlink found
    }
    
    // Build result: target + remaining path
    List<String> result = new ArrayList<>(linkedNode.pathTokens);
    if (index < pathTokens.size() - 1) {
        result.addAll(pathTokens.subList(index + 1, pathTokens.size()));
    }
    
    return result;
}
```

### **Step-by-Step Example**

**Given**:
```
path = "/home/user/docs/file.txt"
pathTokens = ["home", "user", "docs", "file.txt"]

Trie (from symlinks above):
        root
         │
       home
      /    \
   link    user
    |      /   \
 [/etc] docs   pics
```

**Execution**:

```
i=0: token = "home"
  current.children.containsKey("home") → true
  current = root → home
  current.pathTokens.isEmpty() → true (not a symlink)
  
i=1: token = "user"
  current.children.containsKey("user") → true
  current = home → user
  current.pathTokens.isEmpty() → true (not a symlink)
  
i=2: token = "docs"
  current.children.containsKey("docs") → true
  current = user → docs
  current.pathTokens.isEmpty() → false ✓
  linkedNode = docs
  index = 2
  current.pathTokens = ["shared", "documents"]
  
i=3: token = "file.txt"
  current.children.containsKey("file.txt") → false
  break (but we keep linkedNode from i=2)

Result:
  linkedNode.pathTokens = ["shared", "documents"]
  remaining = pathTokens[3:] = ["file.txt"]
  result = ["shared", "documents", "file.txt"]
  return as path: "/shared/documents/file.txt"
```

### **Time Complexity Analysis**

**Build Phase**:
```
For each symlink:
  Split path into tokens: O(k) where k = path length
  Insert into Trie: O(k)
  
Total with m symlinks: O(m × k)
Or: O(S) where S = total characters in all symlink paths
```

**Lookup Phase**:
```
For each token in path (n tokens):
  Check children HashMap: O(1)
  
Total: O(n)
```

**Overall for single path**: O(S) build + O(n) lookup = **O(S + n)**

**But if resolving multiple paths**:
```
Build once: O(S)
Resolve p paths: p × O(n)
Total: O(S + p×n)

Amortized per path: O(S/p + n)
As p → ∞: O(n)
```

### **Pros**

✅ **Optimal lookup time**
```java
// O(n) - only depends on path length
// Independent of number of symlinks!
```

✅ **Efficient for multiple resolutions**
```java
TrieNode root = buildTrie(symlinks);  // Once
// Now resolve thousands of paths
for (String path : paths) {
    resolve(path, root);  // Each is O(n)
}
```

✅ **No string building overhead**
```java
// Just traverse existing nodes
// No intermediate string creation
```

✅ **Natural prefix matching**
```java
// Trie inherently represents prefixes
// Longest match falls out naturally
```

### **Cons**

❌ **Complex implementation**
```java
// Requires TrieNode class
// Build logic is non-trivial
// Harder to debug
```

❌ **Build overhead**
```java
// O(S) preprocessing time
// If only resolving one path, this is wasted
```

❌ **Memory overhead**
```java
// O(S) space for Trie structure
// Each node has HashMap of children
// Pointer overhead per node
```

❌ **Interview time pressure**
```java
// 30-40 minutes to implement correctly
// More error-prone
// Harder to explain quickly
```

---

## 🔢 Performance Comparison with Concrete Numbers

### **Scenario 1: Small Scale**

```
Symlinks: 10
Path segments: 5
Single path resolution
```

**HashMap**:
```
Build: 0 (no build)
Resolve: 5 segments × ~1 check = ~5 operations
Total: ~5 operations
Memory: O(1) extra
```

**Trie**:
```
Build: 10 symlinks × 3 avg tokens = ~30 operations
Resolve: 5 segments × 1 lookup = 5 operations
Total: ~35 operations
Memory: ~30 TrieNodes
```

**Winner**: HashMap (7× faster for one-time use)

---

### **Scenario 2: Medium Scale**

```
Symlinks: 100
Path segments: 8
Single path resolution
```

**HashMap**:
```
Build: 0
Resolve: 8 segments × string operations
Total: ~8-15 operations (with string overhead)
Memory: O(1) extra
```

**Trie**:
```
Build: 100 symlinks × 4 avg tokens = ~400 operations
Resolve: 8 segments × 1 lookup = 8 operations
Total: ~408 operations
Memory: ~400 TrieNodes
```

**Winner**: HashMap (still faster for one-time)

---

### **Scenario 3: Large Scale, One-Time**

```
Symlinks: 1000
Path segments: 10
Single path resolution
```

**HashMap**:
```
Build: 0
Resolve: 10 segments × string comparisons
Total: ~10-20 operations
Memory: O(1) extra
```

**Trie**:
```
Build: 1000 symlinks × 4 avg tokens = ~4000 operations
Resolve: 10 segments × 1 lookup = 10 operations
Total: ~4010 operations
Memory: ~4000 TrieNodes
```

**Winner**: HashMap (400× faster for one-time!)

---

### **Scenario 4: Large Scale, Repeated**

```
Symlinks: 1000
Path segments: 10
Resolve 10,000 paths
```

**HashMap**:
```
Build: 0
Resolve: 10,000 × 10 segments = 100,000 operations
Total: ~100,000 operations
Memory: O(1) extra
```

**Trie**:
```
Build: 1000 symlinks × 4 tokens = 4,000 operations (once)
Resolve: 10,000 × 10 segments = 100,000 operations
Total: ~104,000 operations
Memory: ~4000 TrieNodes
```

**Winner**: Trie (build cost amortized over many resolutions)

---

## 🎯 When to Use Each Approach

### **Use HashMap When:**

✅ **Small number of symlinks** (< 50)
```java
// Overhead of Trie isn't worth it
Map<String, String> symlinks = Map.of(
    "/home/link", "/etc",
    "/var/log", "/mnt/logs"
);
```

✅ **One-time resolution**
```java
// Not resolving multiple paths
String result = cd(currentDir, targetDir, symlinks);
// Done - no more resolutions
```

✅ **Interview setting** ⭐
```java
// Time constraint: 45 minutes total, 20 for Phase 3
// HashMap: 15 minutes to implement
// Trie: 35 minutes to implement
// Clear winner: HashMap
```

✅ **Prototype/MVP**
```java
// Get something working quickly
// Optimize later if needed
```

✅ **Dynamic symlinks**
```java
// If symlinks change frequently
// Rebuilding Trie is expensive
symlinks.put("/new/link", "/new/target");  // Easy with Map
```

---

### **Use Trie When:**

✅ **Many symlinks** (> 100)
```java
// System with hundreds/thousands of symlinks
// /usr, /lib, /bin, ... all potentially symlinked
```

✅ **Repeated resolutions**
```java
// Server handling thousands of cd requests
TrieNode root = buildTrie(symlinks);  // Once at startup

// Then handle requests
while (true) {
    Request req = getRequest();
    String result = resolve(req.path, root);  // Fast
}
```

✅ **Production system** ⭐
```java
// After interview, optimize for performance
// Trie gives O(n) guarantee vs O(n×k) average
```

✅ **Static symlinks**
```java
// Filesystem symlinks don't change often
// Build Trie once, use forever
```

✅ **Memory is not a constraint**
```java
// Can afford O(S) space for better performance
```

---

## 🎤 Interview Discussion Points

### **What to Say After Implementing HashMap**

```
Interviewer: "How would you optimize this?"

You: "Currently I'm doing O(n) iterations where n is the number
      of path segments, and each iteration checks the HashMap.
      
      For many symlinks, I could use a Trie data structure:
      
      1. Build a Trie from all symlink paths (one-time O(S) cost)
      2. Traverse the path through the Trie in O(n) time
      3. The longest match is the deepest symlink node reached
      
      Trade-offs:
      - Trie is faster for repeated resolutions
      - Trie uses O(S) extra space for the tree structure
      - Trie is more complex to implement
      
      For this interview problem with a reasonable number of symlinks,
      the HashMap approach is simpler and adequate. In production with
      thousands of symlinks and high request volume, I'd use a Trie."
```

### **What to Say If Asked to Compare**

```
"Both approaches find the longest matching prefix:

HashMap Iteration:
  ✓ Simple: 40 lines of code
  ✓ No build time
  ✓ O(1) extra space
  ✗ O(n×k) time where k is average symlink path length
  
Trie Traversal:
  ✓ Optimal: O(n) lookup time
  ✓ Efficient for many lookups
  ✗ Complex: ~100 lines with TrieNode class
  ✗ O(S) space for Trie structure
  ✗ O(S) build time
  
For an interview, HashMap is better due to time constraints.
For production, it depends on:
  - Number of symlinks (few → HashMap, many → Trie)
  - Lookup frequency (one-time → HashMap, repeated → Trie)
  - Memory constraints (limited → HashMap, abundant → Trie)"
```

### **What to Say If Interviewer Pushes for Trie**

```
"Sure! I'll implement the Trie approach.

First, I'll create a TrieNode class to represent the tree structure.
Each node has:
  - A name (directory name)
  - Children (map of name to child node)
  - Path tokens (if this node represents a symlink endpoint)
  
Then I'll build the Trie by inserting each symlink path.
Finally, I'll traverse the input path through the Trie to find
the longest matching symlink.

This gives us O(S) build time and O(n) lookup time, where S is
the total characters in all symlink paths.

[Start implementing TrieNode class...]"
```

---

## 📊 Summary Table

| Criteria | HashMap | Trie | Winner |
|----------|---------|------|--------|
| **Code Simplicity** | ⭐⭐⭐⭐⭐ | ⭐⭐ | HashMap |
| **Implementation Time** | 15 min | 35 min | HashMap |
| **Time Complexity (single)** | O(n×k) avg | O(S+n) | Trie* |
| **Time Complexity (repeated)** | O(p×n×k) | O(S+p×n) | Trie |
| **Space Complexity** | O(1) | O(S) | HashMap |
| **Memory Efficiency** | ⭐⭐⭐⭐⭐ | ⭐⭐ | HashMap |
| **Scalability** | ⭐⭐ | ⭐⭐⭐⭐⭐ | Trie |
| **Production Ready** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Trie |
| **Interview Friendly** | ⭐⭐⭐⭐⭐ | ⭐⭐ | HashMap |
| **Debuggability** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | HashMap |

*For single resolution with many symlinks

---

## 🎯 Final Recommendation

### **For Your OpenAI Interview:**

**Phase 1-2**: Implement as usual (no choice needed yet)

**Phase 3**: 
```
Minute 30-45 (15 minutes for Phase 3):

Minute 30-32: Explain approach
"I'll use HashMap iteration for longest prefix matching.
 This is simpler to implement and adequate for this problem."

Minute 32-47: Implement HashMap approach
[Write the ~40 lines of code]

Minute 47-48: Test and debug

Minute 48-50: If time permits, discuss optimization
"For production with many symlinks, I'd use a Trie for O(n) lookup.
 The trade-off is O(S) space and more complex implementation."
```

**Why HashMap First?**
1. ✅ Can implement in 15 minutes
2. ✅ Shows you can deliver working code
3. ✅ Still demonstrates understanding of the problem
4. ✅ Leaves time for testing and discussion
5. ✅ Can discuss Trie as follow-up

**Why Not Trie First?**
1. ❌ Might not finish in 15 minutes
2. ❌ More error-prone under pressure
3. ❌ Harder to debug if something goes wrong
4. ❌ Less time for testing
5. ❌ Might seem over-engineered

### **Your Existing Trie Implementation:**

Your code already has the Trie approach! This is **impressive** and shows:
- ✅ You think about performance
- ✅ You know advanced data structures
- ✅ You can implement complex solutions

**How to leverage it in interview:**
```
1. Implement HashMap version first (safer)
2. Get it working and tested
3. Then say: "I also have a Trie-based optimization..."
4. Show your existing code or explain the approach
5. Discuss trade-offs

This shows:
✓ Pragmatism (HashMap for time constraint)
✓ Depth (know about Trie optimization)
✓ Experience (have production-grade implementation)
```

---

## 💡 Key Takeaway

> **The "best" approach depends on context.**

**In an interview**: HashMap (simple, fast to code, adequate)  
**In production**: Depends on scale and usage pattern  
**To show depth**: Know both, explain trade-offs  

**The skill is not just knowing both approaches,**  
**but knowing WHEN to use each one.**

And that's exactly what you now understand! 🎉

