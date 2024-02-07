package com.wwb.leetcode.easy;

/**
 * Given a string s, check if it can be constructed by taking a substring of it
 * and appending multiple copies of the substring together.
 *
 *
 *
 * Example 1:
 *
 * Input: s = "abab"
 * Output: true
 * Explanation: It is the substring "ab" twice.
 * Example 2:
 *
 * Input: s = "aba"
 * Output: false
 * Example 3:
 *
 * Input: s = "abcabcabcabc"
 * Output: true
 * Explanation: It is the substring "abc" four times or the substring "abcabc" twice.
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 10^4
 * s consists of lowercase English letters.
 */
public class No459 {
    public boolean repeatedSubstringPattern(String s) {
        return solution1(s);
    }

    private boolean solution1(String s) {
        int length = s.length();

        if (length == 1) {
            return true;
        }

        StringBuilder substring = new StringBuilder(length / 2);

        for (int i = 0; i < length / 2; i++) {
            substring.append(s.charAt(i));

            int mod = (length - i - 1) % (i + 1);

            if (mod != 0) {
                continue;
            }

            if (substring.toString().repeat((length - i - 1) / (i + 1)).equals(s.substring(i + 1))) {
                return true;
            }
        }

        return false;
    }

    // https://leetcode.com/problems/repeated-substring-pattern/
    private boolean solution2(String s) {
        return (s + s).substring(1, s.length() * 2 - 1).contains(s);
    }
}
