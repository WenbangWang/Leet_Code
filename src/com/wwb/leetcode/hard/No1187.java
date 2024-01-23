package com.wwb.leetcode.hard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Given two integer arrays arr1 and arr2, return the minimum number of operations (possibly zero)
 * needed to make arr1 strictly increasing.
 *
 * In one operation, you can choose two indices 0 <= i < arr1.length and 0 <= j < arr2.length
 * and do the assignment arr1[i] = arr2[j].
 *
 * If there is no way to make arr1 strictly increasing, return -1.
 *
 *
 *
 * Example 1:
 *
 * Input: arr1 = [1,5,3,6,7], arr2 = [1,3,2,4]
 * Output: 1
 * Explanation: Replace 5 with 2, then arr1 = [1, 2, 3, 6, 7].
 * Example 2:
 *
 * Input: arr1 = [1,5,3,6,7], arr2 = [4,3,1]
 * Output: 2
 * Explanation: Replace 5 with 3 and then replace 3 with 4. arr1 = [1, 3, 4, 6, 7].
 * Example 3:
 *
 * Input: arr1 = [1,5,3,6,7], arr2 = [1,6,3,3]
 * Output: -1
 * Explanation: You can't make arr1 strictly increasing.
 *
 *
 * Constraints:
 *
 * 1 <= arr1.length, arr2.length <= 2000
 * 0 <= arr1[i], arr2[i] <= 10^9
 */
public class No1187 {
    public int makeArrayIncreasing(int[] arr1, int[] arr2) {
        TreeSet<Integer> set = Arrays.stream(arr2).boxed().collect(Collectors.toCollection(TreeSet::new));
        Map<Integer, Integer> dp = new HashMap<>();

        dp.put(-1, 0);

        for (int num : arr1) {
            Map<Integer, Integer> temp = new HashMap<>();

            // There are two possible cases when compare num with previously seen values:
            // 1. num > value
            // 2. num <= value
            // if the current num if bigger than the previous placed possible values,
            // we have two options:
            // 1) not do anything
            // 2) replaced it with a number from arr2 which is slightly bigger than the possible value in the map.
            // For possibility 2, it should be the same as 2).
            for (var entry : dp.entrySet()) {
                if (num > entry.getKey()) {
                    temp.put(num, Math.min(entry.getValue(), temp.getOrDefault(num, Integer.MAX_VALUE)));
                }

                // ceiling returns greater than or "equal to" but we don't want the same number as entry.getKey()
                // hence do the "+1"
                Integer replace = set.ceiling(entry.getKey() + 1);

                if (replace != null) {
                    temp.put(replace, Math.min(entry.getValue() + 1,temp.getOrDefault(replace, Integer.MAX_VALUE)));
                }
            }

            if (temp.isEmpty()) {
                return -1;
            }

            dp = temp;
        }

        return dp.values().stream().min(Integer::compare).get();
    }
}
