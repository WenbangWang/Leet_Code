package com.wwb.leetcode.medium;

/**
 * You are given an n x n 2D matrix representing an image.
 *
 * Rotate the image by 90 degrees (clockwise).

 * Follow up:
 * Could you do this in-place?
 */
public class No48 {

    public void rotate(int[][] matrix) {
        int n = matrix.length;

        for(int layer = 0; layer < n / 2; layer++) {
            int first = layer;
            int last = n - first - 1;

            for(int i = first; i < last; i++) {
                int offset = i - first;
                int topLeft = matrix[first][i];
                int topRight = matrix[i][last];
                int bottomRight = matrix[last][last-offset];
                int bottomLeft = matrix[last - offset][first];

                // bottom-left -> top-left
                matrix[first][i] = bottomLeft;
                // bottom-right -> bottom-left
                matrix[last - offset][first] = bottomRight;
                //top-right -> bottom-right
                matrix[last][last-offset] = topRight;
                // top-left -> top-right
                matrix[i][last] = topLeft;
            }
        }
    }
}
