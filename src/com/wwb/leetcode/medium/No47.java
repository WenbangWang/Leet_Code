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
        return solution1(nums);
    }

    private List<List<Integer>> solution1(int[] nums) {
        if(nums == null || nums.length == 0) {
            return Collections.emptyList();
        }

        return permuteUnique(nums, 0, new HashSet<>());
    }

    private List<List<Integer>> solution2(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();

        Arrays.sort(nums);
        backtrack(new ArrayList<>(), result, nums, new boolean[nums.length]);

        return result;
    }

    private List<List<Integer>> permuteUnique(int[] nums, int level, Set<String> set) {
        List<List<Integer>> result = new ArrayList<>();
        if(nums.length == level) {
            result.add(new ArrayList<>());
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

    private void backtrack(List<Integer> current, List<List<Integer>> result, int[] nums, boolean[] visited) {
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (visited[i]) {
                continue;
            }

            if (i > 0 && nums[i] == nums[i - 1] && !visited[i - 1]) {
                continue;
            }

            visited[i] = true;
            current.add(nums[i]);
            backtrack(current, result, nums, visited);
            current.remove(current.size() - 1);
            visited[i] = false;
        }
    }
}
