package com.wwb.leetcode.tags.binarysearch;

/**
 * Write an efficient algorithm that searches for a value in an m x n matrix.
 * This matrix has the following properties:
 *
 * Integers in each row are sorted in ascending from left to right.
 * Integers in each column are sorted in ascending from top to bottom.
 * For example,
 *
 * Consider the following matrix:
 *
 * [
 *    [1,   4,  7, 11, 15],
 *    [2,   5,  8, 12, 19],
 *    [3,   6,  9, 16, 22],
 *    [10, 13, 14, 17, 24],
 *    [18, 21, 23, 26, 30]
 * ]
 * Given target = 5, return true.
 *
 * Given target = 20, return false.
 */
public class No240 {

    public boolean searchMatrix(int[][] matrix, int target) {
        if(matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return false;
        }

        int row = 0;
        int column = matrix[0].length - 1;

        while(row <= matrix.length - 1 && column >= 0) {
            int value = matrix[row][column];
            if(value == target) {
                return true;
            } else if(target > value) {
                row++;
            } else {
                column--;
            }
        }

        return false;
    }

    private boolean binarySearch(int[][] matrix, int target, int x1, int y1, int x2, int y2) {
        if(x1 > x2 || y1 > y2) {
            return false;
        }

        int midX = (x1 - x2) / 2 + x2;
        int midY = (y1 - y2) / 2 + y2;

        if(matrix[midY][midX] == target) {
            return true;
        } else if(target >= matrix[y1][x1] && target < matrix[midY][midX]) {
            return binarySearch(matrix, target, x1, y1, midX, midY);
        } else if(target >= matrix[midY][x1] && target <= matrix[y2][midX]) {
            return binarySearch(matrix, target, x1, midY, midX, y2);
        } else if(target >= matrix[y1][midX] && target <= matrix[midY][x2]) {
            return binarySearch(matrix, target, midX, y1, x2, midY);
        } else if(target > matrix[midY][midX] && target <= matrix[y2][x2]) {
            return binarySearch(matrix, target, midX, midY, x2, y2);
        }

        return false;
    }
}