package com.wwb.leetcode.medium;

/**
 * Find the contiguous subarray within an array (containing at least one number) which has the largest product.
 *
 * For example, given the array [2,3,-2,4],
 * the contiguous subarray [2,3] has the largest product = 6.
 */
public class No152 {

    public int maxProduct(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        int maxProduct = nums[0];
        int minProduct = nums[0];
        int result = nums[0];

        for(int i = 1; i < nums.length; i++) {
            int num = nums[i];
            int lastMax = maxProduct;

            maxProduct = Math.max(Math.max(maxProduct * num, minProduct * num), num);
            minProduct = Math.min(Math.min(minProduct * num, lastMax * num), num);
            result = Math.max(maxProduct, result);
        }

        return result;
    }
}