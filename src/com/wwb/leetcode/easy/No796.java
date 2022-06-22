package com.wwb.leetcode.easy;

/**
 * Given two strings s and goal, return true if and only if s can become goal after some number of shifts on s.
 *
 * A shift on s consists of moving the leftmost character of s to the rightmost position.
 *
 * For example, if s = "abcde", then it will be "bcdea" after one shift.
 *
 *
 * Example 1:
 *
 * Input: s = "abcde", goal = "cdeab"
 * Output: true
 * Example 2:
 *
 * Input: s = "abcde", goal = "abced"
 * Output: false
 *
 *
 * Constraints:
 *
 * 1 <= s.length, goal.length <= 100
 * s and goal consist of lowercase English letters.
 */
public class No796 {
    public boolean rotateString(String s, String goal) {
        return solution1(s, goal);
    }

    private boolean solution1(String s, String goal) {
        if (s.length() != goal.length()) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {
            var prefix = s.substring(i);

            if (goal.startsWith(prefix) && goal.substring(prefix.length()).equals(s.substring(0, i))) {
                return true;
            }
        }

        return false;
    }

    private boolean solution2(String s, String goal) {
        return s.length() == goal.length() && (s + s).contains(goal);
    }

    // KMP (Knuth-Morris-Pratt) O(N)
    private boolean solution3(String s, String goal) {
        if (s.length() != goal.length()) {
            return false;
        }

        int[] longestProperPrefixAndSuffix = buildLongestProperPrefixAndSuffix(goal);

        int i = 0;
        int j = 0;
        String metaS = s + s;

        while(i < metaS.length()) {
            if (metaS.charAt(i) == goal.charAt(j)) {
                i++;
                j++;
            } else {
                if (j == 0) {
                    i++;
                } else {
                    j = longestProperPrefixAndSuffix[j - 1];
                }
            }

            if (j == goal.length()) {
                return true;
            }
        }

        return false;
    }

    private int[] buildLongestProperPrefixAndSuffix(String goal) {
        int[] longestProperPrefixAndSuffix = new int[goal.length()];
        // index 0 should always be ZERO
        int index = 1;
        int length = 0;

        while (index < goal.length()) {
            if (goal.charAt(index) == goal.charAt(length)) {
                longestProperPrefixAndSuffix[index++] = ++length;
            } else {
                if (length == 0) {
                    index++;
                } else {
                    length = longestProperPrefixAndSuffix[length - 1];
                }
            }
        }

        return longestProperPrefixAndSuffix;
    }
}
