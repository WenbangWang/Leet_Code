package com.wwb.leetcode.tags.hashtable;

import java.util.HashMap;
import java.util.Map;

/**
 * Given a pattern and a string str, find if str follows the same pattern.
 *
 * Here follow means a full match, such that there is a bijection between a letter in pattern and a non-empty word in str.
 *
 * Examples:
 * pattern = "abba", str = "dog cat cat dog" should return true.
 * pattern = "abba", str = "dog cat cat fish" should return false.
 * pattern = "aaaa", str = "dog cat cat dog" should return false.
 * pattern = "abba", str = "dog dog dog dog" should return false.
 * Notes:
 * You may assume pattern contains only lowercase letters, and str contains lowercase letters separated by a single space.
 */
public class No290 {

    public boolean wordPattern(String pattern, String str) {
        if(pattern == null || pattern.isEmpty() || str == null || str.isEmpty()) {
            return false;
        }

        String[] words = str.split("\\s+");

        if(pattern.length() != words.length) {
            return false;
        }

        Map<Character, String> keyValueMap = new HashMap<>();
        Map<String, Character> valueKeyMap = new HashMap<>();

        for(int i = 0; i < words.length; i++) {
            String word = words[i];
            char c = pattern.charAt(i);

            if(keyValueMap.containsKey(c) && !keyValueMap.get(c).equals(word)) {
                return false;
            }

            if(valueKeyMap.containsKey(word) && valueKeyMap.get(word) != c) {
                return false;
            }

            keyValueMap.put(c, word);
            valueKeyMap.put(word, c);
        }

        return true;
    }
}
