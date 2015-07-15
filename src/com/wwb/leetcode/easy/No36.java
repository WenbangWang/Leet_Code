package com.wwb.leetcode.easy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Determine if a Sudoku is valid, according to: Sudoku Puzzles - The Rules.
 *
 * The Sudoku board could be partially filled, where empty cells are filled with the character '.'.
 *
 *
 * A partially filled sudoku which is valid.
 *
 * Note:
 * A valid Sudoku board (partially filled) is not necessarily solvable. Only the filled cells need to be validated.
 */
public class No36 {

    public boolean isValidSudoku(char[][] board) {
        final int LENGTH = 9;
        boolean[][] row = new boolean[LENGTH][LENGTH];
        boolean[][] column = new boolean[LENGTH][LENGTH];
        boolean[][] cube = new boolean[LENGTH][LENGTH];

        for(int rowIndex = 0; rowIndex < LENGTH; rowIndex++) {
            for(int columnIndex = 0; columnIndex < LENGTH; columnIndex++) {
                char c = board[rowIndex][columnIndex];
                if(c != '.') {
                    int num = c - '0' - 1;
                    int cubeIndex = rowIndex / 3 * 3 + columnIndex / 3;

                    if(row[rowIndex][num] || column[columnIndex][num] || cube[cubeIndex][num]) {
                        return false;
                    }

                    row[rowIndex][num] = column[columnIndex][num] = cube[cubeIndex][num] = true;
                }
            }
        }

        return true;
    }
}