package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;

/**
 * Find all possible combinations of k numbers that add up to a number n,
 * given that only numbers from 1 to 9 can be used and each combination should be a unique set of numbers.
 *
 * Ensure that numbers within the set are sorted in ascending order.
 *
 * Example 1:
 *
 * Input: k = 3, n = 7
 *
 * Output:
 *
 * [[1,2,4]]
 *
 * Example 2:
 *
 * Input: k = 3, n = 9
 *
 * Output:
 *
 * [[1,2,6], [1,3,5], [2,3,4]]
 */
public class No216 {

    public List<List<Integer>> combinationSum3(int k, int n) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> resultSet = new ArrayList<>();

        if(k <= 0 || n <= 0) {
            return result;
        }

        combinationSum3(result, resultSet, k, n, 0);

        return result;
    }

    private void combinationSum3(List<List<Integer>> result, List<Integer> resultSet, int k, int n, int index) {
        if(n < 0 || resultSet.size() > k) {
            return;
        }

        if(resultSet.size() == k && n == 0) {
            result.add(new ArrayList<>(resultSet));
            return;
        }

        for(int i = index; i < 9; i++) {
            int num = i + 1;
            resultSet.add(num);
            combinationSum3(result, resultSet, k, n - num, i + 1);
            resultSet.remove(resultSet.size() - 1);
        }
    }
}