package com.wwb.leetcode.medium;

/**
 * Given a sorted array of integers, find the starting and ending position of a given target value.
 *
 * Your algorithm's runtime complexity must be in the order of O(log n).
 *
 * If the target is not found in the array, return [-1, -1].
 *
 * For example,
 * Given [5, 7, 7, 8, 8, 10] and target value 8,
 * return [3, 4].
 */
public class No34 {

    public int[] searchRange(int[] nums, int target) {
        int[] result = {-1, -1};

        if(nums == null) {
            return result;
        }

        if (nums.length == 0) {
            return result;
        }

        int start = 0;
        int end = nums.length - 1;

        while(start <= end) {
            int mid = start + (end - start) / 2;

            if(target > nums[mid]) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        if(start == nums.length || nums[start] != target) {
            return result;
        }

        result[0] = start;
        end = nums.length - 1;

        while(start <= end) {
            int mid = start + (end - start) / 2;

            if(target < nums[mid]) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }

        result[1] = end;

        return result;
    }
}
