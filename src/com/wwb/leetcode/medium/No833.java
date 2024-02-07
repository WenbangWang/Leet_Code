package com.wwb.leetcode.medium;

import java.util.*;

/**
 * You are given a 0-indexed string s that you must perform k replacement operations on.
 * The replacement operations are given as three 0-indexed parallel arrays, indices, sources, and targets,
 * all of length k.
 *
 * To complete the ith replacement operation:
 *
 * Check if the substring sources[i] occurs at index indices[i] in the original string s.
 * If it does not occur, do nothing.
 * Otherwise if it does occur, replace that substring with targets[i].
 * For example, if s = "abcd", indices[i] = 0, sources[i] = "ab", and targets[i] = "eee",
 * then the result of this replacement will be "eeecd".
 *
 * All replacement operations must occur simultaneously,
 * meaning the replacement operations should not affect the indexing of each other.
 * The testcases will be generated such that the replacements will not overlap.
 *
 * For example, a testcase with s = "abc", indices = [0, 1],
 * and sources = ["ab","bc"] will not be generated because the "ab" and "bc" replacements overlap.
 * Return the resulting string after performing all replacement operations on s.
 *
 * A substring is a contiguous sequence of characters in a string.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: s = "abcd", indices = [0, 2], sources = ["a", "cd"], targets = ["eee", "ffff"]
 * Output: "eeebffff"
 * Explanation:
 * "a" occurs at index 0 in s, so we replace it with "eee".
 * "cd" occurs at index 2 in s, so we replace it with "ffff".
 * Example 2:
 *
 *
 * Input: s = "abcd", indices = [0, 2], sources = ["ab","ec"], targets = ["eee","ffff"]
 * Output: "eeecd"
 * Explanation:
 * "ab" occurs at index 0 in s, so we replace it with "eee".
 * "ec" does not occur at index 2 in s, so we do nothing.
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 1000
 * k == indices.length == sources.length == targets.length
 * 1 <= k <= 100
 * 0 <= indexes[i] < s.length
 * 1 <= sources[i].length, targets[i].length <= 50
 * s consists of only lowercase English letters.
 * sources[i] and targets[i] consist of only lowercase English letters.
 */
public class No833 {
    public String findReplaceString(String s, int[] indices, String[] sources, String[] targets) {
        return solution1(s, indices, sources, targets);
    }

    // O(nlogn)
    private String solution1(String s, int[] indices, String[] sources, String[] targets) {
        StringBuilder result = new StringBuilder(s.length());
        List<int[]> sortedIndices = new ArrayList<>(indices.length);
        int pointer = 0;

        for (int i = 0; i < indices.length; i++) {
            sortedIndices.add(new int[]{i, indices[i]});
        }

        sortedIndices.sort(Comparator.comparingInt(a -> a[1]));

        for (var sortedIndex : sortedIndices) {
            int start = sortedIndex[1];
            int index = sortedIndex[0];

            if (start > pointer) {
                result.append(s, pointer, start);
            }

            pointer = start;

            if (s.startsWith(sources[index], start)) {
                result.append(targets[index]);
                pointer += sources[index].length();
            }

        }

        result.append(s, pointer, s.length());

        return result.toString();
    }

    // O(n)
    private String solution2(String s, int[] indices, String[] sources, String[] targets) {
        // Key is index in s
        // value is index in sources and targets
        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < indices.length; i++) {
            if (s.startsWith(sources[i], indices[i])) {
                map.put(indices[i], i);
            }
        }

        StringBuilder result = new StringBuilder(s.length());
        int index = 0;

        while(index < s.length()) {
            if (map.containsKey(index)) {
                result.append(targets[map.get(index)]);
                index += sources[map.get(index)].length();
            } else {
                result.append(s.charAt(index++));
            }
        }

        return result.toString();
    }
}
