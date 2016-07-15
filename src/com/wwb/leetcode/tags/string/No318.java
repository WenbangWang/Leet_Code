package com.wwb.leetcode.tags.string;

/**
 * Given a string array words, find the maximum value of length(word[i]) * length(word[j])
 * where the two words do not share common letters. You may assume that each word will contain only lower case letters.
 * If no such two words exist, return 0.
 *
 * Example 1:
 * Given ["abcw", "baz", "foo", "bar", "xtfn", "abcdef"]
 * Return 16
 * The two words can be "abcw", "xtfn".
 *
 * Example 2:
 * Given ["a", "ab", "abc", "d", "cd", "bcd", "abcd"]
 * Return 4
 * The two words can be "ab", "cd".
 *
 * Example 3:
 * Given ["a", "aa", "aaa", "aaaa"]
 * Return 0
 * No such pair of words.
 */
public class No318 {

    public int maxProduct(String[] words) {
        if(words == null || words.length == 0) {
            return 0;
        }

        int max = 0;
        int length = words.length;
        int[] mask = new int[length];

        for(int i = 0; i < length; i++) {
            for(char c : words[i].toCharArray()) {
                mask[i] |= 1 << (c - 'a');
            }
        }

        for(int i = 0; i < length; i++) {
            for(int j =  i + 1; j < length; j++) {
                if((mask[i] & mask[j]) == 0) {
                    max = Math.max(max, words[i].length() * words[j].length());
                }
            }
        }

        return max;
    }
}
