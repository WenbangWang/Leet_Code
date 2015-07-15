package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given an array of strings, return all groups of strings that are anagrams.
 *
 * Note: All inputs will be in lower-case.
 */
public class No49 {

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