package com.wwb.leetcode.tags.dp;

import java.util.HashMap;
import java.util.Map;

/**
 * Given a string s1, we may represent it as a binary tree by partitioning it to two non-empty substrings recursively.
 *
 * Below is one possible representation of s1 = "great":
 *
 *     great
 *    /    \
 *   gr    eat
 *  / \    /  \
 * g   r  e   at
 *           / \
 *          a   t
 * To scramble the string, we may choose any non-leaf node and swap its two children.
 *
 * For example, if we choose the node "gr" and swap its two children, it produces a scrambled string "rgeat".
 *
 *     rgeat
 *    /    \
 *   rg    eat
 *  / \    /  \
 * r   g  e   at
 *           / \
 *          a   t
 * We say that "rgeat" is a scrambled string of "great".
 *
 * Similarly, if we continue to swap the children of nodes "eat" and "at", it produces a scrambled string "rgtae".
 *
 *     rgtae
 *    /    \
 *   rg    tae
 *  / \    /  \
 * r   g  ta  e
 *           / \
 *          t   a
 * We say that "rgtae" is a scrambled string of "great".
 *
 * Given two strings s1 and s2 of the same length, determine if s2 is a scrambled string of s1.
 */
public class No87 {

    public boolean isScramble(String s1, String s2) {
        Map<String, String> map = new HashMap<>();

        return isScramble(s1, s2, map);
    }

    private boolean isScramble(String s1, String s2, Map<String, String> map) {
        if(s2.equals(map.get(s1))) {
            return true;
        }

        if(s1.equals(s2)) {
            return true;
        }

        if(!isAnagram(s1, s2)) {
            return false;
        }

        for(int i = 1, length = s1.length(); i < length; i++) {
            if(isScramble(s1.substring(0, i), s2.substring(0, i), map) && isScramble(s1.substring(i), s2.substring(i), map) ||
                isScramble(s1.substring(0, i), s2.substring(length - i), map) && isScramble(s1.substring(i), s2.substring(0, length - i), map)) {
                map.put(s1, s2);
                return true;
            }
        }

        return false;
    }

    private boolean isAnagram(String s1, String s2) {
        if(s1.equals(s2)) {
            return true;
        }

        if(s1.length() != s2.length()) {
            return false;
        }

        Map<Character, Integer> map = new HashMap<>();

        for(char c : s1.toCharArray()) {
            Integer count = map.get(c);

            map.put(c, count == null ? 1 : ++count);
        }

        for(char c : s2.toCharArray()) {
            Integer count = map.get(c);

            if(count == null) {
                return false;
            }

            map.put(c, --count);
        }

        for(int count : map.values()) {
            if(count != 0) {
                return false;
            }
        }

        return true;
    }
}
