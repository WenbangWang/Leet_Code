package com.wwb.leetcode.easy;

/**
 * Given a string, determine if it is a palindrome, considering only alphanumeric characters and ignoring cases.
 * <p>
 * For example,
 * "A man, a plan, a canal: Panama" is a palindrome.
 * "race a car" is not a palindrome.
 * <p>
 * Note:
 * Have you consider that the string might be empty? This is a good question to ask during an interview.
 * <p>
 * For the purpose of this problem, we define empty string as valid palindrome.
 */
public class No125 {

    public boolean isPalindrome(String s) {
        return solution1(s);
    }

    private boolean solution1(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }

        s = s.toLowerCase().replaceAll("[^\\da-z]", "");
        char[] chars = s.toCharArray();

        for (int i = 0, length = chars.length; i < length / 2; i++) {
            if (chars[i] != chars[length - 1 - i]) {
                return false;
            }
        }

        return true;
    }

    private boolean solution2(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }

        s = s.toLowerCase();

        int start = 0;
        int end = s.length() - 1;

        while (start <= end) {
            if (!Character.isLetterOrDigit(s.charAt(start))) {
                start++;
            } else if (!Character.isLetterOrDigit(s.charAt(end))) {
                end--;
            } else if (s.charAt(start) != s.charAt(end)) {
                return false;
            } else {
                start++;
                end--;
            }
        }

        return true;
    }
}
