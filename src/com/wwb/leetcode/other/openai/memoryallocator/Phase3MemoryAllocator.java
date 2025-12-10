package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Phase 3: Memory Compaction / Defragmentation (18-25 minutes)
 *
 * THIS IS THE CRITICAL PHASE - where most candidates struggle!
 *
 * EXTENDS Phase 2 with:
 * - compact(): Move all allocations together to eliminate fragmentation
 * - Address translation map (CRITICAL!)
 * - Fragmentation metrics
 *
 * THE CHALLENGE:
 * After many malloc/free operations, memory becomes fragmented:
 *   [A(50)] [FREE(20)] [B(30)] [FREE(10)] [C(40)] [FREE(50)]
 *
 * Total free: 80 bytes, but largest contiguous: 50 bytes
 * Can't allocate 70 bytes even though we have 80 free!
 *
 * SOLUTION: Compact (defragment) memory:
 *   [A(50)] [B(30)] [C(40)] [FREE(80)]
 *
 * Now can allocate 70 bytes!
 *
 * KEY CONCEPTS:
 * 1. Address Translation Map: oldAddr → newAddr (MUST return this!)
 * 2. Move blocks left to fill gaps
 * 3. Merge all free space to the right
 * 4. Update all internal references
 *
 * ALGORITHM:
 * 1. Sort allocated blocks by address
 * 2. Slide each block to leftmost position
 * 3. Track oldAddr → newAddr mapping
 * 4. Update internal state (allocatedBlocks, ownerToAddresses)
 * 5. Create one big free block at end
 *
 * COMPLEXITY: O(n log n) for sort + O(n) for moving = O(n log n)
 * Trade-off: malloc/free are O(1) with HashMap, compact is O(n log n)
 *
 * INTERVIEW TIPS:
 * 1. Ask: "Should compact() always run, or only when needed?" (Caller's choice)
 * 2. Ask: "What if memory is already compact?" (No-op, return empty map)
 * 3. Ask: "How does caller update their pointers?" (Use the translation map!)
 * 4. Mention: "In production, this is a stop-the-world operation" (important!)
 */
public class Phase3MemoryAllocator {

    private final int totalSize;
    private TreeMap<Long, Block> freeBlocks;
    private TreeMap<Long, Block> allocatedBlocks;
    private Map<String, List<Long>> ownerToAddresses;
    private final AllocationStrategy strategy;  // Pluggable strategy!
    private long timestampCounter = 0;

    /**
     * Constructor with pluggable allocation strategy.
     */
    public Phase3MemoryAllocator(int totalSize, AllocationStrategy strategy) {
        this.totalSize = totalSize;
        this.strategy = strategy;
        this.freeBlocks = new TreeMap<>();
        this.allocatedBlocks = new TreeMap<>();
        this.ownerToAddresses = new HashMap<>();

        freeBlocks.put(0L, new Block(0, totalSize));
    }

    /**
     * Constructor with default first-fit strategy.
     */
    public Phase3MemoryAllocator(int totalSize) {
        this(totalSize, new FirstFitStrategy());
    }

    /**
     * Memory compaction - the critical algorithm!
     *
     * Moves all allocated blocks to the start of memory,
     * eliminating fragmentation and creating one large contiguous free block.
     *
     * Returns address translation map so caller can update their pointers.
     *
     * Example:
     *   Before: [A(50)] [FREE(20)] [B(30)] [FREE(40)]
     *   After:  [A(50)] [B(30)] [FREE(60)]
     *
     *   Returns: {"0x0000" → "0x0000", "0x0046" → "0x0032"}
     *                                     (B moved from 70 to 50)
     *
     * @return Map of oldAddress → newAddress for all moved blocks
     */
    public Map<String, String> compact() {
        Map<String, String> addressTranslation = new HashMap<>();

        // Edge case: no allocations or already compact
        if (allocatedBlocks.isEmpty()) {
            return addressTranslation;
        }

        // Step 1: Get all allocated blocks and sort by address
        // Trade-off: HashMap gives O(1) malloc/free, but compact needs O(n log n) sort
        List<Block> blocks = new ArrayList<>(allocatedBlocks.values());
        Collections.sort(blocks);

        // Step 2: Slide blocks to fill gaps
        long targetAddr = 0;
        TreeMap<Long, Block> newAllocatedBlocks = new TreeMap<>();
        Map<String, List<Long>> newOwnerToAddresses = new HashMap<>();

        for (Block block : blocks) {
            long oldAddr = block.getAddress();

            // Check if block needs to move
            if (oldAddr != targetAddr) {
                // Block is moving!
                addressTranslation.put(formatAddress(oldAddr), formatAddress(targetAddr));

                // Update block's address
                block.setAddress(targetAddr);
            }

            // Add to new structures
            newAllocatedBlocks.put(targetAddr, block);

            // Update owner tracking with new address
            newOwnerToAddresses.computeIfAbsent(block.getOwnerId(), k -> new ArrayList<>())
                               .add(targetAddr);

            // Move target pointer forward
            targetAddr += block.getSize();
        }

        // Step 3: Replace old structures with new ones
        this.allocatedBlocks = newAllocatedBlocks;
        this.ownerToAddresses = newOwnerToAddresses;

        // Step 4: Create one big free block at the end
        this.freeBlocks = new TreeMap<>();
        if (targetAddr < totalSize) {
            freeBlocks.put(targetAddr, new Block(targetAddr, totalSize - (int)targetAddr));
        }

        return addressTranslation;
    }

    /**
     * Get fragmentation ratio.
     *
     * Fragmentation = (Total Free - Largest Free Block) / Total Free
     *
     * Examples:
     * - 0.0 = No fragmentation (all free space is contiguous)
     * - 0.5 = 50% fragmented
     * - 1.0 = Maximally fragmented (no contiguous blocks > 1)
     *
     * @return Fragmentation ratio [0.0, 1.0], or 0.0 if no free memory
     */
    public double getFragmentation() {
        int totalFree = getAvailable();
        if (totalFree == 0) {
            return 0.0; // No free memory, no fragmentation
        }

        int largestFree = getLargestFreeBlock();
        return (totalFree - largestFree) / (double) totalFree;
    }

    /**
     * Check if compaction would be beneficial.
     *
     * Heuristic: Compact if fragmentation > 30% and we have free memory
     */
    public boolean shouldCompact() {
        return getFragmentation() > 0.30 && getAvailable() > 0;
    }

    // ============================================================
    // Standard operations (same as Phase 2)
    // ============================================================

    public String malloc(int size, String ownerId) {
        if (size <= 0 || ownerId == null) {
            return null;
        }

        // Use pluggable strategy - returns and removes block (LeetCode 2502 pattern)
        Block block = strategy.findAndRemoveBlock(size, freeBlocks);

        if (block != null) {
            long allocAddress = block.getAddress();
            int allocEnd = (int)(allocAddress + size - 1);

            Block allocated = new Block(allocAddress, size, ownerId, timestampCounter++);
            allocatedBlocks.put(allocAddress, allocated);

            ownerToAddresses.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(allocAddress);

            // Split if block is larger than needed (LeetCode 2502 pattern)
            if (allocEnd < block.getAddress() + block.getSize() - 1) {
                long remainAddress = allocEnd + 1;
                int remainSize = (int)(block.getAddress() + block.getSize() - remainAddress);
                freeBlocks.put(remainAddress, new Block(remainAddress, remainSize));
            }

            return formatAddress(allocAddress);
        }

        return null;
    }

    public void free(String address, String ownerId) {
        long addr = parseAddress(address);

        Block block = allocatedBlocks.get(addr);
        if (block == null) {
            throw new IllegalArgumentException("Invalid address or double-free: " + address);
        }

        if (!block.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException(
                String.format("Access violation: %s tried to free memory owned by %s",
                            ownerId, block.getOwnerId())
            );
        }

        List<Long> addresses = ownerToAddresses.get(ownerId);
        if (addresses != null) {
            addresses.remove(addr);
            if (addresses.isEmpty()) {
                ownerToAddresses.remove(ownerId);
            }
        }

        allocatedBlocks.remove(addr);

        // Coalesce with neighbors (EXACT LeetCode 2502 pattern)
        // Find neighbors
        Map.Entry<Long, Block> lower = freeBlocks.lowerEntry(block.getAddress());
        Map.Entry<Long, Block> higher = freeBlocks.higherEntry(block.getAddress());

        // Directly modify the freed block's fields (your No2502 pattern!)
        if (lower != null && lower.getValue().getAddress() + lower.getValue().getSize() == block.getAddress()) {
            block.setAddress(lower.getValue().getAddress());  // Extend left
            block.setSize(block.getSize() + lower.getValue().getSize());
            freeBlocks.remove(lower.getKey());
        }
        if (higher != null && block.getAddress() + block.getSize() == higher.getValue().getAddress()) {
            block.setSize(block.getSize() + higher.getValue().getSize());  // Extend right
            freeBlocks.remove(higher.getKey());
        }

        // Mark as free and put back
        block.setOwnerId(null);
        freeBlocks.put(block.getAddress(), block);
    }

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
            return address;
        }

        if (newSize < oldSize) {
            return shrinkInPlace(block, newSize);
        } else {
            return growInPlace(block, newSize);
        }
    }

    private String shrinkInPlace(Block block, int newSize) {
        long addr = block.getAddress();
        int oldSize = block.getSize();

        block.setSize(newSize);

        // Free the tail portion (your No2502 pattern)
        Block tail = new Block(addr + newSize, oldSize - newSize);

        // Find right neighbor and extend if adjacent
        Map.Entry<Long, Block> higher = freeBlocks.higherEntry(tail.getAddress());
        if (higher != null && tail.getAddress() + tail.getSize() == higher.getValue().getAddress()) {
            tail.setSize(tail.getSize() + higher.getValue().getSize());  // Extend right
            freeBlocks.remove(higher.getKey());
        }

        // Add freed tail
        freeBlocks.put(tail.getAddress(), tail);

        return formatAddress(addr);
    }

    private String growInPlace(Block block, int newSize) {
        long addr = block.getAddress();
        int oldSize = block.getSize();
        int additionalNeeded = newSize - oldSize;

        long nextAddr = addr + oldSize;
        Block nextBlock = freeBlocks.get(nextAddr);

        if (nextBlock != null && nextBlock.getSize() >= additionalNeeded) {
            freeBlocks.remove(nextAddr);
            block.setSize(newSize);

            int remaining = nextBlock.getSize() - additionalNeeded;
            if (remaining > 0) {
                long remainAddr = nextAddr + additionalNeeded;
                freeBlocks.put(remainAddr, new Block(remainAddr, remaining));
            }

            return formatAddress(addr);
        }

        return null;
    }

    // Removed - coalescing now done inline in free() using LeetCode 2502 pattern

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

    public void printMemoryMap() {
        System.out.println("=== Memory Map ===");
        System.out.println("Total: " + totalSize + " bytes");
        System.out.println("Available: " + getAvailable() + " bytes");
        System.out.println("Largest free block: " + getLargestFreeBlock() + " bytes");
        System.out.println("Fragmentation: " + String.format("%.1f%%", getFragmentation() * 100));
        System.out.println();

        TreeMap<Long, String> map = new TreeMap<>();
        for (Block b : allocatedBlocks.values()) {
            map.put(b.getAddress(), String.format("[ALLOC: %s, %d bytes]", b.getOwnerId(), b.getSize()));
        }
        for (Block b : freeBlocks.values()) {
            map.put(b.getAddress(), String.format("[FREE: %d bytes]", b.getSize()));
        }

        for (Map.Entry<Long, String> entry : map.entrySet()) {
            System.out.printf("  0x%04X: %s\n", entry.getKey(), entry.getValue());
        }
    }

    // ============================================================
    // TEST CASES
    // ============================================================

    public static void main(String[] args) {
        System.out.println("Phase 3: Memory Compaction Tests\n");

        testBasicCompaction();
        testFragmentationMetric();
        testNoCompactionNeeded();
        testAddressTranslation();
        testRealWorldScenario();

        System.out.println("\n✅ All Phase 3 tests passed!");
    }

    private static void testBasicCompaction() {
        System.out.println("Test 1: Basic Compaction");
        Phase3MemoryAllocator alloc = new Phase3MemoryAllocator(1000);

        // Create fragmentation
        String a1 = alloc.malloc(100, "p1"); // [0-99]
        String a2 = alloc.malloc(100, "p2"); // [100-199]
        String a3 = alloc.malloc(100, "p3"); // [200-299]
        String a4 = alloc.malloc(100, "p4"); // [300-399]

        // Free alternating blocks - creates fragmentation
        alloc.free(a2, "p2"); // Free [100-199]
        alloc.free(a4, "p4"); // Free [300-399]

        // Memory: [p1(100)] [FREE(100)] [p3(100)] [FREE(100)] [FREE(600)]
        System.out.println("  Before compact:");
        System.out.println("    Total free: " + alloc.getAvailable());
        System.out.println("    Largest free: " + alloc.getLargestFreeBlock());

        assert alloc.getAvailable() == 800 : "Should have 800 bytes free";
        assert alloc.getLargestFreeBlock() == 600 : "Largest block is 600";

        // Compact!
        Map<String, String> translations = alloc.compact();

        System.out.println("  After compact:");
        System.out.println("    Total free: " + alloc.getAvailable());
        System.out.println("    Largest free: " + alloc.getLargestFreeBlock());
        System.out.println("    Translations: " + translations);

        assert alloc.getAvailable() == 800 : "Still 800 bytes free";
        assert alloc.getLargestFreeBlock() == 800 : "Now one 800-byte block!";
        assert translations.containsKey("0x00C8") : "p3 should have moved";
        assert translations.get("0x00C8").equals("0x0064") : "p3 moved from 200 to 100";

        System.out.println("  ✓ Compaction eliminates fragmentation");
    }

    private static void testFragmentationMetric() {
        System.out.println("\nTest 2: Fragmentation Metric");
        Phase3MemoryAllocator alloc = new Phase3MemoryAllocator(1000);

        // No fragmentation initially
        assert alloc.getFragmentation() == 0.0 : "No fragmentation when empty";

        // Create some allocations
        String a1 = alloc.malloc(200, "p1");
        String a2 = alloc.malloc(200, "p2");
        String a3 = alloc.malloc(200, "p3");

        // Free middle one
        alloc.free(a2, "p2");

        // Now: [p1(200)] [FREE(200)] [p3(200)] [FREE(400)]
        // Total free: 600, Largest: 400
        // Fragmentation = (600 - 400) / 600 = 0.333
        double frag = alloc.getFragmentation();
        System.out.println("  Fragmentation: " + String.format("%.2f", frag));
        assert Math.abs(frag - 0.333) < 0.01 : "Should be ~33% fragmented";

        // Compact reduces fragmentation to 0
        alloc.compact();
        assert alloc.getFragmentation() == 0.0 : "No fragmentation after compact";

        System.out.println("  ✓ Fragmentation metric correct");
    }

    private static void testNoCompactionNeeded() {
        System.out.println("\nTest 3: No Compaction Needed");
        Phase3MemoryAllocator alloc = new Phase3MemoryAllocator(1000);

        String a1 = alloc.malloc(300, "p1");
        String a2 = alloc.malloc(400, "p2");

        // Memory is already compact: [p1] [p2] [FREE]
        Map<String, String> translations = alloc.compact();

        assert translations.isEmpty() : "No blocks moved";
        assert alloc.getLargestFreeBlock() == 300 : "Free space unchanged";

        System.out.println("  ✓ Compact is no-op when already compact");
    }

    private static void testAddressTranslation() {
        System.out.println("\nTest 4: Address Translation Map");
        Phase3MemoryAllocator alloc = new Phase3MemoryAllocator(1000);

        String a1 = alloc.malloc(50, "p1");  // 0x0000
        String a2 = alloc.malloc(50, "p2");  // 0x0032
        String a3 = alloc.malloc(50, "p3");  // 0x0064

        alloc.free(a1, "p1"); // Free first

        // Memory: [FREE(50)] [p2(50)] [p3(50)] [FREE]
        Map<String, String> translations = alloc.compact();

        // p2 should move from 0x0032 to 0x0000
        // p3 should move from 0x0064 to 0x0032
        assert translations.get("0x0032").equals("0x0000") : "p2 moved to start";
        assert translations.get("0x0064").equals("0x0032") : "p3 moved next to p2";

        // Verify p2 can be accessed at new address
        Map<String, AllocationInfo> p2Allocs = alloc.getAllocations("p2");
        assert p2Allocs.containsKey("0x0000") : "p2 now at 0x0000";

        System.out.println("  ✓ Address translation map correct");
    }

    private static void testRealWorldScenario() {
        System.out.println("\nTest 5: Real-World Scenario");
        Phase3MemoryAllocator alloc = new Phase3MemoryAllocator(1000);

        // Simulate many allocations and frees
        List<String> addresses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String addr = alloc.malloc(50, "proc" + i);
            if (addr != null) {
                addresses.add(addr);
            }
        }

        // Free every other allocation
        for (int i = 1; i < addresses.size(); i += 2) {
            String addr = addresses.get(i);
            // Extract owner from allocation
            for (int j = 0; j < 10; j++) {
                Map<String, AllocationInfo> allocs = alloc.getAllocations("proc" + j);
                if (allocs.containsKey(addr)) {
                    alloc.free(addr, "proc" + j);
                    break;
                }
            }
        }

        System.out.println("  Before compact:");
        alloc.printMemoryMap();

        double fragBefore = alloc.getFragmentation();
        assert fragBefore > 0.3 : "Should be significantly fragmented";

        // Compact
        Map<String, String> translations = alloc.compact();

        System.out.println("\n  After compact:");
        alloc.printMemoryMap();

        double fragAfter = alloc.getFragmentation();
        assert fragAfter == 0.0 : "Should be fully defragmented";

        // Should now be able to allocate large block
        String largeAlloc = alloc.malloc(400, "big");
        assert largeAlloc != null : "Large allocation should succeed after compact";

        System.out.println("  ✓ Real-world scenario handled correctly");
    }
}

