package com.wwb.leetcode.medium;

/**
 * Find the contiguous subarray within an array (containing at least one number) which has the largest sum.
 *
 * For example, given the array [−2,1,−3,4,−1,2,1,−5,4],
 * the contiguous subarray [4,−1,2,1] has the largest sum = 6.
 */
public class No53 {

    public int maxSubArray(int[] nums) {
        //return solution1(nums);
        return solution2(nums);
    }

    private int solution1(int[] nums) {
        int maxSum = Integer.MIN_VALUE;
        int sum = 0;

        for(int num : nums) {
            sum += num;

            maxSum = Math.max(maxSum, sum);
            sum = Math.max(sum, 0);
        }

        return maxSum;
    }

    private int solution2(int[] nums) {
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        int max = dp[0];

        for(int i = 1; i < nums.length; i++) {
            dp[i] = nums[i] + Math.max(dp[i - 1], 0);
            max = Math.max(max, dp[i]);
        }

        return max;
    }

    private int solution3(int[] nums) {
        int maxSum = Integer.MIN_VALUE;
        int previousSum = 0;

        for(int num : nums) {
            previousSum = num + Math.max(previousSum, 0);
            maxSum = Math.max(previousSum, maxSum);
        }

        return maxSum;
    }
}
