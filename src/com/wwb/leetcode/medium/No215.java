package com.wwb.leetcode.medium;

/**
 * Find the kth largest element in an unsorted array.
 * Note that it is the kth largest element in the sorted order, not the kth distinct element.
 *
 * For example,
 * Given [3,2,1,5,6,4] and k = 2, return 5.
 *
 * Note:
 * You may assume k is always valid, 1 ≤ k ≤ array's length.
 */
public class No215 {

    public int findKthLargest(int[] nums, int k) {
        int start = 0;
        int end = nums.length - 1;
        int targetIndex = nums.length - k;

        while(start < end) {
            int pivot = partition(nums, start, end);

            if(pivot < targetIndex) {
                start = pivot + 1;
            } else if(pivot > targetIndex) {
                end = pivot - 1;
            } else {
                return nums[pivot];
            }
        }

        return nums[start];
    }

    private int partition(int[] nums, int start, int end) {
        int pivot = start;

        while(start <= end) {
            while(start <= end && nums[start] <= nums[pivot]) {
                start++;
            }
            while(start <= end && nums[end] > nums[pivot]) {
                end--;
            }

            if(start > end) {
                break;
            }
            swap(nums, start, end);
        }

        swap(nums, pivot, end);

        return end;
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}