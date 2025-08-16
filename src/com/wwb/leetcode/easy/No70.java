package com.wwb.leetcode.easy;

/**
 * You are climbing a stair case. It takes n steps to reach to the top.
 * <p>
 * Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?
 */
public class No70 {

    public int climbStairs(int n) {
        return solution1(n);
    }

    private int solution1(int n) {
        int[] map = new int[n + 1];

        return climbStairs(n, map);
    }

    private int solution2(int n) {
        if (n == 0 || n == 1) {
            return 1;
        }

        int[] dp = new int[n + 1];

        dp[0] = dp[1] = 1;

        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }

        return dp[n];
    }

    private int solution3(int n) {
        if (n == 0 || n == 1) {
            return 1;
        }

        int current = 1;
        int previous = 1;

        for (int i = 2; i <= n; i++) {
            int temp = current;
            current += previous;
            previous = temp;
        }

        return current;
    }

    private int climbStairs(int n, int[] map) {
        if (n == 0 || n == 1) {
            map[n] = 1;
            return 1;
        }

        if (map[n] != 0) {
            return map[n];
        }

        map[n] = climbStairs(n - 1, map) + climbStairs(n - 2, map);

        return map[n];
    }
}
