package com.wwb.leetcode.medium;

import java.util.LinkedList;
import java.util.Queue;

/**
 * You are given an m x n grid where each cell can have one of three values:
 * <p>
 * 0 representing an empty cell,
 * 1 representing a fresh orange, or
 * 2 representing a rotten orange.
 * Every minute, any fresh orange that is 4-directionally adjacent to a rotten orange becomes rotten.
 * <p>
 * Return the minimum number of minutes that must elapse until no cell has a fresh orange. If this is impossible, return -1.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: grid = [[2,1,1],[1,1,0],[0,1,1]]
 * Output: 4
 * Example 2:
 * <p>
 * Input: grid = [[2,1,1],[0,1,1],[1,0,1]]
 * Output: -1
 * Explanation: The orange in the bottom left corner (row 2, column 0) is never rotten, because rotting only happens 4-directionally.
 * Example 3:
 * <p>
 * Input: grid = [[0,2]]
 * Output: 0
 * Explanation: Since there are already no fresh oranges at minute 0, the answer is just 0.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * m == grid.length
 * n == grid[i].length
 * 1 <= m, n <= 10
 * grid[i][j] is 0, 1, or 2.
 */
public class No994 {
    public int orangesRotting(int[][] grid) {
        Queue<Coordinate> queue = new LinkedList<>();

        // Step 1). build the initial set of rotten oranges
        int freshOranges = 0;
        int rows = grid.length;
        int columns = grid[0].length;

        for (int row = 0; row < rows; row++)
            for (int column = 0; column < columns; column++)
                if (grid[row][column] == 2) {
                    queue.offer(new Coordinate(row, column));
                } else if (grid[row][column] == 1) {
                    freshOranges++;
                }

        // Step 2). start the rotting process via BFS
        int minutesElapsed = 0;
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        while (!queue.isEmpty()) {
            int size = queue.size();

            while (--size >= 0) {
                Coordinate c = queue.poll();
                int row = c.row;
                int col = c.column;

                // this is a rotten orange
                // then it would contaminate its neighbors
                for (int[] d : directions) {
                    int neighborRow = row + d[0];
                    int neighborCol = col + d[1];
                    if (neighborRow >= 0 && neighborRow < rows &&
                            neighborCol >= 0 && neighborCol < columns) {
                        if (grid[neighborRow][neighborCol] == 1) {
                            // this orange would be contaminated
                            grid[neighborRow][neighborCol] = 2;
                            freshOranges--;
                            // this orange would then contaminate other oranges
                            queue.offer(new Coordinate(neighborRow, neighborCol));
                        }
                    }
                }
            }

            if (!queue.isEmpty()) {
                // We finish one round of processing
                minutesElapsed++;
            }
        }

        // return elapsed minutes if no fresh orange left
        return freshOranges == 0 ? minutesElapsed : -1;
    }

    private static class Coordinate {
        private int row;
        private int column;

        Coordinate(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }
}
