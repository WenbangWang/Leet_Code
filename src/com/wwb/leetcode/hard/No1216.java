package com.wwb.leetcode.hard;

/**
 * Given a string s and an integer k, find out if the given string is a K-Palindrome or not.
 * <p>
 * A string is K-Palindrome if it can be transformed into a palindrome by removing at most k characters from it.
 * <p>
 * <p>
 * <p>
 * <pre>
 * Example 1:
 * Input: s = "abcdeca", k = 2
 * Output: true
 * Explanation: Remove 'b' and 'e' characters.
 * </pre>
 * <p>
 * <p>
 * <pre>
 * Constraints:
 * 1 <= s.length <= 1000
 * s has only lowercase English letters.
 * 1 <= k <= s.length
 * </pre>
 */
public class No1216 {
    public boolean isValidPalindrome(String s, int k) {
        int n = s.length();
        // dp[i][j] represents the longest palindrome within range [i,j] in s.
        int[][] dp = new int[n][n];

        // single character is a palindrome.
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
        }

        // We go backward on start because of the dependencies in the DP recurrence.
        for (int start = n - 2; start >= 0; start--) {
            for (int end = start + 1; end < n; end++) {
                if (s.charAt(start) == s.charAt(end)) {
                    dp[start][end] = dp[start + 1][end - 1] + 2;
                } else {
                    dp[start][end] = Math.max(dp[start + 1][end], dp[start][end - 1]);
                }

                if (dp[start][end] + k >= n) {
                    return true;
                }
            }
        }

        return false;
    }
}
