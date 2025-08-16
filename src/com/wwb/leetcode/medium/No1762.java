package com.wwb.leetcode.medium;

import java.util.LinkedList;

/**
 * There are n buildings in a line. You are given an integer array heights of size n that represents the heights of the buildings in the line.
 * <p>
 * The ocean is to the right of the buildings. A building has an ocean view if the building can see the ocean without obstructions. Formally, a building has an ocean view if all the buildings to its right have a smaller height.
 * <p>
 * Return a list of indices (0-indexed) of buildings that have an ocean view, sorted in increasing order.
 * <p>
 * Example 1:
 * <p>
 * Input: heights = [4,2,3,1]
 * <p>
 * Output: [0,2,3]
 * <p>
 * Explanation: Building 1 (0-indexed) does not have an ocean view because building 2 is taller.
 * <p>
 * Example 2:
 * <p>
 * Input: heights = [4,3,2,1]
 * <p>
 * Output: [0,1,2,3]
 * <p>
 * Explanation: All the buildings have an ocean view.
 * <p>
 * Example 3:
 * <p>
 * Input: heights = [1,3,2,4]
 * <p>
 * Output: [3]
 * <p>
 * Explanation: Only building 3 has an ocean view.
 * <p>
 * Example 4:
 * <p>
 * Input: heights = [2,2,2,2]
 * <p>
 * Output: [3]
 * <p>
 * Explanation: Buildings cannot see the ocean if there are buildings of the same height to its right.
 * <p>
 * Constraints:
 * <p>
 * 1 <= heights.length <= 10^5
 * 1 <= heights[i] <= 10^9
 */
public class No1762 {
    public int[] findBuildings(int[] heights) {
        int max = Integer.MIN_VALUE;
        LinkedList<Integer> result = new LinkedList<>();

        for (int i = heights.length - 1; i >= 0; i--) {
            if (heights[i] > max) {
                result.addFirst(heights[i]);
                max = heights[i];
            }
        }


        return result.stream().mapToInt(i -> i).toArray();
    }
}
