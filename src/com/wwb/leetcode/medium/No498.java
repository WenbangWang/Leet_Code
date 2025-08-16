package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Given an m x n matrix mat, return an array of all the elements of the array in a diagonal order.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <img src="../doc-files/498.jpg" />
 * <p>
 * Input: mat = [[1,2,3],[4,5,6],[7,8,9]]
 * <p>
 * Output: [1,2,4,7,5,3,6,8,9]
 * <p>
 * Example 2:
 * <p>
 * Input: mat = [[1,2],[3,4]]
 * <p>
 * Output: [1,2,3,4]
 * <p>
 * <p>
 * Constraints:
 * <p>
 * m == mat.length
 * <p>
 * n == mat[i].length
 * <p>
 * 1 <= m, n <= 10^4
 * <p>
 * 1 <= m * n <= 10^4
 * <p>
 * -10^5 <= mat[i][j] <= 10^5
 */
public class No498 {
    public int[] findDiagonalOrder(int[][] mat) {
        int m = mat.length;
        int n = mat[0].length;
        int[] result = new int[m * n];
        int index = 0;
        int i = 0;
        int j = 0;

        while (index < m * n) {

        }


        return result;
    }

    private int[] solution1(int[][] mat) {
        int m = mat.length;
        int n = mat[0].length;
        int[] result = new int[m * n];
        int index = 0;
        // for all the cells on the same diagonal, the sum of coordinate of each cell is always the same.
        // use the sum as the index of the map and value to represent the value of each cell on the diagonal.
        Map<Integer, LinkedList<Integer>> indexSumToValue = new HashMap<>();

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int sum = i + j;
                indexSumToValue.putIfAbsent(sum, new LinkedList<>());

                // for even sum index, we are going up, hence the values will be reversed.
                if (sum % 2 == 0) {
                    indexSumToValue.get(sum).addFirst(mat[i][j]);
                } else {
                    // for odd sum index, we are going down, hence the normal order
                    indexSumToValue.get(sum).addLast(mat[i][j]);
                }
            }
        }

        for (int i = 0; i <= m - 1 + n - 1; i++) {
            for (int value : indexSumToValue.get(i)) {
                result[index++] = value;
            }
        }

        return result;
    }

    private int[] solution2(int[][] mat) {
        int m = mat.length;
        int n = mat[0].length;
        int[] result = new int[m * n];
        int row = 0;
        int column = 0;

        for (int i = 0; i < result.length; i++) {
            result[i] = mat[row][column];
            int sum = row + column;

            // going up
            if (sum % 2 == 0) {
                if (column == n - 1) {
                    // last column, going down
                    row++;
                } else if (row == 0) {
                    // at first row, going right
                    column++;
                } else {
                    row--;
                    column++;
                }
            } else {
                // going down
                if (row == m - 1) {
                    // last row, going right
                   column++;
                } else if (column == 0) {
                    // first column, going down
                    row++;
                } else {
                    row++;
                    column--;
                }
            }
        }

        return result;
    }
}
