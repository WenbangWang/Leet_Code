package com.wwb.leetcode.medium;

/**
 * Given an integer n, generate a square matrix filled with elements from 1 to n^2 in spiral order.
 *
 * For example,
 * Given n = 3,
 *
 * You should return the following matrix:
 * [
 *   [ 1, 2, 3 ],
 *   [ 8, 9, 4 ],
 *   [ 7, 6, 5 ]
 * ]
 */
public class No59 {

    public int[][] generateMatrix(int n) {
        int[][] matrix = new int[n][n];
        int x1 = 0;
        int y1 = 0;
        int x2 = n - 1;
        int y2 = n - 1;
        int value = 1;

        while(x1 <= x2 && y1 <= y2) {
            // top
            for(int i = y1; i <= y2; i++) {
                matrix[x1][i] = value++;
            }

            // right
            for(int i = x1 + 1; i <= x2; i++) {
                matrix[i][y2] = value++;
            }

            // bottom
            if(x1 != x2) {
                for(int i = y2 - 1; i >= y1; i--) {
                    matrix[x2][i] = value++;
                }
            }

            // left
            if(y1 != y2) {
                for(int i = x2 - 1; i > x1; i--) {
                    matrix[i][y1] = value++;
                }
            }

            x1++;
            y1++;
            x2--;
            y2--;
        }

        return matrix;
    }
}
