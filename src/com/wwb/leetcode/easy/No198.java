package com.wwb.leetcode.easy;

import java.util.HashMap;
import java.util.Map;

/**
 * You are a professional robber planning to rob houses along a street.
 * Each house has a certain amount of money stashed, the only constraint stopping you from robbing each of them is that
 * adjacent houses have security system connected and it will automatically contact the police if two adjacent houses were broken into on the same night.
 *
 * Given a list of non-negative integers representing the amount of money of each house,
 * determine the maximum amount of money you can rob tonight without alerting the police.
 */
public class No198 {

    public int rob(int[] nums) {
        int notStealCurrent = 0;
        int stealCurrent = 0;

        for(int num : nums) {
            int notStealPrevious = notStealCurrent;
            int stealPrevious = stealCurrent;
            notStealCurrent = Math.max(notStealPrevious, stealPrevious);
            stealCurrent = num + notStealPrevious;
        }

        return Math.max(notStealCurrent, stealCurrent);
    }
}