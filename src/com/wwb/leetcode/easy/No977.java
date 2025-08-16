package com.wwb.leetcode.easy;

/**
 * Given an integer array nums sorted in non-decreasing order, return an array of the squares of each number sorted in non-decreasing order.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: nums = [-4,-1,0,3,10]
 * Output: [0,1,9,16,100]
 * Explanation: After squaring, the array becomes [16,1,0,9,100].
 * After sorting, it becomes [0,1,9,16,100].
 * Example 2:
 *
 * Input: nums = [-7,-3,2,3,11]
 * Output: [4,9,9,49,121]
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * 1 <= nums.length <= 10^4
 * -10^4 <= nums[i] <= 10^4
 * nums is sorted in non-decreasing order.
 * </pre>
 * <p>
 * Follow up: Squaring each element and sorting the new array is very trivial, could you find an O(n) solution using a different approach?
 */
public class No977 {
    public int[] sortedSquares(int[] nums) {
        int start = 0;
        int end = nums.length - 1;
        int index = nums.length - 1;
        int[] result = new int[nums.length];

        while (start < end && nums[start] <= 0 && nums[end] > 0) {
            int startNum = Math.abs(nums[start]);
            int endNum = nums[end];

            if (startNum > endNum) {
                result[index] = startNum * startNum;
                start++;
            } else {
                result[index] = endNum * endNum;
                end--;
            }

            index--;
        }

        while (start < nums.length && nums[start] <= 0) {
            int startNum = Math.abs(nums[start]);
            result[index] = startNum * startNum;
            index--;
            start++;
        }

        while (end >= 0 && nums[end] > 0) {
            result[index] = nums[end] * nums[end];
            index--;
            end--;
        }

        return result;
    }
}
