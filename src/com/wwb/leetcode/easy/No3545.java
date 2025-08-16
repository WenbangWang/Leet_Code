package com.wwb.leetcode.easy;

import java.util.Arrays;

/**
 * You are given a string s consisting of lowercase English letters, and an integer k.
 * <p>
 * Your task is to delete some (possibly none) of the characters in the string so that the number of distinct characters in the resulting string is at most k.
 * <p>
 * Return the minimum number of deletions required to achieve this.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: s = "abc", k = 2
 *
 * Output: 1
 *
 * Explanation:
 *
 * s has three distinct characters: 'a', 'b' and 'c', each with a frequency of 1.
 * Since we can have at most k = 2 distinct characters, remove all occurrences of any one character from the string.
 * For example, removing all occurrences of 'c' results in at most k distinct characters. Thus, the answer is 1.
 * </pre>
 *
 * <pre>
 * Example 2:
 *
 * Input: s = "aabb", k = 2
 *
 * Output: 0
 *
 * Explanation:
 *
 * s has two distinct characters ('a' and 'b') with frequencies of 2 and 2, respectively.
 * Since we can have at most k = 2 distinct characters, no deletions are required. Thus, the answer is 0.
 * </pre>
 *
 *
 * <pre>
 * Example 3:
 *
 * Input: s = "yyyzz", k = 1
 *
 * Output: 2
 *
 * Explanation:
 *
 * s has two distinct characters ('y' and 'z') with frequencies of 3 and 2, respectively.
 * Since we can have at most k = 1 distinct character, remove all occurrences of any one character from the string.
 * Removing all 'z' results in at most k distinct characters. Thus, the answer is 2.
 * </pre>
 *
 * <pre>
 * Constraints:
 *
 * 1 <= s.length <= 16
 * 1 <= k <= 16
 * s consists only of lowercase English letters.
 * </pre>
 */
public class No3545 {
    public int minDeletion(String s, int k) {
        int[] buckets = new int[26];

        for (char c : s.toCharArray()) {
            if (buckets[c - 'a'] == 0) {
                k--;
            }
            buckets[c - 'a']++;
        }

        if (k >= 0) {
            return 0;
        }

        Arrays.sort(buckets);
        int result = 0;

        for (int frequency : buckets) {
            if (frequency == 0) {
                continue;
            }

            result+= frequency;
            k++;

            if (k == 0) {
                break;
            }
        }

        return result;
    }
}
