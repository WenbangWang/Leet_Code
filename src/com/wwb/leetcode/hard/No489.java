package com.wwb.leetcode.hard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Given a robot cleaner in a room modeled as a grid.
 * <p>
 * Each cell in the grid can be empty or blocked.
 * <p>
 * The robot cleaner with 4 given APIs can move forward, turn left or turn right. Each turn it made is 90 degrees.
 * <p>
 * When it tries to move into a blocked cell, its bumper sensor detects the obstacle and it stays on the current cell.
 * <p>
 * Design an algorithm to clean the entire room using only the 4 given APIs shown below.
 *
 * <pre>
 * {@code
 * interface Robot {
 *   // returns true if next cell is open and robot moves into the cell.
 *   // returns false if next cell is obstacle and robot stays on the current cell.
 *   boolean move();
 *
 *   // Robot will stay on the same cell after calling turnLeft/turnRight.
 *   // Each turn will be 90 degrees.
 *   void turnLeft();
 *   void turnRight();
 *
 *   // Clean the current cell.
 *   void clean();
 * }
 * }
 * </pre>
 * <p>
 * Example:
 * <p>
 * Input:
 *
 * <pre>
 * room = [
 *   [1,1,1,1,1,0,1,1],
 *   [1,1,1,1,1,0,1,1],
 *   [1,0,1,1,1,1,1,1],
 *   [0,0,0,1,0,0,0,0],
 *   [1,1,1,1,1,1,1,1]
 * ],
 * row = 1,
 * col = 3
 *
 * </pre>
 * <p>
 * Explanation:
 * <p>
 * All grids in the room are marked by either 0 or 1.
 * <p>
 * 0 means the cell is blocked, while 1 means the cell is accessible.
 * <p>
 * The robot initially starts at the position of row=1, col=3.
 * <p>
 * From the top left corner, its position is one row below and three columns right.
 * <p>
 * <p>
 * Notes:
 * <p>
 * 1. The input is only given to initialize the room and the robot's position internally. You must solve this problem "blindfolded". In other words, you must control the robot using only the mentioned 4 APIs, without knowing the room layout and the initial robot's position.
 * <p>
 * 2. The robot's initial position will always be in an accessible cell.
 * <p>
 * 3. The initial direction of the robot will be facing up.
 * <p>
 * 4. All accessible cells are connected, which means the all cells marked as 1 will be accessible by the robot.
 * <p>
 * 5. Assume all four edges of the grid are all surrounded by wall.
 */
public class No489 {
    public class RobotCleaner {
        // up, right, down, left
        private static final int[][] DIRECTIONS = new int[][]{{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        private final Set<List<Integer>> visited;

        public RobotCleaner() {
            this.visited = new HashSet<>();
        }

        public void cleanRoom(Robot robot) {

        }

        private void dfs(Robot robot, int row, int column, int direction) {
            robot.clean();
            this.visited.add(List.of(row, column));

            for (int i = 0; i < 4; i++) {
                // need to the next direction to be aligned with the clockwise turnRight
                int nextDirection = (direction + i) % 4;
                int nextRow = row + DIRECTIONS[nextDirection][0];
                int nextColumn = column + DIRECTIONS[nextDirection][1];

                if (!this.visited.contains(List.of(nextRow, nextColumn)) && robot.move()) {
                    this.dfs(robot, nextRow, nextColumn, nextDirection);

                    // go back to the previous cell
                    robot.turnRight();
                    robot.turnRight();
                    robot.move();

                    // turn face to the original direction
                    robot.turnRight();
                    robot.turnRight();
                }

                // turn clockwise
                robot.turnRight();
            }
        }
    }

    public interface Robot {
        // returns true if next cell is open and robot moves into the cell.
        // returns false if next cell is obstacle and robot stays on the current cell.
        boolean move();

        // Robot will stay on the same cell after calling turnLeft/turnRight.
        // Each turn will be 90 degrees.
        void turnLeft();

        void turnRight();

        // Clean the current cell.
        void clean();
    }
}
