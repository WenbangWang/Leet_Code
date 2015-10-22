package com.wwb.leetcode.hard;

import java.util.*;

/**
 * Given a string s and a dictionary of words dict, add spaces in s to construct a sentence where each word is a valid dictionary word.
 *
 * Return all such possible sentences.
 *
 * For example, given
 * s = "catsanddog",
 * dict = ["cat", "cats", "and", "sand", "dog"].
 *
 * A solution is ["cats and dog", "cat sand dog"].
 */
public class No140 {

    public List<String> wordBreak(String s, Set<String> wordDict) {
        if(s == null || s.isEmpty() || wordDict == null) {
            return Collections.emptyList();
        }


        return wordBreak(s, wordDict, new HashMap<String, List<String>>());
    }

    private List<String> wordBreak(String s, Set<String> wordDict, Map<String, List<String>> map) {
        if(map.containsKey(s)) {
            return map.get(s);
        }

        List<String> result = new ArrayList<>();

        if(wordDict.contains(s)) {
            result.add(s);
        }

        for(int i = 1, length = s.length(); i < length; i++) {
            String firstHalf = s.substring(0, i);

            if(wordDict.contains(firstHalf)) {
                String secondHalf = s.substring(i);

                for(String subResult : wordBreak(secondHalf, wordDict, map)) {
                    result.add(firstHalf + " " + subResult);
                }
            }
        }

        map.put(s, result);

        return result;
    }
}
