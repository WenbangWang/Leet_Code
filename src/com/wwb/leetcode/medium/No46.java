package com.wwb.leetcode.medium;

import java.util.Arrays;
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

    public static void main(String[] args) {
        No46 no46 = new No46();
        System.out.println(no46.permute(new int[]{1,2,3,4}).size());
    }

    public List<List<Integer>> permute(int[] nums) {
        return getPerm(nums, 0);
    }

    private List<List<Integer>> getPerm(int[] nums, int level) {
        List<List<Integer>> perms = new LinkedList<>();
        if(level == nums.length - 1) {
            perms.add(Arrays.asList(nums[level]));
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
}