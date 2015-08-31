package com.wwb.leetcode.tags.bst;

import java.util.TreeSet;

/**
 * Given an array of integers, find out whether there are two distinct indices i and j in the array
 * such that the difference between nums[i] and nums[j] is at most t and the difference between i and j is at most k.
 */
public class No220 {

    public boolean containsNearbyAlmostDuplicate(int[] nums, int k, int t) {
        if(nums == null || nums.length < 2) {
            return false;
        }
        TreeSet<Integer> tree = new TreeSet<>();

        for(int i = 0; i < nums.length; i++) {
            int num = nums[i];
            Integer floor = tree.floor(num + t);
            Integer ceiling = tree.ceiling(num - t);

            if((floor != null && floor >= num) || (ceiling != null && ceiling <= num)) {
                return true;
            }

            tree.add(num);

            if(i >= k) {
                tree.remove(nums[i - k]);
            }
        }

        return false;
    }
}