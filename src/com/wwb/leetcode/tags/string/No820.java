package com.wwb.leetcode.tags.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A valid encoding of an array of words is any reference string s and array of indices indices such that:
 *
 * words.length == indices.length
 * The reference string s ends with the '#' character.
 * For each index indices[i], the substring of s starting from indices[i]
 * and up to (but not including) the next '#' character is equal to words[i].
 * Given an array of words, return the length of the shortest reference string s possible of any valid encoding of words.
 *
 *
 *
 * Example 1:
 *
 * Input: words = ["time", "me", "bell"]
 * Output: 10
 * Explanation: A valid encoding would be s = "time#bell#" and indices = [0, 2, 5].
 * words[0] = "time", the substring of s starting from indices[0] = 0 to the next '#' is underlined in "time#bell#"
 * words[1] = "me", the substring of s starting from indices[1] = 2 to the next '#' is underlined in "time#bell#"
 * words[2] = "bell", the substring of s starting from indices[2] = 5 to the next '#' is underlined in "time#bell#"
 * Example 2:
 *
 * Input: words = ["t"]
 * Output: 2
 * Explanation: A valid encoding would be s = "t#" and indices = [0].
 *
 *
 * Constraints:
 *
 * 1 <= words.length <= 2000
 * 1 <= words[i].length <= 7
 * words[i] consists of only lowercase letters.
 */
public class No820 {
    public int minimumLengthEncoding(String[] words) {
        return solution1(words);
    }

    private int solution1(String[] words) {
        Trie trie = new Trie();

        for (String word : words) {
            trie.add(word);
        }

        List<Integer> result = trie.wordsLength();

        return result.stream().mapToInt(Integer::intValue).sum();
    }

    private int solution2(String[] words) {
        Set<String> set = new HashSet<>(Arrays.asList(words));

        for (String word : words) {
            for (int i = 1; i < word.length(); i++) {
                set.remove(word.substring(i));
            }
        }

        return set.stream().mapToInt(String::length).sum() + set.size();
    }

    private static class Trie {
        private Node root;

        Trie() {
            this.root = new Node();
        }

        void add(String word) {
            Node node = this.root;

            for (int i = word.length() - 1; i >= 0; i--) {
                if (node.children[word.charAt(i) - 'a'] == null) {
                    node.children[word.charAt(i) - 'a'] = new Node(word.charAt(i));
                }

                node = node.children[word.charAt(i) - 'a'];
            }
        }

        List<Integer> wordsLength() {
            return this.wordsLength(this.root);
        }

        private List<Integer> wordsLength(Node node) {
            List<Integer> result = new ArrayList<>();

            for (Node child : node.children) {
                if (child != null) {
                    List<Integer> childResult = this.wordsLength(child);

                    childResult.replaceAll(i -> i + 1);

                    result.addAll(childResult);
                }
            }

            if (result.isEmpty()) {
                result.add(1);
            }

            return result;
        }
    }

    private static class Node {
        char c;
        Node[] children;

        Node() {
            this.children = new Node[26];
        }

        Node(char c) {
            this();
            this.c = c;
        }
    }
}
