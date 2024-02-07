package com.wwb.leetcode.medium;

import java.util.TreeMap;

/**
 * Given an integer array nums, you need to find one continuous subarray
 * such that if you only sort this subarray in non-decreasing order,
 * then the whole array will be sorted in non-decreasing order.
 *
 * Return the shortest such subarray and output its length.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [2,6,4,8,10,9,15]
 * Output: 5
 * Explanation: You need to sort [6, 4, 8, 10, 9] in ascending order to make the whole array sorted in ascending order.
 * Example 2:
 *
 * Input: nums = [1,2,3,4]
 * Output: 0
 * Example 3:
 *
 * Input: nums = [1]
 * Output: 0
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 10^4
 * -10^5 <= nums[i] <= 10^5
 */
public class No581 {
    public int findUnsortedSubarray(int[] nums) {
        return solution1(nums);
    }

    private int solution1(int[] nums) {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        int minLeft = Integer.MAX_VALUE;

        for (int i = 0; i < nums.length; i++) {
            map.putIfAbsent(nums[i], i);
            // need to exclude the value equal to nums[i]
            Integer left = map.ceilingKey(nums[i] + 1);

            if (left != null) {
                minLeft = Math.min(minLeft, map.get(left));
            }
        }

        if (minLeft == Integer.MAX_VALUE) {
            return 0;
        }

        map = new TreeMap<>();
        int maxRight = Integer.MIN_VALUE;

        for (int i = nums.length - 1; i >= 0; i--) {
            map.putIfAbsent(nums[i], i);

            // need to exclude the value equal to nums[i]
            Integer right = map.floorKey(nums[i] - 1);

            if (right != null) {
                maxRight = Math.max(maxRight, map.get(right));
            }
        }

        return maxRight - minLeft + 1;
    }

    private int solution2(int[] nums) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int start = -1;
        int end = -1;

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] >= max) {
                max = nums[i];
            } else {
                end = i;
            }
        }

        if (end == -1) {
            return 0;
        }

        for (int i = nums.length - 1; i >= 0; i--) {
            if (nums[i] <= min) {
                min = nums[i];
            } else {
                start = i;
            }
        }

        return end - start + 1;
    }
}
