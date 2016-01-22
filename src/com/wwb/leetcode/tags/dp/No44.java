package com.wwb.leetcode.tags.dp;

/**
 * Implement wildcard pattern matching with support for '?' and '*'.
 *
 * '?' Matches any single character.
 * '*' Matches any sequence of characters (including the empty sequence).
 *
 * The matching should cover the entire input string (not partial).
 *
 * The function prototype should be:
 * bool isMatch(const char *s, const char *p)
 *
 * Some examples:
 * isMatch("aa","a") → false
 * isMatch("aa","aa") → true
 * isMatch("aaa","aa") → false
 * isMatch("aa", "*") → true
 * isMatch("aa", "a*") → true
 * isMatch("ab", "?*") → true
 * isMatch("aab", "c*a*b") → false
 */
public class No44 {

    public boolean isMatch(String s, String p) {
//        return solution1(s, p);
        return solution2(s, p);
    }

    private boolean solution1(String s, String p) {
        int sLength = s.length();
        int pLength = p.length();

        boolean[][] dp = new boolean[sLength + 1][pLength + 1];
        dp[0][0] = true;

        for(int i = 1; i <= pLength; i++) {
            if(p.charAt(i - 1) == '*') {
                dp[0][i] = true;
            } else {
                break;
            }
        }

        for(int i = 1; i <= sLength; i++) {
            for(int j = 1; j <= pLength; j++) {
                if(p.charAt(j - 1) == '*') {
                    dp[i][j] = dp[i - 1][j] || dp[i][j - 1];
                } else {
                    dp[i][j] = dp[i - 1][j - 1] && (s.charAt(i - 1) == p.charAt(j - 1) || p.charAt(j - 1) == '?');
                }
            }
        }

        return dp[sLength][pLength];
    }

    private boolean solution2(String s, String p) {
        int sPointer = 0;
        int pPointer = 0;
        int starIndex = -1;
        int lastSPointer = 0;

        while(sPointer < s.length()) {
            if(pPointer < p.length() && (s.charAt(sPointer) == p.charAt(pPointer) || p.charAt(pPointer) == '?')) {
                sPointer++;
                pPointer++;
            } else if(pPointer < p.length() && p.charAt(pPointer) == '*') {
                starIndex = pPointer;
                lastSPointer = sPointer;
                pPointer++;
            } else if(starIndex >= 0) {
                pPointer = starIndex + 1;
                sPointer = ++lastSPointer;
            } else {
                return false;
            }
        }

        while(pPointer < p.length() && p.charAt(pPointer) == '*') {
            pPointer++;
        }

        return pPointer == p.length();
    }
}
