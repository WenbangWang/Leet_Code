package com.wwb.leetcode.hard;

/**
 * Given two sorted 0-indexed integer arrays nums1 and nums2 as well as an integer k,
 * return the kth (1-based) smallest product of nums1[i] * nums2[j] where 0 <= i < nums1.length and 0 <= j < nums2.length.
 *
 *
 * Example 1:
 *
 * Input: nums1 = [2,5], nums2 = [3,4], k = 2
 * Output: 8
 * Explanation: The 2 smallest products are:
 * - nums1[0] * nums2[0] = 2 * 3 = 6
 * - nums1[0] * nums2[1] = 2 * 4 = 8
 * The 2nd smallest product is 8.
 * Example 2:
 *
 * Input: nums1 = [-4,-2,0,3], nums2 = [2,4], k = 6
 * Output: 0
 * Explanation: The 6 smallest products are:
 * - nums1[0] * nums2[1] = (-4) * 4 = -16
 * - nums1[0] * nums2[0] = (-4) * 2 = -8
 * - nums1[1] * nums2[1] = (-2) * 4 = -8
 * - nums1[1] * nums2[0] = (-2) * 2 = -4
 * - nums1[2] * nums2[0] = 0 * 2 = 0
 * - nums1[2] * nums2[1] = 0 * 4 = 0
 * The 6th smallest product is 0.
 * Example 3:
 *
 * Input: nums1 = [-2,-1,0,1,2], nums2 = [-3,-1,2,4,5], k = 3
 * Output: -6
 * Explanation: The 3 smallest products are:
 * - nums1[0] * nums2[4] = (-2) * 5 = -10
 * - nums1[0] * nums2[3] = (-2) * 4 = -8
 * - nums1[4] * nums2[0] = 2 * (-3) = -6
 * The 3rd smallest product is -6.
 *
 *
 * Constraints:
 *
 * 1 <= nums1.length, nums2.length <= 5 * 10^4
 * -10^5 <= nums1[i], nums2[j] <= 10^5
 * 1 <= k <= nums1.length * nums2.length
 * nums1 and nums2 are sorted.
 */
public class No2040 {
    private static final long INF = (long) StrictMath.pow(10, 10);

    public long kthSmallestProduct(int[] nums1, int[] nums2, long k) {
        long start = -INF - 1;
        long end = INF + 1;

        while (start <= end) {
            long mid = start + (end - start) / 2;

            if (countNumbersSmallerThanOrEqualTo(mid, nums1, nums2) >= k) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }

        return start;
    }

    private long countNumbersSmallerThanOrEqualTo(long target, int[] nums1, int[] nums2) {
        if (nums1.length > nums2.length) {
            return countNumbersSmallerThanOrEqualTo(target, nums2, nums1);
        }

        long count = 0;

        for (int num : nums1) {
            if (num > 0) {
                // when num is positive, the product is in ascending order
                count += binarySearch(nums2, num, target);
            } else if (num < 0) {
                // when num is negative, the product is in descending order
                // hence we use the right part of the array to indicate
                // the count of numbers smaller than or equal to target
                count += nums2.length - binarySearch(nums2, num, target);
            } else if (target >= 0) {
                // when num is ZERO and current target is non-negative
                // all products from ZERO are ZEROs hence they should
                // be all smaller than or equal to a non-negative number
                count += nums2.length;
            }
        }

        return count;
    }

    // num has to be non-ZERO and nums has to be sorted.
    // when num is positive  (ascending)
    // - find the right-most index where num * nums[i] is just smaller than or equal to target
    // when num is negative (descending)
    // - find the left-most index where num * nums[i] is just bigger than target
    private int binarySearch(int[] nums, int num, long target) {
        int start = 0;
        int end = nums.length - 1;

        while (start <= end) {
            int mid = start + (end - start) / 2;
            long product = (long) num * nums[mid];

            if ((num > 0) == (product > target)) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }

        return start;
    }
}
