package com.wwb.leetcode.hard;

import java.util.TreeMap;

/**
 * A Range Module is a module that tracks ranges of numbers.
 * Design a data structure to track the ranges represented as half-open intervals and query about them.
 *
 * A half-open interval [left, right) denotes all the real numbers x where left <= x < right.
 *
 * Implement the RangeModule class:
 *
 * RangeModule() Initializes the object of the data structure.
 * void addRange(int left, int right) Adds the half-open interval [left, right),
 * tracking every real number in that interval.
 * Adding an interval that partially overlaps with currently tracked numbers
 * should add any numbers in the interval [left, right) that are not already tracked.
 * boolean queryRange(int left, int right) Returns true if every real number in the interval [left, right)
 * is currently being tracked, and false otherwise.
 * void removeRange(int left, int right) Stops tracking every real number currently being tracked
 * in the half-open interval [left, right).
 *
 *
 * Example 1:
 *
 * Input
 * ["RangeModule", "addRange", "removeRange", "queryRange", "queryRange", "queryRange"]
 * [[], [10, 20], [14, 16], [10, 14], [13, 15], [16, 17]]
 * Output
 * [null, null, null, true, false, true]
 *
 * Explanation
 * RangeModule rangeModule = new RangeModule();
 * rangeModule.addRange(10, 20);
 * rangeModule.removeRange(14, 16);
 * rangeModule.queryRange(10, 14); // return True,(Every number in [10, 14) is being tracked)
 * rangeModule.queryRange(13, 15); // return False,(Numbers like 14, 14.03, 14.17 in [13, 15) are not being tracked)
 * rangeModule.queryRange(16, 17); // return True,
 * (The number 16 in [16, 17) is still being tracked, despite the remove operation)
 *
 *
 * Constraints:
 *
 * 1 <= left < right <= 10^9
 * At most 10^4 calls will be made to addRange, queryRange, and removeRange.
 */
public class No715 {
    public static class RangeModule {
        private TreeMap<Integer, Integer> ranges;

        public RangeModule() {
            this.ranges = new TreeMap<>();
        }

        public void addRange(int left, int right) {
            if (left >= right) {
                return;
            }

            Integer start = this.ranges.floorKey(left);
            Integer end = this.ranges.floorKey(right);

            if (start == null && end == null) {
                this.ranges.put(left, right);
            } else if (start != null && left <= this.ranges.get(start)) {
                this.ranges.put(start, Math.max(right, Math.max(this.ranges.get(start), this.ranges.get(end))));
            } else {
                this.ranges.put(left, Math.max(right, this.ranges.get(end)));
            }

            this.ranges.subMap(left, false, right, true).clear();
        }

        public boolean queryRange(int left, int right) {
            Integer floorKey = this.ranges.floorKey(left);

            if (floorKey != null) {
                return right <= this.ranges.get(floorKey);
            }

            return false;
        }

        public void removeRange(int left, int right) {
            if (left >= right) {
                return;
            }

            Integer start = this.ranges.floorKey(left);
            Integer end = this.ranges.floorKey(right);

            // we need to update the ranges in reverse order to avoid new value overrides old value
            // when end equals to start
            if (end != null && this.ranges.get(end) > right) {
                this.ranges.put(right, this.ranges.get(end));
            }

            if (start != null && this.ranges.get(start) > left) {
                this.ranges.put(start, left);
            }


            this.ranges.subMap(left, true, right, false).clear();
        }
    }

/**
 * Your RangeModule object will be instantiated and called as such:
 * RangeModule obj = new RangeModule();
 * obj.addRange(left,right);
 * boolean param_2 = obj.queryRange(left,right);
 * obj.removeRange(left,right);
 */
}
