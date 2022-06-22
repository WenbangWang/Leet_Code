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
        // value is the higher bound of the interval
        TreeMap<Integer, Integer> map;

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
            if (lowerKey != null && higherKey != null && val == map.get(lowerKey) + 1 && val == higherKey - 1) {
                map.put(lowerKey, map.get(higherKey));

                map.remove(higherKey);
            } else if (lowerKey != null && val <= map.get(lowerKey) + 1) {
                // we can potentially extend the lower internal
                map.put(lowerKey, Math.max(val, map.get(lowerKey)));
            } else if (higherKey != null && val == higherKey - 1) {
                // we can potentially extend the higher internal
                map.put(val, map.get(higherKey));

                map.remove(higherKey);
            } else {
                map.put(val, val);
            }
        }

        public int[][] getIntervals() {
            int[][] res = new int[map.size()][2];

            int i = 0;
            for (var entry : this.map.entrySet()) {
                res[i++] = new int[]{entry.getKey(), entry.getValue()};
            }

            return res;
        }
    }
}
