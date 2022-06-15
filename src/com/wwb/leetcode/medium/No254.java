package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Numbers can be regarded as the product of their factors.
 *
 * For example, 8 = 2 x 2 x 2 = 2 x 4.
 * Given an integer n, return all possible combinations of its factors. You may return the answer in any order.
 *
 * Note that the factors should be in the range [2, n - 1].
 *
 *
 *
 * Example 1:
 *
 * Input: n = 1
 * Output: []
 * Example 2:
 *
 * Input: n = 12
 * Output: [[2,6],[3,4],[2,2,3]]
 * Example 3:
 *
 * Input: n = 37
 * Output: []
 *
 *
 * Constraints:
 *
 * 1 <= n <= 10^7
 */
public class No254 {
    public List<List<Integer>> getFactors(int n) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(result, new ArrayList<>(), n, 2);
        return result;
    }

    public void backtrack(List<List<Integer>> result, List<Integer> factors, int n, int start){
        for (int i = start; i * i <= n; i++) {
            if (n % i == 0) {
                factors.add(i);
                factors.add(n / i);

                result.add(new ArrayList<>(factors));

                // remove n / i so we can populate factors for n/i
                // in backtracking and avoid duplicate for n/i
                factors.remove(factors.size() - 1);
                backtrack(result, factors, n / i, i);
                factors.remove(factors.size() - 1);
            }
        }
    }
}
