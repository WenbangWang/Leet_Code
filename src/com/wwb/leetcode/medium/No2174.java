package com.wwb.leetcode.medium;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * You are given a 0-indexed m x n binary matrix grid.
 * <p>
 * In one operation, you can choose any i and j that meet the following conditions:
 * <p>
 * 0 <= i < m
 * 0 <= j < n
 * grid[i][j] == 1
 * and change the values of all cells in row i and column j to zero.
 * <p>
 * Return the minimum number of operations needed to remove all 1's from grid.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: grid = [[1,1,1],[1,1,1],[0,1,0]]
 * Output: 2
 * Explanation:
 * In the first operation, change all cell values of row 1 and column 1 to zero.
 * In the second operation, change all cell values of row 0 and column 0 to zero.
 * Example 2:
 * <p>
 * <p>
 * Input: grid = [[0,1,0],[1,0,1],[0,1,0]]
 * Output: 2
 * Explanation:
 * In the first operation, change all cell values of row 1 and column 0 to zero.
 * In the second operation, change all cell values of row 2 and column 1 to zero.
 * Note that we cannot perform an operation using row 1 and column 1 because grid[1][1] != 1.
 * Example 3:
 * <p>
 * <p>
 * Input: grid = [[0,0],[0,0]]
 * Output: 0
 * Explanation:
 * There are no 1's to remove so return 0.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * m == grid.length
 * n == grid[i].length
 * 1 <= m, n <= 15
 * 1 <= m * n <= 15
 * grid[i][j] is either 0 or 1.
 */
public class No2174 {
    // O(2^MN * MN^max(M, N))
    public int removeOnes(int[][] grid) {
        return solution1(grid);
    }

    private int solution1(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int minFlips = Integer.MAX_VALUE;
        int[] rowValues = new int[n];
        int[] colValues = new int[m];

        for (int row = 0; row < m; row++) {
            for (int col = 0; col < n; col++) {
                if (grid[row][col] == 0) {
                    continue;
                }

                for (int r = 0; r < m; r++) {
                    colValues[r] = grid[r][col];
                    grid[r][col] = 0;
                }
                for (int c = 0; c < n; c++) {
                    rowValues[c] = grid[row][c];
                    grid[row][c] = 0;
                }
                minFlips = Math.min(minFlips, (1 + removeOnes(grid)));

                //undo operation in reverse order so that grid[i][j] value is restored correctly
                for (int c = 0; c < n; c++) {
                    grid[row][c] = rowValues[c];
                }
                for (int r = 0; r < m; r++) {
                    grid[r][col] = colValues[r];
                }
            }
        }
        return (minFlips == Integer.MAX_VALUE) ? 0 : minFlips;
    }

    // O((m*n) * 2^(m*n) * (m+n))
    private int solution2(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int state = 0;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 1) {
                    state |= encodeToBitMask(grid, i, j);
                }
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        int result = 0;
        queue.offer(state);
        visited.add(state);

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int k = 0; k < size; k++) {
                state = queue.poll();

                if (state == 0) {
                    return result;
                }

                for (int i = 0; i < m; i++) {
                    for (int j = 0; j < n; j++) {
                        if (grid[i][j] == 0) {
                            continue;
                        }

                        int nextState = state;

                        for (int row = 0; row < m; row++) {
                            // ~ is to zero out current row/col
                            nextState &= ~(encodeToBitMask(grid, row, j));
                        }

                        for (int col = 0; col < n; col++) {
                            nextState &= ~(encodeToBitMask(grid, i, col));
                        }

                        if (visited.add(nextState)) {
                            queue.offer(nextState);
                        }
                    }
                }
            }

            result++;
        }

        return -1;
    }

    private int encodeToBitMask(int[][] grid, int i, int j) {
        int n = grid[0].length;

        return 1 << (i * n + j);
    }
}
