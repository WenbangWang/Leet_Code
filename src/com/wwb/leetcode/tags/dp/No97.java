package com.wwb.leetcode.tags.dp;

/**
 * Given s1, s2, s3, find whether s3 is formed by the interleaving of s1 and s2.
 *
 * For example,
 * Given:
 * s1 = "aabcc",
 * s2 = "dbbca",
 *
 * When s3 = "aadbbcbcac", return true.
 * When s3 = "aadbbbaccc", return false.
 */
public class No97 {

    public boolean isInterleave(String s1, String s2, String s3) {
//        return solution1(s1, s2, s3);
        return solution2(s1, s2, s3);
    }

    private boolean solution1(String s1, String s2, String s3) {
        int length1 = s1.length();
        int length2 = s2.length();

        if(length1 + length2 != s3.length()) {
            return false;
        }

        boolean[][] dp = new boolean[length1 + 1][length2 + 1];

        for(int i = 0; i <= length1; i++) {
            for(int j = 0; j <= length2; j++) {
                if(i == 0 && j == 0) {
                    dp[i][j] = true;
                } else if(i == 0) {
                    dp[i][j] = dp[i][j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1);
                } else if(j == 0) {
                    dp[i][j] = dp[i - 1][j] && s1.charAt(i - 1) == s3.charAt(i + j - 1);
                } else {
                    dp[i][j] = dp[i - 1][j] && s1.charAt(i - 1) == s3.charAt(i + j - 1) || dp[i][j - 1] && s2.charAt(j - 1) == s3.charAt(i + j - 1);
                }
            }
        }

        return dp[length1][length2];
    }

    private boolean solution2(String s1, String s2, String s3) {
        int length1 = s1.length();
        int length2 = s2.length();

        if(length1 + length2 != s3.length()) {
            return false;
        }

        if(length1 > length2) {
            return solution1(s2, s1, s3);
        }

        boolean[] dp = new boolean[length1 + 1];

        dp[0] = true;

        for(int i = 1; i <= length1; i++) {
            dp[i] = dp[i - 1] && s1.charAt(i - 1) == s3.charAt(i - 1);
        }

        for(int i = 1; i <= length2; i++) {
            dp[0] = dp[0] && s2.charAt(i - 1) == s3.charAt(i - 1);
            for(int j = 1; j <= length1; j++) {
                dp[j] = dp[j - 1] && s1.charAt(j - 1) == s3.charAt(i + j - 1) || dp[j] && s2.charAt(i - 1) == s3.charAt(i + j - 1);
            }
        }

        return dp[length1];
    }
}
