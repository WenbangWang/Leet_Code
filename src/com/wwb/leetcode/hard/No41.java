package com.wwb.leetcode.hard;

/**
 * Given an unsorted integer array, find the first missing positive integer.
 *
 * For example,
 * Given [1,2,0] return 3,
 * and [3,4,-1,1] return 2.
 *
 * Your algorithm should run in O(n) time and uses constant space.
 */
public class No41 {

    public int firstMissingPositive(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 1;
        }

        int partition = partition(nums) + 1;

        for(int i = 0; i < partition; i++) {
            int temp = Math.abs(nums[i]);

            if(temp <= partition) {
                nums[temp - 1] = nums[temp - 1] < 0 ? nums[temp - 1] : -nums[temp - 1];
            }
        }

        for(int i = 0; i < partition; i++) {
            if(nums[i] > 0) {
                return i + 1;
            }
        }

        return partition + 1;
    }

    private int partition(int[] nums) {
        int index = -1;
        for(int i = 0; i < nums.length; i++) {
            if(nums[i] > 0) {
                nums[++index] = nums[i];
            }
        }

        return index;
    }
}
