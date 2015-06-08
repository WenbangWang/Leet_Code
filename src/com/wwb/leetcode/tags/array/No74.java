package com.wwb.leetcode.tags.array;

/**
 * Write an efficient algorithm that searches for a value in an m x n matrix. This matrix has the following properties:
 *
 * Integers in each row are sorted from left to right.
 * The first integer of each row is greater than the last integer of the previous row.
 * For example,
 *
 * Consider the following matrix:
 *
 * [
 *   [1,   3,  5,  7],
 *   [10, 11, 16, 20],
 *   [23, 30, 34, 50]
 * ]
 * Given target = 3, return true.
 */
public class No74 {

    public boolean searchMatrix(int[][] matrix, int target) {
        return solution1(matrix, target);
    }

    private boolean solution1(int[][] matrix, int target) {
        int row = matrix.length;
        int col = matrix[0].length;
        int start = 0;
        int end = row * col - 1;

        while(start <= end) {
            int mid = start + (end - start) / 2;
            int current = matrix[mid / col][mid % col];

            if(target == current) {
                return true;
            } else if(target > current) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        return false;
    }
}