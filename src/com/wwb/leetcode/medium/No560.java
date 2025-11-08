package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/**
 * Given an array of integers nums and an integer k, return the total number of subarrays whose sum equals to k.
 * <p>
 * A subarray is a contiguous non-empty sequence of elements within an array.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: nums = [1,1,1], k = 2
 * Output: 2
 * Example 2:
 * <p>
 * Input: nums = [1,2,3], k = 3
 * Output: 2
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= nums.length <= 2 * 10^4
 * -1000 <= nums[i] <= 1000
 * -10^7 <= k <= 10^7
 */
public class No560 {
    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> sumToCount = new HashMap<>();
        int result = 0;
        int sum = 0;
        sumToCount.put(0, 1);

        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];

            result += sumToCount.getOrDefault(sum - k, 0);

            sumToCount.put(sum, sumToCount.getOrDefault(sum, 0) + 1);
        }

        return result;
    }
}
