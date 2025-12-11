# 🎯 Versioned Spreadsheet - Quick Start

## What Is This?

A **4-phase progressive interview question** that combines:
- **LeetCode 631** (Excel with formulas + dependency graph)
- **VersionedKV Store** (time-travel with TreeMap)

Perfect for OpenAI interviews focusing on **algorithm + system design**.

---

## 🚀 Run Tests Now

```bash
cd /Users/wenbwang/IdeaProjects/Leet_Code/src/com/wwb/leetcode/other/openai/versionedspreadsheet
bash run_tests.sh
```

**Expected:** All 24 tests pass ✅

---

## 📖 Study Path

### **If Interview is Tomorrow (30 min):**
1. Read this file (5 min)
2. Run tests to see what works (2 min)
3. Read `README.md` "The 4 Phases" section (10 min)
4. Skim `Phase2Spreadsheet.java` for dependency graph (8 min)
5. Review `INTERVIEW_GUIDE.md` timing section (5 min)

### **If Interview is Next Week (3 hours):**
1. **Day 1 (1 hour):**
   - Read `README.md` fully
   - Study `Phase1Spreadsheet.java` and `Phase2Spreadsheet.java`
   - Understand dependency graph + cycle detection

2. **Day 2 (1 hour):**
   - Study `Phase3VersionedSpreadsheet.java`
   - Understand two-mode evaluation (current vs historical)
   - Practice explaining TreeMap.floorEntry() pattern

3. **Day 3 (1 hour):**
   - Implement Phase 1 from scratch (no peeking!)
   - Implement Phase 2 from scratch
   - Compare with solution, note differences

### **If Building Deep Understanding (8 hours):**
- Full study of all 4 phases
- Implement from scratch 2-3 times
- Practice whiteboard explanations
- Research related topics (Git, Google Sheets, MVCC)

---

## 🎯 The Core Insights

### **Phase 1 → Phase 2: The Big Leap**
Phase 1 uses **lazy evaluation** (compute on get).  
Phase 2 adds **eager propagation** (update dependents immediately).

**Key data structure:**
```java
Map<Cell, Set<Cell>> dependents;  // Reverse graph
// A1 → {B1, C1} means "B1 and C1 depend on A1"
```

**Critical algorithm:** Cycle detection via DFS before adding edges.

---

### **Phase 2 → Phase 3: The Pattern**
Phase 3 adds **VersionedKV pattern** to Phase 2.

**Key data structure:**
```java
NavigableMap<Long, CellVersion> history;  // Per cell
```

**Two evaluation modes:**
1. **Current:** `get()` returns cached value (O(1))
2. **Historical:** `get(timestamp)` recursively evaluates (O(log V))

**Critical method:** `TreeMap.floorEntry(timestamp)` finds "at or before"

---

### **Phase 3 → Phase 4: The Pattern**
Phase 4 adds **Command pattern** for undo/redo.

**Key insight:** Undo creates **new version** (not rollback).

Why? Collaborative editing. Alice's undo shouldn't break Bob's formulas.

---

## 🎤 Interview Opening

**Interviewer:** *"Design a spreadsheet system that stores values and formulas."*

**You:** 
```
"Great! Quick clarifications:
1. Cell references: Excel-style (A1, B2)?
2. Formulas: Full expressions or just SUM?
3. Evaluation: Lazy (on get) or eager (immediate)?
4. Scope: Start simple, extend later?

I'll start with in-memory, lazy evaluation, Excel-style references.
We can add complexity progressively."
```

**Then:** Implement Phase 1 (12-15 min).

---

## 📊 Complexity Cheat Sheet

| Operation | Phase 1 | Phase 2 | Phase 3 Current | Phase 3 Historical |
|-----------|---------|---------|-----------------|---------------------|
| set() | O(1) | O(D) | O(log V + D) | - |
| get() | O(F×D) | O(1) | O(1) | O(log V + F×log V) |

**D** = dependents  
**F** = formula depth  
**V** = versions per cell

---

## 🔑 Key Code Patterns

### **Formula Parsing (Phase 1):**
```java
// Handle ranges: "A1:B2" expands to {A1, A2, B1, B2}
if (ref.contains(":")) {
    String[] parts = ref.split(":");
    // ... expand range
}
```

### **Cycle Detection (Phase 2):**
```java
// Check if adding edge from→to creates cycle
// Cycle exists if path already exists to→from
private boolean hasCycle(Cell from, Cell to) {
    return dfsHasPath(to, from, new HashSet<>());
}
```

### **Time-Travel (Phase 3):**
```java
// Find version at or before timestamp
Map.Entry<Long, CellVersion> entry = 
    cell.history.floorEntry(timestamp);
```

### **Undo as New Edit (Phase 4):**
```java
// Don't rollback - create new version!
public void undo(String userId) {
    Operation op = undoStacks.get(userId).pop();
    super.set(row, col, op.getOldValue(), currentTime());
}
```

---

## 🚨 Critical Edge Cases

### **Test These During Interview:**

**Phase 1:**
```java
// Range expansion
sheet.setFormula(3, 'A', new String[]{"A1:B2"});

// Weighted references (A1 appears 3x)
sheet.setFormula(2, 'A', new String[]{"A1", "A1", "A1"});

// Overwrite formula with value
sheet.setFormula(2, 'A', ...);
sheet.set(2, 'A', 100);  // Should clear formula
```

**Phase 2:**
```java
// Cycle detection
sheet.sum(2, 'A', new String[]{"A1"});
sheet.sum(1, 'A', new String[]{"B1"});  // CYCLE!

// Cascading updates
sheet.sum(2, 'A', new String[]{"A1"});  // B1 = A1
sheet.sum(3, 'A', new String[]{"B1"});  // C1 = B1
sheet.set(1, 'A', 10);  // Should update B1 and C1
```

**Phase 3:**
```java
// Time-travel with formulas
sheet.set(1, 'A', 10, 100);
sheet.sum(2, 'A', new String[]{"A1"}, 200);
sheet.set(1, 'A', 20, 300);

sheet.get(2, 'A', 250);  // Should evaluate with A1=10
sheet.get(2, 'A', 350);  // Should evaluate with A1=20

// Current state still live-updates
sheet.get(2, 'A');  // Should be 20 (not recomputed)
```

**Phase 4:**
```java
// Per-user undo stacks
sheet.set("alice", 1, 'A', 10, 100);
sheet.set("bob", 2, 'A', 20, 200);
sheet.undo("alice");  // Only affects A1
```

---

## 📚 What to Review

### **Your Existing Code:**
- ✅ `No631.java` - Excel with formulas (Phase 1-2 basis)
- ✅ `Phase2VersionedKVStore_LockFree.java` - Time-travel pattern (Phase 3 basis)

### **Algorithms:**
- DFS for cycle detection
- Graph traversal for propagation
- TreeMap operations (floorEntry, ceilingEntry)

### **Patterns:**
- Dependency graph (reverse graph optimization)
- Command pattern (undo/redo)
- MVCC (multi-version concurrency control)

---

## ✅ Quick Self-Test

**Can you answer these?**

1. Why use reverse graph (`dependents`) instead of forward graph?
   <details>
   <summary>Answer</summary>
   When A1 changes, we need to find all cells that depend on A1.
   Reverse graph: O(1) lookup. Forward graph: O(all cells) scan.
   </details>

2. Why store formulas as strings in history?
   <details>
   <summary>Answer</summary>
   Cell objects change over time. String references ("A1") are immutable.
   Storing Cell references would break historical evaluation.
   </details>

3. Why does undo create a new version instead of rollback?
   <details>
   <summary>Answer</summary>
   Collaborative editing. If Alice undos while Bob's formula depends on her
   cell, rollback would break Bob's work. New version preserves consistency.
   </details>

4. What's the complexity of `get()` in Phase 2 vs Phase 3 historical?
   <details>
   <summary>Answer</summary>
   Phase 2: O(1) - cached value
   Phase 3 historical: O(log V + F × log V) - recursive evaluation
   </details>

---

## 🎯 You're Ready If...

✅ You can implement Phase 1 in 15 minutes  
✅ You understand dependency graph in Phase 2  
✅ You can explain two-mode evaluation in Phase 3  
✅ You know why undo is "new edit" not "rollback"  
✅ You can state complexity for each operation  

---

## 📁 File Reference

```
Phase1Spreadsheet.java          ← Start here
Phase2Spreadsheet.java          ← Study dependency graph
Phase3VersionedSpreadsheet.java ← Study TreeMap pattern
Phase4UndoableSpreadsheet.java  ← Study Command pattern
README.md                       ← Full documentation
INTERVIEW_GUIDE.md              ← Timing & strategy
```

---

## 🚀 Next Steps

1. **Run tests:** `bash run_tests.sh`
2. **Read implementations:** Phase 1 → Phase 2 → Phase 3
3. **Practice:** Implement Phase 1-2 from scratch
4. **Review:** `INTERVIEW_GUIDE.md` for timing strategy

---

**You've got this!** 💪

This is a synthesis of your No631 and VersionedKV knowledge.  
You already understand the building blocks.  
Now it's just about combining them smoothly in an interview setting.

Good luck! 🍀

