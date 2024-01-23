package com.wwb.leetcode.medium;

import java.util.Stack;

/**
 * Given an array of integers arr, find the sum of min(b), where b ranges over every (contiguous) subarray of arr.
 * Since the answer may be large, return the answer modulo 10^9 + 7.
 *
 *
 *
 * Example 1:
 *
 * Input: arr = [3,1,2,4]
 * Output: 17
 * Explanation:
 * Subarrays are [3], [1], [2], [4], [3,1], [1,2], [2,4], [3,1,2], [1,2,4], [3,1,2,4].
 * Minimums are 3, 1, 2, 4, 1, 1, 2, 1, 1, 1.
 * Sum is 17.
 * Example 2:
 *
 * Input: arr = [11,81,94,43,3]
 * Output: 444
 *
 *
 * Constraints:
 *
 * 1 <= arr.length <= 3 * 104
 * 1 <= arr[i] <= 3 * 104
 */
public class No907 {
    // Assume the there are X elements before current element (E) which are greater
    // and there are Y elements after i which are greater.
    // The number of subarrays including E for elements ...X, E, ...Y
    // is (X + 1) * (Y + 1) since element E is the smallest among
    // all elements mentioned. And the sum of the minimum element
    // for all subarrays could be simplified to
    // sum((X + 1) * (Y + 1) * E) for ALL elements (E)
    // In practice, denote the index for E is i, in order to find
    // X and Y, we need to figure out what is the previous element
    // which is just less than E and what is the next element which is
    // just less than E. Using the index of both elements (p, n for respectively)
    // together with the index for current element i,
    // (X + 1) * (Y + 1) will become (i - p) * (n - i)
    public int sumSubarrayMins(int[] arr) {
        final int MODULO = (int) StrictMath.pow(10, 9) + 7;

        // monotonous increasing stack
        // only store index of the array
        // the meaning of the stack is the value
        // (access by arr[index]) is monotonically increasing
        Stack<Integer> stack = new Stack<>();
        long result = 0;

        for (int nextLess = 0; nextLess <= arr.length; nextLess++) {
            while(!stack.isEmpty() && (nextLess == arr.length || arr[stack.peek()] > arr[nextLess])) {
                int currentMinimum = stack.pop();
                // since the this is a mono-stack, the next peek element in the stack
                // is the previous element which is just less than the current element.
                int previousLess = stack.isEmpty() ? -1 : stack.peek();
                result += (long) arr[currentMinimum] * (currentMinimum - previousLess) * (nextLess - currentMinimum);
            }

            stack.push(nextLess);
        }

        return (int) (result % MODULO);
    }

    private static class Pair {
        int value;
        int index;

        Pair(int value, int index) {
            this.value = value;
            this.index = index;
        }
    }
}
