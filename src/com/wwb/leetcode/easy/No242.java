package com.wwb.leetcode.easy;

/**
 * Given two strings s and t, write a function to determine if t is an anagram of s.
 *
 * For example,
 * s = "anagram", t = "nagaram", return true.
 * s = "rat", t = "car", return false.
 *
 * Note:
 * You may assume the string contains only lowercase alphabets.
 */
public class No242 {

    public boolean isAnagram(String s, String t) {
        if(s == null && t == null) {
            return true;
        }
        if(s == null || t == null) {
            return false;
        }

        if(s.isEmpty() && t.isEmpty()) {
            return true;
        }

        if(s.length() != t.length()) {
            return false;
        }

        int[] chars = new int[26];

        for(char c : s.toCharArray()) {
            chars[c - 'a']++;
        }

        for(char c : t.toCharArray()) {
            chars[c - 'a']--;
        }

        for(int i : chars) {
            if(i != 0) {
                return false;
            }
        }

        return true;
    }
}
