package com.wwb.leetcode.tags.backtracking;

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
        return solution2(nums);
    }

    private List<List<Integer>> solution1(int[] nums) {
        List<List<Integer>> lists = new ArrayList<>();
        lists.add(new ArrayList<Integer>());
        Arrays.sort(nums);
        Map<List<Integer>, Boolean> map = new HashMap<>();

        for(int num : nums) {
            List<List<Integer>> subsets = new ArrayList<>(lists);
            for(List<Integer> list : lists) {
                List<Integer> subset = new ArrayList<>(list);
                subset.add(num);
                if(!map.containsKey(subset)) {
                    subsets.add(subset);
                    map.put(subset, true);
                }
            }

            lists = subsets;
        }

        return lists;
    }

    private List<List<Integer>> solution2(int[] nums) {
        List<List<Integer>> lists = new ArrayList<>();
        Arrays.sort(nums);
        Map<List<Integer>, Boolean> map = new HashMap<>();
        int numberofSets = 1 << nums.length;

        for(int i = 0; i < numberofSets; i++) {
            List<Integer> subset = new ArrayList<>();
            for(int j = 0; j < nums.length; j++) {
                if((i & (1 << j)) > 0) {
                    subset.add(nums[j]);
                }
            }

            if(!map.containsKey(subset)) {
                map.put(subset, true);
                lists.add(subset);
            }
        }

        return lists;
    }

    private List<List<Integer>> solution3(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();

        result.add(new ArrayList<>());

        int startIndex = 0;
        int size = 0;

        for(int i = 0; i < nums.length; i++) {
            startIndex = i > 0 && nums[i] == nums[i - 1] ? size : 0;

            size = result.size();

            for(int j = startIndex; j < size; j++) {
                List<Integer> subset = new ArrayList<>(result.get(j));
                subset.add(nums[i]);
                result.add(subset);
            }
        }

        return result;
    }
}
