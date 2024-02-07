package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a list of unique words, return all the pairs of the distinct indices (i, j) in the given list,
 * so that the concatenation of the two words words[i] + words[j] is a palindrome.
 *
 *
 *
 * Example 1:
 *
 * Input: words = ["abcd","dcba","lls","s","sssll"]
 * Output: [[0,1],[1,0],[3,2],[2,4]]
 * Explanation: The palindromes are ["dcbaabcd","abcddcba","slls","llssssll"]
 * Example 2:
 *
 * Input: words = ["bat","tab","cat"]
 * Output: [[0,1],[1,0]]
 * Explanation: The palindromes are ["battab","tabbat"]
 * Example 3:
 *
 * Input: words = ["a",""]
 * Output: [[0,1],[1,0]]
 *
 *
 * Constraints:
 *
 * 1 <= words.length <= 5000
 * 0 <= words[i].length <= 300
 * words[i] consists of lower-case English letters.
 */
public class No336 {
    public List<List<Integer>> palindromePairs(String[] words) {
        return solution1(words);
    }

    // TLE O(N^2 * K) k is the length of the word
    private List<List<Integer>> solution1(String[] words) {
        if (words == null || words.length <= 1) {
            return Collections.emptyList();
        }

        List<List<Integer>> result = new ArrayList<>();

        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < words.length; j++) {
                if (i != j && isPalindrome(words[i] + words[j])) {
                    result.add(Arrays.asList(i, j));
                }
            }
        }

        return result;
    }

    // still fucking TLE but the time complexity should be O(K^2 * N)
    // and space complexity should be O(N)
    private List<List<Integer>> solution2(String[] words) {
        if (words == null || words.length <= 1) {
            return Collections.emptyList();
        }

        List<List<Integer>> result = new ArrayList<>();
        Map<String, Integer> wordToIndex = new HashMap<>();

        for (int i = 0; i < words.length; i++) {
            wordToIndex.put(words[i], i);
        }

        for (int i = 0; i < words.length; i++) {
            var word = words[i];
            var reversedWord = new StringBuilder(word).reverse().toString();

            if (wordToIndex.containsKey(reversedWord) && wordToIndex.get(reversedWord) != i) {
                result.add(Arrays.asList(i, wordToIndex.get(reversedWord)));
            }

            if (!word.isEmpty() && isPalindrome(word) && wordToIndex.containsKey("")) {
                result.add(Arrays.asList(i, wordToIndex.get("")));
                result.add(Arrays.asList(wordToIndex.get(""), i));
            }

            for (int j = 1; j < word.length(); j++) {
                var prefix =  word.substring(0, j);
                var reversedPrefix = new StringBuilder(prefix).reverse().toString();
                var suffix = word.substring(j);
                var reversedSuffix = new StringBuilder(suffix).reverse().toString();

                if (wordToIndex.containsKey(reversedSuffix) && isPalindrome(prefix)) {
                    result.add(Arrays.asList(wordToIndex.get(reversedSuffix), i));
                }

                if (wordToIndex.containsKey(reversedPrefix) && isPalindrome(suffix)) {
                    result.add(Arrays.asList(i, wordToIndex.get(reversedPrefix)));
                }
            }
        }

        return result;
    }

    public List<List<Integer>> solution3(String[] words) {
        // trie is built in word's reversed order
        TrieNode trie = new TrieNode();

        // Build the Trie
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String reversedWord = new StringBuilder(word).reverse().toString();
            TrieNode currentTrieLevel = trie;
            for (int j = 0; j < reversedWord.length(); j++) {
                // current word starts with a palindrome end at current position
                if (hasPalindromeRemaining(reversedWord, j)) {
                    currentTrieLevel.palindromePrefixRemaining.add(i);
                }
                Character c = reversedWord.charAt(j);

                currentTrieLevel.children.putIfAbsent(c, new TrieNode());
                currentTrieLevel = currentTrieLevel.children.get(c);
            }
            currentTrieLevel.wordEnding = i;
        }

        // Find pairs
        List<List<Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            TrieNode currentTrieLevel = trie;
            for (int j = 0; j < word.length(); j++) {
                // Check for pairs of case 3.
                if (currentTrieLevel.wordEnding != -1 && hasPalindromeRemaining(word, j)) {
                    pairs.add(Arrays.asList(i, currentTrieLevel.wordEnding));
                }
                // Move down to the next trie level.
                Character c = word.charAt(j);
                currentTrieLevel = currentTrieLevel.children.get(c);
                if (currentTrieLevel == null) {
                    break;
                }
            }
            if (currentTrieLevel == null) {
                continue;
            }

            // Check for pairs of case 1. Note the check to prevent non distinct pairs.
            if (currentTrieLevel.wordEnding != -1 && currentTrieLevel.wordEnding != i) {
                pairs.add(Arrays.asList(i, currentTrieLevel.wordEnding));
            }
            // Check for pairs of case 2.
            for (int other : currentTrieLevel.palindromePrefixRemaining) {
                pairs.add(Arrays.asList(i, other));
            }
        }

        return pairs;
    }

    private static class TrieNode {
        int wordEnding = -1; // We'll use -1 to mean there's no word ending here.
        Map<Character, TrieNode> children = new HashMap<>();
        List<Integer> palindromePrefixRemaining = new ArrayList<>();
    }

    private boolean isPalindrome(String s) {
        return hasPalindromeRemaining(s, 0);
    }

    private boolean hasPalindromeRemaining(String s, int start) {
        int end = s.length() - 1;

        while (start < end) {
            if (s.charAt(start) != s.charAt(end)) {
                return false;
            }

            start++;
            end--;
        }

        return true;
    }
}
