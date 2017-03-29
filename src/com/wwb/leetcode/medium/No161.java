package com.wwb.leetcode.medium;

/**
 * Given two strings S and T, determine if they are both one edit distance apart.
 */
public class No161 {
    public boolean isOneEditDistance(String s, String t) {
        for(int i = 0; i < Math.min(s.length(), t.length()); i++) {
            if(s.charAt(i) != t.charAt(i)) {
                if(s.length() == t.length()) {
                    // replace the current char
                    return s.substring(i + 1).equals(t.substring(i + 1));
                } else if (s.length() < t.length()) {
                    // delete a char from t
                    return s.substring(i).equals(t.substring(i + 1));
                } else {
                    // delete a char from s
                    return s.substring(i + 1).equals(t.substring(i));
                }
            }
        }

        return Math.abs(s.length() - t.length()) == 1;
    }
}
