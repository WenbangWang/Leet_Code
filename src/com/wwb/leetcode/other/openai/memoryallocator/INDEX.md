# Memory Allocator Package Index

Quick navigation for the memory allocator interview question.

## 📖 Documentation (Read in Order)

1. **START_HERE.md** - Quick start guide (essential concepts)
2. **README.md** - Complete implementation guide
3. **INTERVIEW_GUIDE.md** - Day-of quick reference

## 💻 Implementation Files

### Core Classes
- **Block.java** - Memory block data structure
- **AllocationInfo.java** - Allocation metadata

### Strategy Pattern
- **AllocationStrategy.java** - Interface for allocation algorithms
- **FirstFitStrategy.java** - Simple O(n) strategy
- **BestFitStrategy.java** - Optimized O(log n) strategy  
- **WorstFitStrategy.java** - Maximize leftover space

### The 4 Phases
- **Phase1MemoryAllocator.java** - Basic malloc/free (12-15 min)
- **Phase2MemoryAllocator.java** - Realloc + ownership (15-18 min)
- **Phase3MemoryAllocator.java** - **Compaction** ⭐ THE KEY! (18-25 min)
- **Phase4MemoryAllocator.java** - Alignment + auto-compact (12-15 min)

## 🚀 Quick Actions

```bash
# Run all tests
./run_tests.sh

# Run individual phases
java Phase1MemoryAllocator
java Phase2MemoryAllocator
java Phase3MemoryAllocator  # Study this one!
java Phase4MemoryAllocator
```

## 🎯 Study Priority

**High Priority (Must Know):**
1. Phase 1 - malloc/free with coalescing
2. **Phase 3 - compaction with translation map** ← Where candidates fail!
3. Strategy Pattern - First-Fit vs Best-Fit trade-offs

**Medium Priority:**
2. Phase 2 - realloc in-place logic
4. Fragmentation metrics

**Low Priority (Nice to Have):**
- Phase 4 - alignment constraints
- Auto-compact thresholds

## 📊 Status

- **Lines of Code:** 1,800+
- **Tests:** 20 passing ✅
- **Pattern:** LeetCode 2502 (iterator + direct modification)
- **Based on:** Real OpenAI interview feedback
