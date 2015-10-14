package com.wwb.leetcode.tags.dp;

import java.util.Arrays;

/**
 * Given a 2D binary matrix filled with 0's and 1's,
 * find the largest rectangle containing all ones and return its area.
 */
public class No85 {

    public int maximalRectangle(char[][] matrix) {
        if(matrix == null || matrix.length == 0 || matrix[0] == null || matrix[0].length == 0) {
            return 0;
        }

        int length = matrix.length;
        int width = matrix[0].length;
        int result = 0;
        int[] left = new int[width];
        int[] right = new int[width];
        int[] height = new int[width];

        Arrays.fill(right, width);

        for(int i = 0; i < length; i++) {
            int currentLeft = 0;
            int currentRight = width;

            for(int j = 0; j < width; j++) {
                if(isValid(matrix, i, j)) {
                    height[j]++;
                } else {
                    height[j] = 0;
                }
            }

            for(int j = 0; j < width; j++) {
                if(isValid(matrix, i, j)) {
                    left[j] = Math.max(left[j], currentLeft);
                } else {
                    left[j] = 0;
                    currentLeft = j + 1;
                }
            }

            for(int j = width - 1; j >= 0; j--) {
                if(isValid(matrix, i, j)) {
                    right[j] = Math.min(right[j], currentRight);
                } else {
                    right[j] = width;
                    currentRight = j;
                }
            }

            for(int j = 0; j < width; j++) {
                result = Math.max(result, (right[j] - left[j]) * height[j]);
            }
        }

        return result;
    }

    private boolean isValid(char[][] matrix, int i, int j) {
        return matrix[i][j] == '1';
    }
}
