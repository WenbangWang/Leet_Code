package com.wwb.leetcode.easy;

import java.util.HashMap;
import java.util.Map;

/**
 * Given a list of dominoes, dominoes[i] = [a, b] is equivalent to dominoes[j] = [c, d]
 * if and only if either (a == c and b == d), or (a == d and b == c) - that is,
 * one domino can be rotated to be equal to another domino.
 *
 * Return the number of pairs (i, j) for which 0 <= i < j < dominoes.length, and dominoes[i] is equivalent to dominoes[j].
 *
 *
 *
 * Example 1:
 *
 * Input: dominoes = [[1,2],[2,1],[3,4],[5,6]]
 * Output: 1
 * Example 2:
 *
 * Input: dominoes = [[1,2],[1,2],[1,1],[1,2],[2,2]]
 * Output: 3
 *
 *
 * Constraints:
 *
 * 1 <= dominoes.length <= 4 * 104
 * dominoes[i].length == 2
 * 1 <= dominoes[i][j] <= 9
 */
public class No1128 {
    public int numEquivDominoPairs(int[][] dominoes) {
        Map<Integer, Integer> count = new HashMap<>();
        int result = 0;

        for (int[] domino : dominoes) {
            // value is within range [1,9] encode two dimension array into single value
            int key = Math.min(domino[0], domino[1]) * 10 + Math.max(domino[0], domino[1]);
            count.put(key, count.getOrDefault(key, 0) + 1);
        }

        for (int n : count.values()) {
            // C(n, 2) == sum (1, n - 1)
            result += n * (n - 1) / 2;
        }

        return result;
    }
}
