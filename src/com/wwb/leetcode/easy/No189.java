package com.wwb.leetcode.easy;

/**
 * Rotate an array of n elements to the right by k steps.
 *
 * For example, with n = 7 and k = 3, the array [1,2,3,4,5,6,7] is rotated to [5,6,7,1,2,3,4].
 */
public class No189 {

    public void rotate(int[] nums, int k) {
        // solution1(nums, k);
        solution2(nums, k);
    }

    private void solution2(int[] nums, int k) {
        k = k % nums.length;
        int[] newNums = new int[nums.length * 2];

        for(int i = 0; i < nums.length; i++) {
            newNums[i] = nums[i];
            newNums[i + nums.length] = nums[i];
        }

        for(int i = 0; i < nums.length; i++) {
            nums[i] = newNums[nums.length - k + i];
        }
    }

    private void solution1(int[] nums, int k) {
        int end = (nums.length + k) % nums.length;

        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, end - 1);
        reverse(nums, end, nums.length - 1);
    }

    private void reverse(int[] nums, int start, int end) {
        while(start < end) {
            swap(nums, start, end);
            start++;
            end--;
        }
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}