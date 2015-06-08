package com.wwb.leetcode.tags.dp;

/**
 * Follow up for "No62":
 *
 * Now consider if some obstacles are added to the grids. How many unique paths would there be?
 *
 * An obstacle and empty space is marked as 1 and 0 respectively in the grid.
 *
 * For example,
 * There is one obstacle in the middle of a 3x3 grid as illustrated below.
 *
 * [
 *   [0,0,0],
 *   [0,1,0],
 *   [0,0,0]
 * ]
 * The total number of unique paths is 2.
 *
 * Note: m and n will be at most 100.
 */
public class No63 {

    public int uniquePathsWithObstacles(int[][] obstacleGrid) {
        // return solution1(obstacleGrid);
        return solution2(obstacleGrid);
    }

    private int solution1(int[][] obstacleGrid) {
        int x = obstacleGrid.length - 1;
        int y = obstacleGrid[0].length - 1;
        int[][] buffer = new int[x + 1][y + 1];

        return solution1(x, y, obstacleGrid, buffer);
    }

    private int solution1(int x, int y, int[][] obstacleGrid, int[][] buffer) {
        if(x < 0 || y < 0) {
            return 0;
        }

        if(obstacleGrid[x][y] == 1) {
            return 0;
        }

        if(x == 0 && y == 0) {
            buffer[x][y] = 1;
            return buffer[x][y];
        }

        if(buffer[x][y] != 0) {
            return buffer[x][y];
        }

        buffer[x][y] = solution1(x - 1, y, obstacleGrid, buffer) + solution1(x, y - 1, obstacleGrid, buffer);

        return buffer[x][y];
    }

    private int solution2(int[][] obstacleGrid) {
        if(obstacleGrid == null || obstacleGrid.length == 0) {
            return 0;
        }

        int x = obstacleGrid.length;
        int y = obstacleGrid[0].length;

        for(int i = 0; i < x; i++) {
            for(int j = 0; j < y; j++) {
                if(obstacleGrid[i][j] == 1) {
                    obstacleGrid[i][j] = 0;
                } else if(i == 0 && j== 0) {
                    obstacleGrid[i][j] = 1;
                } else if(i == 0) {
                    obstacleGrid[i][j] = obstacleGrid[i][j - 1];
                } else if(j == 0) {
                    obstacleGrid[i][j] = obstacleGrid[i - 1][j];
                } else {
                    obstacleGrid[i][j] = obstacleGrid[i - 1][j] + obstacleGrid[i][j - 1];
                }
            }
        }

        return obstacleGrid[x - 1][y - 1];
    }
}