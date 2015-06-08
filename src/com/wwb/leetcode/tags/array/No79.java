package com.wwb.leetcode.tags.array;

/**
 * Given a 2D board and a word, find if the word exists in the grid.
 *
 * The word can be constructed from letters of sequentially adjacent cell, where "adjacent" cells are those horizontally or vertically neighboring. The same letter cell may not be used more than once.
 *
 * For example,
 * Given board =
 *
 * [
 *   ["ABCE"],
 *   ["SFCS"],
 *   ["ADEE"]
 * ]
 * word = "ABCCED", -> returns true,
 * word = "SEE", -> returns true,
 * word = "ABCB", -> returns false.
 */
public class No79 {

    public boolean exist(char[][] board, String word) {
        // return solution1(board, word);
        return solution2(board, word);
    }

    private boolean solution1(char[][] board, String word) {
        boolean[][] map = new boolean[board.length][board[0].length];

        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                if(solution1Helper(board, map, i, j, 0, word)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean solution1Helper(char[][] board, boolean[][] map, int row, int column, int index, String word) {
        if(index == word.length()) {
            return true;
        }
        if(row < 0 || row >= board.length || column < 0 || column >= board[0].length || map[row][column]) {
            return false;
        } else {
            if(board[row][column] == word.charAt(index)) {
                map[row][column] = true;
                boolean result = solution1Helper(board, map, row + 1, column, index + 1, word) ||
                    solution1Helper(board, map, row - 1, column, index + 1, word) ||
                    solution1Helper(board, map, row, column + 1, index + 1, word) ||
                    solution1Helper(board, map, row, column - 1, index + 1, word);
                map[row][column] = false;
                return result;
            } else {
                return false;
            }
        }
    }

    private boolean solution2(char[][] board, String word) {
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                if(solution2Helper(board, i, j, 0, word)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean solution2Helper(char[][] board, int row, int column, int index, String word) {
        if(index == word.length()) {
            return true;
        }
        if(row < 0 || row >= board.length || column < 0 || column >= board[0].length) {
            return false;
        }
        if(board[row][column] == word.charAt(index)) {
            board[row][column] ^= 256;
            boolean result = solution2Helper(board, row + 1, column, index + 1, word) ||
                solution2Helper(board, row - 1, column, index + 1, word) ||
                solution2Helper(board, row, column + 1, index + 1, word) ||
                solution2Helper(board, row, column - 1, index + 1, word);
            board[row][column] ^= 256;
            return result;
        } else {
            return false;
        }
    }
}