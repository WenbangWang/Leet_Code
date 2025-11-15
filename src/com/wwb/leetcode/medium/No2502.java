package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * You are given an integer n representing the size of a 0-indexed memory array. All memory units are initially free.
 * <p>
 * You have a memory allocator with the following functionalities:
 * <p>
 * Allocate a block of size consecutive free memory units and assign it the id mID.
 * Free all memory units with the given id mID.
 * Note that:
 * <p>
 * Multiple blocks can be allocated to the same mID.
 * You should free all the memory units with mID, even if they were allocated in different blocks.
 * Implement the Allocator class:
 * <p>
 * Allocator(int n) Initializes an Allocator object with a memory array of size n.
 * int allocate(int size, int mID) Find the leftmost block of size consecutive free memory units and allocate it with the id mID. Return the block's first index. If such a block does not exist, return -1.
 * int freeMemory(int mID) Free all memory units with the id mID. Return the number of memory units you have freed.
 *
 *
 * <pre>
 * Example 1:
 *
 * Input
 * ["Allocator", "allocate", "allocate", "allocate", "freeMemory", "allocate", "allocate", "allocate", "freeMemory", "allocate", "freeMemory"]
 * [[10], [1, 1], [1, 2], [1, 3], [2], [3, 4], [1, 1], [1, 1], [1], [10, 2], [7]]
 * Output
 * [null, 0, 1, 2, 1, 3, 1, 6, 3, -1, 0]
 *
 * Explanation
 * Allocator loc = new Allocator(10); // Initialize a memory array of size 10. All memory units are initially free.
 * loc.allocate(1, 1); // The leftmost block's first index is 0. The memory array becomes [1,_,_,_,_,_,_,_,_,_]. We return 0.
 * loc.allocate(1, 2); // The leftmost block's first index is 1. The memory array becomes [1,2,_,_,_,_,_,_,_,_]. We return 1.
 * loc.allocate(1, 3); // The leftmost block's first index is 2. The memory array becomes [1,2,3,_,_,_,_,_,_,_]. We return 2.
 * loc.freeMemory(2); // Free all memory units with mID 2. The memory array becomes [1,_, 3,_,_,_,_,_,_,_]. We return 1 since there is only 1 unit with mID 2.
 * loc.allocate(3, 4); // The leftmost block's first index is 3. The memory array becomes [1,_,3,4,4,4,_,_,_,_]. We return 3.
 * loc.allocate(1, 1); // The leftmost block's first index is 1. The memory array becomes [1,1,3,4,4,4,_,_,_,_]. We return 1.
 * loc.allocate(1, 1); // The leftmost block's first index is 6. The memory array becomes [1,1,3,4,4,4,1,_,_,_]. We return 6.
 * loc.freeMemory(1); // Free all memory units with mID 1. The memory array becomes [_,_,3,4,4,4,_,_,_,_]. We return 3 since there are 3 units with mID 1.
 * loc.allocate(10, 2); // We can not find any free block with 10 consecutive free memory units, so we return -1.
 * loc.freeMemory(7); // Free all memory units with mID 7. The memory array remains the same since there is no memory unit with mID 7. We return 0.
 *
 *
 * Constraints:
 *
 * 1 <= n, size, mID <= 1000
 * At most 1000 calls will be made to allocate and freeMemory.
 * </pre>
 */
public class No2502 {
    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    private static class Interval implements Comparable<Interval> {
        int start, end;
        Interval(int s, int e) { start = s; end = e; }

        int size() { return end - start + 1; }

        public int compareTo(Interval o) {
            return this.start == o.start ? this.end - o.end : this.start - o.start;
        }

        @Override
        public String toString() {
            return "[" + start + "," + end + "]";
        }
    }

    private static class FirstFitAllocator {

        /**
         * 有allocate(), free()这些func。先做了brutal force，要求performance优化。
         * 这里我犯了糊涂，做了个系统级的log（N），被面试官发现有bug。
         * 急忙改回per data type 的 log（m）。成功写完。最后面试官提出space complexity要constant。
         * 我也提出了正确的优化方案，没来得及写，就给挂了。
         * 其实过程也算顺利，挂的原因可能是开始有错的方向以及最后的优化口头说了说没写。
         */


        TreeMap<Integer, Interval> freeByAddr = new TreeMap<>();
        Map<Integer, List<Interval>> allocated = new HashMap<>();

        public FirstFitAllocator(int n) {
            freeByAddr.put(0, new Interval(0, n - 1));
        }

        public int allocate(int size, int mID) {
            Iterator<Map.Entry<Integer, Interval>> it = freeByAddr.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<Integer, Interval> e = it.next();
                Interval in = e.getValue();
                if (in.size() >= size) {
                    it.remove();
                    int allocStart = in.start;
                    int allocEnd = allocStart + size - 1;
                    allocated.computeIfAbsent(mID, k -> new ArrayList<>())
                        .add(new Interval(allocStart, allocEnd));
                    if (allocEnd < in.end) {
                        freeByAddr.put(allocEnd + 1, new Interval(allocEnd + 1, in.end));
                    }
                    return allocStart;
                }
            }

            return -1;
        }

        public int free(int mID) {
            if (!allocated.containsKey(mID)) {
                return 0;
            }

            int total = 0;
            for (Interval in : allocated.remove(mID)) {
                total += in.size();
                // find neighbors
                Map.Entry<Integer, Interval> lower = freeByAddr.lowerEntry(in.start);
                Map.Entry<Integer, Interval> higher = freeByAddr.higherEntry(in.start);

                if (lower != null && lower.getValue().end + 1 == in.start) {
                    in.start = lower.getValue().start;
                    freeByAddr.remove(lower.getKey());
                }
                if (higher != null && in.end + 1 == higher.getValue().start) {
                    in.end = higher.getValue().end;
                    freeByAddr.remove(higher.getKey());
                }
                freeByAddr.put(in.start, in);
            }
            return total;
        }
    }

    private static class BestFitAllocator {
        // --- BEFORE ---
        // TreeMap<Integer, Interval> free = new TreeMap<>();

        // --- AFTER ---
        // Split into two views:
        TreeMap<Integer, Interval> freeByAddr = new TreeMap<>();  // by address → for merging
        TreeMap<Integer, TreeSet<Interval>> freeBySize = new TreeMap<>(); // by size → for best-fit  // CHANGED

        Map<Integer, List<Interval>> allocated = new HashMap<>();

        public BestFitAllocator(int n) {
            Interval init = new Interval(0, n - 1);
            freeByAddr.put(0, init);   // CHANGED: insert into address-based map
            addToSizeMap(init);        // NEW: also insert into size-based index
        }

        // --- NEW helper functions to maintain size map ---
        private void addToSizeMap(Interval in) {                    // NEW
            freeBySize.computeIfAbsent(in.size(), k -> new TreeSet<>(
                Comparator.comparingInt(a -> a.start)
            )).add(in);
        }

        private void removeFromSizeMap(Interval in) {               // NEW
            TreeSet<Interval> set = freeBySize.get(in.size());
            if (set != null) {
                set.remove(in);
                if (set.isEmpty()) freeBySize.remove(in.size());
            }
        }

        // -----------------------------------------------------
        // Allocation phase
        // -----------------------------------------------------
        public int allocate(int size, int mID) {
            // --- BEFORE ---
            // for (Map.Entry<Integer, Interval> e : free.entrySet()) { ... }

            // --- AFTER ---
            // Use size tree to find smallest fitting block in O(log n)
            Map.Entry<Integer, TreeSet<Interval>> entry = freeBySize.ceilingEntry(size);  // CHANGED
            if (entry == null) {
                return -1;
            }

            // pick the first interval of that size bucket
            Interval block = entry.getValue().first();   // CHANGED
            removeFromSizeMap(block);                    // NEW: remove from size index
            freeByAddr.remove(block.start);              // CHANGED: remove from address index

            int allocStart = block.start;
            int allocEnd = allocStart + size - 1;
            Interval allocatedBlock = new Interval(allocStart, allocEnd);
            allocated.computeIfAbsent(mID, k -> new ArrayList<>()).add(allocatedBlock);

            // leftover block (if any)
            if (allocEnd < block.end) {
                Interval remain = new Interval(allocEnd + 1, block.end);
                freeByAddr.put(remain.start, remain);    // CHANGED
                addToSizeMap(remain);                    // NEW
            }

            return allocStart;
        }

        // -----------------------------------------------------
        // Free phase
        // -----------------------------------------------------
        public int free(int mID) {
            if (!allocated.containsKey(mID)) {
                return 0;
            }
            int totalFreed = 0;

            for (Interval block : allocated.remove(mID)) {
                totalFreed += block.size();

                // find neighboring free blocks by address
                Map.Entry<Integer, Interval> left = freeByAddr.lowerEntry(block.start);
                Map.Entry<Integer, Interval> right = freeByAddr.higherEntry(block.start);

                // --- Merge left neighbor ---
                if (left != null && left.getValue().end + 1 == block.start) {
                    removeFromSizeMap(left.getValue());         // NEW: keep size tree consistent
                    block.start = left.getValue().start;
                    freeByAddr.remove(left.getKey());
                }

                // --- Merge right neighbor ---
                if (right != null && block.end + 1 == right.getValue().start) {
                    removeFromSizeMap(right.getValue());        // NEW
                    block.end = right.getValue().end;
                    freeByAddr.remove(right.getKey());
                }

                // re-insert merged free block into both structures
                freeByAddr.put(block.start, block);             // CHANGED
                addToSizeMap(block);                            // NEW
            }

            return totalFreed;
        }
    }

/**
 * Your Allocator object will be instantiated and called as such:
 * Allocator obj = new Allocator(n);
 * int param_1 = obj.allocate(size,mID);
 * int param_2 = obj.freeMemory(mID);
 */
}
