package com.wwb.leetcode.tags.string;

/**
 * Given a string s consists of upper/lower-case alphabets and empty space characters ' ',
 * return the length of last word in the string.
 *
 * If the last word does not exist, return 0.
 *
 * Note: A word is defined as a character sequence consists of non-space characters only.
 *
 * For example,
 * Given s = "Hello World",
 * return 5.
 */
public class No58 {

    public int lengthOfLastWord(String s) {
        if(s == null || s.isEmpty()) {
            return 0;
        }

        int last = 0;
        char[] charArray = s.trim().toCharArray();

        for(char c : charArray) {
            if(c == ' ') {
                last = 0;
            } else {
                last++;
            }
        }

        return last;
    }
}
