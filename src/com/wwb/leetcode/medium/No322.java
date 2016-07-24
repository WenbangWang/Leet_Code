package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * You are given coins of different denominations and a total amount of money amount.
 * Write a function to compute the fewest number of coins that you need to make up that amount.
 * If that amount of money cannot be made up by any combination of the coins, return -1.
 * <p>
 * Example 1:
 * coins = [1, 2, 5], amount = 11
 * return 3 (11 = 5 + 5 + 1)
 * <p>
 * Example 2:
 * coins = [2], amount = 3
 * return -1.
 * <p>
 * Note:
 * You may assume that you have an infinite number of each kind of coin.
 */
public class No322 {

    public int coinChange(int[] coins, int amount) {
        if (coins == null || amount < 0) {
            return -1;
        }

        if (coins.length == 0) {
            if (amount == 0) {
                return 0;
            }

            return -1;
        }

//        return solution1(coins, amount);
        return solution2(coins, amount);
    }

    private int solution1(int[] coins, int amount) {
        int[] dp = new int[amount];

        return solution1(coins, amount, dp);
    }

    private int solution1(int[] coins, int amount, int[] dp) {
        if (amount == 0) {
            return 0;
        }

        if (amount < 0) {
            return -1;
        }

        if (dp[amount - 1] != 0) {
            return dp[amount - 1];
        }
        int currentMin = Integer.MAX_VALUE;

        for (int coin : coins) {
            int currentResult = solution1(coins, amount - coin, dp);

            if (currentResult != -1) {
                currentMin = Math.min(currentResult + 1, currentMin);
            }
        }

        dp[amount - 1] = currentMin == Integer.MAX_VALUE ? -1 : currentMin;

        return dp[amount - 1];
    }

    private int solution2(int[] coins, int amount) {
        int[] dp = new int[amount + 1];

        Arrays.sort(coins);

        for (int sum = 1; sum <= amount; sum++) {
            int min = -1;

            for (int coin : coins) {
                if (sum < coin) {
                    break;
                }

                if (dp[sum - coin] != -1) {
                    int currentResult = dp[sum - coin] + 1;

                    if (min < 0) {
                        min = currentResult;
                    } else {
                        min = Math.min(min, currentResult);
                    }
                }
            }

            dp[sum] = min;
        }

        return dp[amount];
    }
}
