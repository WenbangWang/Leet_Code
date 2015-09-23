package com.wwb.leetcode.tags.backtracking;

import java.util.ArrayList;
import java.util.List;

/**
 * Follow up for N-Queens problem.
 *
 * Now, instead outputting board configurations, return the total number of distinct solutions.
 */
public class No52 {

    public int totalNQueens(int n) {
        if(n <= 4) {
            return 0;
        }

        return totalNQueens(0, n, new ArrayList<Integer>());
    }

    private int totalNQueens(int row, int n, List<Integer> columns) {
        if(columns.size() == n) {
            return 1;
        }

        int count = 0;

        for(int col = 0; col < n; col++) {
            if(isValid(col, columns)) {
                columns.add(col);
                count += totalNQueens(row + 1, n, columns);
                columns.remove(columns.size() - 1);
            }
        }

        return count;
    }

    private boolean isValid(int column, List<Integer> columns) {
        int row = columns.size();

        for(int i = 0; i < row; i++) {
            int col = columns.get(i);
            if(column == col) {
                return false;
            }

            if(row - i == Math.abs(column - col)) {
                return false;
            }
        }

        return true;
    }
}
