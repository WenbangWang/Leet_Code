package com.wwb.leetcode.medium;

import java.util.LinkedList;
import java.util.Queue;

/**
 * You are given an n x n binary matrix grid where 1 represents land and 0 represents water.
 * <p>
 * An island is a 4-directionally connected group of 1's not connected to any other 1's. There are exactly two islands in grid.
 * <p>
 * You may change 0's to 1's to connect the two islands to form one island.
 * <p>
 * Return the smallest number of 0's you must flip to connect the two islands.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: grid = [[0,1],[1,0]]
 * Output: 1
 * Example 2:
 *
 * Input: grid = [[0,1,0],[0,0,0],[0,0,1]]
 * Output: 2
 * Example 3:
 *
 * Input: grid = [[1,1,1,1,1],[1,0,0,0,1],[1,0,1,0,1],[1,0,0,0,1],[1,1,1,1,1]]
 * Output: 1
 *
 *
 * Constraints:
 *
 * n == grid.length == grid[i].length
 * 2 <= n <= 100
 * grid[i][j] is either 0 or 1.
 * There are exactly two islands in grid.
 * </pre>
 */
public class No934 {
    public int shortestBridge(int[][] grid) {
        int[][] dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        Queue<int[]> queue = new LinkedList<>();
        int n = grid.length;

        // paint the first found island
        for (int i = 0; i < n && queue.isEmpty(); i++) {
            for (int j = 0; j < n && queue.isEmpty(); j++) {
                paint(grid, dirs, i, j, queue);
            }
        }

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                int[] coord = queue.poll();

                for (int[] dir : dirs) {
                    int x = coord[0] + dir[0];
                    int y = coord[1] + dir[1];

                    if (Math.max(x, y) == n || Math.min(x, y) < 0) {
                        continue;
                    }

                    if (grid[x][y] == 1) {
                        return grid[coord[0]][coord[1]] - 2;
                    }

                    if (grid[x][y] == 0) {
                        grid[x][y] = grid[coord[0]][coord[1]] + 1;
                        queue.offer(new int[]{x, y});
                    }
                }
            }
        }

        return 0;
    }

    private void paint(int[][] grid, int[][] dirs, int x, int y, Queue<int[]> queue) {
        if (Math.max(x, y) == grid.length || Math.min(x, y) < 0 || grid[x][y] != 1) {
            return;
        }

        grid[x][y] = 2;
        queue.offer(new int[]{x, y});

        for (int[] dir : dirs) {
            paint(grid, dirs, x + dir[0], y + dir[1], queue);
        }
    }
}
