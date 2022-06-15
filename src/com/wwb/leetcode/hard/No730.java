package com.wwb.leetcode.hard;

import java.util.Arrays;

/**
 * Given a string s, return the number of different non-empty palindromic subsequences in s. Since the answer may be very large, return it modulo 10^9 + 7.
 * <p>
 * A subsequence of a string is obtained by deleting zero or more characters from the string.
 * <p>
 * A sequence is palindromic if it is equal to the sequence reversed.
 * <p>
 * Two sequences a1, a2, ... and b1, b2, ... are different if there is some i for which ai != bi.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: s = "bccb"
 * Output: 6
 * Explanation: The 6 different non-empty palindromic subsequences are 'b', 'c', 'bb', 'cc', 'bcb', 'bccb'.
 * Note that 'bcb' is counted only once, even though it occurs twice.
 * Example 2:
 * <p>
 * Input: s = "abcdabcdabcdabcdabcdabcdabcdabcddcbadcbadcbadcbadcbadcbadcbadcba"
 * Output: 104860361
 * Explanation: There are 3104860382 different non-empty palindromic subsequences, which is 104860361 modulo 109 + 7.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= s.length <= 1000
 * s[i] is either 'a', 'b', 'c', or 'd'.
 */
public class No730 {
    public int countPalindromicSubsequences(String s) {
        return solution1(s);
    }

    // O(n^3)
    private int solution1(String s) {
        int MODULO = 1000000007;

        int n = s.length();
        // dp[i][j] means palindrome from i to j (inclusive)
        int[][] dp = new int[n][n];

        char[] chars = s.toCharArray();
        for (int i = 0; i < n; i++) {
            // Consider the test case "a", "b" "c"...
            dp[i][i] = 1;
        }

        for (int distance = 1; distance < n; distance++) {
            for (int start = 0; start < n - distance; start++) {
                int end = start + distance;

                if (chars[start] == chars[end]) {
                    int left = start + 1;
                    int right = end - 1;

                    // Variable left and right here are used to get rid of the duplicate
                    while (left <= right && chars[left] != chars[end]) {
                        left++;
                    }
                    while (left <= right && chars[right] != chars[end]) {
                        right--;
                    }

                    if (left > right) {
                        // consider the string from start to end is "a...a" "a...a"...
                        // where there is no character 'a' inside the leftmost and rightmost 'a'
                        // eg:  "aba" while start = 0 and end = 2:  dp[1][1] = 1 records the palindrome{"b"},
                        // the reason why dp[start + 1][end  - 1] * 2 counted is that
                        // we count dp[start + 1][end - 1] one time as {"b"},
                        // and additional time as {"aba"}.
                        // The reason why 2 counted is that we also count {"a", "aa"}.
                        // So totally dp[start][end] record the palindrome: {"a", "b", "aa", "aba"}.
                        dp[start][end] = dp[start + 1][end - 1] * 2 + 2;
                    } else if (left == right) {
                        // consider the string from start to end is "a...a...a"
                        // where there is only one character 'a' inside the leftmost and rightmost 'a'
                        // eg:  "aaa" while start = 0 and end = 2: the dp[start + 1][end - 1] records the palindrome {"a"}.
                        // the reason why dp[start + 1][end  - 1] * 2 counted is that
                        // we count dp[start + 1][end - 1] one time as {"a"}, and additional time as {"aaa"}.
                        // the reason why 1 counted is that we also count {"aa"} that
                        // the first 'a' come from index start and the second come from index end.
                        // So totally dp[start][end] records {"a", "aa", "aaa"}
                        dp[start][end] = dp[start + 1][end - 1] * 2 + 1;
                    } else {
                        // consider the string from start to end is "a...a...a... a" where there are at least two character 'a' close to leftmost and rightmost 'a'
                        // eg: "aacaa" while start = 0 and end = 4: the dp[start + 1][end - 1]
                        // records the palindrome {"a",  "c", "aa", "aca"}.
                        // the reason why dp[start + 1][end  - 1] * 2 counted is that
                        // we count dp[start + 1][end - 1] one time as {"a",  "c", "aa", "aca"},
                        // and additional time as {"aaa",  "aca", "aaaa", "aacaa"}.
                        // Now there is duplicate :  {"aca"}, which is removed by deduce dp[left + 1][right - 1].
                        // So totally dp[start][end] record {"a",  "c", "aa", "aca", "aaa", "aaaa", "aacaa"}
                        dp[start][end] = dp[start + 1][end - 1] * 2 - dp[left + 1][right - 1];
                    }
                } else {
                    dp[start][end] = dp[start][end - 1] + dp[start + 1][end] - dp[start + 1][end - 1];  //s.charAt(start) != s.charAt(end)
                }

                dp[start][end] = dp[start][end] < 0 ? dp[start][end] + MODULO : dp[start][end] % MODULO;
            }
        }

        return dp[0][n - 1];
    }

    // O(n^2)
    private int solution2(String s) {
        int MODULO = 1000000007;

        int n = s.length();
        char[] chars = s.toCharArray();

        int[] leftNext = new int[n];
        int[] rightNext = new int[n];
        // 4 means the input only contains 4 chars a, b, c, d
        int[] lastShownIndexes = new int[4];

        Arrays.fill(lastShownIndexes, -1);

        for (int i = 0; i < n; i++) {
            leftNext[i] = lastShownIndexes[chars[i] - 'a'];
            lastShownIndexes[chars[i] - 'a'] = i;
        }

        Arrays.fill(lastShownIndexes, n);
        for (int i = n - 1; i >= 0; i--) {
            rightNext[i] = lastShownIndexes[chars[i] - 'a'];
            lastShownIndexes[chars[i] - 'a'] = i;
        }

        // dp[i][j] means palindrome from i to j (inclusive)
        int[][] dp = new int[n][n];

        for (int i = 0; i < n; i++) {
            // Consider the test case "a", "b" "c"...
            dp[i][i] = 1;
        }

        for (int distance = 1; distance < n; distance++) {
            for (int start = 0; start < n - distance; start++) {
                int end = start + distance;

                if (chars[start] == chars[end]) {
                    int left = rightNext[start];
                    int right = leftNext[end];

                    if (left > right) {
                        // consider the string from start to end is "a...a" "a...a"...
                        // where there is no character 'a' inside the leftmost and rightmost 'a'
                        // eg:  "aba" while start = 0 and end = 2:  dp[1][1] = 1 records the palindrome{"b"},
                        // the reason why dp[start + 1][end  - 1] * 2 counted is that
                        // we count dp[start + 1][end - 1] one time as {"b"},
                        // and additional time as {"aba"}.
                        // The reason why 2 counted is that we also count {"a", "aa"}.
                        // So totally dp[start][end] record the palindrome: {"a", "b", "aa", "aba"}.
                        dp[start][end] = dp[start + 1][end - 1] * 2 + 2;
                    } else if (left == right) {
                        // consider the string from start to end is "a...a...a"
                        // where there is only one character 'a' inside the leftmost and rightmost 'a'
                        // eg:  "aaa" while start = 0 and end = 2: the dp[start + 1][end - 1] records the palindrome {"a"}.
                        // the reason why dp[start + 1][end  - 1] * 2 counted is that
                        // we count dp[start + 1][end - 1] one time as {"a"}, and additional time as {"aaa"}.
                        // the reason why 1 counted is that we also count {"aa"} that
                        // the first 'a' come from index start and the second come from index end.
                        // So totally dp[start][end] records {"a", "aa", "aaa"}
                        dp[start][end] = dp[start + 1][end - 1] * 2 + 1;
                    } else {
                        // consider the string from start to end is "a...a...a... a" where there are at least two character 'a' close to leftmost and rightmost 'a'
                        // eg: "aacaa" while start = 0 and end = 4: the dp[start + 1][end - 1]
                        // records the palindrome {"a",  "c", "aa", "aca"}.
                        // the reason why dp[start + 1][end  - 1] * 2 counted is that
                        // we count dp[start + 1][end - 1] one time as {"a",  "c", "aa", "aca"},
                        // and additional time as {"aaa",  "aca", "aaaa", "aacaa"}.
                        // Now there is duplicate :  {"aca"}, which is removed by deduce dp[left + 1][right - 1].
                        // So totally dp[start][end] record {"a",  "c", "aa", "aca", "aaa", "aaaa", "aacaa"}
                        dp[start][end] = dp[start + 1][end - 1] * 2 - dp[left + 1][right - 1];
                    }
                } else {
                    dp[start][end] = dp[start][end - 1] + dp[start + 1][end] - dp[start + 1][end - 1];  //s.charAt(start) != s.charAt(end)
                }

                dp[start][end] = dp[start][end] < 0 ? dp[start][end] + MODULO : dp[start][end] % MODULO;
            }
        }

        return dp[0][n - 1];
    }
}
