package com.wwb.leetcode.hard;

/**
 * Given a string s, partition s such that every substring of the partition is a palindrome.
 * <p>
 * Return the minimum cuts needed for a palindrome partitioning of s.
 * <p>
 * For example, given s = "aab",
 * Return 1 since the palindrome partitioning ["aa","b"] could be produced using 1 cut.
 */
public class No132 {

    public int minCut(String s) {
        int length = s.length();
        int[] cuts = new int[length + 1];

        for (int i = 1; i <= length; i++) {
            cuts[i] = Integer.MAX_VALUE;
        }

        cuts[0] = -1;

        for (int centerOfPalindrome = 0; centerOfPalindrome < length; centerOfPalindrome++) {
            // odd length palindrome
            for (int radiusOfPalindrome = 0, start, end; (start = centerOfPalindrome - radiusOfPalindrome) >= 0 && (end = centerOfPalindrome + radiusOfPalindrome) < length && s.charAt(start) == s.charAt(end); radiusOfPalindrome++) {
                // when s(start, end) is a palindrome
                // the cut of s(0, end) can be represented
                // by the cuts for s(0, start - 1) + 1
                // where 1 means one extra cut to break s(0, end)
                // into s(0, start - 1) and s(start, end)
                cuts[end + 1] = Math.min(cuts[end + 1], cuts[start] + 1);
            }

            // even length palindrome
            for (int radiusOfPalindrome = 1, start, end; (start = centerOfPalindrome - radiusOfPalindrome + 1) >= 0 && (end = centerOfPalindrome + radiusOfPalindrome) < length && s.charAt(start) == s.charAt(end); radiusOfPalindrome++) {
                cuts[end + 1] = Math.min(cuts[end + 1], cuts[start] + 1);
            }
        }

        return cuts[length];
    }
}
