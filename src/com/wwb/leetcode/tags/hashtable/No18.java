package com.wwb.leetcode.tags.hashtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Given an array S of n integers, are there elements a, b, c, and d in S such that a + b + c + d = target?
 * Find all unique quadruplets in the array which gives the sum of target.
 *
 * Note:
 * Elements in a quadruplet (a,b,c,d) must be in non-descending order. (ie, a ≤ b ≤ c ≤ d)
 * The solution set must not contain duplicate quadruplets.
 * For example, given array S = {1 0 -1 0 -2 2}, and target = 0.
 *
 * A solution set is:
 * (-1,  0, 0, 1)
 * (-2, -1, 1, 2)
 * (-2,  0, 0, 2)
 */
public class No18 {

    public List<List<Integer>> fourSum(int[] nums, int target) {
        List<List<Integer>> result = new ArrayList<>();

        if(nums == null || nums.length < 4) {
            return result;
        }

        int length = nums.length;
        Arrays.sort(nums);

        for(int first = 0; first < length; first++) {

            for(int second = first + 1; second < length; second++) {
                int sumOfFirstAndSecond = nums[first] + nums[second];
                int third = second + 1;
                int forth = length - 1;

                while(third < forth) {
                    int sumOfThirdAndForth = nums[third] + nums[forth];
                    int sum = sumOfFirstAndSecond + sumOfThirdAndForth;
                    if(sum == target) {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(nums[first]);
                        list.add(nums[second]);
                        list.add(nums[third]);
                        list.add(nums[forth]);

                        result.add(list);

                        third++;
                        forth--;

                        while(third < forth && nums[third] == nums[third - 1]) {
                            third++;
                        }
                        while(third < forth && nums[forth] == nums[forth + 1]) {
                            forth--;
                        }
                    } else if(sum > target) {
                        forth--;
                    } else {
                        third++;
                    }
                }

                while(second < length - 1 && nums[second] == nums[second + 1]) {
                    second++;
                }
            }

            while(first < length - 1 && nums[first] == nums[first + 1]) {
                first++;
            }
        }

        return result;
    }
}