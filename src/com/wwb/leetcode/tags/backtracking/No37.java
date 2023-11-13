package com.wwb.leetcode.tags.backtracking;

/**
 * Write a program to solve a Sudoku puzzle by filling the empty cells.
 *
 * Empty cells are indicated by the character '.'.
 *
 * You may assume that there will be only one unique solution.
 */
public class No37 {

    public void solveSudoku(char[][] board) {
        if(board == null || board.length == 0 || board[0] == null || board[0].length == 0) {
            return;
        }

        solve(board);
    }

    private boolean solve(char[][] board) {
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                if(board[i][j] == '.') {
                    for(char c = '1'; c <= '9'; c++) {
                        if(isValid(board, i, j, c)) {
                            board[i][j] = c;

                            if(solve(board)) {
                                return true;
                            } else {
                                board[i][j] = '.';
                            }
                        }
                    }

                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValid(char[][] board, int row, int col, char c) {
        // Block no. is i/3, first element is i/3*3
        int cubeRow = (row / 3) * 3;
        int cubeCol = (col / 3) * 3;
        for (int n = 0; n < 9; n++)
            if (board[n][col] == c || board[row][n] == c || board[cubeRow + n / 3][cubeCol + n % 3] == c) {
                return false;
            }
        return true;
//        for(int row = 0; row < 9; row++) {
//            if(board[row][j] == c) {
//                return false;
//            }
//        }
//
//        for(int col = 0; col < 9; col++) {
//            if(board[i][col] == c) {
//                return false;
//            }
//        }
//
//        for(int row = (i / 3) * 3; row < (i / 3) * 3 + 3; row++) {
//            for(int col = (j / 3) * 3; col < (j / 3) * 3 + 3; col++) {
//                if(board[row][col] == c) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
    }
}
