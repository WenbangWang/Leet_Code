package com.wwb.leetcode.hard;

/**
 * There are two sorted arrays nums1 and nums2 of size m and n respectively.
 * Find the median of the two sorted arrays.
 * The overall run time complexity should be O(log (m+n)).
 */
public class No4 {

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int length1 = nums1.length;
        int length2 = nums2.length;

        if(length1 < length2) {
            return findMedianSortedArrays(nums2, nums1);
        }

        if(length2 == 0) {
            return ((double) nums1[(length1 - 1) / 2] + (double) nums1[length1 / 2]) / 2;
        }

        int start = 0;
        int end = length2 * 2;

        while(start <= end) {
            int mid2 = (end - start) / 2 + start;
            int mid1 = length1 + length2 - mid2;

            double left1 = (mid1 == 0) ? Integer.MIN_VALUE : nums1[(mid1 - 1) / 2];
            double right1 = (mid1 == length1 * 2) ? Integer.MAX_VALUE : nums1[mid1 / 2];
            double left2 = (mid2 == 0) ? Integer.MIN_VALUE : nums2[(mid2 - 1) / 2];
            double right2 = (mid2 == length2 * 2) ? Integer.MAX_VALUE : nums2[mid2 / 2];

            if(left1 > right2) {
                start = mid2 + 1;
            } else if(left2 > right1) {
                end = mid2 - 1;
            } else {
                return (Math.max(left1, left2) + Math.min(right1, right2)) / 2;
            }
        }

        return -1;
    }
}
