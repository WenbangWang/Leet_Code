package com.wwb.leetcode.tags.hashtable;

import java.util.HashMap;
import java.util.Map;

/**
 * Given a binary array, find the maximum length of a contiguous subarray with equal number of 0 and 1.
 *
 * Example 1:
 * Input: [0,1]
 * Output: 2
 * Explanation: [0, 1] is the longest contiguous subarray with equal number of 0 and 1.
 * Example 2:
 * Input: [0,1,0]
 * Output: 2
 * Explanation: [0, 1] (or [1, 0]) is a longest contiguous subarray with equal number of 0 and 1.
 * Note: The length of the given binary array will not exceed 50,000.
 */
public class No525 {
    public int findMaxLength(int[] nums) {
        if(nums == null || nums.length == 0 || nums.length == 1) {
            return 0;
        }

        Map<Integer, Integer> sumToIndexMap = new HashMap<>();
        int max = 0;
        int sum = 0;

        // length should be 1 bigger than index.
        sumToIndexMap.put(0, -1);
        for(int i = 0; i < nums.length; i++) {
            if(nums[i] == 0) {
                nums[i] = -1;
            }
        }

        for(int i = 0; i < nums.length; i++) {
            sum += nums[i];

            if(sumToIndexMap.containsKey(sum)) {
                max = Math.max(max, i - sumToIndexMap.get(sum));
            } else {
                sumToIndexMap.put(sum, i);
            }
        }

        return max;
    }
}
