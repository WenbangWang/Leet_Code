# Versioned Spreadsheet - Interview Strategy Guide

## 🎯 Question Premise

**Interviewer:** *"Design a spreadsheet system that stores values and formulas. Start simple."*

This is your cue for a **progressive implementation** question. They'll extend requirements after each phase.

---

## ⏱️ Timing Strategy (50-55 min total)

### **Phase 1: Basic Spreadsheet** (12-15 min)
- **Goal:** Get formula parsing and lazy evaluation working
- **Time breakdown:**
  - Clarifying questions: 2 min
  - Design discussion: 3 min
  - Implementation: 7-8 min
  - Testing: 2 min

**Key Milestone:** Must have working lazy evaluation before moving on

---

### **Phase 2: Live Updates** (15-18 min)
- **Goal:** Build dependency graph with cycle detection
- **Time breakdown:**
  - Design discussion: 4 min (draw graph!)
  - Implementation: 9-10 min
  - Cycle detection: 3-4 min
  - Testing: 2 min

**Key Milestone:** Propagation + cycle detection working

---

### **Phase 3: Versioning** (15-18 min)
- **Goal:** Add time-travel with two evaluation modes
- **Time breakdown:**
  - Design discussion: 5 min (explain two modes!)
  - Implementation: 8-10 min
  - Testing: 2-3 min

**Key Milestone:** Historical queries with recursive evaluation

---

### **Phase 4: Undo/Redo** (5-10 min, if time)
- **Goal:** Show command pattern knowledge
- **Time breakdown:**
  - Design discussion: 3-4 min
  - Implementation: 3-5 min (may not finish)
  - Discussion: 2-3 min

**Key Milestone:** Explain approach even if not fully implemented

---

## 🎤 Opening (First 2 minutes)

### **When Asked:**
> "Design a spreadsheet that can store values and formulas."

### **Your Response:**
```
"Great! Let me clarify a few things:

1. Cell references: Are we using Excel-style (A1, B2) or 0-indexed?
   → [Assume Excel-style: A1, B2, etc.]

2. Formula syntax: Should I parse full expressions like '=A1+B2*C3' 
   or just handle SUM operations?
   → [Start with SUM of cell references]

3. Formula evaluation: Lazy (on get) or eager (on set)?
   → [Your call, I'll explain trade-offs]

4. Scope: Just functionality first, or should I think about 
   multi-threading, persistence, etc?
   → [Start simple, we can extend]

I'll start with a basic in-memory implementation with lazy evaluation,
then we can discuss extensions."
```

**Why this works:**
- Shows you think about requirements
- Prevents over-engineering
- Sets expectations for progressive design

---

## 📋 Phase-by-Phase Checklist

### **Phase 1 Checklist:**
- [ ] 2D cell storage (array or HashMap)
- [ ] `set(row, col, value)` - O(1)
- [ ] `setFormula(row, col, references)` - parse formula
- [ ] `get(row, col)` - lazy evaluate
- [ ] Parse single cells ("A1")
- [ ] Parse ranges ("A1:B2")
- [ ] Handle weighted references (A1 appears 3x)
- [ ] Test: basic formula, range, overwrite

**Common Mistakes:**
- ❌ Forgetting to handle ranges
- ❌ Not tracking reference counts
- ❌ Over-engineering formula parsing

---

### **Phase 2 Checklist:**
- [ ] Cell class with position, value, formula
- [ ] Dependency graph: `Map<Cell, Set<Cell>>`
- [ ] `clearFormula()` - remove from graph
- [ ] `propagate()` - recursive update
- [ ] `hasCycle()` - DFS cycle detection
- [ ] `sum()` instead of `setFormula()` (calculates immediately)
- [ ] Test: live update, cascade, cycle detection

**Common Mistakes:**
- ❌ Forgetting cycle detection
- ❌ Not cleaning up old dependencies
- ❌ Infinite recursion on cycles
- ❌ Not maintaining reverse graph

**Pro Tip:** Draw the graph on whiteboard:
```
A1 (10) ──→ B1 (formula: =A1)
        ──→ C1 (formula: =A1+A1)
```

---

### **Phase 3 Checklist:**
- [ ] Add `NavigableMap<Long, CellVersion>` per cell
- [ ] Overload `set(row, col, value, timestamp)`
- [ ] Overload `sum(row, col, refs, timestamp)`
- [ ] `get(row, col, timestamp)` - TreeMap.floorEntry()
- [ ] Store formulas as strings in history
- [ ] Recursive historical evaluation
- [ ] Current state still has live propagation
- [ ] Test: versioning, time-travel, live updates

**Common Mistakes:**
- ❌ Storing Cell references in history (they change!)
- ❌ Breaking live propagation for current state
- ❌ Not handling "no version before timestamp"
- ❌ Confusing current vs historical modes

**Critical Explanation:**
> "I'm maintaining two modes: current state uses cached values with live 
> propagation for O(1) reads. Historical queries recompute on-demand using 
> TreeMap.floorEntry() to find the right version. This trades memory for 
> flexibility in time-travel queries."

---

### **Phase 4 Checklist:**
- [ ] `Operation` class (Command pattern)
- [ ] Per-user undo/redo stacks
- [ ] Global audit log
- [ ] Overload `set(userId, ...)`
- [ ] Overload `sum(userId, ...)`
- [ ] `undo(userId)` - creates new version
- [ ] `redo(userId)` - re-applies operation
- [ ] Test: undo, redo, per-user, audit log

**Common Mistakes:**
- ❌ Implementing undo as rollback (breaks collaboration)
- ❌ Sharing undo stacks between users
- ❌ Not clearing redo stack on new operation

**Critical Explanation:**
> "Undo creates a new version rather than rolling back. This preserves the 
> audit trail and prevents Alice's undo from breaking Bob's formulas that 
> might depend on her changes. It's consistent with collaborative editing."

---

## 💬 Key Discussion Points

### **When Extending from Phase 1 to 2:**
**Interviewer:** *"Now make formulas update automatically when cells change."*

**Your Response:**
```
"Ah, so we need live propagation. Two approaches:

1. Dependency graph: Track which cells depend on each cell.
   When A1 changes, recursively update all dependents.
   Pros: Efficient for sparse updates. Cons: More complex.

2. Recalculate all: On any change, recalc all formulas.
   Pros: Simple. Cons: O(total cells) every time.

For a spreadsheet, dependency graph is better. I'll build a reverse 
graph where A1 → {B1, C1} means B1 and C1 depend on A1.

One critical issue: circular dependencies. I'll need DFS cycle 
detection before adding edges."
```

---

### **When Extending from Phase 2 to 3:**
**Interviewer:** *"Add version history so we can query historical values."*

**Your Response:**
```
"Interesting! So we want time-travel queries. Key decisions:

1. Storage: Per-cell TreeMap<timestamp, version> for O(log V) lookup.

2. Evaluation modes:
   - Current state: Keep Phase 2 behavior (cached + live propagation)
   - Historical state: Recompute on demand at specific timestamp

3. Formula storage: In history, store formulas as strings, not Cell 
   references, because Cell objects change over time.

4. floorEntry(): Use TreeMap.floorEntry(timestamp) to find version 
   'at or before' the query time.

Trade-off: Historical queries are slower (O(log V) per dependency) 
but current state stays fast (O(1))."
```

---

### **When Asked About Production:**

**Possible questions:**
- "How would you scale this?"
- "What about persistence?"
- "How would you handle conflicts in collaborative editing?"

**Your Response:**
```
"Great questions! Production considerations:

**Scaling:**
- Partition by cell ranges (sharding)
- Cache frequently accessed cells (Redis)
- Async propagation for large dependency chains

**Persistence:**
- Snapshot + WAL (write-ahead log) for durability
- Compress version history (delta encoding)
- Lazy load historical versions

**Collaborative Editing:**
- Operational Transform (like Google Docs)
- Or CRDT (conflict-free replicated data types)
- Version vectors for causal consistency

The current design is a good foundation. For Phase 3, we're already 
doing multi-version concurrency control (MVCC) which databases use."
```

---

## 🎯 Evaluation Rubric (What They're Looking For)

### **Strong Hire Signals:**
✅ Asks clarifying questions before coding  
✅ Draws diagrams (especially dependency graph)  
✅ Explains trade-offs proactively  
✅ Gets Phase 1-2 perfect, Phase 3 working  
✅ Clean code with good variable names  
✅ Tests edge cases (cycles, empty, ranges)  
✅ States complexity for each operation  
✅ Connects to real systems (Git, Google Sheets, databases)  
✅ Adapts quickly when requirements change  

### **Hire Signals:**
✅ Completes Phase 1-2  
✅ Correct approach for Phase 3 (even if incomplete)  
✅ Can explain remaining implementation  
✅ Handles basic test cases  
✅ Reasonable complexity analysis  

### **Weak Signals:**
❌ Jumps to coding without clarifying  
❌ Forgets cycle detection  
❌ Confuses current vs historical evaluation  
❌ Can't explain trade-offs  
❌ Doesn't test edge cases  
❌ Mixed up on complexity  

---

## 🚨 Common Pitfalls

### **1. Over-parsing Formulas**
❌ Trying to parse full expressions: `=A1+B2*C3/D4`  
✅ Just handle SUM of cell references: `["A1", "B2"]`

**If asked about expressions:**
> "For a full implementation, I'd use a parser (lex/yacc) or expression tree.
> For now, I'll focus on the dependency graph which is the interesting part."

---

### **2. Wrong Complexity for Phase 1 get()**
❌ Saying O(1) for lazy evaluation  
✅ O(F × D) where F = formula size, D = depth

**Explanation:**
> "get() evaluates recursively. If C1=B1, B1=A1, A1=10, then get(C1) 
> evaluates B1 which evaluates A1. Depth = 2, so O(D)."

---

### **3. Not Detecting Cycles**
❌ Allowing A1=B1, B1=A1  
✅ DFS before adding edge to dependency graph

**Test case:**
```java
sheet.set(1, 'A', 10);
sheet.sum(2, 'A', ["A1"]);  // B1 = A1
sheet.sum(1, 'A', ["B1"]);  // A1 = B1 - CYCLE!
```

---

### **4. Breaking Live Updates in Phase 3**
❌ Only supporting historical queries  
✅ Current state still has O(1) get() with live propagation

**Test case:**
```java
sheet.set(1, 'A', 10, 100);
sheet.sum(2, 'A', ["A1"], 200);
sheet.set(1, 'A', 20, 300);
assert sheet.get(2, 'A') == 20;  // Must auto-update!
```

---

### **5. Undo as Rollback**
❌ `undo()` reverts to previous version directly  
✅ `undo()` creates new version with old value

**Why?**
```
Alice: set(1, 'A', 10, 100)
Bob:   sum(2, 'A', ["A1"], 200)  // B1 = A1 = 10
Alice: undo() at t=300

If rollback: B1's history is broken (depends on non-existent version)
If new version: B1 still valid, just evaluates to new A1 value
```

---

## 📚 Additional Study Topics

If you have extra time before interview:

### **Related LeetCode Problems:**
- **631. Design Excel Sum Formula** (Phase 1-2 exactly!)
- **146. LRU Cache** (similar design patterns)
- **355. Design Twitter** (dependency relationships)

### **Concepts to Review:**
- **Graph algorithms:** DFS, topological sort, cycle detection
- **TreeMap operations:** floorEntry, ceilingEntry, subMap
- **Design patterns:** Command, Memento, Observer
- **MVCC:** Multi-version concurrency control

### **System Design:**
- **Google Sheets architecture:** Operational Transform
- **Git internals:** Commit graph, time-travel
- **Temporal databases:** Point-in-time queries

---

## ✅ Pre-Interview Checklist

### **Day Before:**
- [ ] Implement Phase 1 from scratch (15 min)
- [ ] Implement Phase 2 from scratch (20 min)
- [ ] Review Phase 3 TreeMap.floorEntry() pattern
- [ ] Understand cycle detection DFS
- [ ] Practice explaining two-mode evaluation

### **Morning Of:**
- [ ] Review this guide (10 min)
- [ ] Mental walkthrough: Phase 1 → 2 → 3 transitions
- [ ] Remember edge cases: cycles, ranges, empty cells
- [ ] Remember to ask clarifying questions!

---

## 🎉 You're Ready!

Remember:
1. **Start simple** - Phase 1 perfect
2. **Communicate** - Explain your thinking
3. **Test** - Edge cases as you go
4. **Adapt** - Requirements will change
5. **Connect** - Mention LeetCode 631, Git, Google Sheets

**You've got this!** 🚀

