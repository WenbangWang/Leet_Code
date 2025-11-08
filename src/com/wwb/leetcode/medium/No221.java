package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * Given a 2D binary matrix filled with 0's and 1's, find the largest square containing all 1's and return its area.
 *
 * For example, given the following matrix:
 *
 * 1 0 1 0 0
 * 1 0 1 1 1
 * 1 1 1 1 1
 * 1 0 0 1 0
 * Return 4.
 */
public class No221 {

    public int maximalSquare(char[][] matrix) {
        return solution1(matrix);
    }

    private int solution1(char[][] matrix) {
        if(matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return 0;
        }

        // number of consecutive '1's
        int[][] dp = new int[matrix.length + 1][matrix[0].length + 1];
        int result = 0;

        for(int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[0].length; j++) {
                if(matrix[i][j] == '1') {
                    dp[i + 1][j + 1] = Math.min(dp[i][j], Math.min(dp[i + 1][j], dp[i][j + 1])) + 1;
                    result = Math.max(result, dp[i + 1][j + 1]);
                }
            }
        }

        return result * result;
    }

    private int solution2(char[][] matrix) {
        if(matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return 0;
        }

        int[] currentRow = new int[matrix[0].length + 1];
        int result = 0;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                int[] lastRow = Arrays.copyOf(currentRow, currentRow.length);
                currentRow = new int[matrix[0].length + 1];

                if (matrix[i][j] == '1') {
                    currentRow[j + 1] = Math.min(currentRow[j], Math.min(lastRow[j], lastRow[j + 1])) + 1;
                    result = Math.max(result, currentRow[j + 1]);
                }
            }
        }

        return result * result;
    }

    private int solution3(char[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return 0;
        }

        int rows = matrix.length;
        int cols = matrix[0].length;
        int result = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // convert char to int (0 or 1)
                if (matrix[i][j] == '1') {
                    // only compute dp if not on first row/col
                    if (i > 0 && j > 0) {
                        int top = matrix[i - 1][j] - '0';
                        int left = matrix[i][j - 1] - '0';
                        int topLeft = matrix[i - 1][j - 1] - '0';
                        int curr = Math.min(top, Math.min(left, topLeft)) + 1;
                        // store back as char
                        matrix[i][j] = (char) (curr + '0');
                    }
                    // update max side
                    result = Math.max(result, matrix[i][j] - '0');
                }
            }
        }

        return result * result;
    }
}
