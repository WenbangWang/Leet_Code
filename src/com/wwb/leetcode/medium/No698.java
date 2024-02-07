package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Given an integer array nums and an integer k, return true if it is possible to divide this array
 * into k non-empty subsets whose sums are all equal.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [4,3,2,3,5,2,1], k = 4
 * Output: true
 * Explanation: It is possible to divide it into 4 subsets (5), (1, 4), (2,3), (2,3) with equal sums.
 * Example 2:
 *
 * Input: nums = [1,2,3,4], k = 3
 * Output: false
 *
 *
 * Constraints:
 *
 * 1 <= k <= nums.length <= 16
 * 1 <= nums[i] <= 10^4
 * The frequency of each element is in the range [1, 4].
 */
public class No698 {
    public boolean canPartitionKSubsets(int[] nums, int k) {
        if (k == 0) {
            return false;
        }

        if (k == 1) {
            return true;
        }

        var sum = Arrays.stream(nums).sum();

        if (sum % k != 0) {
            return false;
        }

        Arrays.sort(nums);

        // essentially any number should not be greater than sum / k
        // which means we should have subset consists at least one
        // number.
        if (nums[nums.length - 1] > sum / k) {
            return false;
        }

        return solution2(nums, k, sum);
    }

    // TLE
    // O(N * 2^N) time and O(N) space
    private boolean solution1(int[] nums, int k, int sum) {
        var visited = new boolean[nums.length];

        return backtrack(nums, nums.length - 1, 0, k, 0, sum / k, visited);
    }

    // O(N * 2^N) time and O(2^N) space
    private boolean solution2(int[] nums, int k, int sum) {
        int n = nums.length;
        // represent the total sum when include bit representation of index
        // where each bit position represent nums[i]
        int[] totalSum = new int[1 << n];
        Arrays.fill(totalSum, -1);
        int target = sum / k;

        System.out.println("Target: " + target);

        totalSum[0] = 0;

        for (int i = 0; i < (1 << n); i++) {
            if (totalSum[i] == -1) {
                continue;
            }

            // totalSum[i] could include multiple subsets
            // by %target we would get the remaining
            int remaining = target - (totalSum[i] % target);

            for (int j = 0; j < n; j++) {
                int future = i | (1 << j);

                // already included
                if (i == future) {
                    continue;
                }

                if (nums[j] > remaining) {
                    break;
                }

                totalSum[future] = totalSum[i] + nums[j];
            }
        }

        for (int i = 0; i < totalSum.length; i++) {
            if (totalSum[i] == -1) {
                continue;
            }
            List<Integer> subset = new ArrayList<>();

            String bit = Integer.toBinaryString(i);

            for (int j = 0; j < bit.length(); j++) {
                if (bit.charAt(j) == '1') {
                    subset.add(nums[j]);
                }
            }

            System.out.println("bit " + bit + " subset " + subset + " sum is " + totalSum[i]);
        }

        return totalSum[(1 << n) - 1] % target == 0;
    }

    private boolean backtrack(int[] nums, int index, int count, int k, int currentSum, int targetSum, boolean[] visited) {
        // We made k - 1 subsets with target sum and last subset will also have target sum.
        if (count == k - 1) {
            return true;
        }

        if (currentSum > targetSum) {
            return false;
        }

        if (targetSum == currentSum) {
            return backtrack(nums, nums.length - 1,count + 1, k, 0, targetSum, visited);
        }

        for (int i = index; i >= 0; i--) {
            if (!visited[i]) {
                visited[i] = true;

                if (backtrack(nums, index - 1, count, k, currentSum + nums[i], targetSum, visited)) {
                    return true;
                }

                visited[i] = false;
            }
        }

        return false;
    }
}
