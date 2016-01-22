package com.wwb.leetcode.tags.string;

/**
 * Given a string containing just the characters '(' and ')',
 * find the length of the longest valid (well-formed) parentheses substring.
 *
 * For "(()", the longest valid parentheses substring is "()", which has length = 2.
 *
 * Another example is ")()())", where the longest valid parentheses substring is "()()", which has length = 4.
 */
public class No32 {

    public int longestValidParentheses(String s) {
        if(s == null || s.isEmpty()) {
            return 0;
        }

        int length = s.length();
        int[] dp = new int[length];
        int longest = 0;

        for(int i = 1; i < length; i++) {
            if(s.charAt(i) == ')') {
                if(s.charAt(i - 1) == '(') {
                    dp[i] = 2 + (i > 1 ? dp[i - 2] : 0);
                    longest = Math.max(longest, dp[i]);
                } else {
                    if(i - dp[i - 1] - 1 >= 0 && s.charAt(i - dp[i - 1] - 1) == '(') {
                        dp[i] = dp[i - 1] + 2 + ((i - dp[i - 1] - 2) >= 0 ? dp[i - dp[i - 1] - 2] : 0);
                        longest = Math.max(longest, dp[i]);
                    }
                }
            }
        }

        return longest;
    }
}
