package com.wwb.leetcode.medium;

import java.util.HashMap;

/**
 * Given a string, find the length of the longest substring without repeating characters.
 * For example, the longest substring without repeating letters for "abcabcbb" is "abc",
 * which the length is 3. For "bbbbb" the longest substring is "b", with the length of 1.
 */
public class No3 {

    public int lengthOfLongestSubstring(String s) {
        if(s == null || s.length() == 0) {
            return 0;
        }

        char[] charArray = s.toCharArray();
        HashMap<Character, Integer> map = new HashMap<>();
        int maxLength = 1;

        for(int i = 0, j = 0; i < charArray.length; i++) {
            if(map.containsKey(charArray[i])) {
                j = Math.max(j, map.get(charArray[i]) + 1);
            }

            map.put(charArray[i], i);
            maxLength = Math.max(maxLength, i - j + 1);
        }

        return maxLength;
    }
}