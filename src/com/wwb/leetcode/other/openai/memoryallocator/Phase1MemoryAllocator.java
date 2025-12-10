package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Phase 1: Basic Memory Allocator (12-15 minutes)
 * 
 * Implements malloc() and free() with PLUGGABLE allocation strategies:
 * - First-Fit: Find first block large enough (O(n) malloc)
 * - Best-Fit: Find smallest block that fits (O(log n) malloc with size-indexed map)
 * 
 * COMPARISON:
 * First-Fit:
 *   + Simple implementation
 *   + Fast for small number of free blocks
 *   - Can waste space (uses first block, not best)
 *   - O(n) malloc time
 * 
 * Best-Fit:
 *   + Minimizes wasted space
 *   + O(log n) malloc time (uses size-indexed TreeMap)
 *   - More complex (maintains auxiliary data structure)
 *   - Slightly more overhead
 * 
 * Both strategies use coalescing to prevent fragmentation!
 * 
 * DESIGN PATTERN:
 * Uses Strategy Pattern for pluggable allocation algorithms.
 * This makes it easy to switch strategies or add new ones.
 * 
 * INTERVIEW TIPS:
 * 1. Ask: "Which allocation strategy should I use?" (Start with first-fit, discuss trade-offs)
 * 2. Ask: "Should I coalesce free blocks?" (YES!)
 * 3. Ask: "What's more important - speed or space efficiency?" (Determines strategy)
 * 
 * Leverages implementation patterns from No2502.
 */
public class Phase1MemoryAllocator {
    
    private final int totalSize;
    private final TreeMap<Long, Block> freeBlocks;
    private final Map<Long, Block> allocatedBlocks;  // HashMap - O(1) lookup/insert!
    private final AllocationStrategy strategy;
    
    public Phase1MemoryAllocator(int totalSize, AllocationStrategy strategy) {
        this.totalSize = totalSize;
        this.freeBlocks = new TreeMap<>();
        this.allocatedBlocks = new HashMap<>();  // O(1) operations
        this.strategy = strategy;
        
        // Initially, entire memory is one free block
        Block initial = new Block(0, totalSize);
        freeBlocks.put(0L, initial);
        strategy.onBlockAdded(initial);
    }
    
    /**
     * Allocate memory of given size.
     * 
     * Algorithm (using iterator pattern from No2502):
     * 1. Use strategy to find and remove a suitable free block
     * 2. Split if block is larger than needed
     * 3. Add remainder back to free list
     * 
     * @param size Size to allocate
     * @return Address of allocated block, or -1 if failed
     */
    public long malloc(int size) {
        if (size <= 0) {
            return -1;
        }
        
        // Use strategy to find and remove suitable block
        Block block = strategy.findAndRemoveBlock(size, freeBlocks);
        
        if (block == null) {
            return -1; // No suitable block found
        }
        
        long allocAddress = block.getAddress();
        
        // Create allocated block
        Block allocated = new Block(allocAddress, size);
        allocatedBlocks.put(allocAddress, allocated);
        
        // If block is larger than needed, add remainder back to free list
        if (block.getSize() > size) {
            long remainAddress = allocAddress + size;
            int remainSize = block.getSize() - size;
            Block remain = new Block(remainAddress, remainSize);
            
            freeBlocks.put(remainAddress, remain);
            strategy.onBlockAdded(remain);
        }
        
        return allocAddress;
    }
    
    /**
     * Free allocated memory.
     * 
     * Algorithm (using in-place modification pattern from No2502):
     * 1. Remove from allocated blocks
     * 2. Find neighbors (lower/higher adjacent free blocks)
     * 3. Coalesce by directly modifying block's address/size fields
     * 4. Remove old neighbors and add merged block
     * 
     * Coalescing prevents fragmentation!
     * 
     * @param address Address to free
     */
    public void free(long address) {
        Block block = allocatedBlocks.remove(address);
        if (block == null) {
            throw new IllegalArgumentException("Invalid address or double free: " + address);
        }
        
        // Find neighbors for coalescing
        Map.Entry<Long, Block> lower = freeBlocks.lowerEntry(block.getAddress());
        Map.Entry<Long, Block> higher = freeBlocks.higherEntry(block.getAddress());
        
        // Coalesce with lower neighbor
        if (lower != null && lower.getValue().getAddress() + lower.getValue().getSize() == block.getAddress()) {
            strategy.onBlockRemoved(lower.getValue());
            freeBlocks.remove(lower.getKey());
            block.setAddress(lower.getValue().getAddress());  // Extend left
            block.setSize(block.getSize() + lower.getValue().getSize());
        }
        
        // Coalesce with higher neighbor
        if (higher != null && block.getAddress() + block.getSize() == higher.getValue().getAddress()) {
            strategy.onBlockRemoved(higher.getValue());
            freeBlocks.remove(higher.getKey());
            block.setSize(block.getSize() + higher.getValue().getSize());  // Extend right
        }
        
        // Add merged free block
        freeBlocks.put(block.getAddress(), block);
        strategy.onBlockAdded(block);
    }
    
    public int getAvailable() {
        int total = 0;
        for (Block block : freeBlocks.values()) {
            total += block.getSize();
        }
        return total;
    }
    
    public int getLargestFreeBlock() {
        int max = 0;
        for (Block block : freeBlocks.values()) {
            max = Math.max(max, block.getSize());
        }
        return max;
    }
    
    public void printMemoryMap() {
        System.out.println("=== Memory Map (" + strategy.getName() + ") ===");
        System.out.println("Total: " + totalSize + " bytes");
        
        TreeMap<Long, String> map = new TreeMap<>();
        for (Block b : allocatedBlocks.values()) {
            map.put(b.getAddress(), String.format("[ALLOC: %d bytes]", b.getSize()));
        }
        for (Block b : freeBlocks.values()) {
            map.put(b.getAddress(), String.format("[FREE: %d bytes]", b.getSize()));
        }
        
        for (Map.Entry<Long, String> entry : map.entrySet()) {
            System.out.printf("  0x%04X: %s\n", entry.getKey(), entry.getValue());
        }
        
        System.out.println("Available: " + getAvailable() + " bytes");
        System.out.println("Largest free block: " + getLargestFreeBlock() + " bytes");
    }
    
    // ============================================================
    // TEST CASES
    // ============================================================
    
    public static void main(String[] args) {
        System.out.println("Phase 1: Basic Memory Allocator Tests");
        System.out.println();
        System.out.println("Testing BOTH First-Fit and Best-Fit strategies");
        System.out.println();
        
        testFirstFit();
        testBestFit();
        testStrategyComparison();
        
        System.out.println("✅ All Phase 1 tests passed!");
        System.out.println();
    }
    
    private static void testFirstFit() {
        System.out.println("========== FIRST-FIT STRATEGY TESTS ==========");
        System.out.println();
        
        // Test 1: Basic allocation
        System.out.println("Test 1: Basic First-Fit Allocation");
        Phase1MemoryAllocator alloc = new Phase1MemoryAllocator(1000, new FirstFitStrategy());
        
        long a1 = alloc.malloc(100);
        long a2 = alloc.malloc(200);
        long a3 = alloc.malloc(150);
        
        assert a1 == 0;
        assert a2 == 100;
        assert a3 == 300;
        assert alloc.getAvailable() == 550;
        System.out.println("  ✓ Basic first-fit allocation works");
        System.out.println();
        
        // Test 2: Coalescing
        System.out.println("Test 2: First-Fit Coalescing");
        alloc.free(a2);
        assert alloc.getLargestFreeBlock() == 550; // Not coalesced yet
        
        alloc.free(a3);
        assert alloc.getLargestFreeBlock() == 900; // Coalesced!
        System.out.println("  ✓ First-fit coalescing works correctly");
        System.out.println();
        System.out.println();
        System.out.println("============================================================");
        System.out.println();
    }
    
    private static void testBestFit() {
        System.out.println("========== BEST-FIT STRATEGY TESTS ==========");
        System.out.println();
        
        // Test 1: Basic allocation
        System.out.println("Test 1: Basic Best-Fit Allocation");
        Phase1MemoryAllocator alloc = new Phase1MemoryAllocator(1000, new BestFitStrategy());
        
        long a1 = alloc.malloc(100);
        long a2 = alloc.malloc(200);
        long a3 = alloc.malloc(150);
        
        assert a1 == 0;
        assert a2 == 100;
        assert a3 == 300;
        assert alloc.getAvailable() == 550;
        System.out.println("  ✓ Basic best-fit allocation works");
        System.out.println();
        
        // Test 2: Best-fit optimization
        System.out.println("Test 2: Best-Fit Optimization");
        alloc.free(a2); // Creates 200-byte gap at 100
        
        // Allocate 80 bytes - should use the 200-byte gap (best fit)
        long a4 = alloc.malloc(80);
        assert a4 == 100;
        assert alloc.getAvailable() == 550 + 200 - 80;
        System.out.println("  ✓ Best-fit optimization works correctly");
        System.out.println();
        
        // Test 3: Coalescing
        System.out.println("Test 3: Best-Fit Coalescing");
        Phase1MemoryAllocator alloc2 = new Phase1MemoryAllocator(1000, new BestFitStrategy());
        
        long b1 = alloc2.malloc(100);
        long b2 = alloc2.malloc(100);
        long b3 = alloc2.malloc(100);
        
        alloc2.free(b2);
        alloc2.free(b1); // Should coalesce with b2's freed block
        
        assert alloc2.getLargestFreeBlock() == 800; // b1, b2, and remaining memory coalesced
        System.out.println("  ✓ Best-fit coalescing works correctly");
        System.out.println();
        System.out.println();
        System.out.println("============================================================");
        System.out.println();
    }
    
    private static void testStrategyComparison() {
        System.out.println("========== STRATEGY COMPARISON ==========");
        System.out.println();
        System.out.println("Scenario: Multiple allocations with different request sizes");
        System.out.println("This demonstrates when best-fit outperforms first-fit");
        System.out.println();
        
        // Create fragmentation
        Phase1MemoryAllocator firstFit = new Phase1MemoryAllocator(1000, new FirstFitStrategy());
        Phase1MemoryAllocator bestFit = new Phase1MemoryAllocator(1000, new BestFitStrategy());
        
        // Same allocations for both
        long[] addrsFF = new long[6];
        long[] addrsBF = new long[6];
        
        addrsFF[0] = firstFit.malloc(100);
        addrsBF[0] = bestFit.malloc(100);
        
        addrsFF[1] = firstFit.malloc(200);
        addrsBF[1] = bestFit.malloc(200);
        
        addrsFF[2] = firstFit.malloc(150);
        addrsBF[2] = bestFit.malloc(150);
        
        addrsFF[3] = firstFit.malloc(100);
        addrsBF[3] = bestFit.malloc(100);
        
        addrsFF[4] = firstFit.malloc(50);
        addrsBF[4] = bestFit.malloc(50);
        
        // Free alternating blocks to create fragmentation
        firstFit.free(addrsFF[1]);
        bestFit.free(addrsBF[1]);
        
        firstFit.free(addrsFF[3]);
        bestFit.free(addrsBF[3]);
        
        System.out.println("After creating fragmentation:");
        System.out.println("  Memory: [100] [FREE(200)] [150] [FREE(100)] [50] [FREE(400)]");
        System.out.println();
        
        // Now allocate 80 bytes - shows the difference
        System.out.println("Request: malloc(80)");
        long addr1 = firstFit.malloc(80);
        long addr2 = bestFit.malloc(80);
        
        System.out.println("  First-Fit uses: 0x" + String.format("%04X", addr1) + 
                          " (first gap that fits: 200-byte gap)");
        System.out.println("  Best-Fit uses:  0x" + String.format("%04X", addr2) + 
                          " (best fit: 100-byte gap)");
        System.out.println();
        System.out.println("  Why Best-Fit is better here:");
        System.out.println("    - First-Fit wastes a large 200-byte gap");
        System.out.println("    - Best-Fit preserves the 200-byte gap for larger allocations");
        System.out.println("    - Best-Fit minimizes fragmentation");
        System.out.println();
        
        assert addr1 == 100; // First-fit uses first available (200-byte gap)
        assert addr2 == 450; // Best-fit uses best fit (100-byte gap)
        
        System.out.println("  ✓ Strategy comparison demonstrates trade-offs");
        System.out.println();
    }
}
