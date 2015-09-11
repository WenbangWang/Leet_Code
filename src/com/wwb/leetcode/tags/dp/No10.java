package com.wwb.leetcode.tags.dp;

/**
 * Implement regular expression matching with support for '.' and '*'.
 *
 * '.' Matches any single character.
 * '*' Matches zero or more of the preceding element.
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
 * isMatch("aa", "a*") → true
 * isMatch("aa", ".*") → true
 * isMatch("ab", ".*") → true
 * isMatch("aab", "c*a*b") → true
 */
public class No10 {
    public boolean isMatch(String s, String p) {
        if(s == null && p == null) {
            return true;
        } else if(s == null || p == null) {
            return false;
        }

        if(s.isEmpty() && p.isEmpty()) {
            return true;
        }

        int sLength = s.length();
        int pLength = p.length();
        boolean[][] map = new boolean[sLength + 1][pLength + 1];
        char[] sArray = s.toCharArray();
        char[] pArray = p.toCharArray();

        map[0][0] = true;

        for(int i = 1; i <= pLength; i++) {
            if(pArray[i - 1] == '*' && i > 1) {
                map[0][i] = map[0][i - 2];
            } else {
                map[0][i] = false;
            }
        }

        for(int i = 1; i <= sLength; i++) {
            for(int j = 1; j <= pLength; j++) {
                if(sArray[i - 1] == pArray[j - 1] || pArray[j - 1] == '.') {
                    map[i][j] = map[i - 1][j - 1];
                } else if(pArray[j - 1] == '*' && j > 1) {
                    if(sArray[i - 1] == pArray[j - 2] || pArray[j - 2] == '.') {
                        map[i][j] = map[i - 1][j] || map[i][j - 1] || map[i][j - 2];
                    } else {
                        map[i][j] = map[i][j - 2];
                    }
                } else {
                    map[i][j] = false;
                }
            }
        }

        return map[sLength][pLength];
    }

}
