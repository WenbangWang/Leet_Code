package com.wwb.leetcode.medium;

import java.util.*;

/**
 * You are given two integer arrays nums1 and nums2 sorted in ascending order and an integer k.
 *
 * Define a pair (u, v) which consists of one element from the first array and one element from the second array.
 *
 * Return the k pairs (u1, v1), (u2, v2), ..., (uk, vk) with the smallest sums.
 *
 *
 *
 * Example 1:
 *
 * Input: nums1 = [1,7,11], nums2 = [2,4,6], k = 3
 * Output: [[1,2],[1,4],[1,6]]
 * Explanation: The first 3 pairs are returned from the sequence: [1,2],[1,4],[1,6],[7,2],[7,4],[11,2],[7,6],[11,4],[11,6]
 * Example 2:
 *
 * Input: nums1 = [1,1,2], nums2 = [1,2,3], k = 2
 * Output: [[1,1],[1,1]]
 * Explanation: The first 2 pairs are returned from the sequence: [1,1],[1,1],[1,2],[2,1],[1,2],[2,2],[1,3],[1,3],[2,3]
 * Example 3:
 *
 * Input: nums1 = [1,2], nums2 = [3], k = 3
 * Output: [[1,3],[2,3]]
 * Explanation: All possible pairs are returned from the sequence: [1,3],[2,3]
 *
 *
 * Constraints:
 *
 * 1 <= nums1.length, nums2.length <= 10^5
 * -10^9 <= nums1[i], nums2[i] <= 10^9
 * nums1 and nums2 both are sorted in ascending order.
 * 1 <= k <= 10^4
 */
public class No373 {
    public List<List<Integer>> kSmallestPairs(int[] nums1, int[] nums2, int k) {
        return solution1(nums1, nums2, k);
    }

    // TLE
    private List<List<Integer>> solution1(int[] nums1, int[] nums2, int k) {
        int[] num1VisitedNum2 = new int[nums1.length];
        List<List<Integer>> result = new ArrayList<>();

        Arrays.fill(num1VisitedNum2, -1);

        for (int i = 0; i < nums1.length; i++) {
            while(num1VisitedNum2[i] < nums2.length - 1) {
                int lastMin = nums1[i] + nums2[num1VisitedNum2[i] + 1];
                int nextMinIndex = i;

                for (int j = i + 1; j < nums1.length; j++) {
                    if (num1VisitedNum2[j] == nums2.length - 1) {
                        continue;
                    }

                    int currentInnerPair = nums1[j] + nums2[num1VisitedNum2[j] + 1];

                    if (currentInnerPair < lastMin) {
                        nextMinIndex = j;
                        lastMin = currentInnerPair;
                    }
                }

                num1VisitedNum2[nextMinIndex]++;
                result.add(Arrays.asList(nums1[nextMinIndex], nums2[num1VisitedNum2[nextMinIndex]]));

                if (result.size() == k) {
                    return result;
                }
            }
        }

        return result;
    }

    private List<List<Integer>> solution2(int[] nums1, int[] nums2, int k) {
        if (nums1.length == 0 || nums2.length == 0 || k == 0) {
            return Collections.emptyList();
        }

        int[] num1VisitedNum2 = new int[nums1.length];
        List<List<Integer>> result = new ArrayList<>();
        PriorityQueue<int[]> heap = new PriorityQueue<>((a, b) -> nums1[a[0]] + nums2[a[1]] - nums1[b[0]] - nums2[b[1]]);

        for (int i = 0; i < nums1.length && i < k; i++) {
            heap.offer(new int[]{i, 0});
        }

        while (k-- > 0 && !heap.isEmpty()) {
            var pair = heap.poll();

            result.add(Arrays.asList(nums1[pair[0]], nums2[pair[1]]));

            if (num1VisitedNum2[pair[0]] == nums2.length - 1) {
                continue;
            }

            heap.offer(new int[]{pair[0], ++num1VisitedNum2[pair[0]]});
        }

        return result;
    }
}
