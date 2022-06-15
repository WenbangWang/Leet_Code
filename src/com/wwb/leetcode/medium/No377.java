package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/**
 * Given an integer array with all positive numbers and no duplicates, find the number of possible combinations that add up to a positive integer target.
 *
 * Example:
 *
 * nums = [1, 2, 3]
 * target = 4
 *
 * The possible combination ways are:
 * (1, 1, 1, 1)
 * (1, 1, 2)
 * (1, 2, 1)
 * (1, 3)
 * (2, 1, 1)
 * (2, 2)
 * (3, 1)
 *
 * Note that different sequences are counted as different combinations.
 *
 * Therefore the output is 7.
 * Follow up:
 * What if negative numbers are allowed in the given array?
 * How does it change the problem?
 * What limitation we need to add to the question to allow negative numbers?
 */
public class No377 {
    public int combinationSum4(int[] nums, int target) {
        return solution1(nums, target);
    }

    private int solution1(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;

        for(int sum = 1; sum <= target; sum++) {
            for(int  num : nums) {
                if(sum - num >= 0) {
                    dp[sum] += dp[sum - num];
                }
            }
        }

        return dp[target];
    }

    private int solution2(int[] nums, int target) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // key is the target sum
        // value is number of combinations to the sum
        Map<Integer, Integer> map = new HashMap<>();

        return solution2(nums, target, map);
    }

    private int solution2(int[] nums, int target, Map<Integer, Integer> map) {
        if (target == 0) {
            return 1;
        }

        if (target < 0) {
            return 0;
        }

        if (map.containsKey(target)) {
            return map.get(target);
        }

        int result = 0;

        for (int num : nums) {
            if (num <= target) {
                result += solution2(nums, target - num, map);
            }
        }

        map.put(target, result);

        return result;
    }
}
