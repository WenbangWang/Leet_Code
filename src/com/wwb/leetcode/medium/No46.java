package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a collection of numbers, return all possible permutations.
 *
 * For example,
 * [1,2,3] have the following permutations:
 * [1,2,3], [1,3,2], [2,1,3], [2,3,1], [3,1,2], and [3,2,1].
 */
public class No46 {

    public List<List<Integer>> permute(int[] nums) {
        return solution1(nums);
    }

    private List<List<Integer>> solution1(int[] nums) {
        return getPerm(nums, 0);
    }

    private List<List<Integer>> solution2(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(nums, result, new LinkedList<>(), new boolean[nums.length]);

        return result;
    }

    private List<List<Integer>> getPerm(int[] nums, int level) {
        List<List<Integer>> perms = new LinkedList<>();
        if(level == nums.length - 1) {
            perms.add(List.of(nums[level]));
            return perms;
        }

        List<List<Integer>> nextPerms = getPerm(nums, level + 1);
        int num = nums[level];

        for(List<Integer> nextPerm : nextPerms) {
            for(int i = 0, size = nextPerm.size(); i <= size; i++) {
                List<Integer> currentPerm = new LinkedList<>(nextPerm);
                currentPerm.add(i, num);
                perms.add(currentPerm);
            }
        }

        return perms;
    }

    private void backtrack(int[] nums, List<List<Integer>> result, List<Integer> current, boolean[] visited) {
        if (nums.length == current.size()) {
            result.add(new LinkedList<>(current));

            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (visited[i]) {
                continue;
            }

            visited[i] = true;
            current.add(nums[i]);
            backtrack(nums, result, current, visited);
            visited[i] = false;
            current.remove(current.size() - 1);
        }
    }
}
