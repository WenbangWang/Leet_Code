package com.wwb.leetcode.tags.binarysearch;

import java.util.Arrays;

/**
 * Given an unsorted array of integers, find the length of longest increasing subsequence.
 *
 * For example,
 * Given [10, 9, 2, 5, 3, 7, 101, 18],
 * The longest increasing subsequence is [2, 3, 7, 101], therefore the length is 4.
 * Note that there may be more than one LIS combination, it is only necessary for you to return the length.
 *
 * Your algorithm should run in O(n2) complexity.
 *
 * Follow up: Could you improve it to O(n log n) time complexity?
 */
public class No300 {

    public int lengthOfLIS(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        int maxLength = 0;
        int length = nums.length;
        int[] dp = new int[length];

        for(int num : nums) {
            int index = Arrays.binarySearch(dp, 0, maxLength, num);

            if(index < 0) {
                index = -(index + 1);
            }

            dp[index] = num;

            if(index == maxLength) {
                maxLength++;
            }
        }

        return maxLength;
    }
}
