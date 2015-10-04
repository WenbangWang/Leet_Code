package com.wwb.leetcode.hard;

/**
 * Given an array of non-negative integers, you are initially positioned at the first index of the array.
 *
 * Each element in the array represents your maximum jump length at that position.
 *
 * Your goal is to reach the last index in the minimum number of jumps.
 *
 * For example:
 * Given array A = [2,3,1,1,4]
 *
 * The minimum number of jumps to reach the last index is 2. (Jump 1 step from index 0 to 1, then 3 steps to the last index.)
 */
public class No45 {

    public int jump(int[] nums) {
        int minSteps = 0;

        for(int start = 0, end = 0; end < nums.length - 1; minSteps++) {
            int max = 0;

            for(int i = start; i <= end; i++) {
                max = Math.max(max, nums[i] + i);
            }

            start = end + 1;
            end = max;
        }

        return minSteps;
    }
}
