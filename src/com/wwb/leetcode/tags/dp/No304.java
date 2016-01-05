package com.wwb.leetcode.tags.dp;

/**
 * Given a 2D matrix matrix, find the sum of the elements inside the rectangle defined by its upper left corner (row1, col1)
 * and lower right corner (row2, col2).
 *
 * Range Sum Query 2D
 * The above rectangle (with the red border) is defined by (row1, col1) = (2, 1) and (row2, col2) = (4, 3),
 * which contains sum = 8.
 *
 * Example:
 * Given matrix = [
 *   [3, 0, 1, 4, 2],
 *   [5, 6, 3, 2, 1],
 *   [1, 2, 0, 1, 5],
 *   [4, 1, 0, 1, 7],
 *   [1, 0, 3, 0, 5]
 * ]
 *
 * sumRegion(2, 1, 4, 3) -> 8
 * sumRegion(1, 1, 2, 2) -> 11
 * sumRegion(1, 2, 2, 4) -> 12
 * Note:
 * You may assume that the matrix does not change.
 * There are many calls to sumRegion function.
 * You may assume that row1 ≤ row2 and col1 ≤ col2.
 */
public class No304 {

    public class NumMatrix {

        private int[][] matrix;
        public NumMatrix(int[][] matrix) {
            if(matrix == null || matrix.length == 0 || matrix[0] == null || matrix[0].length == 0) {
                return;
            }

            int m = matrix.length;
            int n = matrix[0].length;

            this.matrix = new int[m + 1][n + 1];

            for(int i = 1; i <= m; i++) {
                for(int j = 1; j <= n; j++) {
                    this.matrix[i][j] = this.matrix[i - 1][j] + this.matrix[i][j - 1] - this.matrix[i - 1][j - 1] + matrix[i - 1][j - 1];
                }
            }
        }

        public int sumRegion(int row1, int col1, int row2, int col2) {
            int rowMax = Math.max(row1, row2);
            int rowMin = Math.min(row1, row2);
            int colMax = Math.max(col1, col2);
            int colMin = Math.min(col1, col2);

            return this.matrix[rowMax + 1][colMax + 1] - this.matrix[rowMin][colMax + 1] - this.matrix[rowMax + 1][colMin] + this.matrix[rowMin][colMin];
        }
    }
}
