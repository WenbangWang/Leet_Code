package com.wwb.leetcode.easy;

import java.util.HashMap;
import java.util.Map;

/**
 * Given an array of integers and an integer k,
 * find out whether there are two distinct indices i and j in the array
 * such that nums[i] = nums[j] and the difference between i and j is at most k.
 */
public class No219 {

    public boolean containsNearbyDuplicate(int[] nums, int k) {
        if(nums == null || nums.length < 2) {
            return false;
        }
        Map<Integer, Integer> map = new HashMap<>();

        for(int i = 0; i < nums.length; i++) {
            int num = nums[i];

            if(map.containsKey(num)) {
                int j = map.get(num);

                if(i - j <= k) {
                    return true;
                }
            }

            map.put(num, i);
        }

        return false;
    }
}