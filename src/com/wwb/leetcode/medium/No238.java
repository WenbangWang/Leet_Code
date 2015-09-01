package com.wwb.leetcode.medium;

/**
 * Given an array of n integers where n > 1, nums,
 * return an array output such that output[i] is equal to the product of all the elements of nums except nums[i].
 *
 * Solve it without division and in O(n).
 *
 * For example, given [1,2,3,4], return [24,12,8,6].
 *
 * Follow up:
 * Could you solve it with constant space complexity?
 * (Note: The output array does not count as extra space for the purpose of space complexity analysis.)
 */
public class No238 {

    public int[] productExceptSelf(int[] nums) {
        int length = nums.length;
        int[] result = new int[length];
        int right = 1;
        result[0] = 1;

        for(int i = 1; i < length; i++) {
            result[i] = result[i - 1] * nums[i - 1];
        }

        for(int i = length - 1; i >= 0; i--) {
            result[i] *= right;
            right *= nums[i];
        }

        return result;
    }
}