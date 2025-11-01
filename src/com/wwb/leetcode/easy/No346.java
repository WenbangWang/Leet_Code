package com.wwb.leetcode.easy;

/**
 * Given a stream of integers and a window size, calculate the moving average of all integers in the sliding window.
 * <p>
 * Example:
 *
 * <div>
 * MovingAverage m = new MovingAverage(3);
 * m.next(1) = 1
 * m.next(10) = (1 + 10) / 2
 * m.next(3) = (1 + 10 + 3) / 3
 * m.next(5) = (10 + 3 + 5) / 3
 * </div>
 */
public class No346 {
    class MovingAverage {
        private int[] arr;
        private long sum;
        private int count;

        MovingAverage(int size) {
            this.arr = new int[size];
            this.sum = 0;
            this.count = 0;
        }

        public double next(int val) {
            int index = this.count % this.arr.length;

            this.sum -= this.arr[index];
            this.arr[index] = val;
            this.sum += this.arr[index];

            this.count++;

            return 1.0 * this.sum / Math.min(this.count, this.arr.length);
        }
    }
}
