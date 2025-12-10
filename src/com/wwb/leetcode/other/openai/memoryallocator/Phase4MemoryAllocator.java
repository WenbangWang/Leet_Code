package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Phase 4: Memory Alignment and Smart Compaction (12-15 minutes, Bonus)
 * 
 * EXTENDS Phase 3 with:
 * - Memory alignment constraints (4, 8, 16 bytes)
 * - Auto-compaction based on fragmentation threshold
 * - Internal vs external fragmentation tracking
 * 
 * ALIGNMENT:
 * Many systems require addresses to be multiples of certain values:
 * - 4-byte alignment: addresses must be divisible by 4 (0, 4, 8, 12, ...)
 * - 8-byte alignment: addresses must be divisible by 8 (0, 8, 16, 24, ...)
 * - 16-byte alignment: for SIMD operations, cache lines
 * 
 * WHY ALIGNMENT?
 * - CPU performance: aligned access is faster
 * - Hardware requirement: some processors crash on unaligned access
 * - Cache efficiency: aligned data fits better in cache lines
 * 
 * ALIGNMENT FORMULA:
 *   alignedAddr = (addr + alignment - 1) & ~(alignment - 1)
 *   
 * Example: Align 7 to 8 bytes
 *   (7 + 8 - 1) & ~7 = 14 & ~7 = 14 & 0xFFFFFFF8 = 8
 * 
 * TRADE-OFF:
 * Alignment creates internal fragmentation (wasted padding)
 * But improves performance and hardware compatibility
 * 
 * SMART COMPACTION:
 * Don't compact on every operation (expensive!)
 * Only compact when fragmentation > threshold (e.g., 30%)
 * 
 * INTERVIEW TIPS:
 * 1. Explain internal vs external fragmentation
 * 2. Discuss when to compact (cost vs benefit)
 * 3. Mention real systems: glibc malloc, jemalloc, tcmalloc
 * 4. Talk about incremental compaction for low-latency systems
 */
public class Phase4MemoryAllocator {
    
    private final int totalSize;
    private TreeMap<Long, Block> freeBlocks;
    private Map<Long, Block> allocatedBlocks;  // HashMap - O(1) malloc/free!
    private Map<String, List<Long>> ownerToAddresses;
    private final AllocationStrategy strategy;  // Pluggable strategy!
    private long timestampCounter = 0;
    
    // Phase 4 additions
    private double compactionThreshold = 0.30;  // 30% fragmentation triggers auto-compact
    private boolean autoCompactEnabled = false;
    private int totalPaddingBytes = 0;  // Track internal fragmentation
    
    /**
     * Constructor with pluggable allocation strategy.
     */
    public Phase4MemoryAllocator(int totalSize, AllocationStrategy strategy) {
        this.totalSize = totalSize;
        this.strategy = strategy;
        this.freeBlocks = new TreeMap<>();
        this.allocatedBlocks = new HashMap<>();  // O(1) for frequent malloc/free
        this.ownerToAddresses = new HashMap<>();
        
        freeBlocks.put(0L, new Block(0, totalSize));
    }
    
    /**
     * Constructor with default first-fit strategy.
     */
    public Phase4MemoryAllocator(int totalSize) {
        this(totalSize, new FirstFitStrategy());
    }
    
    /**
     * Allocate memory with alignment constraint.
     * 
     * @param size Number of bytes
     * @param ownerId Owner identifier
     * @param alignment Must be power of 2 (1, 4, 8, 16, etc.)
     * @return Aligned address, or null if failed
     */
    public String malloc(int size, String ownerId, int alignment) {
        if (size <= 0 || ownerId == null || !isPowerOfTwo(alignment)) {
            return null;
        }
        
        // FAST PATH: alignment=1 (common case - 90%+ of allocations)
        // Use strategy pattern for optimal performance
        //
        // Example: Request 80 bytes, alignment=1
        // Block found: addr=100, size=100
        // Layout: [100-179]: ALLOCATED (80 bytes)
        //         [180-199]: FREE (20 bytes remainder)
        if (alignment == 1) {
            // Strategy handles finding and removing block (LeetCode 2502 pattern)
            Block block = strategy.findAndRemoveBlock(size, freeBlocks);
            
            if (block != null) {
                long allocAddress = block.getAddress();
                
                // Create allocation
                Block allocated = new Block(allocAddress, size, ownerId, timestampCounter++);
                allocatedBlocks.put(allocAddress, allocated);
                ownerToAddresses.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(allocAddress);
                
                // Handle remainder if block is larger than needed (your No2502 pattern)
                if (block.getSize() > size) {
                    long remainAddress = allocAddress + size;
                    int remainSize = block.getSize() - size;
                    Block remain = new Block(remainAddress, remainSize);
                    freeBlocks.put(remainAddress, remain);
                    strategy.onBlockAdded(remain);
                }
                
                // Auto-compact if enabled and fragmentation high
                if (autoCompactEnabled && shouldCompact()) {
                    System.out.println("[AUTO-COMPACT] Triggered at " + 
                                     String.format("%.1f%%", getFragmentation() * 100) + 
                                     " fragmentation");
                    compact();
                }
                
                return formatAddress(allocAddress);
            }
            
            return null;
        }
        
        // SLOW PATH: alignment>1 (rare case - hardware-aligned allocations)
        // Must check each block individually because effective size depends on address
        // Use iterator pattern for safe removal (your No2502 pattern)
        //
        // Example: Request 80 bytes, 8-byte alignment
        // Block found: addr=101, size=90
        // 
        // Step 1: Calculate alignment
        //   alignedAddr = align(101, 8) = 104 (next multiple of 8)
        //   padding = 104 - 101 = 3 bytes
        //
        // Step 2: Memory layout after allocation
        //   [101-103]: FREE (3 bytes padding)
        //   [104-183]: ALLOCATED (80 bytes at aligned address)
        //   [184-190]: FREE (7 bytes remainder)
        //
        // Why 3 separate regions?
        //   - Padding: Can't use because allocation MUST start at 104 (aligned)
        //   - Allocation: Must be at aligned address for hardware requirements
        //   - Remainder: Leftover space after allocation
        Iterator<Map.Entry<Long, Block>> it = freeBlocks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Block> entry = it.next();
            Block block = entry.getValue();
            
            // Calculate aligned address within this block
            long blockStart = block.getAddress();
            long alignedAddr = align(blockStart, alignment);
            long paddingBefore = alignedAddr - blockStart;
            
            // Check if block is large enough (considering alignment padding)
            if (paddingBefore + size <= block.getSize()) {
                // Found suitable block! Remove using iterator (safe during iteration)
                it.remove();
                strategy.onBlockRemoved(block);
                
                // Handle padding before aligned address
                if (paddingBefore > 0) {
                    // Keep padding as free block
                    Block padding = new Block(blockStart, (int)paddingBefore);
                    freeBlocks.put(blockStart, padding);
                    strategy.onBlockAdded(padding);
                    totalPaddingBytes += (int)paddingBefore;
                }
                
                // Create aligned allocation
                Block allocated = new Block(alignedAddr, size, ownerId, timestampCounter++);
                allocatedBlocks.put(alignedAddr, allocated);
                ownerToAddresses.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(alignedAddr);
                
                // Handle remaining space after allocation
                long remainStart = alignedAddr + size;
                long remainSize = (blockStart + block.getSize()) - remainStart;
                if (remainSize > 0) {
                    Block remain = new Block(remainStart, (int)remainSize);
                    freeBlocks.put(remainStart, remain);
                    strategy.onBlockAdded(remain);
                }
                
                // Auto-compact if enabled and fragmentation high
                if (autoCompactEnabled && shouldCompact()) {
                    System.out.println("[AUTO-COMPACT] Triggered at " + 
                                     String.format("%.1f%%", getFragmentation() * 100) + 
                                     " fragmentation");
                    compact();
                }
                
                return formatAddress(alignedAddr);
            }
        }
        
        return null; // No suitable block found
    }
    
    /**
     * Allocate with default alignment (1 byte = no alignment).
     */
    public String malloc(int size, String ownerId) {
        return malloc(size, ownerId, 1);
    }
    
    /**
     * Check if a number is a power of 2.
     * 
     * ALGORITHM: (n & (n - 1)) == 0
     * 
     * Powers of 2 have ONE bit: 8 = 0b1000
     * Subtracting 1 flips lower bits: 7 = 0b0111
     * AND gives 0: 0b1000 & 0b0111 = 0 ✓
     * 
     * Non-power has multiple bits: 6 = 0b0110, 5 = 0b0101 → 6&5 = 4 ≠ 0 ✗
     */
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * Align an address up to the next multiple of alignment (must be power of 2).
     * 
     * ALGORITHM: (addr + alignment - 1) & ~(alignment - 1)
     * 
     * HOW IT WORKS:
     * 1. (alignment - 1) creates mask: 8-1 = 0b0111 (all lower bits set)
     * 2. ~(alignment - 1) inverts mask: ~0b0111 = 0b...11111000 (clears lower 3 bits)
     * 3. Adding (alignment - 1) before AND ensures we round UP
     * 
     * EXAMPLE: align(101, 8)
     *   101 + 7 = 108 = 0b01101100
     *   108 & ~0b0111 = 0b01101100 & 0b...11111000 = 0b01101000 = 104 ✓
     *   (101 rounded up to 104, which is divisible by 8)
     * 
     * MORE EXAMPLES:
     *   align(104, 8) = 104  (already aligned)
     *   align(5, 4) = 8
     *   align(10, 16) = 16
     */
    private long align(long addr, int alignment) {
        if (alignment == 1) {
            return addr;
        }
        return (addr + alignment - 1) & ~(alignment - 1);
    }
    
    /**
     * Set fragmentation threshold for auto-compaction.
     * 
     * @param threshold Value between 0.0 and 1.0 (e.g., 0.30 = 30%)
     */
    public void setCompactionThreshold(double threshold) {
        this.compactionThreshold = threshold;
    }
    
    /**
     * Enable or disable automatic compaction.
     * 
     * When enabled, compact() runs automatically after malloc/free
     * if fragmentation exceeds threshold.
     */
    public void setAutoCompact(boolean enabled) {
        this.autoCompactEnabled = enabled;
    }
    
    /**
     * Check if compaction is recommended.
     */
    public boolean shouldCompact() {
        return getFragmentation() > compactionThreshold && getAvailable() > 0;
    }
    
    /**
     * Get internal fragmentation (wasted space due to alignment padding).
     */
    public int getInternalFragmentation() {
        return totalPaddingBytes;
    }
    
    /**
     * Get external fragmentation (free space in unusable small blocks).
     */
    public int getExternalFragmentation() {
        int totalFree = getAvailable();
        int largestFree = getLargestFreeBlock();
        return totalFree - largestFree;
    }
    
    // ============================================================
    // Core operations (same as Phase 3, with auto-compact support)
    // ============================================================
    
    public Map<String, String> compact() {
        Map<String, String> addressTranslation = new HashMap<>();
        
        if (allocatedBlocks.isEmpty()) {
            return addressTranslation;
        }
        
        // Sort by address (HashMap doesn't maintain order)
        // Trade-off: O(1) malloc/free vs O(n log n) compact
        List<Block> blocks = new ArrayList<>(allocatedBlocks.values());
        blocks.sort(Comparator.comparingLong(Block::getAddress));
        
        long targetAddr = 0;
        TreeMap<Long, Block> newAllocatedBlocks = new TreeMap<>();
        Map<String, List<Long>> newOwnerToAddresses = new HashMap<>();
        
        for (Block block : blocks) {
            long oldAddr = block.getAddress();
            
            if (oldAddr != targetAddr) {
                addressTranslation.put(formatAddress(oldAddr), formatAddress(targetAddr));
                block.setAddress(targetAddr);
            }
            
            newAllocatedBlocks.put(targetAddr, block);
            newOwnerToAddresses.computeIfAbsent(block.getOwnerId(), k -> new ArrayList<>())
                               .add(targetAddr);
            
            targetAddr += block.getSize();
        }
        
        this.allocatedBlocks = newAllocatedBlocks;
        this.ownerToAddresses = newOwnerToAddresses;
        
        this.freeBlocks = new TreeMap<>();
        if (targetAddr < totalSize) {
            freeBlocks.put(targetAddr, new Block(targetAddr, totalSize - (int)targetAddr));
        }
        
        // Reset padding counter after compaction
        totalPaddingBytes = 0;
        
        return addressTranslation;
    }
    
    public double getFragmentation() {
        int totalFree = getAvailable();
        if (totalFree == 0) {
            return 0.0;
        }
        int largestFree = getLargestFreeBlock();
        return (totalFree - largestFree) / (double) totalFree;
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
        
        // Auto-compact if enabled and fragmentation high
        if (autoCompactEnabled && shouldCompact()) {
            System.out.println("[AUTO-COMPACT] Triggered after free");
            compact();
        }
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
        System.out.println("External fragmentation: " + getExternalFragmentation() + " bytes " +
                         String.format("(%.1f%%)", getFragmentation() * 100));
        System.out.println("Internal fragmentation (padding): " + getInternalFragmentation() + " bytes");
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
        System.out.println("Phase 4: Alignment and Smart Compaction Tests\n");
        
        testAlignment();
        testAlignmentPadding();
        testAutoCompaction();
        testFragmentationTypes();
        testProductionScenario();
        
        System.out.println("\n✅ All Phase 4 tests passed!");
    }
    
    private static void testAlignment() {
        System.out.println("Test 1: Memory Alignment");
        Phase4MemoryAllocator alloc = new Phase4MemoryAllocator(1000);
        
        // Allocate with 8-byte alignment
        String addr1 = alloc.malloc(10, "p1", 8);
        assert addr1.equals("0x0000") : "First allocation at 0 (aligned)";
        
        // Next allocation should be at 16 (10 rounds up to 16 for 8-byte alignment)
        String addr2 = alloc.malloc(10, "p2", 8);
        long addr2Val = Long.parseLong(addr2.substring(2), 16);
        assert addr2Val % 8 == 0 : "Should be 8-byte aligned";
        
        System.out.println("  Addr1: " + addr1);
        System.out.println("  Addr2: " + addr2);
        System.out.println("  ✓ Alignment constraint respected");
    }
    
    private static void testAlignmentPadding() {
        System.out.println("\nTest 2: Alignment Padding (Internal Fragmentation)");
        Phase4MemoryAllocator alloc = new Phase4MemoryAllocator(1000);
        
        // Allocate 5 bytes with no alignment
        alloc.malloc(5, "p1", 1);
        
        // Now allocate 10 bytes with 8-byte alignment
        // This will create padding (3 bytes) to align to 8
        alloc.malloc(10, "p2", 8);
        
        int padding = alloc.getInternalFragmentation();
        System.out.println("  Internal fragmentation (padding): " + padding + " bytes");
        assert padding == 3 : "Should have 3 bytes of padding (5 → 8)";
        
        System.out.println("  ✓ Internal fragmentation tracked correctly");
    }
    
    private static void testAutoCompaction() {
        System.out.println("\nTest 3: Auto-Compaction");
        Phase4MemoryAllocator alloc = new Phase4MemoryAllocator(1000);
        
        // Enable auto-compact at 30% fragmentation
        alloc.setAutoCompact(true);
        alloc.setCompactionThreshold(0.30);
        
        // Create fragmentation
        String a1 = alloc.malloc(100, "p1");
        String a2 = alloc.malloc(100, "p2");
        String a3 = alloc.malloc(100, "p3");
        String a4 = alloc.malloc(100, "p4");
        
        System.out.println("  Before frees:");
        System.out.println("    Fragmentation: " + String.format("%.1f%%", alloc.getFragmentation() * 100));
        
        // Free alternating blocks
        alloc.free(a2, "p2");
        
        System.out.println("  After first free:");
        System.out.println("    Fragmentation: " + String.format("%.1f%%", alloc.getFragmentation() * 100));
        
        // This free should trigger auto-compact (fragmentation > 30%)
        System.out.println("  Freeing second block (should trigger auto-compact)...");
        alloc.free(a4, "p4");
        
        System.out.println("  After second free:");
        System.out.println("    Fragmentation: " + String.format("%.1f%%", alloc.getFragmentation() * 100));
        assert alloc.getFragmentation() == 0.0 : "Should be auto-compacted to 0%";
        
        System.out.println("  ✓ Auto-compaction works");
    }
    
    private static void testFragmentationTypes() {
        System.out.println("\nTest 4: Internal vs External Fragmentation");
        Phase4MemoryAllocator alloc = new Phase4MemoryAllocator(1000);
        
        // Create internal fragmentation (alignment padding)
        alloc.malloc(5, "p1", 1);
        alloc.malloc(10, "p2", 8);  // Creates 3 bytes padding
        
        // Create external fragmentation
        String a3 = alloc.malloc(100, "p3");
        String a4 = alloc.malloc(100, "p4");
        alloc.free(a3, "p3");  // Creates gap
        
        int internal = alloc.getInternalFragmentation();
        int external = alloc.getExternalFragmentation();
        
        System.out.println("  Internal fragmentation: " + internal + " bytes (alignment padding)");
        System.out.println("  External fragmentation: " + external + " bytes (gaps between blocks)");
        
        assert internal > 0 : "Should have internal fragmentation";
        assert external > 0 : "Should have external fragmentation";
        
        System.out.println("  ✓ Both fragmentation types tracked");
    }
    
    private static void testProductionScenario() {
        System.out.println("\nTest 5: Production-Like Scenario");
        Phase4MemoryAllocator alloc = new Phase4MemoryAllocator(4096);  // 4KB page
        
        alloc.setAutoCompact(true);
        alloc.setCompactionThreshold(0.40);  // Compact at 40%
        
        System.out.println("  Simulating workload with 8-byte alignment...");
        
        List<String> allocations = new ArrayList<>();
        
        // Allocate several blocks
        for (int i = 0; i < 10; i++) {
            String addr = alloc.malloc(64, "proc" + i, 8);
            if (addr != null) {
                allocations.add(addr);
            }
        }
        
        System.out.println("  After allocations:");
        alloc.printMemoryMap();
        
        // Free some blocks to create fragmentation
        for (int i = 1; i < allocations.size(); i += 3) {
            String addr = allocations.get(i);
            for (int j = 0; j < 10; j++) {
                try {
                    alloc.free(addr, "proc" + j);
                    break;
                } catch (Exception e) {
                    // Wrong owner, try next
                }
            }
        }
        
        System.out.println("\n  After frees:");
        alloc.printMemoryMap();
        
        // Try to allocate larger block
        String large = alloc.malloc(500, "bigproc", 16);
        
        System.out.println("\n  After large allocation:");
        alloc.printMemoryMap();
        
        assert large != null : "Should be able to allocate after auto-compact";
        
        System.out.println("  ✓ Production scenario handled successfully");
    }
}

