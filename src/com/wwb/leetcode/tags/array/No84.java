package com.wwb.leetcode.tags.array;

import java.util.Arrays;
import java.util.Stack;

/**
 * Given n non-negative integers representing the histogram's bar height where the width of each bar is 1, find the area of largest rectangle in the histogram.
 *
 * Above is a histogram where width of each bar is 1, given height = [2,1,5,6,2,3].
 *
 * The largest rectangle is shown in the shaded area, which has area = 10 unit.
 *
 * For example,
 * Given height = [2,1,5,6,2,3],
 * return 10.
 */
public class No84 {

    public int largestRectangleArea(int[] height) {
        if(height == null || height.length == 0) {
            return 0;
        }

        Stack<Integer> stack = new Stack<>();
        int maxArea = 0;
        height = Arrays.copyOf(height, height.length + 1);

        for(int i = 0; i < height.length; i++) {
            while(!stack.isEmpty() && height[stack.peek()] > height[i]) {
                int area = height[stack.pop()] * (stack.isEmpty() ? i : i - 1 - stack.peek());
                maxArea = Math.max(maxArea, area);
            }

            stack.push(i);
        }

        return maxArea;
    }
}
