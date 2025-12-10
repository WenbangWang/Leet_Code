package com.wwb.leetcode.other.openai.memoryallocator;

import java.util.TreeMap;

/**
 * Strategy pattern for memory allocation algorithms.
 * 
 * This interface allows different allocation strategies to be plugged in:
 * - First-Fit: O(n) malloc, simple
 * - Best-Fit: O(n) malloc, space-efficient (can optimize to O(log n))
 * - Worst-Fit: Maximizes leftover space
 * - Next-Fit: Remembers last position
 * 
 * DESIGN PATTERN: Strategy Pattern (Gang of Four)
 * 
 * IMPLEMENTATION PATTERN: Based on LeetCode 2502 proven approach
 * - Returns Block directly (not just address)
 * - Strategy removes block from freeBlocks (avoids extra lookups)
 * - Uses Iterator pattern for safe removal while iterating
 * 
 * Benefits:
 * - Open-closed principle: Can add new strategies without modifying existing code
 * - Testability: Easy to test each strategy independently
 * - Flexibility: Switch strategies at runtime if needed
 * - Efficiency: Single lookup, no extra get/remove operations
 * - Cleaner code: Separates allocation logic from memory management
 * 
 * Interview talking point:
 * "I'm using the Strategy pattern to make allocation algorithms pluggable.
 *  This follows the open-closed principle. The implementation uses an Iterator
 *  for safe removal and returns the Block directly to avoid extra lookups.
 *  This pattern is based on my LeetCode 2502 solution."
 */
public interface AllocationStrategy {
    
    /**
     * Find and REMOVE a suitable free block for allocation.
     * 
     * IMPORTANT: This method removes the block from freeBlocks as a side effect.
     * This follows the LeetCode 2502 pattern and avoids extra TreeMap lookups.
     * 
     * @param size Size requested
     * @param freeBlocks Map of free blocks (by address) - block will be removed
     * @return Selected block (already removed from freeBlocks), or null if none found
     */
    Block findAndRemoveBlock(int size, TreeMap<Long, Block> freeBlocks);
    
    /**
     * Notify strategy that a block was added to free blocks.
     * Used by strategies that maintain auxiliary data structures (e.g., size-indexed map).
     * 
     * @param block The block that was added
     */
    default void onBlockAdded(Block block) {
        // Default: no-op for simple strategies
    }
    
    /**
     * Notify strategy that a block was removed from free blocks.
     * Used by strategies that maintain auxiliary data structures (e.g., size-indexed map).
     * 
     * @param block The block that was removed
     */
    default void onBlockRemoved(Block block) {
        // Default: no-op for simple strategies
    }
    
    /**
     * Get the name of this strategy (for debugging/logging).
     */
    String getName();
    
    /**
     * Get the time complexity of this strategy's findBlock operation.
     */
    String getComplexity();
}

