package com.wwb.leetcode.medium;

/**
 * Given two strings s1 and s2, return the lowest ASCII sum of deleted characters to make two strings equal.
 *
 *
 *
 * Example 1:
 *
 * Input: s1 = "sea", s2 = "eat"
 * Output: 231
 * Explanation: Deleting "s" from "sea" adds the ASCII value of "s" (115) to the sum.
 * Deleting "t" from "eat" adds 116 to the sum.
 * At the end, both strings are equal, and 115 + 116 = 231 is the minimum sum possible to achieve this.
 * Example 2:
 *
 * Input: s1 = "delete", s2 = "leet"
 * Output: 403
 * Explanation: Deleting "dee" from "delete" to turn the string into "let",
 * adds 100[d] + 101[e] + 101[e] to the sum.
 * Deleting "e" from "leet" adds 101[e] to the sum.
 * At the end, both strings are equal to "let", and the answer is 100+101+101+101 = 403.
 * If instead we turned both strings into "lee" or "eet", we would get answers of 433 or 417, which are higher.
 *
 *
 * Constraints:
 *
 * 1 <= s1.length, s2.length <= 1000
 * s1 and s2 consist of lowercase English letters.
 */
public class No712 {
    public int minimumDeleteSum(String s1, String s2) {
        return solution1(s1, s2);
    }

    private int solution1(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for(int i = 1 ; i <= m ; i++) {
            // base case filling up
            dp[i][0] = dp[i - 1][0] + s1.charAt(i - 1);
        }

        for(int j = 1; j <= n ; j++) {
            // base case filling up
            dp[0][j] = dp[0][j - 1] + s2.charAt(j - 1);
        }

        for(int i = 1 ; i <= m ; i++) {
            for(int j = 1 ; j <= n ; j++) {
                if(s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j] + s1.charAt(i - 1), dp[i][j - 1] + s2.charAt(j - 1));
                }
            }
        }

        return dp[m][n];
    }

    private int solution2(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[] dp = new int[m + 1];

        for (int i = 1; i <= m; i++) {
            dp[i] = dp[i - 1] + s1.charAt(i - 1);
        }

        for (int j = 1; j <= n; j++) {
            int t1 = dp[0];
            dp[0] += s2.charAt(j - 1);

            for (int i = 1; i <= m; i++) {
                int t2 = dp[i];
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i] = t1;
                } else {
                    dp[i] = Math.min(dp[i - 1] + s1.charAt(i - 1), dp[i] + s2.charAt(j - 1));
                }

                t1 = t2;
            }
        }

        return dp[m];
    }
}
