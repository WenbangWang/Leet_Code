package com.wwb.leetcode.easy;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * An image is represented by an m x n integer grid image where image[i][j] represents the pixel value of the image.
 * <p>
 * You are also given three integers sr, sc, and color.
 * You should perform a flood fill on the image starting from the pixel image[sr][sc].
 * <p>
 * To perform a flood fill, consider the starting pixel,
 * plus any pixels connected 4-directionally to the starting pixel of the same color as the starting pixel,
 * plus any pixels connected 4-directionally to those pixels (also with the same color),
 * and so on. Replace the color of all of the aforementioned pixels with color.
 * <p>
 * Return the modified image after performing the flood fill.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: image = [[1,1,1],[1,1,0],[1,0,1]], sr = 1, sc = 1, color = 2
 * Output: [[2,2,2],[2,2,0],[2,0,1]]
 * Explanation: From the center of the image with position (sr, sc) = (1, 1) (i.e., the red pixel),
 * all pixels connected by a path of the same color as the starting pixel (i.e., the blue pixels)
 * are colored with the new color.
 * Note the bottom corner is not colored 2, because it is not 4-directionally connected to the starting pixel.
 * Example 2:
 * <p>
 * Input: image = [[0,0,0],[0,0,0]], sr = 0, sc = 0, color = 0
 * Output: [[0,0,0],[0,0,0]]
 * Explanation: The starting pixel is already colored 0, so no changes are made to the image.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * m == image.length
 * n == image[i].length
 * 1 <= m, n <= 50
 * 0 <= image[i][j], color < 2^16
 * 0 <= sr < m
 * 0 <= sc < n
 */
public class No733 {
    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        return solution1(image, sr, sc, color);
    }

    // bfs
    private int[][] solution1(int[][] image, int sr, int sc, int color) {
        int originalColor = image[sr][sc];

        if (originalColor == color) {
            return image;
        }

        Queue<int[]> queue = new ArrayDeque<>();
        int[][] directions = new int[][]{
            {-1, 0}, // up
            {1, 0}, // down
            {0, -1}, // left
            {0, 1} // right
        };

        queue.offer(new int[]{sr, sc});

        while (!queue.isEmpty()) {
            int[] point = queue.poll();

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

    // dfs
    private int[][] solution2(int[][] image, int sr, int sc, int newColor) {
        int oldColor = image[sr][sc];
        if (oldColor != newColor) {
            dfs(image, sr, sc, oldColor, newColor);
        }
        return image;
    }

    private void dfs(int[][] image, int r, int c, int oldColor, int newColor) {
        // Boundary check
        if (r < 0 || r >= image.length || c < 0 || c >= image[0].length || image[r][c] != oldColor) {
            return;
        }

        image[r][c] = newColor;

        // Explore 4 directions
        dfs(image, r + 1, c, oldColor, newColor);
        dfs(image, r - 1, c, oldColor, newColor);
        dfs(image, r, c + 1, oldColor, newColor);
        dfs(image, r, c - 1, oldColor, newColor);
    }
}
