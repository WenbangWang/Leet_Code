package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The n-queens puzzle is the problem of placing n queens on an n×n chessboard such that no two queens attack each other.
 * <p>
 * Given an integer n, return all distinct solutions to the n-queens puzzle.
 * <p>
 * Each solution contains a distinct board configuration of the n-queens' placement,
 * where 'Q' and '.' both indicate a queen and an empty space respectively.
 *
 * <pre>
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
 * </pre>
 */
public class No51 {

    public List<List<String>> solveNQueens(int n) {
        if (n < 0) {
            return Collections.emptyList();
        }

        List<List<String>> queens = new ArrayList<>();
        boolean[] cols = new boolean[n];
        boolean[] diags = new boolean[2 * n];
        boolean[] antiDiags = new boolean[2 * n];

        solveNQueens(n, 0, cols, diags, antiDiags, queens, new ArrayList<>());

        return queens;
    }

    private void solveNQueens(int n, int row, boolean[] cols, boolean[] diags, boolean[] antiDiags, List<List<String>> queens, List<String> workspace) {
        if (n == row) {
            queens.add(new ArrayList<>(workspace));
            return;
        }

        for (int col = 0; col < n; col++) {
            int diag = row + col;
            int antiDiag = row - col + n;
            if (cols[col] || diags[diag] || antiDiags[antiDiag]) {
                continue;
            }

            cols[col] = true;
            diags[diag] = true;
            antiDiags[antiDiag] = true;
            workspace.add(placeQueen(col, n));

            solveNQueens(n, row + 1, cols, diags, antiDiags, queens, workspace);

            cols[col] = false;
            diags[diag] = false;
            antiDiags[antiDiag] = false;
            workspace.remove(workspace.size() - 1);
        }
    }

    private String placeQueen(int index, int n) {
        StringBuilder stringBuilder = new StringBuilder(".".repeat(n));

        stringBuilder.setCharAt(index, 'Q');

        return stringBuilder.toString();
    }
}
