package com.wwb.leetcode.medium;

/**
 * Given a string s, return the number of palindromic substrings in it.
 * <p>
 * A string is a palindrome when it reads the same backward as forward.
 * <p>
 * A substring is a contiguous sequence of characters within the string.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: s = "abc"
 * Output: 3
 * Explanation: Three palindromic strings: "a", "b", "c".
 * </pre>
 *
 * <pre>
 * Example 2:
 *
 * Input: s = "aaa"
 * Output: 6
 * Explanation: Six palindromic strings: "a", "a", "a", "aa", "aa", "aaa".
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * 1 <= s.length <= 1000
 * s consists of lowercase English letters.
 * </pre>
 */
public class No647 {
    public int countSubstrings(String s) {
        return solution1(s);
    }

    private int solution1(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        int result = 0;

        for (int i = 0; i < s.length(); i++) {
            for (int lengthOfSubstring = 1;
                 lengthOfSubstring <= s.length() && lengthOfSubstring + i <= s.length();
                 lengthOfSubstring++) {
                if (isPalindrome(s, i, lengthOfSubstring + i - 1)) {
                    result++;
                }
            }
        }

        return result;
    }

    private int solution2(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        int result = 0;

        for (int mid = 0; mid < s.length(); mid++) {
            // odd length
            result += countPalindrome(s, mid, mid);
            // even length
            result += countPalindrome(s, mid, mid + 1);
        }

        return result;
    }

    private boolean isPalindrome(String s, int start, int end) {
        while (start < end) {
            if (s.charAt(start) != s.charAt(end)) {
                return false;
            }

            start++;
            end--;
        }

        return true;
    }

    private int countPalindrome(String s, int start, int end) {
        int count = 0;

        while (start >= 0 && end < s.length() && s.charAt(start) == s.charAt(end)) {
            count++;
            start--;
            end++;
        }

        return count;
    }
}
