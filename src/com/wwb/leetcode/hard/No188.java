package com.wwb.leetcode.hard;

/**
 * Say you have an array for which the ith element is the price of a given stock on day i.
 *
 * Design an algorithm to find the maximum profit. You may complete at most k transactions.
 *
 * Note:
 * You may not engage in multiple transactions at the same time
 * (ie, you must sell the stock before you buy again).
 */
public class No188 {

    public int maxProfit(int k, int[] prices) {
        int length = prices.length;

        if (k >= length / 2) {
            return quickSolve(prices);
        }

        int[][] dp = new int[k + 1][length];

        for (int i = 1; i <= k; i++) {
            int tmpMax = -prices[0];

            for (int j = 1; j < length; j++) {
                dp[i][j] = Math.max(dp[i][j - 1], prices[j] + tmpMax);
                tmpMax =  Math.max(tmpMax, dp[i - 1][j - 1] - prices[j]);
            }
        }

        return dp[k][length - 1];
    }


    private int quickSolve(int[] prices) {
        int length = prices.length;
        int profit = 0;

        for (int i = 1; i < length; i++) {
            if (prices[i] > prices[i - 1]) {
                profit += prices[i] - prices[i - 1];
            }
        }
        return profit;
    }
}
