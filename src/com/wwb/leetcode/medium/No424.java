package com.wwb.leetcode.medium;

/**
 * You are given a string s and an integer k. You can choose any character of the string and change it to any other uppercase English character. You can perform this operation at most k times.
 * <p>
 * Return the length of the longest substring containing the same letter you can get after performing the above operations.
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: s = "ABAB", k = 2
 * Output: 4
 * Explanation: Replace the two 'A's with two 'B's or vice versa.
 * Example 2:
 *
 * Input: s = "AABABBA", k = 1
 * Output: 4
 * Explanation: Replace the one 'A' in the middle with 'B' and form "AABBBBA".
 * The substring "BBBB" has the longest repeating letters, which is 4.
 * There may exists other ways to achieve this answer too.
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 10^5
 * s consists of only uppercase English letters.
 * 0 <= k <= s.length
 * </pre>
 */
public class No424 {
    // maxFreq tracks the highest frequency of any character
    // we’ve seen in the window so far. We don’t reduce it when the window shrinks,
    // so it may become stale (too large for the current window).
    // That’s fine, because a stale maxFreq only makes us keep the window slightly longer,
    // but it never causes us to count an invalid window as the answer.
    // The true maximum length is always captured when it first occurs.
    public int characterReplacement(String s, int k) {
        int[] count = new int[26];
        int start = 0;
        int maxFreq = 0;
        int maxLen = 0;

        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            count[c - 'A']++;
            maxFreq = Math.max(maxFreq, count[c - 'A']);

            if ((end - start + 1) - maxFreq > k) {
                count[s.charAt(start) - 'A']--;
                start++;
            }

            maxLen = Math.max(maxLen, end - start + 1);
        }

        return maxLen;
    }
}
