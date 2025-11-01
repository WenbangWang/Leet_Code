package com.wwb.leetcode.medium;

/**
 * Given a binary array nums and an integer k, return the maximum number of consecutive 1's in the array if you can flip at most k 0's.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: nums = [1,1,1,0,0,0,1,1,1,1,0], k = 2
 * <p>
 * Output: 6
 * <p>
 * Explanation: [1,1,1,0,0,1,1,1,1,1,1]
 * <p>
 * Bolded numbers were flipped from 0 to 1. The longest subarray is underlined.
 * <p>
 * <p>
 * Example 2:
 * <p>
 * Input: nums = [0,0,1,1,0,0,1,1,1,0,1,1,0,0,0,1,1,1,1], k = 3
 * <p>
 * Output: 10
 * <p>
 * Explanation: [0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1]
 * <p>
 * Bolded numbers were flipped from 0 to 1. The longest subarray is underlined.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= nums.length <= 10^5
 * nums[i] is either 0 or 1.
 * 0 <= k <= nums.length
 */
public class No1004 {
    // For each nums[end], try to find the longest subarray.
    // If nums[start] ~ nums[end] has zeros <= K, we continue to increment end.
    // If nums[start] ~ nums[end] has zeros > K, we increment start (as well as end).
    public int longestOnes(int[] nums, int k) {
        return solution1(nums, k);
    }

    private int solution1(int[] nums, int k) {
        int start = 0;
        int end = 0;

        for (; end < nums.length; end++) {
            if (nums[end] == 0) {
                k--;
            }

            if (k < 0 && nums[start++] == 0) {
                k++;
            }
        }

        return end - start;
    }

    private int solution2(int[] nums, int k) {
        int start = 0;
        int maxLen = 0;

        for (int end = 0; end < nums.length; end++) {
            if (nums[end] == 0) {
                k--; // use one flip
            }

            // shrink from left until window is valid again
            while (k < 0) {
                if (nums[start] == 0) {
                    k++;
                }
                start++;
            }

            // update max length
            maxLen = Math.max(maxLen, end - start + 1);
        }

        return maxLen;
    }
}
