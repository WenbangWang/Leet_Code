package com.wwb.leetcode.medium;

/**
 * A peak element is an element that is greater than its neighbors.
 * <p>
 * Given an input array where num[i] ≠ num[i+1], find a peak element and return its index.
 * <p>
 * The array may contain multiple peaks, in that case return the index to any one of the peaks is fine.
 * <p>
 * You may imagine that num[-1] = num[n] = -∞.
 * <p>
 * For example, in array [1, 2, 3, 1], 3 is a peak element and your function should return the index number 2.
 * <p>
 * Note:
 * Your solution should be in logarithmic complexity.
 */
public class No162 {

    public int findPeakElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        return binarySearch(nums, 0, nums.length - 1);
    }

    private int binarySearch(int[] nums, int start, int end) {
        if (start == end) {
            return start;
        }

        if (start + 1 == end) {
            if (nums[start] > nums[end]) {
                return start;
            }
            return end;
        }

        int mid = (end - start) / 2 + start;
        int pre = mid - 1;
        int next = mid + 1;

        // mid is the peak
        if (nums[mid] > nums[pre] && nums[mid] > nums[next]) {
            return mid;
        }

        // peak on the left
        if (nums[mid] < nums[pre] && nums[mid] > nums[next]) {
            return binarySearch(nums, start, pre);
        }

        // peak on the right
        return binarySearch(nums, next, end);
    }
}
