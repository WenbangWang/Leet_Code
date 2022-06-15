package com.wwb.leetcode.medium;

import java.util.Stack;
import java.util.function.BiPredicate;

/**
 * You are given an integer array nums. The range of a subarray of nums is the difference between the largest and smallest element in the subarray.
 *
 * Return the sum of all subarray ranges of nums.
 *
 * A subarray is a contiguous non-empty sequence of elements within an array.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [1,2,3]
 * Output: 4
 * Explanation: The 6 subarrays of nums are the following:
 * [1], range = largest - smallest = 1 - 1 = 0
 * [2], range = 2 - 2 = 0
 * [3], range = 3 - 3 = 0
 * [1,2], range = 2 - 1 = 1
 * [2,3], range = 3 - 2 = 1
 * [1,2,3], range = 3 - 1 = 2
 * So the sum of all ranges is 0 + 0 + 0 + 1 + 1 + 2 = 4.
 * Example 2:
 *
 * Input: nums = [1,3,3]
 * Output: 4
 * Explanation: The 6 subarrays of nums are the following:
 * [1], range = largest - smallest = 1 - 1 = 0
 * [3], range = 3 - 3 = 0
 * [3], range = 3 - 3 = 0
 * [1,3], range = 3 - 1 = 2
 * [3,3], range = 3 - 3 = 0
 * [1,3,3], range = 3 - 1 = 2
 * So the sum of all ranges is 0 + 0 + 0 + 2 + 0 + 2 = 4.
 * Example 3:
 *
 * Input: nums = [4,-2,-3,4,1]
 * Output: 59
 * Explanation: The sum of all subarray ranges of nums is 59.
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 1000
 * -109 <= nums[i] <= 109
 */
public class No2104 {
    // See No907 for reference.
    // This can be formulated as a similar problem as No907.
    // In No907, we calculate sum(min(E)),
    // for this problem, we just need to calculate sum(max(E))
    // and sum(max(E)) - sum(min(E)) is the result.
    public long subArrayRanges(int[] nums) {
        return subArrayRanges(nums, (a, b) -> a < b) - subArrayRanges(nums, (a, b) -> a > b);
    }

    private long subArrayRanges(int[] nums, BiPredicate<Integer,Integer> comparator) {
        Stack<Integer> stack = new Stack<>();
        long result = 0;

        for (int nextLess = 0; nextLess <= nums.length; nextLess++) {
            while(!stack.isEmpty() && (nextLess == nums.length || comparator.test(nums[stack.peek()], nums[nextLess]))) {
                int currentMinimum = stack.pop();
                // since the this is a mono-stack, the next peek element in the stack
                // is the previous element which is just less than the current element.
                int previousLess = stack.isEmpty() ? -1 : stack.peek();
                result += (long) nums[currentMinimum] * (currentMinimum - previousLess) * (nextLess - currentMinimum);
            }

            stack.push(nextLess);
        }

        return result;
    }
}
