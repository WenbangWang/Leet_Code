package com.wwb.leetcode.medium;

/**
 * Note: This is an extension of House Robber.
 *
 * After robbing those houses on that street,
 * the thief has found himself a new place for his thievery so that he will not get too much attention.
 * This time, all houses at this place are arranged in a circle.
 * That means the first house is the neighbor of the last one.
 * Meanwhile, the security system for these houses remain the same as for those in the previous street.
 *
 * Given a list of non-negative integers representing the amount of money of each house,
 * determine the maximum amount of money you can rob tonight without alerting the police.
 */
public class No213 {

    public int rob(int[] nums) {
        if(nums == null) {
            return 0;
        }

        int length = nums.length;

        if(length == 1) {
            return nums[0];
        }

        return Math.max(rob(nums, 0, length - 2), rob(nums, 1, length - 1));
    }

    private int rob(int[] nums, int from, int to) {
        int stealCurrent = 0;
        int notStealCurrent = 0;

        for(int i = from; i <= to; i++) {
            int stealPrevious = stealCurrent;
            int notStealPrevious = notStealCurrent;
            notStealCurrent = Math.max(notStealPrevious, stealPrevious);
            stealCurrent = notStealPrevious + nums[i];
        }

        return Math.max(stealCurrent, notStealCurrent);
    }
}