package com.wwb.leetcode.medium;

/**
 * Given n non-negative integers a1, a2, ..., an,
 * where each represents a point at coordinate (i, ai).
 * n vertical lines are drawn such that the two endpoints of line i is at (i, ai) and (i, 0).
 * Find two lines, which together with x-axis forms a container, such that the container contains the most water.
 *
 * Note: You may not slant the container.
 */
public class No11 {

    public int maxArea(int[] height) {
        if(height == null || height.length == 1) {
            return 0;
        }
        return solution1(height);
    }

    private int solution1(int[] heightArray) {
        int maxArea = Integer.MIN_VALUE;
        int start = 0;
        int end = heightArray.length - 1;

        while(start < end) {
            int width = end - start;
            int height = Math.min(heightArray[start], heightArray[end]);
            int area = width * height;
            maxArea = Math.max(maxArea, area);

            if(heightArray[start] > heightArray[end]) {
                end--;
            } else {
                start++;
            }
        }

        return maxArea;
    }
}
