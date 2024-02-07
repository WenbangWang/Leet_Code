package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * Given a non-empty array nums containing only positive integers,
 * find if the array can be partitioned into two subsets such that the sum of elements in both subsets is equal.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [1,5,11,5]
 * Output: true
 * Explanation: The array can be partitioned as [1, 5, 5] and [11].
 * Example 2:
 *
 * Input: nums = [1,2,3,5]
 * Output: false
 * Explanation: The array cannot be partitioned into equal sum subsets.
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 200
 * 1 <= nums[i] <= 100
 */
public class No416 {
    public boolean canPartition(int[] nums) {
        return solution1(nums);
    }

    private boolean solution1(int[] nums) {
        if (nums == null || nums.length == 0) {
            return true;
        }

        int sum = Arrays.stream(nums).sum();

        if (sum % 2 != 0) {
            return false;
        }

        int subsetSum = sum / 2;
        boolean[][] dp = new boolean[nums.length + 1][subsetSum + 1];

        dp[0][0] = true;

        for (int i = 1; i <= nums.length; i++) {
            int num = nums[i - 1];

            for (int j = 0; j <= subsetSum; j++) {
                if (j < num) {
                    // we cannot include the current num
                    dp[i][j] = dp[i - 1][j];
                } else {
                    // we could either include the current num to form the current subset sum (j)
                    // or not include.
                    dp[i][j] = dp[i - 1][j] || dp[i - 1][j - num];
                }
            }
        }

        return dp[nums.length][subsetSum];
    }

    private boolean solution2(int[] nums) {
        if (nums == null || nums.length == 0) {
            return true;
        }

        int sum = Arrays.stream(nums).sum();

        if (sum % 2 != 0) {
            return false;
        }

        int subsetSum = sum / 2;
        boolean[] dp = new boolean[subsetSum + 1];

        dp[0] = true;

        for (int num : nums) {
            for (int i = subsetSum; i >= num; i--) {
                dp[i] = dp[i] || dp[i - num];
            }
        }

        return dp[subsetSum];
    }
}
