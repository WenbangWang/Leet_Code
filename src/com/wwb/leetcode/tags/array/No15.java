package com.wwb.leetcode.tags.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Given an array S of n integers, are there elements a, b, c in S such that a + b + c = 0?
 * Find all unique triplets in the array which gives the sum of zero.
 *
 * Note:
 * Elements in a triplet (a,b,c) must be in non-descending order. (ie, a ≤ b ≤ c)
 * The solution set must not contain duplicate triplets.
 * For example, given array S = {-1 0 1 2 -1 -4},
 *
 * A solution set is:
 * (-1, 0, 1)
 * (-1, -1, 2)
 */
public class No15 {

    public List<List<Integer>> threeSum(int[] num) {
        List<List<Integer>> result = new ArrayList<>();

        if(num == null || num.length < 3) {
            return result;
        }

        Arrays.sort(num);

        for(int i = 0; i < num.length; i++) {
            int target = 0 - num[i];
            int start = i + 1;
            int end = num.length - 1;

            while(start < end) {
                int sum = num[start] + num[end];
                if(sum == target) {
                    ArrayList<Integer> currentResult = new ArrayList<>();

                    currentResult.add(num[i]);
                    currentResult.add(num[start]);
                    currentResult.add(num[end]);

                    result.add(currentResult);

                    start++;
                    end--;

                    while(start < end && num[start] == num[start - 1]) {
                        start++;
                    }
                    while(start < end && num[end] == num[end + 1]) {
                        end--;
                    }
                } else if(sum > target) {
                    end--;
                } else {
                    start++;
                }
            }

            while(i < num.length-1 && num[i] == num[i + 1]) {
                i++;
            }
        }

        return result;
    }
}
