package com.wwb.leetcode.tags.twopointers;

/**
 * Given a string, determine if it is a palindrome, considering only alphanumeric characters and ignoring cases.
 *
 * For example,
 * "A man, a plan, a canal: Panama" is a palindrome.
 * "race a car" is not a palindrome.
 *
 * Note:
 * Have you consider that the string might be empty? This is a good question to ask during an interview.
 *
 * For the purpose of this problem, we define empty string as valid palindrome.
 */
public class No125 {

    public boolean isPalindrome(String s) {
        if(s == null || s.isEmpty()) {
            return true;
        }

        s = s.toLowerCase().replaceAll("[^0-9a-z]", "");
        char[] chars = s.toCharArray();

        for(int i = 0, length = chars.length; i < length / 2; i++) {
            if(chars[i] != chars[length - 1 - i]) {
                return false;
            }
        }

        return true;
    }
}
