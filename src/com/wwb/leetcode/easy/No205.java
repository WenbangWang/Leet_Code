package com.wwb.leetcode.easy;

import java.util.HashMap;
import java.util.Map;

/**
 * Given two strings s and t, determine if they are isomorphic.
 * <p>
 * Two strings are isomorphic if the characters in s can be replaced to get t.
 * <p>
 * All occurrences of a character must be replaced with another character while preserving the order of characters.
 * No two characters may map to the same character but a character may map to itself.
 * <p>
 * For example,
 * Given "egg", "add", return true.
 * <p>
 * Given "foo", "bar", return false.
 * <p>
 * Given "paper", "title", return true.
 * <p>
 * Note:
 * You may assume both s and t have the same length.
 */
public class No205 {

    public boolean isIsomorphic(String s, String t) {
        if (s == null || t == null) {
            return false;
        }

        Map<Character, Character> map = new HashMap<>();
        char[] firstCharArray = s.toCharArray();
        char[] secondCharArray = t.toCharArray();

        for (int i = 0; i < firstCharArray.length; i++) {
            char firstChar = firstCharArray[i];
            char secondChar = secondCharArray[i];

            if (map.containsKey(firstChar)) {
                if (!map.get(firstChar).equals(secondChar)) {
                    return false;
                }
            } else {
                if (map.containsValue(secondChar)) {
                    return false;
                }

                map.put(firstChar, secondChar);
            }
        }

        return true;
    }
}
