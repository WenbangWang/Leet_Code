package com.wwb.leetcode.tags.string;

import java.util.Arrays;

/**
 * Write a function to find the longest common prefix string amongst an array of strings.
 */
public class No14 {

    public String longestCommonPrefix(String[] strs) {
        return solution1(strs);
    }

    private String solution1(String[] strs) {
        if(strs == null || strs.length == 0) {
            return "";
        }

        for(int i = 0; i < strs[0].length(); i++) {
            char c = strs[0].charAt(i);

            for(int j = 1; j < strs.length; j++) {
                if(i == strs[j].length() || strs[j].charAt(i) != c) {
                    return strs[0].substring(0, i);
                }
            }
        }

        return strs[0];
    }

    private String solution2(String[] strs) {
        if(strs == null || strs.length == 0) {
            return "";
        }

        Arrays.sort(strs);

        int index = 0;

        while (index < strs[0].length() && index < strs[strs.length - 1].length()) {
            if (strs[0].charAt(index) != strs[strs.length - 1].charAt(index)) {
                break;
            }

            index++;
        }

        return strs[0].substring(0, index);
    }
}
