package com.wwb.leetcode.tags.twopointers;

/**
 * Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it is able to trap after raining.
 *
 * For example,
 * Given [0,1,0,2,1,0,1,3,2,1,2,1], return 6.
 */
public class No42 {

    public int trap(int[] height) {
        if(height == null || height.length <= 2) {
            return 0;
        }

        int sum = 0;
        int leftMax = 0;
        int rightMax = 0;
        int start = 0;
        int end = height.length - 1;

        while(start <= end) {
            leftMax = Math.max(leftMax, height[start]);
            rightMax = Math.max(rightMax, height[end]);

            if(leftMax < rightMax) {
                sum += leftMax - height[start];
                start++;
            } else {
                sum += rightMax - height[end];
                end--;
            }
        }

        return sum;
    }
}
