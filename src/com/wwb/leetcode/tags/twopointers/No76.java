package com.wwb.leetcode.tags.twopointers;

import java.util.HashMap;
import java.util.Map;

/**
 * Given a string S and a string T, find the minimum window in S
 * which will contain all the characters in T in complexity O(n).
 *
 * For example,
 * S = "ADOBECODEBANC"
 * T = "ABC"
 * Minimum window is "BANC".
 *
 * Note:
 * If there is no such window in S that covers all characters in T, return the empty string "".
 *
 * If there are multiple such windows, you are guaranteed
 * that there will always be only one unique minimum window in S.
 */
public class No76 {

    public String minWindow(String s, String t) {
        if(s == null || s.isEmpty() || t == null || t.isEmpty()) {
            return "";
        }

        int sLength = s.length();
        int tLength = t.length();
        int minLength = sLength;
        int minStart = 0;
        int minEnd = sLength - 1;
        int count = tLength;
        int start = 0;
        Map<Character, Integer> map = new HashMap<>();

        for(char c : t.toCharArray()) {
            map.put(c, map.containsKey(c) ? map.get(c) + 1 : 1);
        }

        for(int end = 0; end < sLength; end++) {
            char endChar = s.charAt(end);

            if(map.containsKey(endChar)) {
                if(map.get(endChar) > 0) {
                    count--;
                }

                map.put(endChar, map.get(endChar) - 1);
            }

            if(count == 0) {
                char startChar = s.charAt(start);

                while(!map.containsKey(startChar) || map.get(startChar) < 0) {
                    if(map.containsKey(startChar)) {
                        map.put(startChar, map.get(startChar) + 1);
                    }

                    startChar = s.charAt(++start);
                }

                if(minLength > end - start + 1) {
                    minStart = start;
                    minEnd = end;
                    minLength = end - start + 1;
                }
            }
        }

        if(count != 0) {
            return "";
        }

        return s.substring(minStart, minEnd + 1);
    }
}