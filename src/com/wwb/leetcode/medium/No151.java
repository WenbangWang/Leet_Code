package com.wwb.leetcode.medium;

/**
 * Given an input string, reverse the string word by word.
 *
 * For example,
 * Given s = "the sky is blue",
 * return "blue is sky the".
 */
public class No151 {

    public String reverseWords(String s) {
        if(s == null || s.isEmpty()) {
            return s;
        }

        StringBuilder stringBuilder = new StringBuilder();
        String[] words = s.split("\\s+");
        int length = words.length;

        for(int i = length - 1; i >= 0; i--) {
            stringBuilder.append(words[i]).append(" ");
        }

        return stringBuilder.toString().trim();
    }
}