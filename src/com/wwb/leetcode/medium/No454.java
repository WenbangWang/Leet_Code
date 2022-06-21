package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/**
 * Given four integer arrays nums1, nums2, nums3, and nums4 all of length n, return the number of tuples (i, j, k, l) such that:
 * <p>
 * 0 <= i, j, k, l < n
 * nums1[i] + nums2[j] + nums3[k] + nums4[l] == 0
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: nums1 = [1,2], nums2 = [-2,-1], nums3 = [-1,2], nums4 = [0,2]
 * Output: 2
 * Explanation:
 * The two tuples are:
 * 1. (0, 0, 0, 1) -> nums1[0] + nums2[0] + nums3[0] + nums4[1] = 1 + (-2) + (-1) + 2 = 0
 * 2. (1, 1, 0, 0) -> nums1[1] + nums2[1] + nums3[0] + nums4[0] = 2 + (-1) + (-1) + 0 = 0
 * Example 2:
 * <p>
 * Input: nums1 = [0], nums2 = [0], nums3 = [0], nums4 = [0]
 * Output: 1
 * <p>
 * <p>
 * Constraints:
 * <p>
 * n == nums1.length
 * n == nums2.length
 * n == nums3.length
 * n == nums4.length
 * 1 <= n <= 200
 * -2^28 <= nums1[i], nums2[i], nums3[i], nums4[i] <= 2^28
 */
public class No454 {
    public int fourSumCount(int[] nums1, int[] nums2, int[] nums3, int[] nums4) {
        return kSumCount(new int[][]{nums1, nums2, nums3, nums4});
    }

    private int kSumCount(int[][] lists) {
        Map<Integer, Integer> map = new HashMap<>();
        addToHash(lists, map, 0, 0);
        return countComplements(lists, map, lists.length / 2, 0);
    }

    // populate mapping between sum and count for the first half of the lists
    private void addToHash(int[][] lists, Map<Integer, Integer> map, int index, int sum) {
        if (index == lists.length / 2) {
            map.put(sum, map.getOrDefault(sum, 0) + 1);
        } else {
            for (int a : lists[index]) {
                addToHash(lists, map, index + 1, sum + a);
            }
        }
    }

    // compute the complement in second half of the lists
    private int countComplements(int[][] lists, Map<Integer, Integer> m, int i, int complement) {
        if (i == lists.length) {
            return m.getOrDefault(complement, 0);
        }
        int count = 0;
        for (int num : lists[i]) {
            count += countComplements(lists, m, i + 1, complement - num);
        }
        return count;
    }
}
