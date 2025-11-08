package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given an array of strings, group anagrams together.
 * <p>
 *
 * <pre>
 * For example, given: ["eat", "tea", "tan", "ate", "nat", "bat"],
 * Return:
 *
 * [
 *   ["ate", "eat","tea"],
 *   ["nat","tan"],
 *   ["bat"]
 *   ]
 * Note:
 * For the return value, each inner list's elements must follow the lexicographic order.
 * All inputs will be in lower-case.
 * </pre>
 */
public class No49 {

    public List<List<String>> groupAnagrams(String[] strs) {
        if(strs == null || strs.length == 0) {
            return Collections.emptyList();
        }

        Map<String, List<String>> sortedStrs = new HashMap<>();

        for(String str : strs) {
            String sortedStr = sort2(str);

            sortedStrs.putIfAbsent(sortedStr, new ArrayList<>());

            sortedStrs.get(sortedStr).add(str);
        }

        return new ArrayList<>(sortedStrs.values());
    }

    private String sort1(String s) {
        char[] charArray = s.toCharArray();
        Arrays.sort(charArray);
        return new String(charArray);
    }

    private String sort2(String s) {
        int[] bucket = new int[26];

        for (char c : s.toCharArray()) {
            bucket[c - 'a']++;
        }

        StringBuilder sb = new StringBuilder(s.length());

        for (int i = 0; i < 26; i++) {
            sb.append(String.valueOf(i + 'a').repeat(bucket[i]));
        }

        return sb.toString();
    }
}
