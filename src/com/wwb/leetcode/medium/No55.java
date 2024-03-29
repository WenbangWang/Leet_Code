package com.wwb.leetcode.medium;

/**
 * Given an array of non-negative integers, you are initially positioned at the first index of the array.
 *
 * Each element in the array represents your maximum jump length at that position.
 *
 * Determine if you are able to reach the last index.
 *
 * For example:
 * A = [2,3,1,1,4], return true.
 *
 * A = [3,2,1,0,4], return false.
 */
public class No55 {

    public boolean canJump(int[] nums) {
        int max = 0;

        for(int i = 0; i < nums.length; i++) {
            if (i > max) {
                return false;
            }

            if (max >= nums.length - 1) {
                return true;
            }

            max = Math.max(max, nums[i] + i);
        }

        return true;
    }
}
