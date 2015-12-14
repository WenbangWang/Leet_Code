package com.wwb.leetcode.medium;

/**
 * According to the Wikipedia's article: "The Game of Life, also known simply as Life,
 * is a cellular automaton devised by the British mathematician John Horton Conway in 1970."
 *
 * Given a board with m by n cells, each cell has an initial state live (1) or dead (0).
 * Each cell interacts with its eight neighbors (horizontal, vertical, diagonal)
 * using the following four rules (taken from the above Wikipedia article):
 *
 * Any live cell with fewer than two live neighbors dies, as if caused by under-population.
 * Any live cell with two or three live neighbors lives on to the next generation.
 * Any live cell with more than three live neighbors dies, as if by over-population..
 * Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.
 * Write a function to compute the next state (after one update) of the board given its current state.
 *
 * Follow up:
 * Could you solve it in-place? Remember that the board needs to be updated at the same time:
 * You cannot update some cells first and then use their updated values to update other cells.
 * In this question, we represent the board using a 2D array. In principle, the board is infinite,
 * which would cause problems when the active area encroaches the border of the array.
 * How would you address these problems?
 */
public class No289 {

    public void gameOfLife(int[][] board) {
        if(board == null || board.length == 0 || board[0] == null || board[0].length == 0) {
            return;
        }

        int x = board.length;
        int y = board[0].length;

        for(int i = 0; i < x; i++) {
            for(int j = 0; j < y; j++) {
                int liveCells = calculateLiveCells(board, i, j, x, y);

                if(board[i][j] == 1 && (liveCells == 2 || liveCells == 3)) {
                    board[i][j] = 3;
                }

                if(board[i][j] == 0 && liveCells == 3) {
                    board[i][j] = 2;
                }
            }
        }

        for(int i = 0; i < x; i++) {
            for(int j = 0; j < y; j++) {
                board[i][j] >>= 1;
            }
        }
    }

    public int calculateLiveCells(int[][] board, int i, int j, int x, int y) {
        int liveCells = 0;

        for(int m = Math.max(i - 1, 0); m <= Math.min(i + 1, x - 1); m++) {
            for(int n = Math.max(j - 1, 0); n <= Math.min(j + 1, y - 1); n++) {
                liveCells += board[m][n] & 1;
            }
        }

        liveCells -= board[i][j] & 1;

        return liveCells;
    }
}
