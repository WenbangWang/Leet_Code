package com.wwb.leetcode.medium;

/**
 * A sequence of number is called arithmetic if it consists of at least three elements and if the difference between any two consecutive elements is the same.
 *
 * For example, these are arithmetic sequence:
 *
 * 1, 3, 5, 7, 9
 * 7, 7, 7, 7
 * 3, -1, -5, -9
 * The following sequence is not arithmetic.
 *
 * 1, 1, 2, 5, 7
 *
 * A zero-indexed array A consisting of N numbers is given. A slice of that array is any pair of integers (P, Q) such that 0 <= P < Q < N.
 *
 * A slice (P, Q) of array A is called arithmetic if the sequence:
 * A[P], A[p + 1], ..., A[Q - 1], A[Q] is arithmetic. In particular, this means that P + 1 < Q.
 *
 * The function should return the number of arithmetic slices in the array A.
 *
 *
 * Example:
 *
 * A = [1, 2, 3, 4]
 *
 * return: 3, for 3 arithmetic slices in A: [1, 2, 3], [2, 3, 4] and [1, 2, 3, 4] itself.
 */
public class No413 {
    public int numberOfArithmeticSlices(int[] A) {
        return solution2(A);
    }

    private int solution2(int[] a) {
        if (a == null || a.length < 2) {
            return 0;
        }

        int result = 0;
        int count = 0;

        for(int i = 1; i < a.length - 1; i++) {
            if (a[i] - a[i - 1] == a[i + 1] - a[i]) {
                count++;
                result += count;
            } else {
                count = 0;
            }
        }

        return result;
    }

    private int solution1(int[] A) {
        var sum = new int[1];
        solution1(A, A.length - 1, sum);

        return sum[0];
    }

    private int solution1(int[]A, int index, int[] sum) {
        if (index < 2) {
            return 0;
        }

        int count = 0;

        if (A[index] - A[index - 1] == A[index - 1] - A[index - 2]) {
            count = 1 + solution1(A, index - 1, sum);
            sum[0] += count;
        } else {
            solution1(A, index - 1, sum);
        }

        return count;
    }

    private static class ArithmeticSlices {
        int sum;
        int count;
    }
}
