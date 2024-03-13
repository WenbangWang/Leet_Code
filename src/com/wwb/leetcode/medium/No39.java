package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Given an array of distinct integers candidates and a target integer target,
 * return a list of all unique combinations of candidates where the chosen numbers sum to target.
 * You may return the combinations in any order.
 *
 * The same number may be chosen from candidates an unlimited number of times.
 * Two combinations are unique if the frequency of at least one of the chosen numbers is different.
 *
 * The test cases are generated such that the number of unique combinations
 * that sum up to target is less than 150 combinations for the given input.
 *
 *
 *
 * Example 1:
 *
 * Input: candidates = [2,3,6,7], target = 7
 * Output: [[2,2,3],[7]]
 * Explanation:
 * 2 and 3 are candidates, and 2 + 2 + 3 = 7. Note that 2 can be used multiple times.
 * 7 is a candidate, and 7 = 7.
 * These are the only two combinations.
 * Example 2:
 *
 * Input: candidates = [2,3,5], target = 8
 * Output: [[2,2,2,2],[2,3,3],[3,5]]
 * Example 3:
 *
 * Input: candidates = [2], target = 1
 * Output: []
 *
 *
 * Constraints:
 *
 * 1 <= candidates.length <= 30
 * 2 <= candidates[i] <= 40
 * All elements of candidates are distinct.
 * 1 <= target <= 40
 */
public class No39 {

    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        return solution1(candidates, target);
    }

    // O(N^T)
    private List<List<Integer>> solution1(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> combination = new ArrayList<>();
        Arrays.sort(candidates);

        getCombinations(candidates, target, 0, 0, combination, result);

        return result;
    }

    // O(2^T) WHERE T = TARGET / CANDIDATES[i]
    // https://leetcode.com/problems/combination-sum/solutions/1777569/full-explanation-with-state-space-tree-recursion-and-backtracking-well-explained-c/
    private List<List<Integer>> solution2(int[] candidates, int target) {
        List<List<List<Integer>>> dp = new ArrayList<>(target + 1);

        for (int i = 0; i <= target; i++) {
            dp.add(new ArrayList<>());
        }

        dp.get(0).add(new ArrayList<>());

        for (int candidate: candidates) {
            for (int j = candidate; j <= target; j++) {
                for (List<Integer> comb: dp.get(j - candidate)) {
                    List<Integer> newComb = new ArrayList<>(comb);
                    newComb.add(candidate);
                    dp.get(j).add(newComb);
                }
            }
        }

        return dp.get(target);
    }

    private void getCombinations(int[] candidates, int target, int sum, int level, List<Integer> combination, List<List<Integer>> result) {
        if(sum == target) {
            result.add(new ArrayList<>(combination));
            return;
        }

        for(int i = level; i < candidates.length; i++) {
            int candidate = candidates[i];
            sum += candidate;

            if(sum > target) {
                return;
            }

            combination.add(candidate);
            getCombinations(candidates, target, sum, i, combination, result);
            combination.remove(combination.size() - 1);
            sum -= candidate;
        }
    }
}
