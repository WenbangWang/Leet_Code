package com.wwb.leetcode.medium;

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

        int length = nums.length;
        int[] count = new int[length];
        // previous element of subset ending at ith index
        int[] predecessors = new int[length];
        int maxIndex = 0;

        // initially count[i]=1 since we can always form subset of size=1 ending at i.
        Arrays.fill(count, 1);
        // predecessors[i]=-1 because we haven't found any predecessors for any subset yet
        Arrays.fill(predecessors, -1);

        for (int i = 1; i < length; i++) {
            for (int j = 0; j < i; j++) {
                // nums[i] should divide nums[j] if it is to be included in its subset (i.e count[j])
                // only include nums[i] in subset ending at j
                // if resultant subset size (count[j]+1) is larger than already possible (count[i])
                if (nums[i] % nums[j] == 0 && count[i] < 1 + count[j]) {
                    count[i] = 1 + count[j];
                    // jth element will be predecessor to subset ending at ith element
                    predecessors[i] = j;
                }
            }

            if (count[i] > count[maxIndex]) {
                maxIndex = i;
            }
        }

        List<Integer> result = new ArrayList<>();

        // start with index where largest subset ended. Reconstruct from that point to the start
        for (; maxIndex >= 0; maxIndex = predecessors[maxIndex]) {
            result.add(nums[maxIndex]);
        }

        return result;
    }
}
