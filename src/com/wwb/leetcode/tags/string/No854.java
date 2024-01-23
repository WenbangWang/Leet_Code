package com.wwb.leetcode.tags.string;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Strings s1 and s2 are k-similar (for some non-negative integer k)
 * if we can swap the positions of two letters in s1 exactly k times so that the resulting string equals s2.
 *
 * Given two anagrams s1 and s2, return the smallest k for which s1 and s2 are k-similar.
 *
 *
 *
 * Example 1:
 *
 * Input: s1 = "ab", s2 = "ba"
 * Output: 1
 * Explanation: The two string are 1-similar because we can use one swap to change s1 to s2: "ab" --> "ba".
 * Example 2:
 *
 * Input: s1 = "abc", s2 = "bca"
 * Output: 2
 * Explanation: The two strings are 2-similar because we can use two swaps to change s1 to s2: "abc" --> "bac" --> "bca".
 *
 *
 * Constraints:
 *
 * 1 <= s1.length <= 20
 * s2.length == s1.length
 * s1 and s2 contain only lowercase letters from the set {'a', 'b', 'c', 'd', 'e', 'f'}.
 * s2 is an anagram of s1.
 */
public class No854 {
    public int kSimilarity(String s1, String s2) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(s1);
        visited.add(s1);
        int result = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                String s = queue.poll();

                if (s.equals(s2)) {
                    return result;
                }

                for (String neighbor : getNeighbours(s, s2)) {
                    if (visited.add(neighbor)) {
                        queue.offer(neighbor);
                    }
                }
            }

            result++;
        }

        return result;
    }

    private List<String> getNeighbours(String s, String target) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder(s);

        int i = 0;
        for (; i < sb.length(); i++) {
            if (sb.charAt(i) != target.charAt(i)) {
                break;
            }
        }

        for (int j = i + 1; j < sb.length(); j++) {
            if (sb.charAt(j) == target.charAt(i)) {
                swap(sb, i, j);
                result.add(sb.toString());
                swap(sb, i, j);
            }
        }

        return result;
    }

    private void swap(StringBuilder sb, int i, int j) {
        char tmp = sb.charAt(i);

        sb.setCharAt(i, sb.charAt(j));
        sb.setCharAt(j, tmp);
    }
}
