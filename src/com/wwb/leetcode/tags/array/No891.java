package com.wwb.leetcode.tags.array;

import java.util.Arrays;

/**
 * The width of a sequence is the difference between the maximum and minimum elements in the sequence.
 *
 * Given an array of integers nums, return the sum of the widths of all the non-empty subsequences of nums.
 * Since the answer may be very large, return it modulo 109 + 7.
 *
 * A subsequence is a sequence that can be derived from an array by deleting some or no elements
 * without changing the order of the remaining elements.
 * For example, [3,6,2,7] is a subsequence of the array [0,3,1,6,2,2,7].
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [2,1,3]
 * Output: 6
 * Explanation: The subsequences are [1], [2], [3], [2,1], [2,3], [1,3], [2,1,3].
 * The corresponding widths are 0, 0, 0, 1, 1, 2, 2.
 * The sum of these widths is 6.
 * Example 2:
 *
 * Input: nums = [2]
 * Output: 0
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 10^5
 * 1 <= nums[i] <= 10^5
 */
public class No891 {
    /**
     * For each number A[i]:
     *
     * There are i smaller numbers,
     * so there are 2 ^ i sequences in which A[i] is maximum.
     * we should do res += A[i] * 2^i
     *
     * There are n - i - 1 bigger numbers,
     * so there are 2 ^ (n - i - 1) sequences in which A[i] is minimum.
     * we should do res -= A[i] * 2^(n - i - 1)
     * @param nums
     * @return
     */
    public int sumSubseqWidths(int[] nums) {
        final int MODULO = (int)1e9 + 7;

        Arrays.sort(nums);

        long sum = 0;
        long[] exp = new long[nums.length];
        exp[0] = 1;

        for (int i = 1; i < nums.length; i++) {
            exp[i] = exp[i - 1] * 2 % MODULO;
        }

        for (int i = 0, n = nums.length; i < n; i++) {
            sum = (sum + nums[i] * exp[i] - nums[i] * exp[n - i - 1]) % MODULO;
        }

        return (int) ((sum + MODULO) % MODULO);
    }
}
