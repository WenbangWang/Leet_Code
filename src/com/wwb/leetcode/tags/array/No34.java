package com.wwb.leetcode.tags.array;

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

        int index = binarySearch(nums, 0, nums.length - 1, target);
        int start = index;
        int end = index;

        if(index == -1) {
            return result;
        }

        for(int i = index; i < nums.length; i++) {
            if(nums[i] == target) {
                end = i;
            }
        }

        for(int i = index; i >= 0; i--) {
            if(nums[i] == target) {
                start = i;
            }
        }

        result[0] = start;
        result[1] = end;

        return result;
    }

    private int binarySearch(int[] array, int start, int end, int target) {
        if(start > end) {
            return -1;
        }

        int mid = (start + end) / 2;

        if(start == end && array[mid] != target) {
            return -1;
        }

        if(array[mid] == target) {
            return mid;
        } else if(array[mid] < target) {
            return binarySearch(array, mid + 1, end, target);
        } else {
            return binarySearch(array, start, mid - 1, target);
        }
    }
}