package com.wwb.leetcode.hard;

import java.util.Arrays;

/**
 * You are given a 2D array of integers envelopes where envelopes[i] = [wi, hi] represents the width and the height of an envelope.
 * <p>
 * One envelope can fit into another if and only if both the width and height of one envelope are greater than the other envelope's width and height.
 * <p>
 * Return the maximum number of envelopes you can Russian doll (i.e., put one inside the other).
 * <p>
 * Note: You cannot rotate an envelope.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: envelopes = [[5,4],[6,4],[6,7],[2,3]]
 * Output: 3
 * Explanation: The maximum number of envelopes you can Russian doll is 3 ([2,3] => [5,4] => [6,7]).
 * Example 2:
 *
 * Input: envelopes = [[1,1],[1,1],[1,1]]
 * Output: 1
 * </pre>
 * <p>
 * <p>
 * Constraints:
 *
 * <pre>
 * 1 <= envelopes.length <= 10^5
 * envelopes[i].length == 2
 * 1 <= wi, hi <= 10^5
 * </pre>
 */
public class No354 {
    public int maxEnvelopes(int[][] envelopes) {
        Arrays.sort(envelopes, (a, b) -> a[0] == b[0] ? b[1] - a[1] : a[0] - b[0]);

        int[] dp = new int[envelopes.length];
        int result = 0;
        for (int i = 0; i < envelopes.length; i++) {
            int height = envelopes[i][1];
            int insertIndex = binarySearch(dp, height, result - 1);

            if (insertIndex == result) {
                result++;
            }

            dp[insertIndex] = height;
        }
        return result;
    }


    private int binarySearch(int[] dp, int height, int end) {
        int start = 0;

        while (start <= end) {
            int mid = (end - start) / 2 + start;

            if (dp[mid] < height) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }
        return start;
    }
}
