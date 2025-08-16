package com.wwb.leetcode.hard;

/**
 * Given an m x n integers matrix, return the length of the longest increasing path in matrix.
 * <p>
 * From each cell, you can either move in four directions: left, right, up, or down. You may not move diagonally or move outside the boundary (i.e., wrap-around is not allowed).
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/329_1.jpg" />
 *
 * Input: matrix = [[9,9,4],[6,6,8],[2,1,1]]
 * Output: 4
 * Explanation: The longest increasing path is [1, 2, 6, 9].
 * Example 2:
 *
 * <img src="../doc-files/329_2.jpg" />
 *
 * Input: matrix = [[3,4,5],[3,2,6],[2,2,1]]
 * Output: 4
 * Explanation: The longest increasing path is [3, 4, 5, 6]. Moving diagonally is not allowed.
 * Example 3:
 *
 * Input: matrix = [[1]]
 * Output: 1
 *
 *
 * Constraints:
 *
 * m == matrix.length
 * n == matrix[i].length
 * 1 <= m, n <= 200
 * 0 <= matrix[i][j] <= 2^31 - 1
 * </pre>
 */
public class No329 {
    // O(mn)
    public int longestIncreasingPath(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] cache = new int[m][n];
        int result = 0;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result = Math.max(result, dfs(matrix, dirs, cache, i, j, Integer.MIN_VALUE));
            }
        }

        return result;
    }

    private int dfs(int[][] matrix, int[][] dirs, int[][] cache, int x, int y, int previous) {
        int m = matrix.length;
        int n = matrix[0].length;

        if (x == m || y == n || x < 0 || y < 0) {
            return 0;
        }

        if (matrix[x][y] <= previous) {
            return 0;
        }

        if (cache[x][y] > 0) {
            return cache[x][y];
        }

        int result = 0;

        for (int[] dir : dirs) {
            result = Math.max(result, dfs(matrix, dirs, cache, x + dir[0], y + dir[1], matrix[x][y]));
        }

        cache[x][y] = result + 1;

        return result + 1;
    }
}
