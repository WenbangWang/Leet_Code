package com.wwb.leetcode.medium;

import java.util.Objects;
import java.util.PriorityQueue;
import java.util.TreeSet;

/**
 * You are given a sorted integer array arr containing 1 and prime numbers, where all the integers of arr are unique.
 * You are also given an integer k.
 *
 * For every i and j where 0 <= i < j < arr.length, we consider the fraction arr[i] / arr[j].
 *
 * Return the kth smallest fraction considered. Return your answer as an array of integers of size 2,
 * where answer[0] == arr[i] and answer[1] == arr[j].
 *
 *
 *
 * Example 1:
 *
 * Input: arr = [1,2,3,5], k = 3
 * Output: [2,5]
 * Explanation: The fractions to be considered in sorted order are:
 * 1/5, 1/3, 2/5, 1/2, 3/5, and 2/3.
 * The third fraction is 2/5.
 * Example 2:
 *
 * Input: arr = [1,7], k = 1
 * Output: [1,7]
 *
 *
 * Constraints:
 *
 * 2 <= arr.length <= 1000
 * 1 <= arr[i] <= 3 * 10^4
 * arr[0] == 1
 * arr[i] is a prime number for i > 0.
 * All the numbers of arr are unique and sorted in strictly increasing order.
 * 1 <= k <= arr.length * (arr.length - 1) / 2
 */
public class No786 {
    public int[] kthSmallestPrimeFraction(int[] arr, int k) {
        return solution1(arr, k);
    }

    private int[] solution1(int[] arr, int k) {
        TreeSet<Node> heap = new TreeSet<>();

        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                heap.add(new Node(arr[i], arr[j]));

                if (heap.size() == k + 1) {
                    heap.pollLast();
                }
            }
        }

        Node last = heap.pollLast();

        return new int[]{last.divisor, last.dividend};
    }

    private int[] solution2(int[] arr, int k) {
        int n = arr.length;
        PriorityQueue<int[]> queue = new PriorityQueue<>((a, b) -> Integer.compare(arr[a[0]] * arr[n - 1 - b[1]], arr[b[0]] * arr[n - 1 - a[1]]));

        for (int i = 0; i < n; i++) {
            queue.offer(new int[]{i, 0});
        }

        while (--k > 0) {
            int[] p = queue.poll();

            if (++p[1] < n) {
                queue.offer(p);
            }
        }

        return new int[]{arr[queue.peek()[0]], arr[n - 1 - queue.peek()[1]]};
    }

    private int[] solution3(int[] arr, int k) {
        double left = 0;
        double right = 1;
        int divisor = 0;
        int dividend = 1;

        for (int n = arr.length, count = 0; true; count = 0, divisor = 0) {
            double mid = left + (right - left) / 2;

            // find the number of fractions smaller than mid
            for (int i = 0, j = n - 1; i < n; i++) {
                // arr[i] / arr[n - 1 - j] > mid
                while (j >= 0 && arr[i] > mid * arr[n - 1 - j]) {
                    j--;
                }

                count+= (j + 1);

                // record the current divisor and dividend
                // divisor / dividend < arr[i] / arr[n - 1 - j]
                if (j >= 0 && divisor * arr[n - 1 - j] < dividend * arr[i]) {
                    divisor = arr[i];
                    dividend = arr[n - 1 - j];
                }
            }

            if (count < k ) {
                left = mid;
            } else if (count > k) {
                right = mid;
            } else {
                return new int[] {divisor, dividend};
            }
        }
    }

    private static class Node implements Comparable<Node> {
        int divisor;
        int dividend;


        Node(int divisor, int dividend) {
            this.divisor = divisor;
            this.dividend = dividend;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Node node)) {
                return false;
            }
            return divisor == node.divisor && dividend == node.dividend;
        }

        @Override
        public int hashCode() {
            return Objects.hash(divisor, dividend);
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.divisor * o.dividend, o.divisor * this.dividend);
        }
    }
}
