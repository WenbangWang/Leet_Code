package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given a collection of integers that might contain duplicates, nums, return all possible subsets.
 *
 * Note:
 * Elements in a subset must be in non-descending order.
 * The solution set must not contain duplicate subsets.
 * For example,
 * If nums = [1,2,2], a solution is:
 *
 * [
 *   [2],
 *   [1],
 *   [1,2,2],
 *   [2,2],
 *   [1,2],
 *   []
 * ]
 */
public class No90 {

    public List<List<Integer>> subsetsWithDup(int[] nums) {
        // return solution1(nums);
//        return solution2(nums);
        return solution3(nums);
    }

    private List<List<Integer>> solution1(int[] nums) {
        List<List<Integer>> lists = new ArrayList<>();
        lists.add(new ArrayList<>());
        Arrays.sort(nums);
        Set<List<Integer>> sets = new HashSet<>();

        for(int num : nums) {
            List<List<Integer>> subsets = new ArrayList<>(lists);
            for(List<Integer> list : lists) {
                List<Integer> subset = new ArrayList<>(list);
                subset.add(num);

                if(sets.add(subset)) {
                    subsets.add(subset);
                }
            }

            lists = subsets;
        }

        return lists;
    }

    private List<List<Integer>> solution2(int[] nums) {
        List<List<Integer>> lists = new ArrayList<>();
        Arrays.sort(nums);
        Set<List<Integer>> sets = new HashSet<>();
        int numberOfSets = 1 << nums.length;

        for(int i = 0; i < numberOfSets; i++) {
            List<Integer> subset = new ArrayList<>();
            for(int j = 0; j < nums.length; j++) {
                if((i & (1 << j)) > 0) {
                    subset.add(nums[j]);
                }
            }

            if(sets.add(subset)) {
                lists.add(subset);
            }
        }

        return lists;
    }

    private List<List<Integer>> solution3(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();

        result.add(new ArrayList<>());

        int lastResultSize = 0;

        for(int i = 0; i < nums.length; i++) {
            int startIndex = i > 0 && nums[i] == nums[i - 1] ? lastResultSize : 0;

            lastResultSize = result.size();

            for(int j = startIndex; j < lastResultSize; j++) {
                List<Integer> subset = new ArrayList<>(result.get(j));
                subset.add(nums[i]);
                result.add(subset);
            }
        }

        return result;
    }
}
