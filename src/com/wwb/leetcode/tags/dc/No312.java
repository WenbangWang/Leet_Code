package com.wwb.leetcode.tags.dc;

/**
 * Given n balloons, indexed from 0 to n-1.
 * Each balloon is painted with a number on it represented by array nums.
 * You are asked to burst all the balloons.
 * If the you burst balloon i you will get nums[left] * nums[i] * nums[right] coins.
 * Here left and right are adjacent indices of i.
 * After the burst, the left and right then becomes adjacent.
 *
 * Find the maximum coins you can collect by bursting the balloons wisely.
 *
 * Note:
 * (1) You may imagine nums[-1] = nums[n] = 1. They are not real therefore you can not burst them.
 * (2) 0 ≤ n ≤ 500, 0 ≤ nums[i] ≤ 100
 *
 * Example:
 *
 * Given [3, 1, 5, 8]
 *
 * Return 167
 *
 * nums = [3,1,5,8] --> [3,5,8] -->   [3,8]   -->  [8]  --> []
 * coins =  3*1*5      +  3*5*8    +  1*3*8      + 1*8*1   = 167
 */
public class No312 {

    public int maxCoins(int[] nums) {
        return solution2(nums);
    }

    private int solution1(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        int[] newNums = new int[nums.length + 2];
        int n = 1;

        for(int num : nums) {
            if(num > 0) {
                newNums[n++] = num;
            }
        }

        newNums[0] = newNums[n++] = 1;

        int[][] map = new int[n][n];

        return burst(map, newNums, 0, n - 1);
    }

    private int solution2(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        int[] newNums = new int[nums.length + 2];
        int n = 1;

        for(int num : nums) {
            if(num > 0) {
                newNums[n++] = num;
            }
        }

        newNums[0] = newNums[n++] = 1;

        int[][] dp = new int[n][n];

        for(int i = 2; i < n; i++) {
            for(int left = 0; left < n - i; left++) {
                int right = left + i;

                for(int j = left + 1; j < right; j++) {
                    dp[left][right] = Math.max(dp[left][right],
                        newNums[left] * newNums[j] * newNums[right] + dp[left][j] + dp[j][right]);
                }
            }
        }

        return dp[0][n - 1];
    }

    private int burst(int[][] map, int[] nums, int start, int end) {
        if(start + 1 == end) {
            return 0;
        }

        if(map[start][end] > 0) {
            return map[start][end];
        }

        int result = 0;

        for(int i = start + 1; i < end; i++) {
            result = Math.max(result,
                nums[start] * nums[i] * nums[end] + burst(map, nums, start, i) + burst(map, nums, i, end));
        }

        map[start][end] = result;

        return result;
    }
}
