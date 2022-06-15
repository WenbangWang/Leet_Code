package com.wwb.leetcode.medium;

/**
 * Given an array of numbers nums,
 * in which exactly two elements appear only once and all the other elements appear exactly twice.
 * Find the two elements that appear only once.
 *
 * For example:
 *
 * Given nums = [1, 2, 1, 3, 2, 5], return [3, 5].
 *
 * Note:
 * The order of the result is not important. So in the above example, [5, 3] is also correct.
 * Your algorithm should run in linear runtime complexity. Could you implement it using only constant space complexity?
 */
public class No260 {

    public int[] singleNumber(int[] nums) {
        int diff = 0;

        for(int num : nums) {
            diff ^= num;
        }

        // at this point, diff is the XOR of
        // the two single occurrence numbers.

        // Get the rightmost significant bit
        // to separate numbers into two groups.
        // When this bit is set, it means the XOR
        // of the two single occurrence numbers (a, b)
        // these two numbers at this position is different.
        diff &= -diff;
        int[] result = new int[2];

        for(int num : nums) {
            if((num & diff) == 0) {
                result[0] ^= num;
            } else {
                result[1] ^= num;
            }
        }

        return result;
    }
}
