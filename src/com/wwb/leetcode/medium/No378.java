package com.wwb.leetcode.medium;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Given a n row n matrix where each of the rows and columns are sorted in ascending order, find the kth smallest element in the matrix.
 *
 * Note that it is the kth smallest element in the sorted order, not the kth distinct element.
 *
 * Example:
 *
 * matrix = [
 *   [ 1,  5,  9],
 *   [10, 11, 13],
 *   [12, 13, 15]
 * ],
 * k = 8,
 *
 * return 13.
 * Note:
 * You may assume k is always valid, 1 ≤ k ≤ n2.
 */
public class No378 {
    public int kthSmallest(int[][] matrix, int k) {
        if (matrix == null || matrix[0] == null || matrix.length == 0 || matrix[0].length == 0) {
            return -1;
        }


        Queue<Tuple> minHeap = new PriorityQueue<>();
        int rowLength = matrix[0].length;
        int columnLength = matrix.length;

        for (int column = 0; column < columnLength; column++) {
            minHeap.offer(new Tuple(0, column, matrix[0][column]));
        }

        for (int row = 0; row < k - 1; row++) {
            Tuple tuple = minHeap.poll();

            if (tuple.row == rowLength - 1) {
                continue;
            }

            minHeap.offer(new Tuple(tuple.row + 1, tuple.column, matrix[tuple.row + 1][tuple.column]));
        }

        return minHeap.peek().value;
    }

    private class Tuple implements Comparable<Tuple> {
        private int row;
        private int column;
        private int value;

        public Tuple(int row, int column, int value) {
            this.row = row;
            this.column = column;
            this.value = value;
        }

        public int compareTo (Tuple anotherTuple) {
            return this.value - anotherTuple.value;
        }
    }
}
