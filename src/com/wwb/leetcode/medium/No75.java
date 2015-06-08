package com.wwb.leetcode.medium;

/**
 * Given an array with n objects colored red, white or blue,
 * sort them so that objects of the same color are adjacent, with the colors in the order red, white and blue.
 *
 * Here, we will use the integers 0, 1, and 2 to represent the color red, white, and blue respectively.
 *
 * Note:
 * You are not suppose to use the library's sort function for this problem.
 *
 * Follow up:
 * A rather straight forward solution is a two-pass algorithm using counting sort.
 * First, iterate the array counting number of 0's, 1's, and 2's,
 * then overwrite array with total number of 0's, then 1's and followed by 2's.
 *
 * Could you come up with an one-pass algorithm using only constant space?
 */
public class No75 {

    public void sortColors(int[] nums) {
        // solution1(nums);
        solution2(nums);
    }

    private void solution1(int[] nums) {
        int pointer = 0;
        int runner = 0;
        int checker = 0;

        while(pointer < nums.length && checker <= 2) {
            for(int i = runner; i < nums.length; i++) {
                if(nums[i] == checker) {
                    swap(nums, i, pointer++);
                }
            }
            checker++;
            runner = pointer;
        }
    }

    private void solution2(int[] nums) {
        int j = 0;
        int n = nums.length - 1;

        for(int i = 0; i <= n; i++) {
            if(nums[i] == 0) {
                swap(nums, i, j++);
            } else if(nums[i] == 2) {
                swap(nums, i--, n--);
            }
        }
    }

    private void swap(int[] nums, int x, int y) {
        int temp = nums[x];
        nums[x] = nums[y];
        nums[y] = temp;
    }
}