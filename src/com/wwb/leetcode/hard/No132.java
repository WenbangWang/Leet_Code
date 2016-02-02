package com.wwb.leetcode.hard;

/**
 * Given a string s, partition s such that every substring of the partition is a palindrome.
 *
 * Return the minimum cuts needed for a palindrome partitioning of s.
 *
 * For example, given s = "aab",
 * Return 1 since the palindrome partitioning ["aa","b"] could be produced using 1 cut.
 */
public class No132 {

    public int minCut(String s) {
        int length = s.length();
        int[] dp = new int[length + 1];

        for(int i = 1; i <= length; i++) {
            dp[i] = Integer.MAX_VALUE;
        }

        dp[0] = -1;

        for(int i = 0; i < length; i++) {
            for(int j = 0, start, end; (start = i - j) >= 0 && (end = i + j) < length && s.charAt(start) == s.charAt(end); j++) {
                dp[end + 1] = Math.min(dp[end + 1], dp[start] + 1);
            }

            for(int j = 1, start, end; (start = i - j + 1) >= 0 && (end = i + j) < length && s.charAt(start) == s.charAt(end); j++) {
                dp[end + 1] = Math.min(dp[end + 1], dp[start] + 1);
            }
        }

        return dp[length];
    }
}
