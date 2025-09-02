package com.wwb.leetcode.medium;

import java.util.HashMap;

/**
 * Given a string, find the length of the longest substring without repeating characters.
 * For example, the longest substring without repeating letters for "abcabcbb" is "abc",
 * which the length is 3. For "bbbbb" the longest substring is "b", with the length of 1.
 */
public class No3 {

    public int lengthOfLongestSubstring(String s) {
        if(s == null || s.isEmpty()) {
            return 0;
        }

        char[] charArray = s.toCharArray();
        HashMap<Character, Integer> map = new HashMap<>();
        int maxLength = 1;

        for(int end = 0, start = 0; end < charArray.length; end++) {
            // Reset the start of the window to the index of current char
            // as it is duplicated in the original substring
            if(map.containsKey(charArray[end])) {
                // map.get(charArray[end]) is the last index where character c appeared.
                // We only care if the duplicate is inside the current window.
                start = Math.max(start, map.get(charArray[end]) + 1);
            }

            map.put(charArray[end], end);
            maxLength = Math.max(maxLength, end - start + 1);
        }

        return maxLength;
    }
}
