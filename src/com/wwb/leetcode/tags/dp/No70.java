package com.wwb.leetcode.tags.dp;

/**
 * You are climbing a stair case. It takes n steps to reach to the top.
 *
 * Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?
 */
public class No70 {

    public int climbStairs(int n) {
        int[] map = new int[n + 1];

        return climbStairs(n, map);
    }

    private int climbStairs(int n, int[] map) {
        if(n == 0 || n == 1) {
            map[n] = 1;
            return 1;
        }

        if(map[n] != 0) {
            return map[n];
        }

        map[n] = climbStairs(n - 1, map) + climbStairs(n - 2, map);

        return map[n];
    }
}