package com.wwb.leetcode.tags.array;

import java.util.List;

/**
 * Given a triangle, find the minimum path sum from top to bottom.
 * Each step you may move to adjacent numbers on the row below.
 *
 * For example, given the following triangle
 * [
 *     [2],
 *    [3,4],
 *   [6,5,7],
 *  [4,1,8,3]
 * ]
 * The minimum path sum from top to bottom is 11 (i.e., 2 + 3 + 5 + 1 = 11).
 *
 * Note:
 * Bonus point if you are able to do this using only O(n) extra space,
 * where n is the total number of rows in the triangle.
 */
public class No120 {

    public int minimumTotal(List<List<Integer>> triangle) {
        // return solution1(triangle);
        return solution2(triangle);
    }

    private int solution1(List<List<Integer>> triangle) {
        if(triangle == null || triangle.isEmpty()) {
            return 0;
        }
        int totalRows = triangle.size() - 1;
        int[] dp = new int[totalRows + 1];

        for(int i = 0; i <=totalRows; i++) {
            dp[i] = triangle.get(totalRows).get(i);
        }

        for(int i = totalRows - 1; i >= 0; i--) {
            List<Integer> row = triangle.get(i);

            for(int j = 0; j <= i; j++) {
                dp[j] = Math.min(dp[j], dp[j + 1]) + row.get(j);
            }
        }

        return dp[0];
    }

    private int solution2(List<List<Integer>> triangle) {
        if(triangle == null || triangle.isEmpty()) {
            return 0;
        }
        int totalRows = triangle.size() - 1;

        for(int i = totalRows - 1; i >= 0; i--) {
            for(int j = 0; j <= i; j++) {
                triangle.get(i).set(j, triangle.get(i).get(j) + Math.min(triangle.get(i + 1).get(j), triangle.get(i + 1).get(j + 1)));
            }
        }

        return triangle.get(0).get(0);
    }
}