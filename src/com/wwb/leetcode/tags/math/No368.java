package com.wwb.leetcode.tags.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Given a set of distinct positive integers, find the largest subset such that every pair (Si, Sj) of elements in this subset satisfies: Si % Sj = 0 or Sj % Si = 0.
 *
 * If there are multiple solutions, return any subset is fine.
 *
 * Example 1:
 *
 * nums: [1,2,3]
 *
 * Result: [1,2] (of course, [1,3] will also be ok)
 * Example 2:
 *
 * nums: [1,2,4,8]
 *
 * Result: [1,2,4,8]
 */
public class No368 {
    public List<Integer> largestDivisibleSubset(int[] nums) {
        if (nums == null || nums.length == 0) {
            return Collections.emptyList();
        }

        Arrays.sort(nums);

        List<Integer> result = new ArrayList<>();
        int length = nums.length;
        int[] count = new int[length];
        int[] parent = new int[length];
        int max = 0;
        int maxIndex = 0;

        for (int i = length - 1; i >= 0; i--) {
            for (int j = i; j < length; j++) {
                if (nums[j] % nums[i] == 0 && count[i] < 1 + count[j]) {
                    count[i] = 1 + count[j];
                    parent[i] = j;
                }
            }

            if (count[i] > max) {
                max = count[i];
                maxIndex = i;
            }
        }

        for (int i = 0; i < max; i++) {
            result.add(nums[maxIndex]);
            maxIndex = parent[maxIndex];
        }

        return result;
    }
}
