package com.wwb.leetcode.tags.binarysearch;

/**
 * Given an array nums containing n + 1 integers where each integer is between 1 and n (inclusive),
 * prove that at least one duplicate number must exist. Assume that there is only one duplicate number,
 * find the duplicate one.
 *
 * Note:
 * You must not modify the array (assume the array is read only).
 * You must use only constant, O(1) extra space.
 * Your runtime complexity should be less than O(n2).
 * There is only one duplicate number in the array, but it could be repeated more than once.
 */
public class No287 {

    public int findDuplicate(int[] nums) {
        return solution2(nums);
    }

    private int solution1(int[] nums) {
        if(nums == null || nums.length <= 1) {
            return -1;
        }

        int slow = nums[0];
        int fast = nums[nums[0]];

        while(slow != fast) {
            slow = nums[slow];
            fast = nums[nums[fast]];
        }

        fast = 0;

        while(slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }

        return slow;
    }

    private int solution2(int[] nums) {
        if(nums == null || nums.length <= 1) {
            return -1;
        }

        int length = nums.length;
        int left = 0;
        int right = length - 1;

        while(left < right) {
            int mid = (right - left) / 2 + left;
            int numberBelow = numberBelow(nums, mid);

            if(numberBelow > mid) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    private int numberBelow(int[] nums, int target) {
        int result = 0;

        for(int num : nums) {
            if(num <= target) {
                result++;
            }
        }

        return result;
    }
}
