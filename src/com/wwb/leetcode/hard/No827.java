package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * You are given an n x n binary matrix grid. You are allowed to change at most one 0 to be 1.
 * <p>
 * Return the size of the largest island in grid after applying this operation.
 * <p>
 * An island is a 4-directionally connected group of 1s.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: grid = [[1,0],[0,1]]
 * <p>
 * Output: 3
 * <p>
 * Explanation: Change one 0 to 1 and connect two 1s, then we get an island with area = 3.
 * <p>
 * Example 2:
 * <p>
 * Input: grid = [[1,1],[1,0]]
 * <p>
 * Output: 4
 * <p>
 * Explanation: Change the 0 to 1 and make the island bigger, only one island with area = 4.
 * <p>
 * Example 3:
 * <p>
 * <p>
 * Input: grid = [[1,1],[1,1]]
 * <p>
 * Output: 4
 * <p>
 * Explanation: Can't change any 0 to 1, only one island with area = 4.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * n == grid.length
 * <p>
 * n == grid[i].length
 * <p>
 * 1 <= n <= 500
 * <p>
 * grid[i][j] is either 0 or 1.
 */
public class No827 {
    public static void main(String[] args) {
        new No827().largestIsland(new int[][]{new int[]{1, 0}, new int[]{1, 1}});
    }

    public int largestIsland(int[][] grid) {
        return solution1(grid);
    }

    // TLE, can go up to O(n^4)
    private int solution1(int[][] grid) {
        int result = Integer.MIN_VALUE;
        int n = grid.length;

        List<int[]> zeros = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 0) {
                    zeros.add(new int[]{i, j});
                }
            }
        }

        if (zeros.isEmpty()) {
            return n * n;
        }

        for (int[] coordinate : zeros) {
            grid[coordinate[0]][coordinate[1]] = 1;
            result = Math.max(result, explore(grid, coordinate[0], coordinate[1]));
            grid[coordinate[0]][coordinate[1]] = 0;
        }

        return result;
    }

    // O(n^2)
    private int solution2(int[][] grid) {
        int n = grid.length;
        // start from 2 to represent the index of the island
        int index = 2;
        Map<Integer, Integer> indexToArea = new HashMap<>();
        int result = 0;

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (grid[x][y] == 1) {
                    indexToArea.put(index, explore(grid, x, y, n, index));
                    result = Math.max(result, indexToArea.get(index));
                    index++;
                }
            }
        }

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (grid[x][y] == 0) {
                    Set<Integer> visited = new HashSet<>();
                    // 1 represents we flip the current 0 to 1
                    int connectedResult = 1;

                    for (Coordinate coordinate : move(x, y, n)) {
                        index = grid[coordinate.x][coordinate.y];

                        if (index > 1 && visited.add(index)) {
                            connectedResult += indexToArea.get(index);
                        }
                    }

                    result = Math.max(result, connectedResult);
                }
            }
        }

        return result;
    }

    private int explore(int[][] grid, int x, int y, int n, int index) {
        int result = 1;

        grid[x][y] = index;

        for (Coordinate coordinate : move(x, y, n)) {
            if (grid[coordinate.x][coordinate.y] == 1) {
                result += explore(grid, coordinate.x, coordinate.y, n, index);
            }
        }

        return result;
    }

    private boolean isValid(int x, int y, int n) {
        return x >= 0 && y >= 0 && x < n && y < n;
    }

    private List<Coordinate> move(int x, int y, int n) {
        List<Coordinate> coordinates = new ArrayList<>();

        if (isValid(x - 1, y, n)) {
            coordinates.add(new Coordinate(x - 1, y));
        }

        if (isValid(x + 1, y, n)) {
            coordinates.add(new Coordinate(x + 1, y));
        }

        if (isValid(x, y - 1, n)) {
            coordinates.add(new Coordinate(x, y - 1));
        }

        if (isValid(x, y + 1, n)) {
            coordinates.add(new Coordinate(x, y + 1));
        }

        return coordinates;
    }

    private int explore(int[][] grid, int x, int y) {
        int n = grid.length;

        Set<List<Integer>> visited = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();
        int result = 0;

        queue.offer(new int[]{x, y});

        while (!queue.isEmpty()) {
            int[] coord = queue.poll();

            // out of range
            if (coord[0] < 0 || coord[1] < 0 || coord[0] == n || coord[1] == n) {
                continue;
            }

            // visited
            if (!visited.add(List.of(coord[0], coord[1]))) {
                continue;
            }

            if (grid[coord[0]][coord[1]] == 0) {
                continue;
            }

            result++;
            // up
            queue.offer(new int[]{coord[0] - 1, coord[1]});
            // down
            queue.offer(new int[]{coord[0] + 1, coord[1]});
            // left
            queue.offer(new int[]{coord[0], coord[1] - 1});
            // right
            queue.offer(new int[]{coord[0], coord[1] + 1});
        }

        return result;
    }

    private static class Coordinate {
        int x;
        int y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
