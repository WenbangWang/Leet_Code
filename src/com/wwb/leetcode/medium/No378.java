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
        int numberOfColumns = matrix[0].length;
        int numberOfRows = matrix.length;

        for (int column = 0; column < numberOfColumns; column++) {
            minHeap.offer(new Tuple(0, column, matrix[0][column]));
        }

        for (int row = 0; row < k - 1; row++) {
            Tuple tuple = minHeap.poll();

            if (tuple.row == numberOfRows - 1) {
                continue;
            }

            minHeap.offer(new Tuple(tuple.row + 1, tuple.column, matrix[tuple.row + 1][tuple.column]));
        }

        return minHeap.peek().value;
    }

    private static class Tuple implements Comparable<Tuple> {
        private int row;
        private int column;
        private int value;

        Tuple(int row, int column, int value) {
            this.row = row;
            this.column = column;
            this.value = value;
        }

        public int compareTo (Tuple anotherTuple) {
            return Integer.compare(this.value, anotherTuple.value);
        }
    }

    private int solution2(int[][] matrix, int k) {
        if (matrix == null || matrix[0] == null || matrix.length == 0 || matrix[0].length == 0) {
            return -1;
        }

        int n = matrix.length;

        int smallest = matrix[0][0];
        int largest = matrix[n - 1][n - 1];

        while (smallest < largest) {
            int mid = (largest - smallest) / 2 + smallest;

            var pair = getCountSmallerThanOrEqualTo(mid, matrix);

            if (pair.count == k) {
                return pair.smaller;
            }

            if (pair.count > k) {
                largest = pair.smaller;
            } else {
                smallest = pair.bigger;
            }
        }

        return smallest;
    }

    private Pair getCountSmallerThanOrEqualTo(int value, int[][] matrix) {
        int n = matrix.length;
        int row = n - 1;
        int column = 0;

        Pair pair = new Pair(Integer.MIN_VALUE, Integer.MAX_VALUE);

        while (row >= 0 && column < n) {
            if (matrix[row][column] > value) {
                // keep track of the smallest number greater than "value".
                pair.bigger = Math.min(pair.bigger, matrix[row][column]);
                // all numbers at this row would be greater than "value"
                // hence move one row up
                row--;
            } else {
                // keep track of the biggest number smaller than or equal to "value"
                pair.smaller = Math.max(pair.smaller, matrix[row][column]);
                column++;
                // all the numbers within this column should be smaller than or equal to "value"
                // hence using "row + 1" to represent the count within this column
                pair.count += row + 1;
            }
        }

        return pair;
    }

    private static class Pair {
        int smaller;
        int bigger;
        int count;

        Pair(int smaller, int bigger) {
            this.smaller = smaller;
            this.bigger = bigger;
        }
    }
}
