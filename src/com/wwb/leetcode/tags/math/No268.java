package com.wwb.leetcode.tags.math;

/**
 * Given an array containing n distinct numbers taken from 0, 1, 2, ..., n,
 * find the one that is missing from the array.
 *
 * For example,
 * Given nums = [0, 1, 3] return 2.
 *
 * Note:
 * Your algorithm should run in linear runtime complexity.
 * Could you implement it using only constant extra space complexity?
 */
public class No268 {

    public int missingNumber(int[] nums) {
        int result = 0;

        for(int i = 0; i < nums.length; i++) {
            result += i + 1 - nums[i];
        }

        return result;
    }
}