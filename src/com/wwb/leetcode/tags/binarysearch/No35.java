package com.wwb.leetcode.tags.binarysearch;

/**
 * Given a sorted array and a target value, return the index if the target is found.
 * If not, return the index where it would be if it were inserted in order.
 *
 * You may assume no duplicates in the array.
 *
 * Here are few examples.
 * [1,3,5,6], 5 → 2
 * [1,3,5,6], 2 → 1
 * [1,3,5,6], 7 → 4
 * [1,3,5,6], 0 → 0
 */
public class No35 {

    public int searchInsert(int[] nums, int target) {
        return findTarget(nums, 0, nums.length - 1, target);
    }

    private int findTarget(int[] nums, int start, int end, int target) {
        if(start > end) {
            if(nums[start] > target) {
                return start;
            } else {
                return start + 1;
            }
        }

        int mid = (start + end) / 2;

        if(start == end && nums[mid] != target) {
            if(nums[mid] > target) {
                return mid;
            } else {
                return mid + 1;
            }
        }

        if(nums[mid] == target) {
            return mid;
        } else if(nums[mid] < target) {
            return findTarget(nums, mid + 1, end, target);
        } else {
            return findTarget(nums, start, mid - 1, target);
        }
    }
}