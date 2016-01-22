package com.wwb.leetcode.tags.string;

import java.util.*;

/**
 * You are given a string, s, and a list of words, words, that are all of the same length.
 * Find all starting indices of substring(s) in s that is a concatenation of each word in words exactly once
 * and without any intervening characters.
 *
 * For example, given:
 * s: "barfoothefoobarman"
 * words: ["foo", "bar"]
 *
 * You should return the indices: [0,9].
 * (order does not matter).
 */
public class No30 {

    public List<Integer> findSubstring(String s, String[] words) {
        if(s == null || s.isEmpty() || words == null || words.length == 0) {
            return Collections.emptyList();
        }

        int singleWordLength = words[0].length();
        int permutationLength = words.length * singleWordLength;
        int length = s.length();
        Map<String, Integer> map = new HashMap<>();
        List<Integer> result = new ArrayList<>();

        for(String word : words) {
            map.put(word, map.containsKey(word) ? map.get(word) + 1 : 1);
        }

        for(int i = 0, start = 0; i < singleWordLength && start + permutationLength <= length; start = ++i) {
            Map<String, Integer> tempMap = new HashMap<>();

            for(int j = i; j <= length - singleWordLength && start + permutationLength <= length; j += singleWordLength) {
                String word = s.substring(j, j + singleWordLength);

                if(map.containsKey(word)) {
                    if(!tempMap.containsKey(word) || tempMap.get(word) < map.get(word)) {
                        tempMap.put(word, tempMap.containsKey(word) ? tempMap.get(word) + 1 : 1);

                        //Found
                        if(start + permutationLength == j + singleWordLength) {
                            result.add(start);
                            decreaseCount(tempMap, s.substring(start, start + singleWordLength));
                            //Move one word forward
                            start += singleWordLength;
                        }
                    } else {
                        //Enough "word" in tempMap then slide the window forward till the first word same as "word"
                        String temp;
                        while(!(temp = s.substring(start, start + singleWordLength)).equals(word)) {
                            decreaseCount(tempMap, temp);
                            start += singleWordLength;
                        }

                        //Skip "word"
                        start += singleWordLength;
                    }
                } else {
                    //Not in the map then move one word forward and clear the map
                    start = j + singleWordLength;
                    tempMap.clear();
                }
            }
        }

        return result;
    }

    private void decreaseCount(Map<String, Integer> map, String key) {
        int value = map.get(key);
        if (value == 1) {
            map.remove(key);
        } else {
            map.put(key, value - 1);
        }
    }
}
