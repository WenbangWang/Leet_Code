package com.wwb.leetcode.easy;

/**
 * Given two strings s and t, return true if they are equal when both are typed into empty text editors.
 * '#' means a backspace character.
 *
 * Note that after backspacing an empty text, the text will continue empty.
 *
 *
 *
 * Example 1:
 *
 * Input: s = "ab#c", t = "ad#c"
 * Output: true
 * Explanation: Both s and t become "ac".
 * Example 2:
 *
 * Input: s = "ab##", t = "c#d#"
 * Output: true
 * Explanation: Both s and t become "".
 * Example 3:
 *
 * Input: s = "a#c", t = "b"
 * Output: false
 * Explanation: s becomes "c" while t becomes "b".
 *
 *
 * Constraints:
 *
 * 1 <= s.length, t.length <= 200
 * s and t only contain lowercase letters and '#' characters.
 *
 *
 * Follow up: Can you solve it in O(n) time and O(1) space?
 */
public class No844 {
    public boolean backspaceCompare(String s, String t) {
        int i = s.length() - 1;
        int j = t.length() - 1;
        // number of '#'
        int count1 = 0;
        int count2 = 0;

        while (i >= 0 || j >= 0) {
            while (i >= 0 && (s.charAt(i) == '#' || count1 > 0)) {
                count1 += s.charAt(i) == '#' ? 1 : -1;

                i--;
            }

            while (j >= 0 && (t.charAt(j) == '#' || count2 > 0)) {
                count2 += s.charAt(j) == '#' ? 1 : -1;

                j--;
            }

            if (i >= 0 && j >= 0 && s.charAt(i) == t.charAt(j)) {
                i--;
                j--;
            } else {
                break;
            }
        }

        return i == -1 && j == -1;
    }
}
