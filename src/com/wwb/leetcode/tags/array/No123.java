package com.wwb.leetcode.tags.array;

/**
 * Say you have an array for which the ith element is the price of a given stock on day i.
 *
 * Design an algorithm to find the maximum profit. You may complete at most two transactions.
 *
 * Note:
 * You may not engage in multiple transactions at the same time (ie, you must sell the stock before you buy again).
 */
public class No123 {

    public int maxProfit(int[] prices) {
        int maxProfit1 = 0;
        int maxProfit2 = 0;
        int minBuyPrice1 = Integer.MAX_VALUE;
        int minBuyPrice2 = Integer.MAX_VALUE;

        for(int price : prices) {
            maxProfit2 = Math.max(maxProfit2, price - minBuyPrice2);
            minBuyPrice2 = Math.min(minBuyPrice2, price - maxProfit1);
            maxProfit1 = Math.max(maxProfit1, price - minBuyPrice1);
            minBuyPrice1 = Math.min(minBuyPrice1, price);
        }

        return maxProfit2;
    }
}
