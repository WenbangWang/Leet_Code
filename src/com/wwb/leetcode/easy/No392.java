package com.wwb.leetcode.easy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given two strings s and t, return true if s is a subsequence of t, or false otherwise.
 * <p>
 * A subsequence of a string is a new string that is formed from the original string by deleting some (can be none) of the characters without disturbing the relative positions of the remaining characters. (i.e., "ace" is a subsequence of "abcde" while "aec" is not).
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: s = "abc", t = "ahbgdc"
 * Output: true
 * Example 2:
 * <p>
 * Input: s = "axc", t = "ahbgdc"
 * Output: false
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 0 <= s.length <= 100
 * 0 <= t.length <= 104
 * s and t consist only of lowercase English letters.
 * <p>
 * <p>
 * Follow up: Suppose there are lots of incoming s, say s1, s2, ..., sk where k >= 109, and you want to check one by one to see if t has its subsequence. In this scenario, how would you change your code?
 */
public class No392 {
    public boolean isSubsequence(String s, String t) {
        return solution1(s, t);
    }

    private boolean solution1(String s, String t) {
        if (s == null || t == null) {
            return false;
        }

        if (t.isEmpty() && !s.isEmpty()) {
            return false;
        }

        if (s.isEmpty()) {
            return true;
        }

        int sIndex = 0;
        int tIndex = 0;

        while (sIndex < s.length() && tIndex < t.length()) {
            if (s.charAt(sIndex) == t.charAt(tIndex)) {
                sIndex++;
            }
            tIndex++;
        }

        return sIndex == s.length();
    }

    private boolean solution2(String s, String t) {
        List<Integer>[] indexes = new List[256]; // Just for clarity
        for (int i = 0; i < t.length(); i++) {
            if (indexes[t.charAt(i)] == null) {
                indexes[t.charAt(i)] = new ArrayList<>();
            }
            indexes[t.charAt(i)].add(i);
        }

        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            if (indexes[s.charAt(i)] == null) {
                return false; // Note: char of S does NOT exist in T causing NPE
            }
            int j = Collections.binarySearch(indexes[s.charAt(i)], start);
            if (j < 0) {
                j = -j - 1;
            }
            if (j == indexes[s.charAt(i)].size()) {
                return false;
            }
            // Move to next position of t
            start = indexes[s.charAt(i)].get(j) + 1;
        }
        return true;
    }
}
