package com.wwb.leetcode.hard;

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
//        return solution1(s, t);
        return solution2(s, t);
    }

    private int solution1(String s, String t) {
        int sLength = s.length();
        int tLength = t.length();
        int[][] dp = new int[sLength + 1][tLength + 1];

        for(int i = 0; i <= sLength; i++) {
            dp[i][0] = 1;
        }

        for(int i = 1; i <= sLength; i++) {
            for(int j = 1; j <= tLength; j++) {
                if(s.charAt(i - 1) == t.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + dp[i][j - 1];
                } else {
                    dp[i][j] = dp[i][j - 1];
                }
            }
        }

        return dp[sLength][tLength];
    }

    private int solution2(String s, String t) {
        int sLength = s.length();
        int tLength = t.length();
        int[] dp = new int[tLength + 1];
        dp[0] = 1;

        for(int i = 1; i <= sLength; i++) {
            for(int j = tLength; j > 0; j--) {
                if(s.charAt(i - 1) == t.charAt(j - 1)) {
                    dp[j] = dp[j] + dp[j - 1];
                }
            }
        }

        return dp[tLength];
    }
}
