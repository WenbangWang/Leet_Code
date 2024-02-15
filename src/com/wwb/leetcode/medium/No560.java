package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/**
 * Given an array of integers nums and an integer k, return the total number of subarrays whose sum equals to k.
 *
 * A subarray is a contiguous non-empty sequence of elements within an array.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [1,1,1], k = 2
 * Output: 2
 * Example 2:
 *
 * Input: nums = [1,2,3], k = 3
 * Output: 2
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 2 * 10^4
 * -1000 <= nums[i] <= 1000
 * -10^7 <= k <= 10^7
 */
public class No560 {
    public int subarraySum(int[] nums, int k) {
        int[] prefixSum = new int[nums.length];
        prefixSum[0] = nums[0];

        for (int i = 1; i < nums.length; i++) {
            prefixSum[i] = prefixSum[i - 1] + nums[i];
        }

        Map<Integer, Integer> prefixSumToCount = new HashMap<>();
        int result = 0;

        for (int i = 0; i < prefixSum.length; i++) {
            if (prefixSum[i] == k) {
                result++;
            }

            result += prefixSumToCount.getOrDefault(prefixSum[i] - k, 0);

            prefixSumToCount.put(prefixSum[i], prefixSumToCount.getOrDefault(prefixSum[i], 0) + 1);
        }

        return result;
    }
}
