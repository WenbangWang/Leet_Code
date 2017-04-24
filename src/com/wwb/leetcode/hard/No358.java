package com.wwb.leetcode.hard;

/**
 * Given a non-empty string s and an integer k, rearrange the string such that the same characters are at least distance k from each other.
 *
 * All input strings are given in lowercase letters. If it is not possible to rearrange the string, return an empty string "".
 *
 * Example 1:
 * s = "aabbcc", k = 3
 *
 * Result: "abcabc"
 *
 * The same letters are at least distance 3 from each other.
 * Example 2:
 * s = "aaabc", k = 3
 *
 * Answer: ""
 *
 * It is not possible to rearrange the string.
 * Example 3:
 * s = "aaadbbcc", k = 2
 *
 * Answer: "abacabcd"
 *
 * Another possible answer is: "abcabcda"
 *
 * The same letters are at least distance 2 from each other.
 */
public class No358 {

    public String rearrangeString(String str, int k) {
        if (str == null || str.isEmpty() || k < 0) {
            return "";
        }

        int[] counts = new int[26];
        int[] positions = new int[26];
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : str.toCharArray()) {
            counts[c - 'a']++;
        }

        for (int offset = 0, length = str.length(); offset < length; offset++) {
            int maxPosition = findNextMaxPosition(counts, positions, offset);

            if (maxPosition == -1) {
                return "";
            }

            counts[maxPosition]--;
            positions[maxPosition] = k + offset;

            stringBuilder.append((char) ('a' + maxPosition));
        }

        return stringBuilder.toString();
    }

    private int findNextMaxPosition(int[] counts, int[] positions, int offset) {
        int max = Integer.MIN_VALUE;
        int maxPosition = -1;

        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0 && counts[i] > max && offset >= positions[i]) {
                max = counts[i];
                maxPosition = i;
            }
        }

        return maxPosition;
    }
}
