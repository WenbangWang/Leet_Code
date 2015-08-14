package com.wwb.leetcode.tags.bitmanipulation;

/**
 * Given an array of integers, every element appears three times except for one. Find that single one.
 *
 * Note:
 * Your algorithm should have a linear runtime complexity. Could you implement it without using extra memory?
 */
public class No137 {

    public int singleNumber(int[] nums) {
        int ones = 0;
        int twos = 0;

        for(int i = 0; i < nums.length; i++) {
            ones = (ones ^ nums[i]) & ~twos;
            twos = (twos ^ nums[i]) & ~ones;
        }

        return ones;
    }
}