package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The n-queens puzzle is the problem of placing n queens on an n×n chessboard such that no two queens attack each other.
 *
 * Given an integer n, return all distinct solutions to the n-queens puzzle.
 *
 * Each solution contains a distinct board configuration of the n-queens' placement,
 * where 'Q' and '.' both indicate a queen and an empty space respectively.
 *
 * For example,
 * There exist two distinct solutions to the 4-queens puzzle:
 *
 * [
 *     [".Q..",  // Solution 1
 *     "...Q",
 *     "Q...",
 *     "..Q."],
 *
 *     ["..Q.",  // Solution 2
 *     "Q...",
 *     "...Q",
 *     ".Q.."]
 * ]
 */
public class No51 {

    public List<List<String>> solveNQueens(int n) {
        if(n < 0) {
            return Collections.emptyList();
        }

        List<List<String>> queens = new ArrayList<>();

        solveNQueens(n, 0, new ArrayList<Integer>(), queens, new ArrayList<String>());

        return queens;
    }

    public void solveNQueens(int n, int row, List<Integer> columns, List<List<String>> queens, List<String> workspace) {
        if(n == row) {
            queens.add(new ArrayList<>(workspace));
            return;
        }

        for(int col = 0; col < n; col++) {
            if(isSolveable(col, columns)) {
                columns.add(col);
                workspace.add(placeQueen(col, n));

                solveNQueens(n, row + 1, columns, queens, workspace);

                columns.remove(columns.size() - 1);
                workspace.remove(workspace.size() - 1);
            }
        }
    }

    private boolean isSolveable(int col, List<Integer> columns) {
        for(int i = 0, row = columns.size(); i < row; i++) {
            int column = columns.get(i);

            if(column == col || row - i == Math.abs(column - col)) {
                return false;
            }
        }

        return true;
    }

    private String placeQueen(int index, int n) {
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < n; i++) {
            if(i != index) {
                stringBuilder.append(".");
            } else {
                stringBuilder.append("Q");
            }
        }

        return stringBuilder.toString();
    }
}
