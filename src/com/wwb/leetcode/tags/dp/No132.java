package com.wwb.leetcode.tags.dp;

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
        int[] dp = new int[s.length() + 1];

        for(int i = 0; i < s.length(); i++) {
            dp[i] = Integer.MAX_VALUE;
        }

        dp[s.length()] = -1;

        for(int i = s.length() - 1; i >= 0; i--) {
            for(int start = i, end = i; start >= 0 && end < s.length() && s.charAt(start) == s.charAt(end); start--, end++) {
                dp[start] = Math.min(dp[start], dp[end + 1] + 1);
            }

            for(int start = i, end = i + 1; start >= 0 && end < s.length() && s.charAt(start) == s.charAt(end); start--, end++) {
                dp[start] = Math.min(dp[start], dp[end + 1] + 1);
            }
        }

        return dp[0];
    }
}
