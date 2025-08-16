package com.wwb.leetcode.easy;

import java.util.Arrays;

/**
 * Given an array of size n, find the majority element. The majority element is the element that appears more than ⌊ n/2 ⌋ times.
 * <p>
 * You may assume that the array is non-empty and the majority element always exist in the array.
 */
public class No169 {

    public int majorityElement(int[] nums) {
        // return solution1(nums);
        return solution2(nums);
    }

    private int solution1(int[] nums) {
        Arrays.sort(nums);

        return nums[nums.length / 2];
    }

    private int solution2(int[] nums) {
        int majority = nums[0];
        int count = 1;

        for (int i = 1; i < nums.length; i++) {
            if (count == 0) {
                count = 1;
                majority = nums[i];
            } else if (nums[i] == majority) {
                count++;
            } else {
                count--;
            }
        }

        return majority;
    }
}
