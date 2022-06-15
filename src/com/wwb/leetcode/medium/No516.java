package com.wwb.leetcode.medium;

/**
 * Given a string s, find the longest palindromic subsequence's length in s.
 *
 * A subsequence is a sequence that can be derived from another sequence by deleting some or no elements without changing the order of the remaining elements.
 *
 *
 *
 * Example 1:
 *
 * Input: s = "bbbab"
 * Output: 4
 * Explanation: One possible longest palindromic subsequence is "bbbb".
 * Example 2:
 *
 * Input: s = "cbbd"
 * Output: 2
 * Explanation: One possible longest palindromic subsequence is "bb".
 *
 * Constraints:
 *
 * 1 <= s.length <= 1000
 * s consists only of lowercase English letters.
 */
public class No516 {
    public int longestPalindromeSubseq(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        // dp[i][j] means the number of longest palindrome from i to j (inclusive)
        int[][] dp = new int[s.length()][s.length()];

        // single character should be a palindrome itself
        for (int i = 0; i < s.length(); i++) {
            dp[i][i] = 1;
        }

        for (int distance = 1; distance < s.length(); distance++) {
            for (int start = 0; start + distance < s.length(); start++) {
                int end = start + distance;

                if (s.charAt(start) == s.charAt(end)) {
                    // a...a. the longest palindrome between the two 'a's plus two
                    dp[start][end] = dp[start + 1][end - 1] + 2;
                } else {
                    // a, c...d, b. the longest palindrome should be
                    // the max between a, c...d and c...d, b
                    dp[start][end] = Math.max(dp[start][end - 1], dp[start + 1][end]);
                }
            }
        }

        return dp[0][s.length() - 1];
    }
}
