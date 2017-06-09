package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.Map;

/**
 * Given a string, find the length of the longest substring T that contains at most 2 distinct characters.
 *
 * For example, Given s = “eceba”,
 *
 * T is "ece" which its length is 3.
 */
public class No159 {
    public int lengthOfLongestSubstringTwoDistinct(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        Map<Character, Integer> windowWithTwoUniqueCharacters = new HashMap<>();
        int start = 0;
        int end = 0;
        int length = s.length();
        int maxLength = Integer.MIN_VALUE;

        while (end < length) {
            if (windowWithTwoUniqueCharacters.size() <= 2) {
                char c = s.charAt(end);
                windowWithTwoUniqueCharacters.put(c, end++);
            }

            if (windowWithTwoUniqueCharacters.size() > 2) {
                int leftMost = length;

                for (int index : windowWithTwoUniqueCharacters.values()) {
                    leftMost = Math.min(leftMost, index);
                }

                char c = s.charAt(leftMost);
                windowWithTwoUniqueCharacters.remove(c);
                start = leftMost + 1;

            }

            maxLength = Math.max(maxLength, end - start);
        }

        return maxLength;
    }
}
