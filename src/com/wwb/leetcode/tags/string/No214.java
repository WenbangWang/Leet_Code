package com.wwb.leetcode.tags.string;

/**
 * Given a string S, you are allowed to convert it to a palindrome by adding characters in front of it.
 * Find and return the shortest palindrome you can find by performing this transformation.
 *
 * For example:
 *
 * Given "aacecaaa", return "aaacecaaa".
 *
 * Given "abcd", return "dcbabcd".
 */
public class No214 {

    public String shortestPalindrome(String s) {
        int j = 0;
        int length = s.length();

        for(int i = length - 1; i >= 0; i--) {
            if(s.charAt(i) == s.charAt(j)) {
                j++;
            }
        }

        if(j == length) {
            return s;
        }
        String suffix = s.substring(j);

        return new StringBuilder(suffix).reverse().append(shortestPalindrome(s.substring(0, j))).append(suffix).toString();
    }
}
