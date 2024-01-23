package com.wwb.leetcode.tags.trie;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Queue;

/**
 * Given an array of strings words representing an English Dictionary,
 * return the longest word in words that can be built one character at a time by other words in words.
 *
 * If there is more than one possible answer, return the longest word with the smallest lexicographical order.
 * If there is no answer, return the empty string.
 *
 * Note that the word should be built from left to right with each additional character
 * being added to the end of a previous word.
 *
 *
 *
 * Example 1:
 *
 * Input: words = ["w","wo","wor","worl","world"]
 * Output: "world"
 * Explanation: The word "world" can be built one character at a time by "w", "wo", "wor", and "worl".
 * Example 2:
 *
 * Input: words = ["a","banana","app","appl","ap","apply","apple"]
 * Output: "apple"
 * Explanation: Both "apply" and "apple" can be built from other words in the dictionary. However, "apple"
 * is lexicographically smaller than "apply".
 *
 *
 * Constraints:
 *
 * 1 <= words.length <= 1000
 * 1 <= words[i].length <= 30
 * words[i] consists of lowercase English letters.
 */
public class No720 {
    public String longestWord(String[] words) {
        return solution1(words);
    }

    private String solution1(String[] words) {
        Arrays.sort(words, Comparator.comparingInt(String::length).thenComparing(a -> a));
        String longestWord = "";
        Trie trie = new Trie();

        for (String word : words) {
            TrieNode node = trie.addWord(word);

            if (node != null && (longestWord.isEmpty() || longestWord.length() < word.length())) {
                longestWord = word;
            }
        }

        return longestWord;
    }

    private String solution2(String[] words) {
        Queue<TrieNode> queue = new ArrayDeque<>();
        Trie trie = new Trie();
        String result = "";

        for (String word : words) {
            trie.insert(word);
        }

        queue.add(trie.root);

        while(!queue.isEmpty()) {
            TrieNode node = queue.poll();

            for (int i = 25; i >= 0; i--) {
                if (node.children[i] != null && node.children[i].word != null) {
                    result = node.children[i].word;
                    queue.offer(node.children[i]);
                }
            }
        }

        return result;
    }

    static class Trie {
        TrieNode root;

        Trie() {
            this.root = new TrieNode(' ', 0);
        }

        TrieNode addWord(String word) {
            return addWord(word, this.root, 0);
        }

        void insert(String word) {
            TrieNode node = this.root;

            for (int i = 0; i < word.length(); i++) {
                int charIndex = word.charAt(i) - 'a';

                if (node.children[charIndex] == null) {
                    node.children[charIndex] = new TrieNode(word.charAt(i), word.length());
                }

                node = node.children[charIndex];
            }

            node.word = word;
        }

        private TrieNode addWord(String word, TrieNode node, int level) {
            if (node.longestWordLength + 1 < word.length()) {
                return null;
            }

            if (level == word.length()) {
                return node;
            }

            int charIndex = word.charAt(level) - 'a';

            if (node == this.root && node.children[charIndex] == null && word.length() > 1) {
                return null;
            }

            if (node.children[charIndex] == null) {
                if (word.length() - level > 1) {
                    return null;
                }
                node.children[charIndex] = new TrieNode(word.charAt(level), word.length());
            }
            node.longestWordLength = Math.max(node.longestWordLength, word.length());

            return addWord(word, node.children[charIndex], level + 1);
        }
    }

    static class TrieNode {
        char value;
        TrieNode[] children;
        int longestWordLength;
        String word;

        TrieNode(char value, int longestWordLength) {
            this.value = value;
            this.longestWordLength = longestWordLength;
            this.children = new TrieNode[26];
        }
    }
}
