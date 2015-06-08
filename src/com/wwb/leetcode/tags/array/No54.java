package com.wwb.leetcode.tags.array;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a matrix of m x n elements (m rows, n columns), return all elements of the matrix in spiral order.
 *
 * For example,
 * Given the following matrix:
 *
 * [
 *    [ 1, 2, 3 ],
 *    [ 4, 5, 6 ],
 *    [ 7, 8, 9 ]
 * ]
 * You should return [1,2,3,6,9,8,7,4,5].
 */
public class No54 {

    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if(matrix.length == 0) {
            return result;
        }
        int x1 = 0;
        int y1 = 0;
        int x2 = matrix.length - 1;
        int y2 = matrix[0].length - 1;

        while(x1 <= x2 && y1 <= y2) {
            //top
            for(int i = y1; i <= y2; i++) {
                result.add(matrix[x1][i]);
            }

            //right
            for(int i = x1 + 1; i <= x2; i++) {
                result.add(matrix[i][y2]);
            }

            if(x1 != x2) {
                //bottom
                for(int i = y2 - 1 ; i >= y1; i--) {
                    result.add(matrix[x2][i]);
                }
            }

            if(y1 != y2) {
                //left
                for(int i = x2 - 1; i > x1; i--) {
                    result.add(matrix[i][y1]);
                }
            }

            x1++;
            y1++;
            x2--;
            y2--;

        }
        return result;
    }
}