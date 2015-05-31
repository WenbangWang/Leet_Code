package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * Given an array S of n integers, find three integers in S such that the sum is closest to a given number, target.
 * Return the sum of the three integers. You may assume that each input would have exactly one solution.
 *
 * For example, given array S = {-1 2 1 -4}, and target = 1.
 *
 * The sum that is closest to the target is 2. (-1 + 2 + 1 = 2).
 */
public class No16 {

    public int threeSumClosest(int[] nums, int target) {
        if(nums == null || nums.length < 3) {
            return 0;
        }

        Arrays.sort(nums);
        int diff = Integer.MAX_VALUE;
        int record = 0;

        for(int i = 0; i < nums.length; i++) {
            int start = i + 1;
            int end = nums.length - 1;

            while(start < end) {
                int sum = nums[start] + nums[end] + nums[i];
                int sub = Math.abs(target - sum);

                if(sum == target) {
                    record = sum;
                    return record;
                } else if(sum > target) {
                    if(sub < diff) {
                        diff = sub;
                        record = sum;
                    }
                    end--;
                } else {
                    if(sub < diff) {
                        diff = sub;
                        record = sum;
                    }
                    start++;
                }
            }

            while(i < nums.length-1 && nums[i] == nums[i + 1]) {
                i++;
            }
        }

        return record;
    }
}
