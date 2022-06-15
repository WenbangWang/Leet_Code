package com.wwb.leetcode.medium;

/**
 * You are given a 0-indexed m x n binary matrix grid.
 * <p>
 * In one operation, you can choose any i and j that meet the following conditions:
 * <p>
 * 0 <= i < m
 * 0 <= j < n
 * grid[i][j] == 1
 * and change the values of all cells in row i and column j to zero.
 * <p>
 * Return the minimum number of operations needed to remove all 1's from grid.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: grid = [[1,1,1],[1,1,1],[0,1,0]]
 * Output: 2
 * Explanation:
 * In the first operation, change all cell values of row 1 and column 1 to zero.
 * In the second operation, change all cell values of row 0 and column 0 to zero.
 * Example 2:
 * <p>
 * <p>
 * Input: grid = [[0,1,0],[1,0,1],[0,1,0]]
 * Output: 2
 * Explanation:
 * In the first operation, change all cell values of row 1 and column 0 to zero.
 * In the second operation, change all cell values of row 2 and column 1 to zero.
 * Note that we cannot perform an operation using row 1 and column 1 because grid[1][1] != 1.
 * Example 3:
 * <p>
 * <p>
 * Input: grid = [[0,0],[0,0]]
 * Output: 0
 * Explanation:
 * There are no 1's to remove so return 0.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * m == grid.length
 * n == grid[i].length
 * 1 <= m, n <= 15
 * 1 <= m * n <= 15
 * grid[i][j] is either 0 or 1.
 */
public class No2174 {
    public int removeOnes(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int minFlips = Integer.MAX_VALUE;
        int[] rowValues = new int[n];
        int[] colValues = new int[m];

        for (int row = 0; row < m; row++) {
            for (int col = 0; col < n; col++) {
                if (grid[row][col] == 0) {
                    continue;
                }

                for (int r = 0; r < m; r++) {
                    colValues[r] = grid[r][col];
                    grid[r][col] = 0;
                }
                for (int c = 0; c < n; c++) {
                    rowValues[c] = grid[row][c];
                    grid[row][c] = 0;
                }
                minFlips = Math.min(minFlips, (1 + removeOnes(grid)));

                //undo operation in reverse order so that grid[i][j] value is restored correctly
                for (int c = 0; c < n; c++) {
                    grid[row][c] = rowValues[c];
                }
                for (int r = 0; r < m; r++) {
                    grid[r][col] = colValues[r];
                }
            }
        }
        return ((minFlips == Integer.MAX_VALUE) ? 0 : minFlips);
    }
}
