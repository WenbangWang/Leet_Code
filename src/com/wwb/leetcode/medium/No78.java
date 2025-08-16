package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a set of distinct integers, nums, return all possible subsets.
 *
 * <pre>
 * Note:
 * Elements in a subset must be in non-descending order.
 * The solution set must not contain duplicate subsets.
 * For example,
 * If nums = [1,2,3], a solution is:
 *
 * [
 *   [3],
 *   [1],
 *   [2],
 *   [1,2,3],
 *   [1,3],
 *   [2,3],
 *   [1,2],
 *   []
 * ]
 * </pre>
 */
public class No78 {

    public List<List<Integer>> subsets(int[] nums) {
        Arrays.sort(nums);
        // return solution1(nums);
        return solution2(nums);
    }

    private List<List<Integer>> solution1(int[] nums) {
        List<List<Integer>> lists = new ArrayList<>();
        lists.add(new ArrayList<>());

        for (int num : nums) {
            List<List<Integer>> subsets = new ArrayList<>(lists);
            for (List<Integer> list : lists) {
                List<Integer> subset = new ArrayList<>(list);
                subset.add(num);
                subsets.add(subset);
            }

            lists = subsets;
        }

        return lists;
    }

    private List<List<Integer>> solution2(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        int length = nums.length;
        for (int i = 0; i < StrictMath.pow(2, length); i++) {
            List<Integer> subset = new ArrayList<>();
            for (int j = 0; j < length; j++) {
                if (((i >> j) & 1) != 0) {
                    subset.add(nums[j]);
                }
            }

            result.add(subset);
        }

        return result;
    }

    private List<List<Integer>> solution3(int[] nums) {
        return solution3(nums, 0);
    }

    private List<List<Integer>> solution3(int[] nums, int index) {
        List<List<Integer>> result = new ArrayList<>();
        if (index == nums.length) {
            result.add(new LinkedList<>());
            return result;
        }

        List<List<Integer>> subResult = solution3(nums, index + 1);

        result.addAll(subResult);

        for (List<Integer> s : subResult) {
            List<Integer> subset = new LinkedList<>(s);
            subset.add(0, nums[index]);

            result.add(subset);
        }

        return result;
    }
}
