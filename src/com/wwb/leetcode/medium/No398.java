package com.wwb.leetcode.medium;

import java.util.Random;

/**
 * Given an array of integers with possible duplicates, randomly output the index of a given target number.
 * You can assume that the given target number must exist in the array.
 * <p>
 * Note:
 * The array size can be very large. Solution that uses too much extra space will not pass the judge.
 * <p>
 * Example:
 * <p>
 * int[] nums = new int[] {1,2,3,3,3};
 * Solution solution = new Solution(nums);
 * <p>
 * // pick(3) should return either index 2, 3, or 4 randomly. Each index should have equal probability of returning.
 * solution.pick(3);
 * <p>
 * // pick(1) should return 0. Since in the array only nums[0] is equal to 1.
 * solution.pick(1);
 */
public class No398 {
    public class Solution {

        private int[] nums;
        private Random rnd;

        public Solution(int[] nums) {
            this.nums = nums;
            this.rnd = new Random();
        }

        public int pick(int target) {
            int result = -1;
            int count = 0;
            for (int i = 0; i < this.nums.length; i++) {
                if (this.nums[i] != target) {
                    continue;
                }
                if (this.rnd.nextInt(++count) == 0) {
                    result = i;
                }
            }

            return result;
        }
    }
}
