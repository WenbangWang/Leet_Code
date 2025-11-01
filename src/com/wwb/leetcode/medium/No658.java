package com.wwb.leetcode.medium;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Given a sorted integer array arr, two integers k and x, return the k closest integers to x in the array.
 * The result should also be sorted in ascending order.
 * <p>
 * An integer a is closer to x than an integer b if:
 *
 * <pre>
 * |a - x| < |b - x|, or
 * |a - x| == |b - x| and a < b
 * </pre>
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: arr = [1,2,3,4,5], k = 4, x = 3
 * Output: [1,2,3,4]
 * Example 2:
 *
 * Input: arr = [1,2,3,4,5], k = 4, x = -1
 * Output: [1,2,3,4]
 *
 *
 * Constraints:
 *
 * 1 <= k <= arr.length
 * 1 <= arr.length <= 10^4
 * arr is sorted in ascending order.
 * -10^4 <= arr[i], x <= 10^4
 * </pre>
 */
public class No658 {
    public List<Integer> findClosestElements(int[] arr, int k, int x) {
        int left = 0;
        int right = arr.length - k;

        while (left < right) {
            int mid = (right - left) / 2 + left;
            // x - arr[mid] → distance from left edge of the window to x
            // arr[mid + k] - x → distance from element just outside the right edge to x.
            if (x - arr[mid] > arr[mid + k] - x) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return Arrays.stream(arr, left, left + k).boxed().collect(Collectors.toList());
    }
}
