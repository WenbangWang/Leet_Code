package com.wwb.leetcode.medium;

import java.util.HashSet;
import java.util.Set;

/**
 * A robot on an infinite XY-plane starts at point (0, 0) facing north.
 * The robot can receive a sequence of these three possible types of commands:
 *
 * -2: Turn left 90 degrees.
 * -1: Turn right 90 degrees.
 * 1 <= k <= 9: Move forward k units, one unit at a time.
 * Some of the grid squares are obstacles. The ith obstacle is at grid point obstacles[i] = (xi, yi).
 * If the robot runs into an obstacle, then it will instead stay in its current location and move on to the next command.
 *
 * Return the maximum Euclidean distance that the robot ever gets from the origin squared (i.e. if the distance is 5, return 25).
 *
 * Note:
 *
 * North means +Y direction.
 * East means +X direction.
 * South means -Y direction.
 * West means -X direction.
 * There can be obstacle in [0,0].
 *
 *
 * Example 1:
 *
 * Input: commands = [4,-1,3], obstacles = []
 * Output: 25
 * Explanation: The robot starts at (0, 0):
 * 1. Move north 4 units to (0, 4).
 * 2. Turn right.
 * 3. Move east 3 units to (3, 4).
 * The furthest point the robot ever gets from the origin is (3, 4), which squared is 32 + 42 = 25 units away.
 * Example 2:
 *
 * Input: commands = [4,-1,4,-2,4], obstacles = [[2,4]]
 * Output: 65
 * Explanation: The robot starts at (0, 0):
 * 1. Move north 4 units to (0, 4).
 * 2. Turn right.
 * 3. Move east 1 unit and get blocked by the obstacle at (2, 4), robot is at (1, 4).
 * 4. Turn left.
 * 5. Move north 4 units to (1, 8).
 * The furthest point the robot ever gets from the origin is (1, 8), which squared is 12 + 82 = 65 units away.
 * Example 3:
 *
 * Input: commands = [6,-1,-1,6], obstacles = []
 * Output: 36
 * Explanation: The robot starts at (0, 0):
 * 1. Move north 6 units to (0, 6).
 * 2. Turn right.
 * 3. Turn right.
 * 4. Move south 6 units to (0, 0).
 * The furthest point the robot ever gets from the origin is (0, 6), which squared is 62 = 36 units away.
 *
 *
 * Constraints:
 *
 * 1 <= commands.length <= 10^4
 * commands[i] is either -2, -1, or an integer in the range [1, 9].
 * 0 <= obstacles.length <= 10^4
 * -3 * 10^4 <= xi, yi <= 3 * 10^4
 * The answer is guaranteed to be less than 2^31.
 */
public class No874 {
    public int robotSim(int[] commands, int[][] obstacles) {
        Set<String> index = new HashSet<>();
        Robot robot = new Robot();
        int max = 0;

        for (int[] obstacle : obstacles) {
            index.add(obstacle[0] + "_" + obstacle[1]);
        }

        for (int command : commands) {
            if (command < 0) {
                robot.turn(command);
            } else {
                robot.move(command, index);
            }

            max = Math.max(max, robot.distance());
        }

        return max;
    }

    private enum Direction {
        // in clock-wise order
        UP(0, 0, 1),
        RIGHT(1, 1, 0),
        DOWN(2, 0, -1),
        LEFT(3, -1, 0);

        public final int value;
        public final int x;
        public final int y;

        Direction(int value, int x, int y) {
            this.value = value;
            this.x = x;
            this.y = y;
        }

        static Direction create(int value) {
            return switch (value) {
                case 0 -> Direction.UP;
                case 1 -> Direction.RIGHT;
                case 2 -> Direction.DOWN;
                case 3 -> Direction.LEFT;
                default -> throw new RuntimeException("fuck");
            };
        }
    }

    private static class Robot {
        int x;
        int y;
        Direction direction;

        Robot() {
            this.x = 0;
            this.y = 0;
            this.direction = Direction.UP;
        }

        // command has to be -1 or -2
        void turn(int command) {
            if (command == -1) {
                this.direction = Direction.create((this.direction.value + 1) % 4);
            } else if (command == -2) {
                this.direction = Direction.create((this.direction.value - 1 + 4) % 4);
            }
        }

        // command has to be positive
        void move(int command, Set<String> index) {
            while (command-- > 0 && !index.contains((this.x + this.direction.x) + "_" + (this.y + this.direction.y))) {
                this.x += this.direction.x;
                this.y += this.direction.y;
            }
        }

        int distance() {
            return this.x * this.x + this.y * this.y;
        }
    }
}
