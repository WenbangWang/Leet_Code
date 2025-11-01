package com.wwb.leetcode.hard;

import java.util.LinkedList;
import java.util.Queue;

/**
 * You want to build a house on an empty land which reaches all buildings in the shortest amount of distance. You can only move up, down, left and right. You are given a 2D grid of values 0, 1 or 2, where:
 * <p>
 * Each 0 marks an empty land which you can pass by freely.
 * <p>
 * Each 1 marks a building which you cannot pass through.
 * <p>
 * Each 2 marks an obstacle which you cannot pass through.
 *
 * <pre>
 * Example:
 *
 * Input: [[1,0,2,0,1],[0,0,0,0,0],[0,0,1,0,0]]
 *
 * 1 - 0 - 2 - 0 - 1
 * |   |   |   |   |
 * 0 - 0 - 0 - 0 - 0
 * |   |   |   |   |
 * 0 - 0 - 1 - 0 - 0
 *
 * Output: 7
 *
 * Explanation: Given three buildings at (0,0), (0,4), (2,2), and an obstacle at (0,2),
 *              the point (1,2) is an ideal empty land to build a house, as the total
 *              travel distance of 3+3+1=7 is minimal. So return 7.
 * Note:
 * There will be at least one building. If it is not possible to build such house according to the above rules, return -1.
 * </pre>
 */
public class No317 {
    public int shortestDistance(int[][] grid) {
        Queue<int[]> buildings = new LinkedList<>();
        int[][] dirs = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        int[][] reached = new int[grid.length][grid[0].length];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == 1) {
                    buildings.offer(new int[]{i, j});
                }
            }
        }
        int numberOfBuildings = buildings.size();

        for (int i = 0; i < numberOfBuildings; i++) {
            int[] building = buildings.poll();
            paint(grid, dirs, reached, building[0], building[1]);
        }

        int result = Integer.MAX_VALUE;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == 0 || grid[i][j] == 1 || grid[i][j] == 2 || reached[i][j] != numberOfBuildings) {
                    continue;
                }

                result = Math.min(result, grid[i][j]);
            }
        }

        return result - 2;
    }

    private void paint(int[][] grid, int[][] dirs, int[][] reached, int row, int col) {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[grid.length][grid[0].length];

        queue.offer(new int[]{row, col});

        while (!queue.isEmpty()) {
            for (int[] dir : dirs) {
                int newRow = row + dir[0];
                int newCol = col + dir[0];

                if (newRow < 0 || newRow == grid.length || newCol < 0 || newCol == grid[0].length) {
                    continue;
                }

                if (visited[newRow][newCol]) {
                    continue;
                }

                if (grid[newRow][newCol] == 1 || grid[newRow][newCol] == 2) {
                    continue;
                }

                visited[newRow][newCol] = true;
                reached[newCol][newCol]++;
                queue.offer(new int[]{newRow, newCol});

                if (grid[newRow][newCol] == 0) {
                    grid[newRow][newCol] = 3;
                } else {
                    grid[newRow][newCol]++;
                }
            }
        }
    }
}
