package com.wwb.leetcode.tags.dfs;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * An image is represented by an m x n integer grid image where image[i][j] represents the pixel value of the image.
 *
 * You are also given three integers sr, sc, and color.
 * You should perform a flood fill on the image starting from the pixel image[sr][sc].
 *
 * To perform a flood fill, consider the starting pixel,
 * plus any pixels connected 4-directionally to the starting pixel of the same color as the starting pixel,
 * plus any pixels connected 4-directionally to those pixels (also with the same color),
 * and so on. Replace the color of all of the aforementioned pixels with color.
 *
 * Return the modified image after performing the flood fill.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: image = [[1,1,1],[1,1,0],[1,0,1]], sr = 1, sc = 1, color = 2
 * Output: [[2,2,2],[2,2,0],[2,0,1]]
 * Explanation: From the center of the image with position (sr, sc) = (1, 1) (i.e., the red pixel),
 * all pixels connected by a path of the same color as the starting pixel (i.e., the blue pixels)
 * are colored with the new color.
 * Note the bottom corner is not colored 2, because it is not 4-directionally connected to the starting pixel.
 * Example 2:
 *
 * Input: image = [[0,0,0],[0,0,0]], sr = 0, sc = 0, color = 0
 * Output: [[0,0,0],[0,0,0]]
 * Explanation: The starting pixel is already colored 0, so no changes are made to the image.
 *
 *
 * Constraints:
 *
 * m == image.length
 * n == image[i].length
 * 1 <= m, n <= 50
 * 0 <= image[i][j], color < 2^16
 * 0 <= sr < m
 * 0 <= sc < n
 */
public class No733 {
    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        Queue<int[]> queue = new ArrayDeque<>();
        int originalColor = image[sr][sc];
        int[][] directions = new int[][]{
            {-1, 0}, // up
            {1, 0}, // down
            {0, -1}, // left
            {0, 1} // right
        };

        queue.offer(new int[]{sr, sc});

        while (!queue.isEmpty()) {
            int[] point = queue.poll();

            if (image[point[0]][point[1]] == originalColor) {
                continue;
            }

            image[point[0]][point[1]] = color;

            for (int[] direction : directions) {
                int x = point[0] + direction[0];
                int y = point[1] + direction[1];

                if (x >= 0 && x < image.length && y >= 0 && y < image[0].length && image[x][y] == originalColor && image[x][y] >> 16 != 1) {
                    queue.offer(new int[]{x, y});
                    image[x][y] += (1 << 16);
                }
            }
        }


        return image;
    }
}
