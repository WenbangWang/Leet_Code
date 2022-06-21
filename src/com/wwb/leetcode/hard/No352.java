package com.wwb.leetcode.hard;

import java.util.TreeMap;

/**
 * Given a data stream input of non-negative integers a1, a2, ..., an, summarize the numbers seen so far as a list of disjoint intervals.
 * <p>
 * Implement the SummaryRanges class:
 * <p>
 * SummaryRanges() Initializes the object with an empty stream.
 * void addNum(int val) Adds the integer val to the stream.
 * int[][] getIntervals() Returns a summary of the integers in the stream currently as a list of disjoint intervals [starti, endi].
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input
 * ["SummaryRanges", "addNum", "getIntervals", "addNum", "getIntervals", "addNum", "getIntervals", "addNum", "getIntervals", "addNum", "getIntervals"]
 * [[], [1], [], [3], [], [7], [], [2], [], [6], []]
 * Output
 * [null, null, [[1, 1]], null, [[1, 1], [3, 3]], null, [[1, 1], [3, 3], [7, 7]], null, [[1, 3], [7, 7]], null, [[1, 3], [6, 7]]]
 * <p>
 * Explanation
 * SummaryRanges summaryRanges = new SummaryRanges();
 * summaryRanges.addNum(1);      // arr = [1]
 * summaryRanges.getIntervals(); // return [[1, 1]]
 * summaryRanges.addNum(3);      // arr = [1, 3]
 * summaryRanges.getIntervals(); // return [[1, 1], [3, 3]]
 * summaryRanges.addNum(7);      // arr = [1, 3, 7]
 * summaryRanges.getIntervals(); // return [[1, 1], [3, 3], [7, 7]]
 * summaryRanges.addNum(2);      // arr = [1, 2, 3, 7]
 * summaryRanges.getIntervals(); // return [[1, 3], [7, 7]]
 * summaryRanges.addNum(6);      // arr = [1, 2, 3, 6, 7]
 * summaryRanges.getIntervals(); // return [[1, 3], [6, 7]]
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 0 <= val <= 10^4
 * At most 3 * 10^4 calls will be made to addNum and getIntervals.
 */
public class No352 {
    public class SummaryRanges {
        // key is the smallest value and
        // value is a size 2 array which are
        // start and the end of the internal respectively
        TreeMap<Integer, int[]> map;

        public SummaryRanges() {
            map = new TreeMap<>();
        }

        public void addNum(int val) {
            if (map.containsKey(val)) {
                return;
            }

            Integer lowerKey = map.lowerKey(val);
            Integer higherKey = map.higherKey(val);

            // by adding the new value we can merge the two intervals.
            if (lowerKey != null && higherKey != null && val == map.get(lowerKey)[1] + 1 && val == map.get(higherKey)[0] - 1) {
                map.get(lowerKey)[1] = map.get(higherKey)[1];

                map.remove(higherKey);
            } else if (lowerKey != null && val <= map.get(lowerKey)[1] + 1) {
                // we can potentially extend the lower internal
                map.get(lowerKey)[1] = Math.max(val, map.get(lowerKey)[1]);
            } else if (higherKey != null && val == map.get(higherKey)[0] - 1) {
                // we can potentially extend the higher internal
                map.put(val, new int[]{val, map.get(higherKey)[1]});

                map.remove(higherKey);
            } else {
                map.put(val, new int[]{val, val});
            }
        }

        public int[][] getIntervals() {
            int[][] res = new int[map.size()][2];

            int i = 0;
            for (int[] a : map.values()) {
                res[i++] = a;
            }

            return res;
        }
    }
}
