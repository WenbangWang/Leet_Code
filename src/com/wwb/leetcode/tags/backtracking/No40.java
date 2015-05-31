package com.wwb.leetcode.tags.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Given a collection of candidate numbers (C) and a target number (T),
 * find all unique combinations in C where the candidate numbers sums to T.
 *
 * Each number in C may only be used once in the combination.
 *
 * Note:
 * All numbers (including target) will be positive integers.
 * Elements in a combination (a1, a2, … , ak) must be in non-descending order. (ie, a1 ≤ a2 ≤ … ≤ ak).
 * The solution set must not contain duplicate combinations.
 * For example, given candidate set 10,1,2,7,6,1,5 and target 8,
 * A solution set is:
 * [1, 7]
 * [1, 2, 5]
 * [2, 6]
 * [1, 1, 6]
 */
public class No40 {

    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> combination = new ArrayList<>();
        Arrays.sort(candidates);

        getCombinations(candidates, target, 0, 0, combination, result);

        return result;
    }

    private void getCombinations(int[] candidates, int target, int sum, int level, List<Integer> combination, List<List<Integer>> result) {
        if(sum > target) {
            return;
        }

        if(sum == target) {
            result.add(new ArrayList<>(combination));
            return;
        }

        for(int i = level; i < candidates.length; i++) {
            int candidate = candidates[i];
            sum += candidate;
            combination.add(candidate);
            getCombinations(candidates, target, sum, i + 1, combination, result);
            combination.remove(combination.size() - 1);
            sum -= candidate;

            while(i < candidates.length - 1 && candidates[i] == candidates[i + 1]) {
                i++;
            }
        }
    }
}