package com.wwb.leetcode.medium;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Given an input string, reverse the string word by word.
 * <p>
 * For example,
 * Given s = "the sky is blue",
 * return "blue is sky the".
 */
public class No151 {

    public String reverseWords(String s) {
        return solution1(s);
    }

    private String solution1(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        var words = Arrays.stream(s.split("\\s+"))
            .filter(Predicate.not(String::isEmpty))
            .collect(Collectors.toList());

        for (int i = 0; i < words.size() / 2; i++) {
            String temp = words.get(i);
            words.set(i, words.get(words.size() - 1 - i));
            words.set(words.size() - 1 - i, temp);
        }

        return String.join(" ", words);
    }

    private String solution2(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        char[] chars = s.toCharArray();
        int n = chars.length;

        // step 1. reverse the whole string
        reverse(chars, 0, n - 1);
        // step 2. reverse each word
        reverseWords(chars, n);
        // step 3. clean up spaces
        int end = cleanSpaces(chars, n);

        return new String(chars).substring(0, end);
    }

    void reverseWords(char[] chars, int n) {
        int start = 0;
        int end = 0;

        while (start < n) {
            // skip spaces
            while (start < end || start < n && chars[start] == ' ') {
                start++;
            }
            // skip non spaces
            while (end < start || end < n && chars[end] != ' ') {
                end++;
            }
            // reverse the word
            reverse(chars, start, end - 1);
        }
    }

    private int cleanSpaces(char[] chars, int n) {
        int start = 0;
        int end = 0;

        while (end < n) {
            // skip spaces
            while (end < n && chars[end] == ' ') {
                end++;
            }
            // keep non spaces
            while (end < n && chars[end] != ' ') {
                chars[start++] = chars[end++];
            }
            // skip spaces
            while (end < n && chars[end] == ' ') {
                end++;
            }
            // keep only one space
            if (end < n) {
                chars[start++] = ' ';
            }
        }

        return start;
    }

    private void reverse(char[] chars, int start, int end) {
        while (start < end) {
            char c = chars[start];
            chars[start++] = chars[end];
            chars[end--] = c;
        }
    }
}
