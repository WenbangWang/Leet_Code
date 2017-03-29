package com.wwb.leetcode.medium;

/**
 * Given two sparse matrices A and B, return the result of AB.
 *
 * You may assume that A's column number is equal to B's row number.
 *
 * Example:
 *
 * A = [
 *  [ 1, 0, 0],
 *  [-1, 0, 3]
 * ]
 *
 * B = [
 *  [ 7, 0, 0 ],
 *  [ 0, 0, 0 ],
 *  [ 0, 0, 1 ]
 * ]
 *
 *
 *      |  1 0 0 |   | 7 0 0 |   |  7 0 0 |
 * AB = | -1 0 3 | x | 0 0 0 | = | -7 0 3 |
 *                   | 0 0 1 |
 */
public class No311 {
    public int[][] multiply(int[][] A, int[][] B) {
        int rowA = A.length;
        int column = A[0].length;
        int columnB = B[0].length;
        int[][] C = new int[rowA][columnB];

        for(int i = 0; i < rowA; i++) {
            for(int j = 0; j < column; j++) {
                if(A[i][j] != 0) {
                    for(int k = 0; k < columnB; k++) {
                        if(B[j][k] != 0) {
                            C[i][k] += A[i][j] * B[j][k];
                        }
                    }
                }
            }
        }

        return C;
    }
}
