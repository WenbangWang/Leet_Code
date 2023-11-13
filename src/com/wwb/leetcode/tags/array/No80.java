package com.wwb.leetcode.tags.array;

/**
 * Follow up for "Remove Duplicates":
 * What if duplicates are allowed at most twice?
 * <p>
 * For example,
 * Given sorted array nums = [1,1,1,2,2,3],
 * <p>
 * Your function should return length = 5, with the first five elements of nums being 1, 1, 2, 2 and 3.
 * It doesn't matter what you leave beyond the new length.
 */
public class No80 {

    public int removeDuplicates(int[] nums) {
        return solution1(nums);
    }

    private int solution1(int[] nums) {
        if (nums.length <= 2) {
            return nums.length;
        }
        int count = 1;
        final int duplicates = 2;
        int i = 1;
        int j = 1;

        while (i < nums.length) {
            if (nums[i] != nums[i - 1]) {
                count = 1;
                nums[j++] = nums[i];
            } else if (count < duplicates) {
                count++;
                nums[j++] = nums[i];
            }
            i++;
        }


        return j;
    }
}
