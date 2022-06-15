package com.wwb.leetcode.medium;

import java.util.stream.IntStream;

/**
 * You are given an m x n binary matrix grid.
 * <p>
 * In one operation, you can choose any row or column and flip each value in that row or column (i.e., changing all 0's to 1's, and all 1's to 0's).
 * <p>
 * Return true if it is possible to remove all 1's from grid using any number of operations or false otherwise.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: grid = [[0,1,0],[1,0,1],[0,1,0]]
 * Output: true
 * Explanation: One possible way to remove all 1's from grid is to:
 * - Flip the middle row
 * - Flip the middle column
 * Example 2:
 * <p>
 * <p>
 * Input: grid = [[1,1,0],[0,0,0],[0,0,0]]
 * Output: false
 * Explanation: It is impossible to remove all 1's from grid.
 * Example 3:
 * <p>
 * <p>
 * Input: grid = [[0]]
 * Output: true
 * Explanation: There are no 1's in grid.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * m == grid.length
 * n == grid[i].length
 * 1 <= m, n <= 300
 * grid[i][j] is either 0 or 1.
 */
public class No2128 {
    public boolean removeOnes(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;

        IntStream.range(0, n).forEach(column -> {
            if (grid[0][column] == 1) {
                flipColumn(column, grid);
            }
        });

        for (int row = 1; row < m; row++) {
            for (int column = 1; column < n; column++) {
                if (grid[row][column] != grid[row][column - 1]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void flipColumn(int column, int[][] grid) {
        IntStream.range(0, grid.length).forEach(row -> {
            grid[row][column] = 1 - grid[row][column];
        });
    }
}
