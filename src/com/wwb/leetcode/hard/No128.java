package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.Map;

/**
 * Given an unsorted array of integers, find the length of the longest consecutive elements sequence.
 *
 * For example,
 * Given [100, 4, 200, 1, 3, 2],
 * The longest consecutive elements sequence is [1, 2, 3, 4]. Return its length: 4.
 *
 * Your algorithm should run in O(n) complexity.
 */
public class No128 {

    public int longestConsecutive(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        Map<Integer, Integer> numToCount = new HashMap<>();
        int result = 0;

        for(int num : nums) {
            if(!numToCount.containsKey(num)) {
                int leftCount = numToCount.getOrDefault(num - 1, 0);
                int rightCount = numToCount.getOrDefault(num + 1, 0);
                int sum = leftCount + rightCount + 1;

                result = Math.max(result, sum);

                numToCount.put(num, sum);
                numToCount.put(num - leftCount, sum);
                numToCount.put(num + rightCount, sum);
            }
        }

        return result;
    }
}
