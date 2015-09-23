package com.wwb.leetcode.hard;

import java.util.Arrays;

/**
 * Given an unsorted array, find the maximum difference between the successive elements in its sorted form.
 *
 * Try to solve it in linear time/space.
 *
 * Return 0 if the array contains less than 2 elements.
 *
 * You may assume all elements in the array are non-negative integers and fit in the 32-bit signed integer range.
 */
public class No164 {

    public int maximumGap(int[] nums) {
        if(nums == null || nums.length < 2) {
            return 0;
        }

        int max = nums[0];
        int min = nums[0];
        int length = nums.length;

        for(int num : nums) {
            max = Math.max(max, num);
            min = Math.min(min, num);
        }

        int gap = (max - min) / (length - 1) + 1;
        int[] bucketMin = new int[length - 1];
        int[] bucketMax = new int[length - 1];
        Arrays.fill(bucketMin, Integer.MAX_VALUE);
        Arrays.fill(bucketMax, Integer.MIN_VALUE);

        for(int num : nums) {
            if(num != max && num != min) {
                int index = (num - min) / gap;

                bucketMin[index] = Math.min(bucketMin[index], num);
                bucketMax[index] = Math.max(bucketMax[index], num);
            }
        }

        int maxGap = Integer.MIN_VALUE;
        int previous = min;

        for(int i = 0; i < length - 1; i++) {
            if(bucketMin[i] == Integer.MAX_VALUE && bucketMax[i] == Integer.MIN_VALUE) {
                continue;
            }

            maxGap = Math.max(maxGap, bucketMin[i] - previous);
            previous = bucketMax[i];
        }

        maxGap = Math.max(maxGap, max - previous);

        return maxGap;
    }
}
