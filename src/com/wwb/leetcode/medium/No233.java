package com.wwb.leetcode.medium;

/**
 * Given an integer n, count the total number of digit 1 appearing in all non-negative integers less than or equal to n.
 *
 * For example:
 * Given n = 13,
 * Return 6, because digit 1 occurred in the following numbers: 1, 10, 11, 12, 13.
 */
public class No233 {

    public int countDigitOne(int n) {
        int count = 0;

        for(long i = 1; i <= n; i *= 10) {
            long mod = n % i;
            long divide = n / i;
            count += (divide + 8) / 10 * i + (divide % 10 == 1 ? mod + 1 : 0);
        }

        return count;
    }
}