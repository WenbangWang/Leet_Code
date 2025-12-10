# Memory Allocator - Interview Day Quick Reference

**Read this the morning of your interview!** ☕

---

## 🎯 The 30-Second Summary

**What:** Implement malloc/free with compaction (defragmentation)  
**Key Challenge:** Compaction with address translation map (where candidates fail!)  
**Time:** 50 minutes for 3-4 phases  
**Win Condition:** Complete Phase 1-3 with working compaction

---

## 🚀 Quick Start Strategy

### **Opening (First 2 minutes):**

> "I'll implement a memory allocator. I can use first-fit (simpler, O(n) malloc) 
> or best-fit (optimized, O(log n) malloc with size-indexed TreeMap). Which do 
> you prefer? Should I use actual memory addresses or array indices? Should I 
> coalesce adjacent free blocks automatically?"

**Shows:** You know data structures, thinking about requirements, asking good questions

---

### **Phase 1: Basic Allocator (Minutes 2-15)**

**Goal:** malloc() and free() working with coalescing

**Option A: First-Fit (Start with this - simpler)**
```java
TreeMap<Long, Block> freeBlocks;
TreeMap<Long, Block> allocatedBlocks;

long malloc(int size) {
    // First-fit: O(n) - find first free block >= size
    for (Block block : freeBlocks.values()) {
        if (block.size >= size) {
            // Allocate, split if needed, return address
        }
    }
    return -1;
}
```

**Option B: Best-Fit (Mention if asked about optimization)**
```java
TreeMap<Long, Block> freeByAddr;                   // For coalescing
TreeMap<Integer, TreeSet<Block>> freeBySize;       // For O(log n) malloc!
TreeMap<Long, Block> allocatedBlocks;

long malloc(int size) {
    // Best-fit: O(log n) - use size-indexed map
    Map.Entry<Integer, TreeSet<Block>> entry = freeBySize.ceilingEntry(size);
    if (entry == null) return -1;
    
    Block block = entry.getValue().first();  // Smallest fitting block
    // Remove from both maps, allocate, split if needed...
}

void free(long address) {
    Block block = allocatedBlocks.remove(address);
    
    // CRITICAL: Coalesce with adjacent free blocks!
    Block left = freeBlocks.floorEntry(address - 1);
    Block right = freeBlocks.ceilingEntry(address + size);
    
    // Merge if adjacent...
}
```

**Key Points:**
- ✅ Start with first-fit (simpler)
- ✅ Use TreeMap for sorted access
- ✅ **Always coalesce** (critical!)
- ✅ Handle edge cases: double-free, invalid address
- ✅ Test as you go: malloc, free, malloc again
- ✅ Can discuss best-fit optimization if time permits

**Time:** 12-15 minutes

**If Interviewer Asks: "Can you optimize malloc?"**
> "Yes! Instead of O(n) first-fit, I can use best-fit with a size-indexed 
> TreeMap. This gives O(log n) malloc by using ceilingEntry(size) to find 
> the smallest block that fits. Trade-off is maintaining two TreeMaps 
> (one by address for coalescing, one by size for allocation)."

---

### **Phase 2: Realloc + Ownership (Minutes 15-30)**

**Goal:** Add ownership tracking and realloc()

```java
String malloc(int size, String ownerId) {
    // Same as Phase 1, but:
    // 1. Return hex string "0x0000"
    // 2. Track ownership
    // 3. Store timestamp
}

void free(String address, String ownerId) {
    Block block = allocatedBlocks.get(parseAddress(address));
    
    // CRITICAL: Validate ownership!
    if (!block.ownerId.equals(ownerId)) {
        throw new IllegalArgumentException("Access violation!");
    }
    
    // Then free...
}

String realloc(String address, int newSize, String ownerId) {
    if (newSize < oldSize) {
        // Shrink: always succeeds in-place
        block.size = newSize;
        coalesceAndAddFree(address + newSize, oldSize - newSize);
        return address;
    } else {
        // Grow: check if next block is free
        Block next = freeBlocks.get(address + oldSize);
        if (next && next.size >= additionalNeeded) {
            // Expand in-place
            return address;
        }
        return null;  // Can't expand
    }
}
```

**Key Points:**
- ✅ Validate ownership before free/realloc
- ✅ Try in-place realloc first
- ✅ Shrink always succeeds, grow may fail
- ✅ Track allocations per owner

**Time:** 12-15 minutes

---

### **Phase 3: Compaction ⭐ (Minutes 30-48)** 

**THIS IS THE CRITICAL PHASE!**

**Goal:** Implement compact() with address translation

```java
public Map<String, String> compact() {
    Map<String, String> addressTranslation = new HashMap<>();
    
    if (allocatedBlocks.isEmpty()) {
        return addressTranslation;  // Nothing to compact
    }
    
    // Step 1: Sort all allocated blocks by address
    List<Block> blocks = new ArrayList<>(allocatedBlocks.values());
    Collections.sort(blocks);  // Uses Block.compareTo()
    
    // Step 2: Slide blocks left to fill gaps
    long targetAddr = 0;
    Map<Long, Block> newAllocatedBlocks = new TreeMap<>();
    Map<String, List<Long>> newOwnerToAddresses = new HashMap<>();
    
    for (Block block : blocks) {
        long oldAddr = block.address;
        
        // Track if block moves
        if (oldAddr != targetAddr) {
            // CRITICAL: Record the move!
            addressTranslation.put(
                formatAddress(oldAddr), 
                formatAddress(targetAddr)
            );
            
            block.address = targetAddr;  // Update block
        }
        
        // Add to new structures
        newAllocatedBlocks.put(targetAddr, block);
        newOwnerToAddresses
            .computeIfAbsent(block.ownerId, k -> new ArrayList<>())
            .add(targetAddr);
        
        targetAddr += block.size;
    }
    
    // Step 3: Replace old structures with new
    this.allocatedBlocks = newAllocatedBlocks;
    this.ownerToAddresses = newOwnerToAddresses;
    
    // Step 4: Create one big free block at end
    this.freeBlocks = new TreeMap<>();
    if (targetAddr < totalSize) {
        freeBlocks.put(targetAddr, 
                      new Block(targetAddr, totalSize - (int)targetAddr));
    }
    
    // CRITICAL: Return translation map!
    return addressTranslation;
}
```

**Key Points:**
- ✅ Sort blocks by address
- ✅ Slide each block to leftmost position
- ✅ **Return address translation map** (CRITICAL!)
- ✅ Update allocatedBlocks with new addresses
- ✅ Update ownerToAddresses with new addresses
- ✅ Create one big free block at end
- ✅ Handle already-compact case (return empty map)

**Complexity:** O(n log n) for sort + O(n) for moving = O(n log n)

**Time:** 15-18 minutes

---

## 🎤 Key Interview Moments

### **When Interviewer Says: "The memory is fragmented"**

> "Right! After many malloc/free operations, we have free memory but it's 
> in small scattered blocks. I'll implement compact() to move all allocations
> together, creating one large contiguous free block. Critically, I need to
> return an address translation map so the caller can update their pointers."

---

### **When Interviewer Asks: "How does caller update their pointers?"**

> "The compact() method returns a Map<String, String> that maps old addresses 
> to new addresses. For example, if block B moved from 0x0064 to 0x0032, the 
> map contains '0x0064' → '0x0032'. The caller iterates through this map and 
> updates any variables pointing to old addresses."

---

### **When Interviewer Asks: "When should you compact?"**

> "Compaction is expensive - O(n log n) time and stop-the-world operation. 
> I'd use a fragmentation threshold, like 30%. We calculate fragmentation as:
> (totalFree - largestFree) / totalFree. Only compact when this exceeds 0.30.
> This balances the cost of compaction against the benefit of defragmentation."

---

## ⚠️ Common Pitfalls (DON'T DO THESE!)

### ❌ **Forgetting to Coalesce**
```java
void free(long address) {
    Block block = allocatedBlocks.remove(address);
    freeBlocks.put(address, block);  // ❌ WRONG! No coalescing!
}

// Result: Fragmentation explodes, compact() needed constantly
```

**✅ CORRECT:** Always check and merge adjacent free blocks

---

### ❌ **Not Returning Address Translation**
```java
public void compact() {
    // ... move blocks around ...
    // ❌ WRONG! Caller doesn't know where blocks moved!
}
```

**✅ CORRECT:** Return `Map<String, String>` of oldAddr → newAddr

---

### ❌ **Forgetting to Update Owner Tracking**
```java
public Map<String, String> compact() {
    // ... create newAllocatedBlocks ...
    this.allocatedBlocks = newAllocatedBlocks;
    // ❌ WRONG! ownerToAddresses still has old addresses!
}
```

**✅ CORRECT:** Also rebuild ownerToAddresses with new addresses

---

### ❌ **O(n²) Compaction Algorithm**
```java
// ❌ WRONG: Nested loops
for (Block block : blocks) {
    for (Block other : blocks) {
        // Move block if space...
    }
}
```

**✅ CORRECT:** O(n log n) - sort once, then linear scan

---

## 📊 Complexity Cheat Sheet

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| malloc | O(n) | O(1) | n = free blocks, first-fit scan |
| free | O(log n) | O(1) | TreeMap find neighbors |
| realloc | O(1) | O(1) | If in-place; else need malloc+copy+free |
| compact | O(n log n) | O(n) | Sort + rebuild structures |
| getFragmentation | O(n) | O(1) | Sum free blocks |

---

## 🎯 Phase Completion Checklist

### **Phase 1 Done When:**
- [ ] malloc returns address (or -1)
- [ ] free throws on double-free
- [ ] Adjacent free blocks coalesce
- [ ] Can allocate entire memory
- [ ] Can free and reallocate space

### **Phase 2 Done When:**
- [ ] malloc tracks ownership
- [ ] free validates ownership
- [ ] realloc shrinks in-place
- [ ] realloc grows in-place when possible
- [ ] Can query allocations by owner

### **Phase 3 Done When:**
- [ ] compact() moves blocks left
- [ ] compact() returns translation map
- [ ] compact() updates internal state
- [ ] compact() creates big free block at end
- [ ] Fragmentation metric works
- [ ] Already-compact is no-op

### **Phase 4 Done When:**
- [ ] Alignment constraint respected
- [ ] Alignment must be power of 2
- [ ] Internal fragmentation tracked
- [ ] Auto-compact triggers at threshold
- [ ] Can discuss alignment purpose

---

## 💬 Sample Responses

### **"How do you prevent fragmentation?"**

> "Two strategies: First, coalescing - when freeing memory, I immediately 
> merge adjacent free blocks. This prevents fragmentation from accumulating. 
> Second, compaction - when fragmentation exceeds a threshold, I run compact() 
> to move all allocations together, creating one large free block."

---

### **"What's the cost of compaction?"**

> "Time complexity is O(n log n) for sorting blocks plus O(n) to rebuild 
> structures. More importantly, it's a stop-the-world operation - all 
> allocations block while compacting. In production, I'd only compact when 
> fragmentation exceeds 30-40%, balancing the cost against the benefit."

---

### **"Why not always compact after every free?"**

> "Too expensive! Compaction is O(n log n) and blocks all allocations. 
> Instead, I use lazy compaction - only run when fragmentation > threshold. 
> This is similar to garbage collection strategies - minor GCs are frequent, 
> major GCs (with compaction) are rare."

---

### **"How does this compare to real allocators?"**

> "Real systems use more sophisticated strategies:
> - glibc malloc: Uses bins of different sizes, reduces fragmentation
> - jemalloc: Thread-local caches, size classes
> - Buddy allocator: Powers of 2, fast merge/split
> - Slab allocator: Pre-allocated objects of same size
> 
> My implementation is conceptually similar but simplified for interviews."

---

## 🔗 Real-World Examples to Mention

### **OpenAI's GPU Memory:**
> "This is directly relevant to OpenAI's infrastructure. GPU memory management 
> is similar - allocate CUDA buffers, free them, and deal with fragmentation. 
> The compact() operation is like defragmenting GPU memory between batch 
> inference jobs."

### **Python Garbage Collector:**
> "Python uses mark-sweep-compact GC. The compact phase is essentially what 
> we implemented - move live objects together to eliminate fragmentation and 
> improve cache locality."

### **Operating Systems:**
> "malloc/free in C uses a similar approach. glibc's malloc maintains free 
> lists and coalesces adjacent blocks. My implementation follows the same 
> principles but simplified."

---

## ⏱️ Time Management

**If running behind:**
- Phase 1 taking too long? → Skip some test cases, focus on core algorithm
- Phase 2 taking too long? → Simplify realloc (just shrink, not grow)
- Phase 3 taking too long? → Implement compact, explain translation map verbally

**If ahead of schedule:**
- Finished Phase 3 early? → Add fragmentation metrics, discuss threshold
- Time for Phase 4? → Implement alignment, explain internal vs external fragmentation
- Extra time? → Discuss buddy allocator, slab allocator, thread-local caches

---

## 🎓 Final Reminders

### **The 3 Critical Things:**

1. **Always coalesce** adjacent free blocks when freeing
2. **Return address translation map** from compact()
3. **Update all structures** (allocatedBlocks AND ownerToAddresses)

### **The Opening Line:**

> "I'll implement a memory allocator using TreeMap for O(log n) operations. 
> I'll use first-fit strategy and coalesce adjacent free blocks to prevent 
> fragmentation. Should I return addresses as longs or hex strings?"

### **The Confidence Booster:**

You have:
- ✅ Working implementations of all phases
- ✅ Understanding of where candidates fail
- ✅ Real-world context (CUDA, Python GC)
- ✅ Trade-off analysis (when to compact)

You're **over-prepared**. Trust your preparation! 💪

---

## 📋 Final Checklist

**Night before:**
- [ ] Read this guide one more time
- [ ] Review compact() algorithm in Phase3MemoryAllocator.java
- [ ] Sleep well! 😴

**Morning of:**
- [ ] Skim this guide (10 min)
- [ ] Review key algorithms: coalesce, compact
- [ ] Remind yourself: "Return address translation map!"

**During interview:**
- [ ] Ask clarifying questions
- [ ] Think out loud
- [ ] Test edge cases
- [ ] Return address translation map from compact()!
- [ ] Discuss trade-offs

---

**You've got this!** 🚀

Good luck with your OpenAI interview!

---

**Package:** `com.wwb.leetcode.other.openai.memoryallocator`  
**Interview Focus:** Phase 3 compaction is THE differentiator  
**Time Budget:** 50 minutes total, 15-18 min for Phase 3  
**Win Condition:** Working compact() with address translation map

