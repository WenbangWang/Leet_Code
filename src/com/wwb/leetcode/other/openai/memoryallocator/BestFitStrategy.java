package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Best-Fit allocation strategy - OPTIMIZED VERSION.
 * 
 * ALGORITHM:
 * Find the smallest block that fits (minimizes wasted space).
 * 
 * COMPLEXITY: O(log n) with size-indexed TreeMap
 * 
 * KEY OPTIMIZATION:
 * Maintains auxiliary TreeMap<Integer, TreeSet<Block>> for size-indexed lookup.
 * - TreeMap keys are block sizes (sorted)
 * - TreeSet values hold blocks of that size (sorted by address)
 * - Uses ceilingEntry(size) to find smallest sufficient block in O(log n)
 * 
 * IMPLEMENTATION PATTERN (from LeetCode 2502):
 * - Returns block itself after removal
 * - Maintains internal state via onBlockAdded/Removed callbacks
 * 
 * PROS:
 * - O(log n) malloc time (vs O(n) for naive implementation)
 * - Minimizes wasted space
 * - Better space utilization than first-fit
 * - Preserves large blocks for large allocations
 * 
 * CONS:
 * - More complex than first-fit
 * - Extra memory for size-indexed map
 * - Overhead of maintaining two data structures
 * - Can create many small unusable fragments
 * 
 * WHEN TO USE:
 * - Production systems where space matters
 * - Memory-constrained environments
 * - When fragmentation must be minimized
 * - When there are many free blocks (O(log n) vs O(n) matters)
 */
public class BestFitStrategy implements AllocationStrategy {
    
    // Size-indexed map: size -> set of blocks with that size (ordered by address)
    private final TreeMap<Integer, TreeSet<Block>> freeBySize;
    
    public BestFitStrategy() {
        this.freeBySize = new TreeMap<>();
    }
    
    @Override
    public Block findAndRemoveBlock(int size, TreeMap<Long, Block> freeBlocks) {
        // Best-fit: find smallest block that fits in O(log n)
        Map.Entry<Integer, TreeSet<Block>> entry = freeBySize.ceilingEntry(size);
        
        if (entry == null) {
            return null; // No suitable block
        }
        
        // Pick the first (leftmost by address) block of that size
        Block block = entry.getValue().first();
        
        // Remove from both maps
        removeFromSizeMap(block);
        freeBlocks.remove(block.getAddress());
        
        return block;
    }
    
    @Override
    public void onBlockAdded(Block block) {
        addToSizeMap(block);
    }
    
    @Override
    public void onBlockRemoved(Block block) {
        removeFromSizeMap(block);
    }
    
    /**
     * Add block to size-indexed map.
     * Multiple blocks can have same size, so use TreeSet ordered by address.
     */
    private void addToSizeMap(Block block) {
        freeBySize.computeIfAbsent(block.getSize(), k -> new TreeSet<>(
            Comparator.comparingLong(Block::getAddress)
        )).add(block);
    }
    
    /**
     * Remove block from size-indexed map.
     */
    private void removeFromSizeMap(Block block) {
        TreeSet<Block> set = freeBySize.get(block.getSize());
        if (set != null) {
            set.remove(block);
            if (set.isEmpty()) {
                freeBySize.remove(block.getSize());
            }
        }
    }
    
    @Override
    public String getName() {
        return "Best-Fit";
    }
    
    @Override
    public String getComplexity() {
        return "O(log n)";
    }
}

