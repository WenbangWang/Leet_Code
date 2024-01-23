package com.wwb.leetcode.tags.heap;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Given an integer array nums and an integer k, return the length of the shortest non-empty subarray of nums
 * with a sum of at least k. If there is no such subarray, return -1.
 *
 * A subarray is a contiguous part of an array.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [1], k = 1
 * Output: 1
 * Example 2:
 *
 * Input: nums = [1,2], k = 4
 * Output: -1
 * Example 3:
 *
 * Input: nums = [2,-1,2], k = 3
 * Output: 3
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 10^5
 * -10^5 <= nums[i] <= 10^5
 * 1 <= k <= 10^9
 */
public class No862 {
    public int shortestSubarray(int[] nums, int k) {
        int result = nums.length;
        long[] prefixSum = new long[nums.length + 1];
        Deque<Integer> dq = new ArrayDeque<>();

        for (int i = 1; i < prefixSum.length; i++) {
            prefixSum[i] = prefixSum[i - 1] + nums[i - 1];
        }

        for (int i = 0; i < prefixSum.length; i++) {
            while (!dq.isEmpty() && prefixSum[i] - prefixSum[dq.peekFirst()] >= k) {
                result = Math.min(result, i - dq.pollFirst());
            }

            // to keep the deque increasing.
            // if we keep numbers greater than prefixSum[i] in the dequeue, it will never meet the condition
            // prefixSum[i] - dq.peekFirst() >= k when k is positive
            while (!dq.isEmpty() && prefixSum[i] <= prefixSum[dq.peekLast()]) {
                dq.pollLast();
            }

            dq.offerLast(i);
        }

        return result == nums.length ? -1 : result;
    }
}
