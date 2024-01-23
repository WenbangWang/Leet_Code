package com.wwb.leetcode.medium;

import java.util.Stack;

/**
 * We are given an array asteroids of integers representing asteroids in a row.
 *
 * For each asteroid, the absolute value represents its size,
 * and the sign represents its direction (positive meaning right, negative meaning left).
 * Each asteroid moves at the same speed.
 *
 * Find out the state of the asteroids after all collisions. If two asteroids meet, the smaller one will explode.
 * If both are the same size, both will explode. Two asteroids moving in the same direction will never meet.
 *
 *
 *
 * Example 1:
 *
 * Input: asteroids = [5,10,-5]
 * Output: [5,10]
 * Explanation: The 10 and -5 collide resulting in 10. The 5 and 10 never collide.
 * Example 2:
 *
 * Input: asteroids = [8,-8]
 * Output: []
 * Explanation: The 8 and -8 collide exploding each other.
 * Example 3:
 *
 * Input: asteroids = [10,2,-5]
 * Output: [10]
 * Explanation: The 2 and -5 collide resulting in -5. The 10 and -5 collide resulting in 10.
 *
 *
 * Constraints:
 *
 * 2 <= asteroids.length <= 10^4
 * -1000 <= asteroids[i] <= 1000
 * asteroids[i] != 0
 */
public class No735 {
    public int[] asteroidCollision(int[] asteroids) {
        Stack<Integer> stack = new Stack<>();

        for (int asteroid : asteroids) {
            if (asteroid > 0) {
                stack.push(asteroid);
                continue;
            }

            // stack goes right and queue goes left

            // asteroid in queue wins
            while(!stack.isEmpty() && stack.peek() > 0 && stack.peek() + asteroid < 0) {
                stack.pop();
            }

            if (stack.isEmpty() || stack.peek() < 0) {
                stack.push(asteroid);
            } else if (stack.peek() + asteroid == 0) { // collide, same size and mutually destroyed
                stack.pop();
            }
        }

        int[] result = new int[stack.size()];
        int index = stack.size() - 1;

        while(!stack.isEmpty()) {
            result[index--] = stack.pop();
        }

        return result;
    }
}
