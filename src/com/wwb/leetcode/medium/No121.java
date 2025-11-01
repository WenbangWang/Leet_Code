package com.wwb.leetcode.medium;

/**
 * Say you have an array for which the ith element is the price of a given stock on day i.
 * <p>
 * If you were only permitted to complete at most one transaction (ie, buy one and sell one share of the stock),
 * design an algorithm to find the maximum profit.
 */
public class No121 {

    public int maxProfit(int[] prices) {
        if (prices == null || prices.length == 0) {
            return 0;
        }

        int maxProfit = 0;
        int minPrice = Integer.MAX_VALUE;

        for (int price : prices) {
            minPrice = Math.min(minPrice, price);
            maxProfit = Math.max(price - minPrice, maxProfit);
        }

        return maxProfit;
    }

    public int minRoundTrip(int[] depart, int[] arrive) {
        int n = depart.length;
        if (n == 0 || arrive.length != n) {
            return -1;
        }

        int minDepart = depart[0];
        int result = Integer.MAX_VALUE;

        for (int j = 1; j < n; j++) { // must return after departure
            result = Math.min(result, minDepart + arrive[j]);
            minDepart = Math.min(minDepart, depart[j]);
        }

        return result == Integer.MAX_VALUE ? -1 : result;
    }
}
