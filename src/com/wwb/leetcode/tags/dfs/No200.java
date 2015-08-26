package com.wwb.leetcode.tags.dfs;

/**
 * Given a 2d grid map of '1's (land) and '0's (water), count the number of islands.
 * An island is surrounded by water and is formed by connecting adjacent lands horizontally or vertically.
 * You may assume all four edges of the grid are all surrounded by water.
 *
 * Example 1:
 *
 *  11110
 *  11010
 *  11000
 *  00000
 * Answer: 1
 *
 * Example 2:
 *
 *  11000
 *  11000
 *  00100
 *  00011
 * Answer: 3
 */
public class No200 {

    public int numIslands(char[][] grid) {
        if(grid == null || grid.length == 0) {
            return 0;
        }
        int[] dx = {-1, 0, 0, 1};
        int[] dy = {0, -1, 1, 0};
        int result = 0;

        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] == '1') {
                    explore(grid, j, i, dx, dy);
                    result++;
                }
            }
        }

        return result;
    }

    private void explore(char[][] grid, int x, int y, int[] dx, int[] dy) {
        grid[y][x] = 'x';

        for(int i = 0; i < dx.length; i++) {
            int currentX = x + dx[i];
            int currentY = y + dy[i];
            if(currentY < grid.length && currentY >= 0 && currentX < grid[0].length && currentX >= 0 && grid[currentY][currentX] == '1') {
                explore(grid, x + dx[i], y + dy[i], dx, dy);
            }
        }
    }
}