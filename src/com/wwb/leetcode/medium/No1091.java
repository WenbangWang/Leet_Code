package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Given an n x n binary matrix grid, return the length of the shortest clear path in the matrix. If there is no clear path, return -1.
 * <p>
 * A clear path in a binary matrix is a path from the top-left cell (i.e., (0, 0)) to the bottom-right cell (i.e., (n - 1, n - 1)) such that:
 * <p>
 * All the visited cells of the path are 0.
 * All the adjacent cells of the path are 8-directionally connected (i.e., they are different and they share an edge or a corner).
 * The length of a clear path is the number of visited cells of this path.
 * <p>
 * <p>
 * <p>
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/1091_1.png" />
 *
 * Input: grid = [[0,1],[1,0]]
 * Output: 2
 * Example 2:
 *
 * <img src="../doc-files/1091_1.png" />
 *
 * Input: grid = [[0,0,0],[1,1,0],[1,1,0]]
 * Output: 4
 * Example 3:
 *
 * Input: grid = [[1,0,0],[1,1,0],[1,1,0]]
 * Output: -1
 *
 *
 * Constraints:
 *
 * n == grid.length
 * n == grid[i].length
 * 1 <= n <= 100
 * grid[i][j] is 0 or 1
 * </pre>
 */
public class No1091 {
    public int shortestPathBinaryMatrix(int[][] grid) {
        int result = shortestPathBinaryMatrix(grid, 0, 0);

        return result == Integer.MAX_VALUE ? -1 : result;
    }

    // DFS, TLE...
    private int solution1(int[][] grid) {
        int result = shortestPathBinaryMatrix(grid, 0, 0);

        return result == Integer.MAX_VALUE ? -1 : result;
    }

    // BFS
    private int solution2(int[][] grid) {
        int n = grid.length;

        if (grid[0][0] == 1 || grid[n - 1][n - 1] == 1) {
            return -1;
        }

        Queue<int[]> queue = new LinkedList<>();
        int result = 0;

        queue.offer(new int[]{0, 0});

        grid[0][0] -= 2;

        while (!queue.isEmpty()) {
            int size = queue.size();
            result++;

            for (int i = 0; i < size; i++) {
                int[] cell = queue.poll();

                if (cell[0] == n - 1 && cell[1] == n - 1) {
                    return result;
                }

                for (int j = -1; j < 2; j++) {
                    for (int k = -1; k < 2; k++) {
                        if (j == 0 && k == 0) {
                            continue;
                        }

                        int x = cell[0] + j;
                        int y = cell[1] + k;

                        if (x < 0 || y < 0 || x == n || y == n || grid[x][y] < 0 || grid[x][y] == 1) {
                            continue;
                        }

                        grid[x][y] -= 2;
                        queue.offer(new int[]{x, y});
                    }
                }
            }
        }

        return -1;
    }

    private int shortestPathBinaryMatrix(int[][] grid, int x, int y) {
        int n = grid.length;

        if (x < 0 || x == n || y < 0 || y == n) {
            return Integer.MAX_VALUE;
        }

        // visited
        if (grid[x][y] < 0) {
            return Integer.MAX_VALUE;
        }

        if (grid[x][y] == 1) {
            return Integer.MAX_VALUE;
        }

        if (x == n - 1 && y == n - 1) {
            return 1;
        }

        // now that grid[x][y] == 0 which means current cell is visit-able

        // mark current cell visited
        grid[x][y] -= 2;

        List<Integer> neighbours = new ArrayList<>(8);

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                neighbours.add(shortestPathBinaryMatrix(grid, x + i, y + j));
            }
        }

        // un-mark current cell visited
        grid[x][y] += 2;

        int min = neighbours.stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);

        return min == Integer.MAX_VALUE ? min : min + 1;
    }
}
