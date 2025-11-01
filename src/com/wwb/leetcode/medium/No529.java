package com.wwb.leetcode.medium;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Let's play the minesweeper game (Wikipedia, online game)!
 * <p>
 * You are given an m x n char matrix board representing the game board where:
 * <p>
 * 'M' represents an unrevealed mine,
 * <p>
 * 'E' represents an unrevealed empty square,
 * <p>
 * 'B' represents a revealed blank square that has no adjacent mines (i.e., above, below, left, right, and all 4 diagonals),
 * <p>
 * digit ('1' to '8') represents how many mines are adjacent to this revealed square, and
 * 'X' represents a revealed mine.
 * <p>
 * You are also given an integer array click where click = [clickr, clickc] represents the next click position among all the unrevealed squares ('M' or 'E').
 * <p>
 * <p>
 * Return the board after revealing this position according to the following rules:
 * <p>
 * If a mine 'M' is revealed, then the game is over. You should change it to 'X'.
 * If an empty square 'E' with no adjacent mines is revealed, then change it to a revealed blank 'B' and all of its adjacent unrevealed squares should be revealed recursively.
 * If an empty square 'E' with at least one adjacent mine is revealed, then change it to a digit ('1' to '8') representing the number of adjacent mines.
 * Return the board when no more squares will be revealed.
 *
 *
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/529_1.jpeg" />
 *
 * Input: board = [["E","E","E","E","E"],["E","E","M","E","E"],["E","E","E","E","E"],["E","E","E","E","E"]], click = [3,0]
 * Output: [["B","1","E","1","B"],["B","1","M","1","B"],["B","1","1","1","B"],["B","B","B","B","B"]]
 * </pre>
 *
 * <pre>
 * Example 2:
 *
 * <img src="../doc-files/529_2.jpeg" />
 *
 * Input: board = [["B","1","E","1","B"],["B","1","M","1","B"],["B","1","1","1","B"],["B","B","B","B","B"]], click = [1,2]
 * Output: [["B","1","E","1","B"],["B","1","X","1","B"],["B","1","1","1","B"],["B","B","B","B","B"]]
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * m == board.length
 * n == board[i].length
 * 1 <= m, n <= 50
 * board[i][j] is either 'M', 'E', 'B', or a digit from '1' to '8'.
 * click.length == 2
 * 0 <= clickr < m
 * 0 <= clickc < n
 * board[clickr][clickc] is either 'M' or 'E'.
 * </pre>
 */
public class No529 {
    public char[][] updateBoard(char[][] board, int[] click) {
        if (board[click[0]][click[1]] == 'M') {
            board[click[0]][click[1]] = 'X';
            return board;
        }

        Queue<int[]> queue = new LinkedList<>();
        int[][] dirs = new int[][]{{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        int m = board.length;
        int n = board[0].length;

        queue.offer(click);

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                int numberOfMinesSurrounded = 0;
                int[] cell = queue.poll();
                Queue<int[]> neighbours = new LinkedList<>();

                for (int[] dir : dirs) {
                    int row = cell[0] + dir[0];
                    int col = cell[1] + dir[1];

                    if (row >= m || row < 0) {
                        continue;
                    }

                    if (col >= n || col < 0) {
                        continue;
                    }

                    if (board[row][col] == 'M') {
                        numberOfMinesSurrounded++;
                        continue;
                    }

                    if (board[row][col] == 'E') {
                        neighbours.offer(new int[]{row, col});
                    }
                }

                if (numberOfMinesSurrounded > 0) {
                    board[cell[0]][cell[1]] = (char) (numberOfMinesSurrounded + '0');
                } else {
                    board[cell[0]][cell[1]] = 'B';
                    for (int[] neighbour : neighbours) {
                        board[neighbour[0]][neighbour[1]] = 'B';
                    }
                    queue.addAll(neighbours);
                }
            }
        }

        return board;
    }
}
