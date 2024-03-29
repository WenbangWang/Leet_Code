package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given an integer array of size n, find all elements that appear more than ⌊ n/3 ⌋ times.
 * The algorithm should run in linear time and in O(1) space.
 */
public class No229 {

    public List<Integer> majorityElement(int[] nums) {
        if(nums == null || nums.length == 0) {
            return Collections.emptyList();
        }

        List<Integer> result = new ArrayList<>();
        int count1 = 0;
        int count2 = 0;
        int candidate1 = 0;
        int candidate2 = 0;

        for(int num : nums) {
            if(num == candidate1) {
                count1++;
            } else if(num == candidate2) {
                count2++;
            } else if(count1 == 0) {
                candidate1 = num;
                count1++;
            } else if(count2 == 0) {
                candidate2 = num;
                count2++;
            } else {
                count1--;
                count2--;
            }
        }

        count1 = 0;
        count2 = 0;
        for(int num : nums) {
            if(num == candidate1) {
                count1++;
            } else if(num == candidate2) {
                count2++;
            }
        }

        if(count1 > nums.length / 3) {
            result.add(candidate1);
        }
        if(count2 > nums.length / 3) {
            result.add(candidate2);
        }

        return result;
    }
}
