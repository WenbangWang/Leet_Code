package com.wwb.leetcode.hard;

/**
 * There are two sorted arrays nums1 and nums2 of size m and n respectively.
 * Find the median of the two sorted arrays.
 * The overall run time complexity should be O(log (m+n)).
 */
public class No4 {

    // https://leetcode.com/problems/median-of-two-sorted-arrays/discuss/2471/Very-concise-O(log(min(MN)))-iterative-solution-with-detailed-explanation
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int longerLength = nums1.length;
        int shorterLength = nums2.length;

        if(longerLength < shorterLength) {
            return findMedianSortedArrays(nums2, nums1);
        }

        if(shorterLength == 0) {
            return ((double) nums1[(longerLength - 1) / 2] + (double) nums1[longerLength / 2]) / 2;
        }

        int start = 0;
        int end = shorterLength * 2;

        // If we have longerLeft > shorterRight, it means there are too many large numbers on the left half of longerArray,
        // then we must move longer cut to the left (i.e. move shorter cut to the right);
        // If shorterLeft > longerRight, then there are too many large numbers on the left half of shorterArray,
        // and we must move shorter cut to the left.
        // Otherwise, this cut is the right one.
        // After we find the cut, the medium can be computed as (max(longerLeft, shorterLeft) + min(longerRight, shorterRight)) / 2;
        while(start <= end) {
            int shorterMid = (end - start) / 2 + start;
            int longerMid = longerLength + shorterLength - shorterMid;

            double longerLeft = (longerMid == 0) ? Integer.MIN_VALUE : nums1[(longerMid - 1) / 2];
            double longerRight = (longerMid == longerLength * 2) ? Integer.MAX_VALUE : nums1[longerMid / 2];
            double shorterLeft = (shorterMid == 0) ? Integer.MIN_VALUE : nums2[(shorterMid - 1) / 2];
            double shorterRight = (shorterMid == shorterLength * 2) ? Integer.MAX_VALUE : nums2[shorterMid / 2];

            if(longerLeft > shorterRight) {
                start = shorterMid + 1;
            } else if(shorterLeft > longerRight) {
                end = shorterMid - 1;
            } else { // longerLeft <= shorterRight && shorterLeft <= longerRight
                return (Math.max(longerLeft, shorterLeft) + Math.min(longerRight, shorterRight)) / 2;
            }
        }

        return -1;
    }
}
