package com.wwb.leetcode.tags.binarysearch;

/**
 * Suppose a sorted array is rotated at some pivot unknown to you beforehand.
 *
 * (i.e., 0 1 2 4 5 6 7 might become 4 5 6 7 0 1 2).
 *
 * Find the minimum element.
 *
 * The array may contain duplicates.
 */
public class No154 {

    public int findMin(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        int start = 0;
        int end = nums.length - 1;

        while(start < end) {
            if(nums[start] < nums[end]) {
                return nums[start];
            }

            int mid = (end - start) / 2 + start;

            if(nums[mid] < nums[start]) {
                end = mid;
            } else if(nums[mid] > nums[start]) {
                start = mid + 1;
            } else {
                if(nums[start] == nums[end]) {
                    start++;
                    end--;
                } else {
                    start = mid + 1;
                }
            }
        }

        return nums[start];
    }
}
