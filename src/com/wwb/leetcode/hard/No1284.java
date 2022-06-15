package com.wwb.leetcode.hard;

/**
 * Given a m x n binary matrix mat. In one step, you can choose one cell and flip it and all the four neighbors of it if they exist (Flip is changing 1 to 0 and 0 to 1). A pair of cells are called neighbors if they share one edge.
 * <p>
 * Return the minimum number of steps required to convert mat to a zero matrix or -1 if you cannot.
 * <p>
 * A binary matrix is a matrix with all cells equal to 0 or 1 only.
 * <p>
 * A zero matrix is a matrix with all cells equal to 0.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: mat = [[0,0],[0,1]]
 * Output: 3
 * Explanation: One possible solution is to flip (1, 0) then (0, 1) and finally (1, 1) as shown.
 * Example 2:
 * <p>
 * Input: mat = [[0]]
 * Output: 0
 * Explanation: Given matrix is a zero matrix. We do not need to change it.
 * Example 3:
 * <p>
 * Input: mat = [[1,0,0],[1,0,0]]
 * Output: -1
 * Explanation: Given matrix cannot be a zero matrix.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * m == mat.length
 * n == mat[i].length
 * 1 <= m, n <= 3
 * mat[i][j] is either 0 or 1.
 */
public class No1284 {
    public int minFlips(int[][] mat) {
        var result = minFlips(mat, 0, 0);

        return result == Integer.MAX_VALUE ? -1 : result;
    }

    private int minFlips(int[][] mat, int startRow, int startColumn) {
        int m = mat.length;
        int n = mat[0].length;

        if (startColumn == n) {
            startRow++;
            startColumn = 0;
        }

        if (startRow == m) {
            return areAllZeros(mat) ? 0 : Integer.MAX_VALUE;
        }

        int notFlipCurrent = minFlips(mat, startRow, startColumn + 1);

        flip(mat, startRow, startColumn);
        int flipCurrent = minFlips(mat, startRow, startColumn + 1);
        flip(mat, startRow, startColumn);

        int minFlips = Math.min(notFlipCurrent, flipCurrent);

        if (minFlips != Integer.MAX_VALUE && minFlips == flipCurrent) {
            // plus one when we want to flip the current cell
            minFlips++;
        }

        return minFlips;
    }

    private boolean areAllZeros(int[][] mat) {
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                if (mat[i][j] == 1) {
                    return false;
                }
            }
        }

        return true;
    }

    private void flip(int[][] mat, int x, int y) {
        mat[x][y] ^= 1;

        if (x - 1 >= 0) {
            mat[x - 1][y] ^= 1;
        }

        if (x + 1 < mat.length) {
            mat[x + 1][y] ^= 1;
        }

        if (y - 1 >= 0) {
            mat[x][y - 1] ^= 1;
        }

        if (y + 1 < mat[0].length) {
            mat[x][y + 1] ^= 1;
        }
    }
}
