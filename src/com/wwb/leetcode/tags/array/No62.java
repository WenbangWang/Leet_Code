package com.wwb.leetcode.tags.array;

/**
 * A robot is located at the top-left corner of a m x n grid (marked 'Start' in the diagram below).
 *
 * The robot can only move either down or right at any point in time.
 * The robot is trying to reach the bottom-right corner of the grid (marked 'Finish' in the diagram below).
 *
 * How many possible unique paths are there?
 *
 *
 * Above is a 3 x 7 grid. How many possible unique paths are there?
 *
 * Note: m and n will be at most 100.
 */
public class No62 {

    public int uniquePaths(int m, int n) {
        // return solution1(m - 1, n - 1);
        // return solution2(m, n);
        // return solution3(m, n);
        return solution4(m, n);
    }

    //Time Limit Exceeded
    private int solution1(int m, int n) {
        if(m < 0 || n < 0) {
            return 0;
        }

        if(m == 0 && n == 0) {
            return 1;
        } else {
            return solution1(m - 1, n) + solution1(m, n - 1);
        }
    }

    private int solution2(int m, int n) {
        int[][] buffer = new int[m][n];

        return solution2(m - 1, n - 1, buffer);
    }

    private int solution2(int x, int y, int[][] buffer) {
        if(x < 0 || y < 0) {
            return 0;
        }

        if(x == 0 && y == 0) {
            buffer[x][y] = 1;
            return buffer[x][y];
        }

        if(buffer[x][y] != 0) {
            return buffer[x][y];
        }

        buffer[x][y] = solution2(x - 1, y, buffer) + solution2(x, y - 1, buffer);

        return buffer[x][y];
    }

    private int solution3(int m, int n) {
        int[][] buffer = new int[m][n];

        for(int i = 0; i < m; i++) {
            buffer[i][0] = 1;

            for(int j = 1; j < n; j++) {
                if(i == 0) {
                    buffer[i][j] = 1;
                } else {
                    buffer[i][j] = buffer[i - 1][j] + buffer[i][j - 1];
                }
            }
        }

        return buffer[m - 1][n - 1];
    }

    private int solution4(int m, int n) {
        int[] buffer = new int[n];

        buffer[0] = 1;

        for(int i = 0; i < m; i++) {
            for(int j = 1; j < n; j++) {
                buffer[j] = buffer[j - 1] + buffer[j];
            }
        }

        return buffer[n - 1];
    }
}