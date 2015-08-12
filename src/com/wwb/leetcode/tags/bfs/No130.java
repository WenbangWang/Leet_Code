package com.wwb.leetcode.tags.bfs;

import java.awt.*;
import java.util.Stack;

/**
 * Given a 2D board containing 'X' and 'O', capture all regions surrounded by 'X'.
 *
 * A region is captured by flipping all 'O's into 'X's in that surrounded region.
 *
 * For example,
 * X X X X
 * X O O X
 * X X O X
 * X O X X
 * After running your function, the board should be:
 *
 * X X X X
 * X X X X
 * X X X X
 * X O X X
 */
public class No130 {

    public void solve(char[][] board) {
        if(board == null || board[0] == null || board.length == 0 || board[0].length == 0) {
            return;
        }

        int column = board[0].length;
        int row = board.length;

        if(column < 3 || row < 3) {
            return;
        }

        //top and bottom border
        for(int i = 0; i < column; i++) {
            if(board[0][i] == 'O') {
                flipToUnFlippable(0, i, board);
            }
            if(board[row - 1][i] == 'O') {
                flipToUnFlippable(row - 1, i, board);
            }
        }

        //left and right border
        for(int i = 0; i < row; i++) {
            if(board[i][0] == 'O') {
                flipToUnFlippable(i, 0, board);
            }
            if(board[i][column - 1] == 'O') {
                flipToUnFlippable(i, column - 1, board);
            }
        }

        for(int i = 0; i < row; i++) {
            for(int j = 0; j < column; j++) {
                if(board[i][j] == 'O') {
                    board[i][j] = 'X';
                }
                if(board[i][j] == 'U') {
                    board[i][j] = 'O';
                }
            }
        }
    }

    private void flipToUnFlippable(int i, int j, char[][] board) {
        int column = board[0].length;
        int row = board.length;
        Stack<Point> stack = new Stack<>();

        board[i][j] = 'U';
        stack.push(new Point(i, j));

        while(!stack.isEmpty()) {
            Point current = stack.pop();
            int x = current.x;
            int y = current.y;

            //up
            if(x != 0 && board[x - 1][y] == 'O') {
                stack.push(new Point(x - 1, y));
                board[x - 1][y] = 'U';
            }

            //bottom
            if(x != row - 1 && board[x + 1][y] == 'O') {
                stack.push(new Point(x + 1, y));
                board[x + 1][y] = 'U';
            }

            //left
            if(y != 0 && board[x][y - 1] == 'O') {
                stack.push(new Point(x, y - 1));
                board[x][y - 1] = 'U';
            }

            //right
            if(y != column - 1 && board[x][y + 1] == 'O') {
                stack.push(new Point(x, y + 1));
                board[x][y + 1] = 'U';
            }
        }
    }
}