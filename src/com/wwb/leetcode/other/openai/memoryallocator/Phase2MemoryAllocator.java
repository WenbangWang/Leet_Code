package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Phase 2: Memory Allocator with Ownership and Reallocation (15-18 minutes)
 * 
 * EXTENDS Phase 1 with:
 * - Ownership tracking (multi-tenant support)
 * - realloc() for resizing allocations in-place
 * - Metadata queries per owner
 * - **Pluggable allocation strategy** (Strategy Pattern)
 * 
 * KEY CONCEPTS:
 * - Ownership validation: can't free others' memory
 * - In-place reallocation: try to expand/shrink without moving
 * - Metadata tracking: Map<address, AllocationInfo>
 * - Strategy pattern: Allocation algorithm is pluggable
 * 
 * REALLOC STRATEGY:
 * - Shrinking: Always succeeds in-place, free the tail
 * - Growing: Check if next block is free and large enough
 *   - If yes: extend in-place
 *   - If no: return null (caller must malloc + copy + free)
 * 
 * DESIGN PATTERN:
 * Uses Strategy pattern for allocation algorithm.
 * Can switch between FirstFit, BestFit, WorstFit without changing core logic.
 * 
 * INTERVIEW TIPS:
 * 1. Ask: "Can realloc fail?" (Yes, return null)
 * 2. Ask: "Should realloc try to expand in-place?" (Yes!)
 * 3. Ask: "What if owner tries to free others' memory?" (Throw exception)
 * 4. Mention: "I'm using Strategy pattern to make allocation pluggable"
 */
public class Phase2MemoryAllocator {
    
    private final int totalSize;
    private final TreeMap<Long, Block> freeBlocks;
    private final Map<Long, Block> allocatedBlocks;  // HashMap - O(1) lookup/insert!
    private final Map<String, List<Long>> ownerToAddresses; // Track what each owner has
    private final AllocationStrategy strategy;  // Pluggable strategy!
    private long timestampCounter = 0;
    
    /**
     * Constructor with pluggable allocation strategy.
     * 
     * @param totalSize Total memory size
     * @param strategy Allocation strategy (FirstFit, BestFit, etc.)
     */
    public Phase2MemoryAllocator(int totalSize, AllocationStrategy strategy) {
        this.totalSize = totalSize;
        this.strategy = strategy;
        this.freeBlocks = new TreeMap<>();
        this.allocatedBlocks = new HashMap<>();  // O(1) for frequent malloc/free
        this.ownerToAddresses = new HashMap<>();
        
        Block initial = new Block(0, totalSize);
        freeBlocks.put(0L, initial);
        strategy.onBlockAdded(initial);
    }
    
    /**
     * Constructor with default first-fit strategy.
     */
    public Phase2MemoryAllocator(int totalSize) {
        this(totalSize, new FirstFitStrategy());
    }
    
    /**
     * Allocate memory with ownership tracking.
     * Uses pluggable allocation strategy (first-fit, best-fit, etc.)
     * 
     * IMPLEMENTATION: Based on LeetCode 2502 pattern
     * - Strategy removes and returns block (no extra lookups)
     * - Split remainder if block is larger than needed
     * 
     * @param size Number of bytes
     * @param ownerId Owner identifier
     * @return Hex string address like "0x0000", or null if failed
     */
    public String malloc(int size, String ownerId) {
        if (size <= 0 || ownerId == null) {
            return null;
        }
        
        // Use pluggable strategy - returns and removes block in one call (LeetCode 2502 pattern)
        Block block = strategy.findAndRemoveBlock(size, freeBlocks);
        
        if (block != null) {
            long allocAddress = block.getAddress();
            int allocEnd = (int)(allocAddress + size - 1);
            
            // Create allocated block with ownership
            Block allocated = new Block(allocAddress, size, ownerId, timestampCounter++);
            allocatedBlocks.put(allocAddress, allocated);
            
            // Track ownership
            ownerToAddresses.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(allocAddress);
            
            // Split if block is larger than needed (LeetCode 2502 pattern)
            if (allocEnd < block.getAddress() + block.getSize() - 1) {
                long remainAddress = allocEnd + 1;
                int remainSize = (int)(block.getAddress() + block.getSize() - remainAddress);
                Block remain = new Block(remainAddress, remainSize);
                freeBlocks.put(remainAddress, remain);
                strategy.onBlockAdded(remain);
            }
            
            return formatAddress(allocAddress);
        }
        
        return null;
    }
    
    /**
     * Free memory with ownership validation.
     * 
     * IMPLEMENTATION: Based on LeetCode 2502 pattern
     * - Reuse freed block object (no new object creation)
     * - In-place modification for coalescing
     * - Find neighbors using lowerEntry/higherEntry
     * 
     * @param address Hex string address like "0x0000"
     * @param ownerId Owner who is freeing
     * @throws IllegalArgumentException if invalid or not owned by this owner
     */
    public void free(String address, String ownerId) {
        long addr = parseAddress(address);
        
        Block block = allocatedBlocks.get(addr);
        if (block == null) {
            throw new IllegalArgumentException("Invalid address or double-free: " + address);
        }
        
        // Validate ownership
        if (!block.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException(
                String.format("Access violation: %s tried to free memory owned by %s", 
                            ownerId, block.getOwnerId())
            );
        }
        
        // Remove from owner tracking
        List<Long> addresses = ownerToAddresses.get(ownerId);
        if (addresses != null) {
            addresses.remove(addr);
            if (addresses.isEmpty()) {
                ownerToAddresses.remove(ownerId);
            }
        }
        
        // Remove from allocated
        allocatedBlocks.remove(addr);
        
        // Coalesce with neighbors (EXACT LeetCode 2502 pattern)
        // Find neighbors
        Map.Entry<Long, Block> lower = freeBlocks.lowerEntry(block.getAddress());
        Map.Entry<Long, Block> higher = freeBlocks.higherEntry(block.getAddress());
        
        // Directly modify the freed block's fields (your No2502 pattern!)
        if (lower != null && lower.getValue().getAddress() + lower.getValue().getSize() == block.getAddress()) {
            strategy.onBlockRemoved(lower.getValue());
            block.setAddress(lower.getValue().getAddress());  // Extend left
            block.setSize(block.getSize() + lower.getValue().getSize());
            freeBlocks.remove(lower.getKey());
        }
        if (higher != null && block.getAddress() + block.getSize() == higher.getValue().getAddress()) {
            strategy.onBlockRemoved(higher.getValue());
            block.setSize(block.getSize() + higher.getValue().getSize());  // Extend right
            freeBlocks.remove(higher.getKey());
        }
        
        // Mark as free and put back
        block.setOwnerId(null);
        freeBlocks.put(block.getAddress(), block);
        strategy.onBlockAdded(block);
    }
    
    /**
     * Reallocate (resize) an existing allocation.
     * 
     * Strategy:
     * - Shrinking: Always succeeds in-place
     * - Growing: Try to extend into next free block
     *   - Success: extend in-place, return same address
     *   - Failure: return null (caller must malloc+copy+free)
     * 
     * @param address Current allocation address
     * @param newSize New size requested
     * @param ownerId Owner making the request
     * @return New address (same if in-place, null if failed)
     */
    public String realloc(String address, int newSize, String ownerId) {
        if (newSize <= 0) {
            return null;
        }
        
        long addr = parseAddress(address);
        Block block = allocatedBlocks.get(addr);
        
        if (block == null) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        
        if (!block.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Not owned by " + ownerId);
        }
        
        int oldSize = block.getSize();
        
        if (newSize == oldSize) {
            return address; // No change
        }
        
        if (newSize < oldSize) {
            // SHRINKING - always succeeds in-place
            return shrinkInPlace(block, newSize);
        } else {
            // GROWING - try to extend in-place
            return growInPlace(block, newSize);
        }
    }
    
    /**
     * Shrink allocation in-place.
     * Free the tail portion.
     */
    private String shrinkInPlace(Block block, int newSize) {
        long addr = block.getAddress();
        int oldSize = block.getSize();
        
        // Update allocated block size
        block.setSize(newSize);
        
        // Free the tail portion (your No2502 pattern)
        Block tail = new Block(addr + newSize, oldSize - newSize);
        
        // Find right neighbor and extend if adjacent
        Map.Entry<Long, Block> higher = freeBlocks.higherEntry(tail.getAddress());
        if (higher != null && tail.getAddress() + tail.getSize() == higher.getValue().getAddress()) {
            strategy.onBlockRemoved(higher.getValue());
            tail.setSize(tail.getSize() + higher.getValue().getSize());  // Extend right
            freeBlocks.remove(higher.getKey());
        }
        
        // Add freed tail
        freeBlocks.put(tail.getAddress(), tail);
        strategy.onBlockAdded(tail);
        
        return formatAddress(addr);
    }
    
    /**
     * Try to grow allocation in-place.
     * Only succeeds if next block is free and large enough.
     */
    private String growInPlace(Block block, int newSize) {
        long addr = block.getAddress();
        int oldSize = block.getSize();
        int additionalNeeded = newSize - oldSize;
        
        // Check if next block is free and has enough space
        long nextAddr = addr + oldSize;
        Block nextBlock = freeBlocks.get(nextAddr);
        
        if (nextBlock != null && nextBlock.getSize() >= additionalNeeded) {
            // Can extend in-place!
            
            // Remove next free block
            strategy.onBlockRemoved(nextBlock);
            freeBlocks.remove(nextAddr);
            
            // Update current block size
            block.setSize(newSize);
            
            // If next block is larger, split and return remainder
            int remaining = nextBlock.getSize() - additionalNeeded;
            if (remaining > 0) {
                long remainAddr = nextAddr + additionalNeeded;
                Block remain = new Block(remainAddr, remaining);
                freeBlocks.put(remainAddr, remain);
                strategy.onBlockAdded(remain);
            }
            
            return formatAddress(addr);
        }
        
        // Can't grow in-place
        return null;
    }
    
    /**
     * Get all allocations for a specific owner.
     */
    public Map<String, AllocationInfo> getAllocations(String ownerId) {
        Map<String, AllocationInfo> result = new HashMap<>();
        
        List<Long> addresses = ownerToAddresses.get(ownerId);
        if (addresses != null) {
            for (Long addr : addresses) {
                Block block = allocatedBlocks.get(addr);
                if (block != null) {
                    AllocationInfo info = new AllocationInfo(
                        formatAddress(addr),
                        block.getSize(),
                        block.getTimestamp(),
                        block.getOwnerId()
                    );
                    result.put(formatAddress(addr), info);
                }
            }
        }
        
        return result;
    }
    
    // Helper: coalesce and add free block
    // Removed - coalescing now done inline in free() using LeetCode 2502 pattern
    
    // Utilities
    public int getAvailable() {
        return freeBlocks.values().stream().mapToInt(Block::getSize).sum();
    }
    
    public int getLargestFreeBlock() {
        return freeBlocks.values().stream().mapToInt(Block::getSize).max().orElse(0);
    }
    
    private String formatAddress(long addr) {
        return String.format("0x%04X", addr);
    }
    
    private long parseAddress(String addr) {
        return Long.parseLong(addr.substring(2), 16);
    }
    
    // ============================================================
    // TEST CASES
    // ============================================================
    
    public static void main(String[] args) {
        System.out.println("Phase 2: Ownership and Reallocation Tests\n");
        
        System.out.println("Testing with First-Fit Strategy:");
        System.out.println("=" .repeat(50) + "\n");
        testOwnership();
        testRealloc();
        testReallocGrow();
        testReallocShrink();
        testOwnershipViolation();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("\nTesting with Best-Fit Strategy:");
        System.out.println("=" .repeat(50) + "\n");
        testWithBestFit();
        
        System.out.println("\n✅ All Phase 2 tests passed!");
        System.out.println("✅ Strategy pattern working correctly!");
    }
    
    private static void testOwnership() {
        System.out.println("Test 1: Ownership Tracking");
        Phase2MemoryAllocator alloc = new Phase2MemoryAllocator(1000);
        
        String addr1 = alloc.malloc(100, "process1");
        String addr2 = alloc.malloc(200, "process2");
        String addr3 = alloc.malloc(150, "process1");
        
        assert addr1.equals("0x0000") : "First allocation";
        assert addr2.equals("0x0064") : "Second allocation";
        assert addr3.equals("0x012C") : "Third allocation";
        
        // Check process1's allocations
        Map<String, AllocationInfo> p1Allocs = alloc.getAllocations("process1");
        assert p1Allocs.size() == 2 : "Process1 should have 2 allocations";
        
        // Check process2's allocations
        Map<String, AllocationInfo> p2Allocs = alloc.getAllocations("process2");
        assert p2Allocs.size() == 1 : "Process2 should have 1 allocation";
        
        System.out.println("  ✓ Ownership tracking works");
    }
    
    private static void testRealloc() {
        System.out.println("\nTest 2: Basic Realloc");
        Phase2MemoryAllocator alloc = new Phase2MemoryAllocator(1000);
        
        String addr = alloc.malloc(100, "proc1");
        assert addr.equals("0x0000");
        
        // Shrink - should always succeed
        String newAddr = alloc.realloc(addr, 50, "proc1");
        assert newAddr.equals("0x0000") : "Shrink should return same address";
        assert alloc.getLargestFreeBlock() >= 50 : "Should free 50 bytes";
        
        System.out.println("  ✓ Basic realloc works");
    }
    
    private static void testReallocGrow() {
        System.out.println("\nTest 3: Realloc Growth");
        Phase2MemoryAllocator alloc = new Phase2MemoryAllocator(1000);
        
        String addr1 = alloc.malloc(100, "proc1");
        String addr2 = alloc.malloc(100, "proc2");
        
        // Free second block - creates free space after first
        alloc.free(addr2, "proc2");
        
        // Now grow first block into freed space
        String newAddr = alloc.realloc(addr1, 150, "proc1");
        assert newAddr != null : "Should succeed growing into free space";
        assert newAddr.equals(addr1) : "Should be in-place growth";
        
        System.out.println("  ✓ In-place growth works");
    }
    
    private static void testReallocShrink() {
        System.out.println("\nTest 4: Realloc Shrink");
        Phase2MemoryAllocator alloc = new Phase2MemoryAllocator(1000);
        
        String addr = alloc.malloc(200, "proc1");
        int availBefore = alloc.getAvailable();
        
        String newAddr = alloc.realloc(addr, 100, "proc1");
        assert newAddr.equals(addr) : "Should shrink in-place";
        
        int availAfter = alloc.getAvailable();
        assert availAfter == availBefore + 100 : "Should free 100 bytes";
        
        System.out.println("  ✓ Shrinking works correctly");
    }
    
    private static void testOwnershipViolation() {
        System.out.println("\nTest 5: Ownership Violation");
        Phase2MemoryAllocator alloc = new Phase2MemoryAllocator(1000);
        
        String addr = alloc.malloc(100, "process1");
        
        try {
            alloc.free(addr, "process2");
            assert false : "Should throw on ownership violation";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Access violation");
        }
        
        try {
            alloc.realloc(addr, 200, "process2");
            assert false : "Realloc should also check ownership";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Not owned by");
        }
        
        System.out.println("  ✓ Ownership validation works");
    }
    
    private static void testWithBestFit() {
        System.out.println("Test 6: Using Best-Fit Strategy");
        
        // Create allocator with best-fit strategy
        Phase2MemoryAllocator alloc = new Phase2MemoryAllocator(1000, new BestFitStrategy());
        
        // Create different-sized gaps
        String a1 = alloc.malloc(100, "p1");  // [0-99]
        String a2 = alloc.malloc(200, "p2");  // [100-299]
        String a3 = alloc.malloc(300, "p3");  // [300-599]
        
        // Free to create gaps
        alloc.free(a1, "p1");  // 100-byte gap at [0-99]
        alloc.free(a3, "p3");  // 300-byte gap at [300-599]
        
        // Memory: [FREE(100)] [p2(200)] [FREE(300)] [FREE(400)]
        
        // Best-fit should use 100-byte gap for 50 bytes (not 300 or 400)
        String a4 = alloc.malloc(50, "p4");
        assert a4.equals("0x0000") : "Best-fit should use smallest suitable gap";
        
        // Verify we can also use worst-fit
        Phase2MemoryAllocator worstFit = new Phase2MemoryAllocator(1000, new WorstFitStrategy());
        
        a1 = worstFit.malloc(100, "p1");
        a2 = worstFit.malloc(200, "p2");
        a3 = worstFit.malloc(300, "p3");
        
        worstFit.free(a1, "p1");
        worstFit.free(a3, "p3");
        
        // Worst-fit should use 400-byte gap (largest)
        a4 = worstFit.malloc(50, "p4");
        assert a4.equals("0x0258") : "Worst-fit should use largest gap";
        
        System.out.println("  ✓ Strategy pattern allows pluggable algorithms");
        System.out.println("  ✓ Best-fit uses smallest gap");
        System.out.println("  ✓ Worst-fit uses largest gap");
    }
}

