package com.wwb.leetcode.hard;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * There is a new alien language which uses the latin alphabet.
 * However, the order among letters are unknown to you.
 * You receive a list of non-empty words from the dictionary,
 * where words are sorted lexicographically by the rules of this new language.
 * Derive the order of letters in this language.
 *
 * <pre>
 * Example 1:
 * Given the following words in dictionary,
 *
 * [
 *   "wrt",
 *   "wrf",
 *   "er",
 *   "ett",
 *   "rftt"
 * ]
 * The correct order is: "wertf".
 *
 * Example 2:
 * Given the following words in dictionary,
 *
 * [
 *   "z",
 *   "x"
 * ]
 * The correct order is: "zx".
 *
 * Example 3:
 * Given the following words in dictionary,
 *
 * [
 *   "z",
 *   "x",
 *   "z"
 * ]
 * The order is invalid, so return "".
 *
 * Note:
 * You may assume all letters are in lowercase.
 * You may assume that if a is a prefix of b, then a must appear before b in the given dictionary.
 * If the order is invalid, return an empty string.
 * There may be multiple valid order of letters, return any one of them is fine.
 * </pre>
 */
public class No269 {
    public static void main(String[] args) {
        List<String> list = new java.util.ArrayList<>(List.of("e", "abcd"));
        Collections.sort(list);
        System.out.println(list);
    }

    public String alienOrder(String[] words) {
        // Step 1: Initialize graph and inDegree map
        Map<Character, Set<Character>> graph = new HashMap<>();
        Map<Character, Integer> inDegree = new HashMap<>();

        for (String word : words) {
            for (char c : word.toCharArray()) {
                graph.putIfAbsent(c, new HashSet<>());
                inDegree.putIfAbsent(c, 0);
            }
        }

        // Step 2: Build the graph from adjacent words
        for (int i = 0; i < words.length - 1; i++) {
            String w1 = words[i];
            String w2 = words[i + 1];
            int minLen = Math.min(w1.length(), w2.length());

            // Invalid case: prefix problem
            if (w1.length() > w2.length() && w1.startsWith(w2)) {
                return "";
            }

            for (int j = 0; j < minLen; j++) {
                char c1 = w1.charAt(j);
                char c2 = w2.charAt(j);
                if (c1 != c2) {
                    if (!graph.get(c1).contains(c2)) {
                        graph.get(c1).add(c2);
                        inDegree.put(c2, inDegree.get(c2) + 1);
                    }
                    break; // only the first difference matters
                }
            }
        }

        // Step 3: Topological Sort (BFS - Kahn's Algorithm)
        Queue<Character> queue = new LinkedList<>();
        for (char c : inDegree.keySet()) {
            if (inDegree.get(c) == 0) {
                queue.offer(c);
            }
        }

        StringBuilder result = new StringBuilder(inDegree.size());
        while (!queue.isEmpty()) {
            char c = queue.poll();
            result.append(c);
            for (char next : graph.get(c)) {
                inDegree.put(next, inDegree.get(next) - 1);

                if (inDegree.get(next) < 0) {
                    return "";
                }

                if (inDegree.get(next) == 0) {
                    queue.offer(next);
                }

            }
        }

        // Step 4: Check for cycle
        if (result.length() != inDegree.size()) {
            return "";
        }
        return result.toString();
    }
}
