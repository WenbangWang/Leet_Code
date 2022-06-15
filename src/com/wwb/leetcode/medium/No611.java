package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * Given an integer array nums, return the number of triplets chosen from the array that can make triangles if we take them as side lengths of a triangle.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [2,2,3,4]
 * Output: 3
 * Explanation: Valid combinations are:
 * 2,3,4 (using the first 2)
 * 2,3,4 (using the second 2)
 * 2,2,3
 * Example 2:
 *
 * Input: nums = [4,2,3,4]
 * Output: 4
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 1000
 * 0 <= nums[i] <= 1000
 */
public class No611 {
    public int triangleNumber(int[] nums) {
        if (nums == null || nums.length < 3) {
            return 0;
        }

        Arrays.sort(nums);

        int result = 0;
//        boolean firstTime = true;

        for (int first = 0; first < nums.length - 2; first++) {
            int third = first + 2;
            for (int second = first + 1; second < nums.length - 1 && nums[first] != 0; second++) {
//                if (firstTime) {
//                    int target = nums[first] + nums[second];
//                    third = binarySearch(nums, third, nums.length - 1, target);
//                    firstTime = false;
//                } else {
                    while (third < nums.length && nums[first] + nums[second] > nums[third]) {
                        third++;
                    }
//                }

                result += third - second - 1;
            }
        }

        return result;
    }

    private int binarySearch(int[] nums, int start, int end, int target) {
        while (start <= end) {
            int mid = start + (end - start) / 2;

            if (target > nums[mid]) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        return start;
    }
}
