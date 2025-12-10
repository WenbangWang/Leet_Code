package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * First-Fit allocation strategy.
 * 
 * ALGORITHM:
 * Scan free blocks in address order, return first one that fits.
 * Uses Iterator pattern for safe removal (from LeetCode 2502).
 * 
 * COMPLEXITY: O(n) where n = number of free blocks
 * 
 * IMPLEMENTATION PATTERN (from LeetCode 2502):
 * - Use Iterator for safe removal while iterating
 * - Remove block directly (no separate remove() call needed)
 * - Return the block itself (not just address)
 * 
 * PROS:
 * - Simple to implement
 * - Fast when few free blocks
 * - Tends to keep allocations near start of memory
 * - Efficient: single pass, no extra lookups
 * 
 * CONS:
 * - May waste space (uses first fit, not best)
 * - Can create small unusable fragments at start
 * - Linear scan doesn't scale well
 * 
 * WHEN TO USE:
 * - Simple systems
 * - Small number of allocations
 * - When implementation simplicity matters
 */
public class FirstFitStrategy implements AllocationStrategy {
    
    @Override
    public Block findAndRemoveBlock(int size, TreeMap<Long, Block> freeBlocks) {
        // Use Iterator pattern for safe removal (LeetCode 2502 style)
        Iterator<Map.Entry<Long, Block>> it = freeBlocks.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<Long, Block> entry = it.next();
            Block block = entry.getValue();
            
            if (block.getSize() >= size) {
                it.remove();  // Safe removal during iteration
                return block;  // Return the block itself
            }
        }
        
        return null;  // No suitable block
    }
    
    @Override
    public String getName() {
        return "First-Fit";
    }
    
    @Override
    public String getComplexity() {
        return "O(n)";
    }
}

