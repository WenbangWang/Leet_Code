package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given an array of strings, group anagrams together.
 *
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
 */
public class No49 {

    public List<List<String>> groupAnagrams(String[] strs) {
        if(strs == null || strs.length == 0) {
            return Collections.emptyList();
        }

        Arrays.sort(strs);
        Map<String, List<String>> sortedStrs = new HashMap<>();

        for(String str : strs) {
            char[] charArray = str.toCharArray();
            Arrays.sort(charArray);
            String sortedStr = new String(charArray);

            if (!sortedStrs.containsKey(sortedStr)) {
                sortedStrs.put(sortedStr, new ArrayList<>());
            }
            sortedStrs.get(sortedStr).add(str);
        }

        return new ArrayList<>(sortedStrs.values());
    }

    public List<String> anagrams(String[] strs) {
        List<String> result = new ArrayList<>();
        Map<String, List<String>> sortedStrs = new HashMap<>();

        for(String str : strs) {
            char[] charArray = str.toCharArray();
            Arrays.sort(charArray);
            String sortedStr = new String(charArray);
            List<String> anagrams = sortedStrs.get(sortedStr);

            if(anagrams == null) {
                anagrams = new ArrayList<>();
                anagrams.add(str);
                sortedStrs.put(sortedStr, anagrams);
            } else {
                anagrams.add(str);
            }
        }

        for(Map.Entry<String, List<String>> entry : sortedStrs.entrySet()) {
            List<String> anagrams = entry.getValue();
            int size = anagrams.size();

            if(size > 1) {
                result.addAll(anagrams);
            }
        }

        return result;
    }
}
