package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Given an array S of n integers, are there elements a, b, c, and d in S such that a + b + c + d = target?
 * Find all unique quadruplets in the array which gives the sum of target.
 * <p>
 * Note:
 * Elements in a quadruplet (a,b,c,d) must be in non-descending order. (ie, a ≤ b ≤ c ≤ d)
 * The solution set must not contain duplicate quadruplets.
 * For example, given array S = {1 0 -1 0 -2 2}, and target = 0.
 * <p>
 * A solution set is:
 * (-1,  0, 0, 1)
 * (-2, -1, 1, 2)
 * (-2,  0, 0, 2)
 */
public class No18 {

    public List<List<Integer>> fourSum(int[] nums, int target) {
        Arrays.sort(nums);

        return solution1(nums, target);
    }

    private List<List<Integer>> solution1(int[] nums, int target) {
        List<List<Integer>> result = new ArrayList<>();

        if (nums == null || nums.length < 4) {
            return result;
        }

        int length = nums.length;
        Arrays.sort(nums);

        for (int first = 0; first < length; first++) {

            for (int second = first + 1; second < length; second++) {
                int sumOfFirstAndSecond = nums[first] + nums[second];
                int third = second + 1;
                int forth = length - 1;

                while (third < forth) {
                    int sumOfThirdAndForth = nums[third] + nums[forth];
                    int sum = sumOfFirstAndSecond + sumOfThirdAndForth;
                    if (sum == target) {
                        result.add(Arrays.asList(nums[first], nums[second], nums[third], nums[forth]));

                        third++;
                        forth--;

                        while (third < forth && nums[third] == nums[third - 1]) {
                            third++;
                        }
                        while (third < forth && nums[forth] == nums[forth + 1]) {
                            forth--;
                        }
                    } else if (sum > target) {
                        do {
                            forth--;
                        } while (third < forth && nums[forth] == nums[forth + 1]);
                    } else {
                        do {
                            third++;
                        } while (third < forth && nums[third] == nums[third - 1]);
                    }
                }

                while (second < length - 1 && nums[second] == nums[second + 1]) {
                    second++;
                }
            }

            while (first < length - 1 && nums[first] == nums[first + 1]) {
                first++;
            }
        }

        return result;
    }

    private List<List<Integer>> kSum(int[] nums, int target, int start, int k) {
        List<List<Integer>> result = new ArrayList<>();

        // If we have run out of numbers to add, return result.
        if (start == nums.length) {
            return result;
        }

        // There are k remaining values to add to the sum. The
        // average of these values is at least target / k.
        int averageValue = target / k;

        // We cannot obtain a sum of target if the smallest value
        // in nums is greater than target / k or if the largest
        // value in nums is smaller than target / k.
        if  (nums[start] > averageValue || averageValue > nums[nums.length - 1]) {
            return result;
        }

        if (k == 2) {
            return twoSum(nums, target, start);
        }

        for (int i = start; i < nums.length; i++) {
            if (i == start || nums[i - 1] != nums[i]) {
                for (List<Integer> subset : kSum(nums, target - nums[i], i + 1, k - 1)) {
                    var current = new ArrayList<>(subset);
                    current.add(nums[i]);

                    result.add(current);
                }
            }
        }

        return result;
    }

    public List<List<Integer>> twoSum(int[] nums, int target, int start) {
        List<List<Integer>> res = new ArrayList<>();
        int low = start;
        int high = nums.length - 1;

        while (low < high) {
            int sum = nums[low] + nums[high];

            if (sum < target || (low > start && nums[low] == nums[low - 1])) {
                ++low;
            } else if (sum > target || (high < nums.length - 1 && nums[high] == nums[high + 1])) {
                --high;
            } else {
                res.add(Arrays.asList(nums[low++], nums[high--]));
            }
        }

        return res;
    }
}
