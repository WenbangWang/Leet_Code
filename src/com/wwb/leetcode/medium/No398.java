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

        // The first time we saw target 3 is at index 2.
        // The count is 0. Our reservoir only have 0 and we need to pick rnd.nextInt(++count) == 0.
        // The probability is 1. Result = 2.
        // Then we went to index 3.
        // The count is 1. Our reservoir is has [0,1].
        // We say if we get 0, we'll change the the result, otherwise we keep it.
        // Then chance that we keep the result=2 is 1/2 which means we got 1 from the reservoir.
        // Then we went to index 4. count =2.
        // Our reservoir has [0,1,2].
        // Same as before, if we get 0, then we'll change the result.
        // The chance we get 0 is 1/3, while the chance we didn't get is 2/3. i.e
        // The chance we keep the result ==2 is 2/3.
        // The chance we get index=2 is 1*1/2*2/3=1/3
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
