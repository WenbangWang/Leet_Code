package com.wwb.leetcode.medium;

/**
 * Given a m x n matrix, if an element is 0, set its entire row and column to 0. Do it in place.
 *
 * Follow up:
 * Did you use extra space?
 * A straight forward solution using O(mn) space is probably a bad idea.
 * A simple improvement uses O(m + n) space, but still not the best solution.
 * Could you devise a constant space solution?
 */
public class No73 {

    public void setZeroes(int[][] matrix) {
        // solution1(matrix);
        solution2(matrix);
    }

    private void solution1(int[][] matrix) {
        boolean[] row = new boolean[matrix.length];
        boolean[] column = new boolean[matrix[0].length];

        for(int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[0].length; j++) {
                if(matrix[i][j] == 0) {
                    row[i] = true;
                    column[j] = true;
                }
            }
        }

        for(int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[0].length; j++) {
                if(row[i] || column[j]) {
                    matrix[i][j] = 0;
                }
            }
        }
    }

    private void solution2(int[][] matrix) {
        boolean firstRowHasZero = false;
        boolean firstColHasZero = false;
        int x = matrix[0].length;
        int y = matrix.length;

        for(int i = 0; i < x; i++) {
            if(matrix[0][i] == 0) {
                firstRowHasZero = true;
                break;
            }
        }

        for(int i = 0; i < y; i++) {
            if(matrix[i][0] == 0) {
                firstColHasZero = true;
                break;
            }
        }

        for(int i = 1; i < y; i++) {
            for(int j = 1; j < x; j++) {
                if(matrix[i][j] == 0) {
                    matrix[0][j] = 0;
                    matrix[i][0] = 0;
                }
            }
        }

        for(int i = 1; i < x; i++) {
            if(matrix[0][i] == 0) {
                for(int j = 1; j< y; j++) {
                    matrix[j][i] = 0;
                }
            }
        }

        for(int i = 1; i < y; i++) {
            if(matrix[i][0] == 0) {
                for(int j = 0; j < x; j++) {
                    matrix[i][j] = 0;
                }
            }
        }

        if(firstRowHasZero) {
            for(int i = 0; i < x; i++) {
                matrix[0][i] = 0;
            }
        }

        if(firstColHasZero) {
            for(int i = 0; i < y; i++) {
                matrix[i][0] = 0;
            }
        }
    }
}