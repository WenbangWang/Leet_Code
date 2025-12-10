package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.Map;
import java.util.TreeMap;

/**
 * Worst-Fit allocation strategy.
 * 
 * ALGORITHM:
 * Find the LARGEST block (maximizes leftover space).
 * 
 * COMPLEXITY: O(n) where n = number of free blocks
 * 
 * IMPLEMENTATION PATTERN (from LeetCode 2502):
 * - Two-pass: first find worst (largest), then remove
 * - Returns block itself after removal
 * 
 * RATIONALE:
 * By using the largest block, the leftover space is maximized,
 * which may be large enough for future allocations.
 * Avoids creating many small unusable fragments.
 * 
 * PROS:
 * - Leftover fragments tend to be large enough to reuse
 * - Can reduce external fragmentation in some workloads
 * - Simple to implement
 * 
 * CONS:
 * - Quickly exhausts large blocks
 * - Can't handle large allocations later
 * - Generally poor space utilization
 * - Rarely used in practice
 * 
 * WHEN TO USE:
 * - Theoretical interest (rarely used in production)
 * - When most allocations are similar size
 * - Academic/research contexts
 * 
 * INTERVIEW NOTE:
 * Good to mention as contrast to best-fit. Shows you understand
 * the spectrum of allocation strategies and their trade-offs.
 */
public class WorstFitStrategy implements AllocationStrategy {
    
    @Override
    public Block findAndRemoveBlock(int size, TreeMap<Long, Block> freeBlocks) {
        Block worstBlock = null;
        int worstSize = -1;
        
        // First pass: find largest block that fits
        for (Map.Entry<Long, Block> entry : freeBlocks.entrySet()) {
            Block block = entry.getValue();
            if (block.getSize() >= size && block.getSize() > worstSize) {
                worstBlock = block;
                worstSize = block.getSize();
            }
        }
        
        // Second pass: remove and return
        if (worstBlock != null) {
            freeBlocks.remove(worstBlock.getAddress());
            return worstBlock;
        }
        
        return null;
    }
    
    @Override
    public String getName() {
        return "Worst-Fit";
    }
    
    @Override
    public String getComplexity() {
        return "O(n)";
    }
}

