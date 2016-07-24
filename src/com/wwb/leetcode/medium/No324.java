package com.wwb.leetcode.medium;

/**
 * Given an unsorted array nums, reorder it such that nums[0] < nums[1] > nums[2] < nums[3]....
 * <p>
 * Example:
 * (1) Given nums = [1, 5, 1, 1, 6, 4], one possible answer is [1, 4, 1, 5, 1, 6].
 * (2) Given nums = [1, 3, 2, 2, 3, 1], one possible answer is [2, 3, 1, 3, 1, 2].
 * <p>
 * Note:
 * You may assume all input has valid answer.
 * <p>
 * Follow Up:
 * Can you do it in O(n) time and/or in-place with O(1) extra space?
 */
public class No324 {

    public void wiggleSort(int[] nums) {
        int median = nums[findMedianIndex(nums)];
        int length = nums.length;
        int start = 0;
        int pointer = 0;
        int end = length - 1;

        while (pointer <= end) {
            if (nums[remappedIndex(length, pointer)] > median) {
                swap(nums, remappedIndex(length, start++), remappedIndex(length, pointer++));
            } else if (nums[remappedIndex(length, pointer)] < median) {
                swap(nums, remappedIndex(length, pointer), remappedIndex(length, end--));
            } else {
                pointer++;
            }
        }
    }

    private int remappedIndex(int n, int i) {
        return (2 * i + 1) % (n | 1);
    }

    private int findMedianIndex(int[] nums) {
        int start = 0;
        int end = nums.length - 1;
        int targetIndex = nums.length / 2;

        while (start < end) {
            int pivot = partition(nums, start, end);

            if (pivot < targetIndex) {
                start = pivot + 1;
            } else if (pivot > targetIndex) {
                end = pivot - 1;
            } else {
                return pivot;
            }
        }

        return start;
    }

    private int partition(int[] nums, int start, int end) {
        int pivot = start;

        while (start <= end) {
            while (start <= end && nums[start] <= nums[pivot]) {
                start++;
            }

            while (start <= end && nums[end] > nums[pivot]) {
                end--;
            }

            if (start > end) {
                break;
            }

            swap(nums, start, end);
        }

        swap(nums, pivot, end);

        return end;
    }

    private void swap(int[] nums, int i, int j) {
        if (i == j) {
            return;
        }

        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }
}
