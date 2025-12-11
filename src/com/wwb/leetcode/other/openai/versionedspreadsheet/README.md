# Versioned Spreadsheet with Time-Travel - OpenAI Interview Question

**Progressive 4-phase implementation combining dependency graphs + version control**

[![Tests](https://img.shields.io/badge/tests-24%20passing-brightgreen)]() [![Java](https://img.shields.io/badge/java-11%2B-blue)]()

---

## 🚀 Quick Start

```bash
cd /Users/wenbwang/IdeaProjects/Leet_Code/src/com/wwb/leetcode/other/openai/versionedspreadsheet
bash run_tests.sh
```

**Expected:** All 41 tests pass ✅ (24 functional + 17 validation)

---

## 📦 Package Structure

```
versionedspreadsheet/
├── Phase1Spreadsheet.java          # Basic cells + formulas (lazy)
├── Phase2Spreadsheet.java          # Live updates + dependency graph
├── Phase3VersionedSpreadsheet.java # Time-travel queries
├── Phase4UndoableSpreadsheet.java  # Undo/redo + audit
├── CellVersion.java                # Version history entry
├── Operation.java                  # Command pattern
├── README.md                       # This file
├── INTERVIEW_GUIDE.md              # Timing & strategy
├── START_HERE.md                   # Quick reference
└── run_tests.sh                    # Test runner
```

---

## 🎯 The 4 Phases

### **Phase 1: Basic Spreadsheet** (12-15 min)
**Goal:** Lazy evaluation of formulas with input validation

**API:**
```java
void set(int row, char col, int value);                // Validates bounds
void setFormula(int row, char col, String[] references); // Validates bounds, null, cell refs
int get(int row, char col);                             // Validates bounds, evaluates on demand
```

**Validations:**
- Row/column bounds checking
- Null reference array detection
- Cell reference format validation ("A1", "B2", etc.)
- Range validation (start <= end)

**Example:**
```java
sheet.set(1, 'A', 10);
sheet.set(2, 'A', 20);
sheet.setFormula(3, 'A', new String[]{"A1", "A2"});
sheet.get(3, 'A');  // → 30 (evaluated lazily)
```

**Key Concepts:**
- Cell storage (2D array)
- Formula parsing ("A1", "A1:B2" ranges)
- Weighted dependencies (if A1 appears 3x → weight=3)
- Recursive evaluation

**Complexity:** O(F × D) per get() where D = formula depth

---

### **Phase 2: Live Updates** (15-18 min)
**Goal:** Auto-propagate changes to dependent cells

**New API:**
```java
int sum(int row, char col, String[] references);  // Sets formula + calculates
// Changes to A1 auto-update all formulas referencing A1
```

**Example:**
```java
sheet.set(1, 'A', 10);
sheet.sum(2, 'A', new String[]{"A1"});  // B1 = A1
sheet.get(2, 'A');  // → 10

sheet.set(1, 'A', 20);  // Change A1
sheet.get(2, 'A');  // → 20 (auto-updated!)
```

**Key Concepts:**
- Dependency graph: `Map<Cell, Set<Cell>> dependents` (reverse graph)
- Cycle detection via DFS (prevent A1=B1, B1=A1)
- Recursive propagation when cell changes
- Clean formula removal from graph

**Complexity:** O(D) per set() where D = total dependents

**Pattern:** Exactly **LeetCode 631** (Excel Sheet) with live updates

---

### **Phase 3: Versioning + Time-Travel** (15-18 min)
**Goal:** Query historical values at any timestamp with validation

**New API:**
```java
long set(int row, char col, int value, long timestamp);      // Validates timestamp >= 0
long sum(int row, char col, String[] refs, long timestamp);  // Validates timestamp + refs
int get(int row, char col, long timestamp);                  // Validates timestamp
List<CellVersion> getHistory(int row, char col);             // Validates bounds
```

**New Validations:**
- Timestamp non-negativity check

**Example:**
```java
sheet.set(1, 'A', 10, 100);
sheet.set(1, 'A', 20, 200);

sheet.get(1, 'A', 150);  // → 10 (historical value)
sheet.get(1, 'A', 250);  // → 20
sheet.get(1, 'A');       // → 20 (current value)
```

**Key Concepts:**
- Per-cell version history: `NavigableMap<Long, CellVersion>`
- **Two evaluation modes:**
  - **Current:** Cached, live propagation (Phase 2)
  - **Historical:** Computed on demand with recursive time-travel
- **TreeMap.floorEntry()** for "at or before" lookup
- Store formulas as **strings** in history (Cell objects change)

**Complexity:** O(log V + F × log V) per historical get()

**Pattern:** Combines Phase 2 + **VersionedKV Store** pattern

---

### **Phase 4: Undo/Redo** (10-12 min, Bonus)
**Goal:** Per-user operation history with undo/redo and validation

**New API:**
```java
long set(String userId, int row, char col, int value, long timestamp);      // Validates userId
long sum(String userId, int row, char col, String[] refs, long timestamp);  // Validates userId
void undo(String userId);                            // Validates userId
void redo(String userId);                            // Validates userId
List<Operation> getOperationHistory(String userId);  // Validates userId
List<Operation> getAllOperations();  // Audit log
```

**New Validations:**
- UserId null/empty check

**Example:**
```java
sheet.set("alice", 1, 'A', 10, 100);
sheet.undo("alice");  // Revert to 0
sheet.redo("alice");  // Restore to 10
```

**Key Concepts:**
- **Command pattern:** `Operation` class
- Per-user undo/redo stacks
- Global audit log
- **Undo creates new version** (not rollback)

**Why undo as new edit?**
- Alice's undo doesn't break Bob's formulas
- Full audit trail preserved
- Time-travel still works correctly

**Complexity:** O(log V + D) per undo/redo

---

## 🛡️ Input Validation Strategy

All phases implement **fail-fast validation** with detailed error messages:

### **Critical Validations (All Phases):**

1. **Bounds Checking**
   ```java
   validateBounds(int row, char col);
   // Throws: "Row 100 out of bounds [1, 5]"
   // Throws: "Column 'Z' out of bounds ['A', 'E']"
   ```

2. **Null Checks**
   ```java
   if (references == null) {
       throw new IllegalArgumentException("References array cannot be null");
   }
   ```

3. **Cell Reference Validation**
   ```java
   validateCellReference("A1");  // Valid
   validateCellReference("1A");  // Throws: "Invalid cell reference format: '1A'"
   validateCellReference("Z999"); // Throws: "Column 'Z' out of bounds"
   ```

4. **Range Validation**
   ```java
   validateRange(["B2", "A1"]);  // Throws: "Invalid range 'B2:A1': start must be <= end"
   ```

### **Phase-Specific Validations:**

**Phase 3:** Timestamp validation
```java
validateTimestamp(-1);  // Throws: "Timestamp cannot be negative: -1"
```

**Phase 4:** UserId validation
```java
validateUserId(null);   // Throws: "User ID cannot be null"
validateUserId("  ");   // Throws: "User ID cannot be empty"
```

### **Why Fail-Fast?**
- ✅ Catches bugs early in development
- ✅ Prevents silent data corruption
- ✅ Clear, actionable error messages
- ✅ Production-quality code

### **Interview Discussion Point:**
> "I've added input validation with fail-fast semantics. In production, this prevents 
> ArrayIndexOutOfBoundsException and NullPointerException, which are hard to debug. 
> The detailed error messages help developers quickly identify issues."

---

## 💡 Key Design Decisions

### 1. **Why Two Evaluation Modes?**
- **Current state:** Cached values, live propagation → O(1) reads
- **Historical state:** Recompute on demand → O(log V) per dependency
- **Trade-off:** Memory vs flexibility

### 2. **Why Store Formula as Strings in History?**
- Cell objects change over time
- Storing Cell references would break historical evaluation
- String references ("A1") are immutable and portable

### 3. **Why Undo Creates New Version?**
- Collaborative editing: Alice's undo shouldn't break Bob's work
- Undo is just another edit at current timestamp
- Preserves full audit trail

### 4. **Why Not Cache Historical Values?**
- Formulas depend on other cells' historical values
- Caching requires invalidation on any historical query
- Simpler to recompute (acceptable performance for interviews)

### 5. **Why Dependency Graph Instead of Recalc-All?**
- Sparse updates: Only affected cells recompute
- O(D) vs O(total cells) for each change
- Critical for large spreadsheets

---

## 📊 Complexity Summary

| Operation | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|-----------|---------|---------|---------|---------|
| set() | O(1) | O(D) | O(log V + D) | Same |
| setFormula/sum() | O(F) | O(F + C + D) | O(F + log V + C + D) | Same |
| get() | O(F × D) | O(1) | O(1) current<br>O(log V + F × log V) historical | Same |
| undo/redo | - | - | - | O(log V + D) |

**Where:**
- **F** = formula size (cells referenced)
- **D** = dependent cells (propagation cost)
- **V** = versions per cell
- **C** = cycle detection cost

---

## 🎤 Interview Strategy

### Time Allocation (55 minutes):
- **Phase 1** (12-15 min): Get core parsing + evaluation right
- **Phase 2** (15-18 min): Focus on dependency graph + cycle detection
- **Phase 3** (15-18 min): Explain two-mode evaluation clearly
- **Phase 4** (5-10 min): If time permits, discuss undo semantics

### Critical Questions to Ask:

**Phase 1:**
- "Should formulas evaluate eagerly or lazily?"
- "How should we handle invalid references?"
- "Are cell coordinates 0-indexed or 1-indexed?"

**Phase 2:**
- "Should changes propagate immediately or on next get()?"
- "How should we handle circular dependencies?"
- "What happens if we set() a cell that has a formula?"

**Phase 3:**
- "Should historical queries recompute or use cached values?"
- "Can timestamps be out of order?"
- "Does current state maintain live propagation?"

**Phase 4:**
- "Should undo create a new version or rollback?"
- "What if Alice undoes while Bob's formula depends on it?"
- "Do we need global undo or per-user?"

---

## 🔥 Interview Tips

### Do's:
- ✅ Start with simplest Phase 1 (lazy evaluation)
- ✅ Draw dependency graph on whiteboard for Phase 2
- ✅ Explain two-mode evaluation clearly for Phase 3
- ✅ Test edge cases: cycles, empty cells, out-of-order timestamps
- ✅ State complexity for each operation
- ✅ Connect to real systems (Google Sheets, Excel, Git)

### Don'ts:
- ❌ Jump to Phase 3 immediately
- ❌ Forget cycle detection in Phase 2
- ❌ Mix up current vs historical evaluation
- ❌ Store Cell references in historical formulas
- ❌ Implement undo as rollback (creates consistency issues)

---

## 🎓 What This Tests

### Technical Skills:
- **Data Structures:** HashMap, Graph, TreeMap, Stack
- **Algorithms:** DFS (cycle detection), graph traversal (propagation)
- **Design Patterns:** Command pattern (undo/redo)
- **System Design:** Versioning, conflict resolution, audit logging

### Interview Skills:
- **Progressive thinking:** Simple → production-ready
- **Clarifying questions:** What to ask at each phase
- **Complexity analysis:** Big-O for all operations
- **Trade-off discussion:** Cached vs computed, memory vs flexibility

---

## 🔗 Related Concepts

This question connects to:
- **LeetCode 631:** Excel Sheet (Phase 1-2)
- **Version Control:** Git (branching, time-travel)
- **Databases:** Temporal tables, MVCC
- **Collaboration:** Google Docs operational transform
- **Pattern:** Command (undo/redo), Memento (snapshots)

---

## 🚀 Running Tests

### All phases:
```bash
bash run_tests.sh
```

### Individual phase:
```bash
cd /Users/wenbwang/IdeaProjects/Leet_Code

# Compile
/usr/bin/javac src/com/wwb/leetcode/other/openai/versionedspreadsheet/*.java

# Run
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase1Spreadsheet
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase2Spreadsheet
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase3VersionedSpreadsheet
/usr/bin/java -cp src com.wwb.leetcode.other.openai.versionedspreadsheet.Phase4UndoableSpreadsheet
```

---

## ✅ Success Criteria

### **Strong Hire:**
- ✅ Phase 1-2 perfect with cycle detection
- ✅ Phase 3 with clear two-mode explanation
- ✅ Phase 4 or deep discussion of production concerns
- ✅ Clean code, good test cases
- ✅ Rich discussion of trade-offs

### **Hire:**
- ✅ Phase 1-2 complete
- ✅ Phase 3 with correct approach (even if incomplete)
- ✅ Can explain remaining implementation

---

## 📝 Practice Drills

### Drill 1: Speed Implementation (15 min)
```
Set timer for 15 minutes
Implement Phase 1 from scratch (don't look)
Compare with solution
Note what you missed
```

### Drill 2: Whiteboard Design (20 min)
```
Design Phase 2 on whiteboard (no coding)
Draw dependency graph
Explain propagation algorithm
Discuss cycle detection approach
```

### Drill 3: Mock Interview (50 min)
```
Have someone ask you the question
Work through all phases progressively
Practice thinking out loud
Get feedback on communication
```

---

## 🎯 You're Ready!

**Files:** 6 Java files + 3 documentation files  
**Tests:** 24 test cases across all phases  
**Pattern:** LeetCode 631 + VersionedKV synthesis  
**Status:** ✅ Production-quality, interview-optimized  

Good luck with your OpenAI interview! 🍀

