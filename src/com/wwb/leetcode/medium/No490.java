package com.wwb.leetcode.medium;

/**
 * There is a ball in a maze with empty spaces and walls. The ball can go through empty spaces by rolling up, down, left or right, but it won't stop rolling until hitting a wall. When the ball stops, it could choose the next direction.
 * <p>
 * Given the ball's start position, the destination and the maze, determine whether the ball could stop at the destination.
 * <p>
 * The maze is represented by a binary 2D array. 1 means the wall and 0 means the empty space. You may assume that the borders of the maze are all walls. The start and destination coordinates are represented by row and column indexes.
 * <p>
 * <p>
 * <p>
 * <pre>
 * Example 1:
 *
 * Input 1: a maze represented by a 2D array
 *
 * 0 0 1 0 0
 * 0 0 0 0 0
 * 0 0 0 1 0
 * 1 1 0 1 1
 * 0 0 0 0 0
 *
 * Input 2: start coordinate (rowStart, colStart) = (0, 4)
 * Input 3: destination coordinate (rowDest, colDest) = (4, 4)
 *
 * Output: true
 *
 * Explanation: One possible way is : left -> down -> left -> down -> right -> down -> right.
 *
 * <img src="../doc-files/490_1.png" />
 *
 * Example 2:
 *
 * Input 1: a maze represented by a 2D array
 *
 * 0 0 1 0 0
 * 0 0 0 0 0
 * 0 0 0 1 0
 * 1 1 0 1 1
 * 0 0 0 0 0
 *
 * Input 2: start coordinate (rowStart, colStart) = (0, 4)
 * Input 3: destination coordinate (rowDest, colDest) = (3, 2)
 *
 * Output: false
 *
 * Explanation: There is no way for the ball to stop at the destination.
 *
 * <img src="../doc-files/490_2.png" />
 * </pre>
 * <p>
 * <p>
 * Note:
 * <p>
 * There is only one ball and one destination in the maze.
 * <p>
 * Both the ball and the destination exist on an empty space, and they will not be at the same position initially.
 * <p>
 * The given maze does not contain border (like the red rectangle in the example pictures), but you could assume the border of the maze are all walls.
 * <p>
 * The maze contains at least 2 empty spaces, and both the width and height of the maze won't exceed 100.
 */
public class No490 {
    public boolean hasPath(int[][] maze, int[] start, int[] destination) {

    }

    private boolean dfs(
        int[][] maze,
        boolean[][] visited,
        int[][] dirs,
        int x,
        int y,
        int[] destination
    ) {
        int m = maze.length;
        int n = maze[0].length;

        if (x == m || y == n || x < 0 || y < 0) {
            return false;
        }

        if (x == destination[0] && y == destination[1]) {
            return true;
        }

        if (visited[x][y]) {
            return false;
        }

        if (maze[x][y] == 1) {
            return false;
        }

        visited[x][y] = true;

        for (int[] dir : dirs) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            while (newX < m && newY < n && newX >= 0 && newY >= 0 && maze[newX][newY] == 0) {
                newX += dir[0];
                newY += dir[1];
            }

            // go back to the 0 it should be stopped
            newX -= dir[0];
            newY -= dir[1];

            if (dfs(maze, visited, dirs, newX, newY, destination)) {
                return true;
            }
        }

        return false;
    }
}
