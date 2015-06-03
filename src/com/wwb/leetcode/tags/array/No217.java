package com.wwb.leetcode.tags.array;

import java.util.HashSet;
import java.util.Set;

/**
 * Given an array of integers, find if the array contains any duplicates.
 *
 * Your function should return true if any value appears at least twice in the array,
 * and it should return false if every element is distinct.
 */
public class No217 {

    public boolean containsDuplicate(int[] nums) {
        if(nums == null || nums.length == 0) {
            return false;
        }

        Set<Integer> hashSet = new HashSet<>();

        for(int num : nums) {
            if(hashSet.contains(num)) {
                return true;
            }

            hashSet.add(num);
        }

        return false;
    }
}