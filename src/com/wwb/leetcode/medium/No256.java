package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * There is a row of n houses, where each house can be painted one of three colors: red, blue, or green.
 * The cost of painting each house with a certain color is different.
 * You have to paint all the houses such that no two adjacent houses have the same color.
 * <p>
 * The cost of painting each house with a certain color is represented by an n x 3 cost matrix costs.
 * <p>
 * For example, costs[0][0] is the cost of painting house 0 with the color red;
 * costs[1][2] is the cost of painting house 1 with color green, and so on...
 * Return the minimum cost to paint all houses.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: costs = [[17,2,17],[16,16,5],[14,3,19]]
 * Output: 10
 * Explanation: Paint house 0 into blue, paint house 1 into green, paint house 2 into blue.
 * Minimum cost: 2 + 5 + 3 = 10.
 * Example 2:
 * <p>
 * Input: costs = [[7,6,2]]
 * Output: 2
 * <p>
 * <p>
 * Constraints:
 * <p>
 * costs.length == n
 * costs[i].length == 3
 * 1 <= n <= 100
 * 1 <= costs[i][j] <= 20
 */
public class No256 {
    public int minCost(int[][] costs) {
        return solution1(costs);
    }

    private int solution1(int[][] costs) {
        int[][] dp = new int[costs.length][Color.values().length];

        dp[0] = costs[0];

        for (int house = 1; house < costs.length; house++) {
            for (Color color : Color.values()) {
                dp[house][color.ordinal()] = getPreviousMinCost(color, house, dp) + costs[house][color.ordinal()];
            }
        }

        return Math.min(
            dp[costs.length - 1][Color.BLUE.ordinal()],
            Math.min(
                dp[costs.length - 1][Color.RED.ordinal()],
                dp[costs.length - 1][Color.GREEN.ordinal()]
            )
        );
    }

    private int solution2(int[][] costs) {
        int[] dp = costs[0];

        for (int house = 1; house < costs.length; house++) {
            int[] currentCost = Arrays.copyOf(dp, dp.length);

            for (Color color : Color.values()) {
                currentCost[color.ordinal()] = getPreviousMinCost(color, house, dp) + costs[house][color.ordinal()];
            }

            dp = currentCost;
        }

        return Math.min(
            dp[Color.BLUE.ordinal()],
            Math.min(
                dp[Color.RED.ordinal()],
                dp[Color.GREEN.ordinal()]
            )
        );
    }

    private int getPreviousMinCost(Color currentColor, int currentHouse, int[][] dp) {
        return switch (currentColor) {
            case RED -> Math.min(
                dp[currentHouse - 1][Color.BLUE.ordinal()],
                dp[currentHouse - 1][Color.GREEN.ordinal()]
            );
            case BLUE -> Math.min(
                dp[currentHouse - 1][Color.RED.ordinal()],
                dp[currentHouse - 1][Color.GREEN.ordinal()]
            );
            case GREEN -> Math.min(
                dp[currentHouse - 1][Color.RED.ordinal()],
                dp[currentHouse - 1][Color.BLUE.ordinal()]
            );
            default -> 0;
        };
    }

    private int getPreviousMinCost(Color currentColor, int currentHouse, int[] dp) {
        return switch (currentColor) {
            case RED -> Math.min(
                dp[Color.BLUE.ordinal()],
                dp[Color.GREEN.ordinal()]
            );
            case BLUE -> Math.min(
                dp[Color.RED.ordinal()],
                dp[Color.GREEN.ordinal()]
            );
            case GREEN -> Math.min(
                dp[Color.RED.ordinal()],
                dp[Color.BLUE.ordinal()]
            );
            default -> 0;
        };
    }

    private enum Color {
        RED,
        BLUE,
        GREEN
    }
}
