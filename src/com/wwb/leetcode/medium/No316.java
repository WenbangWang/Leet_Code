package com.wwb.leetcode.medium;

/**
 * Given a string which contains only lowercase letters, remove duplicate letters so that every letter appear once and only once.
 * You must make sure your result is the smallest in lexicographical order among all possible results.
 *
 * Example:
 * Given "bcabc"
 * Return "abc"
 *
 * Given "cbacdcbc"
 * Return "acdb"
 */
public class No316 {

    public String removeDuplicateLetters(String s) {
        if(s == null || s.isEmpty()) {
            return "";
        }

        int[] counts = new int[26];
        int firstPosition = 0;

        for(char c : s.toCharArray()) {
            counts[c - 'a']++;
        }

        for(int i = 0, length = s.length(); i < length; i++) {
            if(s.charAt(i) < s.charAt(firstPosition)) {
                firstPosition = i;
            }

            if(--counts[s.charAt(i) - 'a'] == 0) {
                break;
            }
        }

        return s.charAt(firstPosition) + removeDuplicateLetters(s.substring(firstPosition + 1).replaceAll(String.valueOf(s.charAt(firstPosition)), ""));
    }
}
