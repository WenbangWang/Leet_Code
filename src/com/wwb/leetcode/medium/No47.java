package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given a collection of numbers that might contain duplicates, return all possible unique permutations.
 *
 * For example,
 * [1,1,2] have the following unique permutations:
 * [1,1,2], [1,2,1], and [2,1,1].
 */
public class No47 {

    public List<List<Integer>> permuteUnique(int[] nums) {
        if(nums == null || nums.length == 0) {
            return Collections.emptyList();
        }

        return permuteUnique(nums, 0, new HashSet<String>());
    }

    private List<List<Integer>> permuteUnique(int[] nums, int level, Set<String> set) {
        List<List<Integer>> result = new ArrayList<>();
        if(nums.length == level) {
            result.add(new ArrayList<Integer>());
            return result;
        }

        List<List<Integer>> lastPermutations = permuteUnique(nums, level + 1, set);

        for(List<Integer> lastPermutation : lastPermutations) {
            for(int i = 0, size = lastPermutation.size(); i <= size; i++) {
                List<Integer> permutation = new ArrayList<>(lastPermutation);
                permutation.add(i, nums[level]);

                if(set.add(permutation.toString())) {
                    result.add(permutation);
                }
            }
        }

        return result;
    }
}
