package com.wwb.leetcode.medium;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
}
