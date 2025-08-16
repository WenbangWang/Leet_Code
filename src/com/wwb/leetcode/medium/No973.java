package com.wwb.leetcode.medium;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Given an array of points where points[i] = [xi, yi] represents a point on the X-Y plane and an integer k,
 * return the k closest points to the origin (0, 0).
 * <p>
 * The distance between two points on the X-Y plane is the Euclidean distance (i.e., √(x1 - x2)2 + (y1 - y2)2).
 * <p>
 * You may return the answer in any order. The answer is guaranteed to be unique (except for the order that it is in).
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: points = [[1,3],[-2,2]], k = 1
 * <p>
 * Output: [[-2,2]]
 * <p>
 * Explanation:
 * <p>
 * The distance between (1, 3) and the origin is sqrt(10).
 * <p>
 * The distance between (-2, 2) and the origin is sqrt(8).
 * <p>
 * Since sqrt(8) < sqrt(10), (-2, 2) is closer to the origin.
 * <p>
 * We only want the closest k = 1 points from the origin, so the answer is just [[-2,2]].
 * <p>
 * <p>
 * Example 2:
 * <p>
 * Input: points = [[3,3],[5,-1],[-2,4]], k = 2
 * <p>
 * Output: [[3,3],[-2,4]]
 * <p>
 * Explanation: The answer [[-2,4],[3,3]] would also be accepted.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= k <= points.length <= 10^4
 * -10^4 <= xi, yi <= 10^4
 */
public class No973 {
    public int[][] kClosest(int[][] points, int k) {
        return solution1(points, k);
    }

    private int[][] solution1(int[][] points, int k) {
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((p1, p2) -> distance(p2) - distance(p1));

        for (int[] point : points) {
            maxHeap.offer(point);

            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }

        return maxHeap.toArray(n -> new int[n][2]);
    }

    private int[][] solution2(int[][] points, int k) {
        List<int[]> list = Arrays.asList(points);
        Collections.shuffle(list);
        points = list.toArray(n -> new int[n][2]);
        int start = 0;
        int end = points.length - 1;

        while (start <= end) {
            int pivot = quickSelect(points, start, end);

            if (pivot == k) {
                break;
            } else if (pivot > k) {
                end = pivot - 1;
            } else {
                start = pivot + 1;
            }
        }

        return Arrays.copyOfRange(points, 0, k);
    }

    private int quickSelect(int[][] points, int start, int end) {
        int pivot = start;
        int pivotDistance = distance(points[pivot]);

        while (start <= end) {
            while (start <= end && distance(points[start]) <= pivotDistance) {
                start++;
            }

            while (start <= end && distance(points[end]) > pivotDistance) {
                end--;
            }

            if (start > end) {
                break;
            }

            swap(points, start, end);
        }

        swap(points, pivot, end);

        return end;
    }

    private int distance(int[] point) {
        return point[0] * point[0] + point[1] * point[1];
    }

    private void swap(int[][] arr, int i, int j) {
        int[] temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
