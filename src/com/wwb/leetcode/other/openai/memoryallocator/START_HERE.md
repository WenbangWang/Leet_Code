# Memory Allocator - Quick Start

## What This Is
4-phase memory allocator implementation based on real OpenAI interview feedback. **Compaction (Phase 3) is where candidates fail** - you now have a working implementation.

## Run Tests
```bash
./run_tests.sh
```

## The 4 Phases

### Phase 1: Basic malloc/free (12-15 min)
- `malloc(size)` - allocate memory
- `free(address)` - deallocate
- Coalesce adjacent free blocks
- **Strategy Pattern**: First-Fit (O(n)) or Best-Fit (O(log n))

### Phase 2: Realloc + Ownership (15-18 min)  
- `realloc(address, newSize, owner)` - resize in-place when possible
- Ownership tracking - prevent freeing others' memory

### Phase 3: Memory Compaction ⭐ THE KEY! (18-25 min)
- `compact()` - move blocks together to eliminate fragmentation
- **Must return address translation map!**
- This is where most candidates struggle

### Phase 4: Alignment + Auto-compact (12-15 min)
- `malloc(size, owner, alignment)` - align to 4/8/16 bytes
- Auto-compact when fragmentation > threshold

## Critical Concepts

**Coalescing:**
```
Free blocks: [FREE(50)] [FREE(50)] [FREE(50)]
After coalescing: [FREE(150)]  // Essential!
```

**Compaction:**
```
Before: [A(50)] [FREE(20)] [B(30)] [FREE(40)]
After:  [A(50)] [B(30)] [FREE(60)]
Returns: {"0x0046" → "0x0032"}  // Translation map!
```

**Alignment:**
```
malloc(80, "p", 8)
Block at addr=101 needs alignment
→ Allocate at 104 (next multiple of 8)
Layout: [101-103]: padding (FREE)
        [104-183]: allocated (80 bytes)
        [184-...]: remainder (FREE)
```

## Key Implementation Patterns

### Your No2502 Pattern
```java
// malloc: Use iterator for safe removal
Iterator<...> it = freeBlocks.entrySet().iterator();
while (it.hasNext()) {
    Block block = it.next().getValue();
    if (suitable) {
        it.remove();  // Safe during iteration
        // ... use block
    }
}

// free: Direct block modification, object reuse
Block block = allocatedBlocks.remove(addr);
// Find neighbors
if (lower != null && adjacent) {
    block.setAddress(lower.addr);  // Extend left
    block.setSize(block.size + lower.size);
    freeBlocks.remove(lower.key);
}
// ... coalesce right, then add back
freeBlocks.put(block.addr, block);
```

### Strategy Pattern
```java
interface AllocationStrategy {
    Block findAndRemoveBlock(int size, TreeMap<Long, Block> freeBlocks);
    void onBlockAdded(Block block);    // For BestFit optimization
    void onBlockRemoved(Block block);  // For BestFit optimization
}

// FirstFitStrategy: O(n) - simple
// BestFitStrategy: O(log n) - uses size-indexed TreeMap
```

## Performance Summary

| Phase | malloc | free | compact |
|-------|--------|------|---------|
| 1-2 | O(log n) BestFit | O(log n) | - |
| 3-4 | O(log n) align=1<br>O(n) align>1 | O(log n) | O(n log n) |

**Data Structures:**
- `freeBlocks`: TreeMap<Long, Block> - O(log n) neighbor finding
- `allocatedBlocks`: HashMap<Long, Block> - O(1) lookup (not TreeMap!)
- BestFit: Additional TreeMap<Integer, TreeSet<Block>> for O(log n) size lookup

## Interview Strategy

1. **Start simple** - Phase 1 with First-Fit
2. **Mention coalescing** - "Essential to prevent fragmentation"
3. **Discuss Best-Fit** - "Can optimize with size-indexed TreeMap for O(log n)"
4. **Nail compaction** - "Return translation map so caller updates pointers"
5. **Production thinking** - "Stop-the-world operation, only when frag > 30%"

## Common Pitfalls

❌ Forget to coalesce → fragmentation explodes  
❌ No address translation map → caller can't update pointers  
❌ Use TreeMap for allocatedBlocks → O(log n) instead of O(1)  
❌ compact() doesn't respect alignment → blocks move to unaligned addresses  
❌ Manual remove() during iteration → ConcurrentModificationException

## Next Steps

1. Review `Phase1MemoryAllocator.java` - understand basic structure
2. **Study `Phase3MemoryAllocator.java` `compact()` method** - THE KEY!
3. Practice explaining: "Compaction is like GC's compact phase - eliminates fragmentation but is stop-the-world"

---

**Files:** Phase1-4 (1,800+ LOC) | Tests: 20 passing ✅  
**Pattern:** LeetCode 2502 iterator + direct modification  
**Based on:** Real OpenAI feedback - compaction is the failure point!
