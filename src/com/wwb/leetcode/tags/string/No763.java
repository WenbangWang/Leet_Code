package com.wwb.leetcode.tags.string;

import java.util.ArrayList;
import java.util.List;

/**
 * You are given a string s. We want to partition the string into as many parts as possible
 * so that each letter appears in at most one part.
 *
 * Note that the partition is done so that after concatenating all the parts in order, the resultant string should be s.
 *
 * Return a list of integers representing the size of these parts.
 *
 *
 *
 * Example 1:
 *
 * Input: s = "ababcbacadefegdehijhklij"
 * Output: [9,7,8]
 * Explanation:
 * The partition is "ababcbaca", "defegde", "hijhklij".
 * This is a partition so that each letter appears in at most one part.
 * A partition like "ababcbacadefegde", "hijhklij" is incorrect, because it splits s into less parts.
 * Example 2:
 *
 * Input: s = "eccbbbbdec"
 * Output: [10]
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 500
 * s consists of lowercase English letters.
 */
public class No763 {
    public List<Integer> partitionLabels(String s) {
        int[] lastOccurPositions = new int[26];

        for (int i = 0; i < s.length(); i++) {
            lastOccurPositions[s.charAt(i) - 'a'] = i;
        }

        List<Integer> result = new ArrayList<>();

        int index = 0;

        while (index < s.length()) {
            int lastPosition = lastOccurPositions[s.charAt(index) - 'a'];

            for (int i = index; i <= lastPosition; i++) {
                int newPosition = lastOccurPositions[s.charAt(i) - 'a'];

                // extend partition
                if (newPosition > lastPosition) {
                    lastPosition = newPosition;
                }
            }

            result.add(lastPosition - index + 1);
            index = lastPosition + 1;
        }

        return result;
    }
}
