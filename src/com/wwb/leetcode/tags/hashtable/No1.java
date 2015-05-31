package com.wwb.leetcode.tags.hashtable;

import java.util.HashMap;

/**
 * Given an array of integers, find two numbers such that they add up to a specific target number.
 *
 * The function twoSum should return indices of the two numbers such that they add up to the target, where index1 must be less than index2. Please note that your returned answers (both index1 and index2) are not zero-based.
 *
 * You may assume that each input would have exactly one solution.
 *
 * Input: numbers={2, 7, 11, 15}, target=9
 * Output: index1=1, index2=2
 */
public class No1 {

    public int[] twoSum(int[] nums, int target) {
        // return solution1(nums, target);
        return solution2(nums, target);
    }

    private int[] solution1(int[] nums, int target) {
        int[] result = new int[2];

        for(int i = 0; i < nums.length; i++) {
            for(int j = i + 1; j < nums.length; j++) {
                if((nums[i] + nums[j]) == target) {
                    result[0] = i + 1;
                    result[1] = j + 1;

                    return result;
                }
            }
        }

        return result;
    }

    private int[] solution2(int[] nums, int target) {
        int[] result = new int[2];
        HashMap<Integer, Integer> map = new HashMap<>();

        for(int i = 0; i < nums.length; i++) {
            map.put(nums[i], i);
        }

        for(int i = 0; i < nums.length; i++) {
            int sub = target - nums[i];

            if(map.containsKey(sub) && i < map.get(sub)) {
                result[0] = i + 1;
                result[1] = map.get(sub) + 1;

                return result;
            }
        }

        return result;
    }
}
