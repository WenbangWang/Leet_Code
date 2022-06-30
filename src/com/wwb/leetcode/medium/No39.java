package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Given a set of candidate numbers (C) and a target number (T),
 * find all unique combinations in C where the candidate numbers sums to T.
 *
 * The same repeated number may be chosen from C unlimited number of times.
 *
 * Note:
 * All numbers (including target) will be positive integers.
 * Elements in a combination (a1, a2, … , ak) must be in non-descending order. (ie, a1 ≤ a2 ≤ … ≤ ak).
 * The solution set must not contain duplicate combinations.
 * For example, given candidate set 2,3,6,7 and target 7,
 * A solution set is:
 * [7]
 * [2, 2, 3]
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
