package com.wwb.leetcode.tags.dp;

/**
 * Given a m x n grid filled with non-negative numbers,
 * find a path from top left to bottom right which minimizes the sum of all numbers along its path.
 *
 * Note: You can only move either down or right at any point in time.
 */
public class No64 {

    public int minPathSum(int[][] grid) {
        int x = grid.length;
        int y = grid[0].length;
        int[] buffer = new int[y];

        buffer[0] = grid[0][0];

        for(int i = 1; i < y; i++) {
            buffer[i] = grid[0][i] + buffer[i - 1];
        }

        for(int i = 1; i < x; i++) {
            buffer[0] = buffer[0] + grid[i][0];

            for(int j = 1; j < y; j++) {
                buffer[j] = grid[i][j] + Math.min(buffer[j], buffer[j - 1]);
            }
        }

        return buffer[y - 1];
    }
}