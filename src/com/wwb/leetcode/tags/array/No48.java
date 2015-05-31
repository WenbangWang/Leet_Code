package com.wwb.leetcode.tags.array;

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
                //top
                int top = matrix[first][i];
                //right
                int right = matrix[i][last];
                //bottom
                int bottom = matrix[last][last-offset];
                //left
                int left = matrix[last - offset][first];

                //top -> left
                matrix[first][i] = left;
                //left -> bottom
                matrix[last - offset][first] = bottom;
                //bottom -> right
                matrix[last][last-offset] = right;
                //right -> top
                matrix[i][last] = top;
            }
        }
    }
}