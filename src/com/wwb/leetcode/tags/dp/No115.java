package com.wwb.leetcode.tags.dp;

/**
 * Given a string S and a string T, count the number of distinct subsequences of T in S.
 *
 * A subsequence of a string is a new string which is formed from the original string by deleting some (can be none) of the characters without disturbing the relative positions of the remaining characters. (ie, "ACE" is a subsequence of "ABCDE" while "AEC" is not).
 *
 * Here is an example:
 * S = "rabbbit", T = "rabbit"
 *
 * Return 3.
 */
public class No115 {

    public int numDistinct(String s, String t) {
        int sLength = s.length();
        int tLength = t.length();
        int[][] dp = new int[tLength + 1][sLength + 1];

        for(int i = 0; i <= sLength; i++) {
            dp[0][i] = 1;
        }

        for(int i = 0; i < tLength; i++) {
            for(int j = 0; j < sLength; j++) {
                if(s.charAt(j) == t.charAt(i)) {
                    dp[i + 1][j + 1] = dp[i][j] + dp[i + 1][j];
                } else {
                    dp[i + 1][j + 1] = dp[i + 1][j];
                }
            }
        }

        return dp[tLength][sLength];
    }
}
