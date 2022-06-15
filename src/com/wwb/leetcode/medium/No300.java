package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * Given an unsorted array of integers, find the length of longest increasing subsequence.
 *
 * For example,
 * Given [10, 9, 2, 5, 3, 7, 101, 18],
 * The longest increasing subsequence is [2, 3, 7, 101], therefore the length is 4.
 * Note that there may be more than one LIS combination, it is only necessary for you to return the length.
 *
 * Your algorithm should run in O(n^2) complexity.
 *
 * Follow up: Could you improve it to O(n log n) time complexity?
 */
public class No300 {

    public int lengthOfLIS(int[] nums) {
        return solution2(nums);
    }

    private int solution2(int[] nums) {
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

    private int solution1(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        int length = nums.length;
        int[] dp = new int[length];

        Arrays.fill(dp, 1);

        for (int i = 1; i < length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[i] > nums[j]) {
                    dp[i] = Math.max(dp[j] + 1, dp[i]);
                }
            }
        }

        int longest = 0;

        for (int currentLongest : dp) {
            longest = Math.max(currentLongest, longest);
        }

        return longest;
    }
}
