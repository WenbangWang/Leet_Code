package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * Given an integer array nums of size n, return the minimum number of moves required to make all array elements equal.
 *
 * In one move, you can increment n - 1 elements of the array by 1.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [1,2,3]
 * Output: 3
 * Explanation: Only three moves are needed (remember each move increments two elements):
 * [1,2,3]  =>  [2,3,3]  =>  [3,4,3]  =>  [4,4,4]
 * Example 2:
 *
 * Input: nums = [1,1,1]
 * Output: 0
 *
 *
 * Constraints:
 *
 * n == nums.length
 * 1 <= nums.length <= 10^5
 * -10^9 <= nums[i] <= 10^9
 * The answer is guaranteed to fit in a 32-bit integer.
 */
public class No453 {
    public int minMoves(int[] nums) {
        // Assume we need M moves to make all nums equal to X, there's N numbers in total.
        // We can have sum + M * (N - 1) = X * N => M = (X * N - sum) / (N - 1).
        // Since every time we increment, we would always increase the least number,
        // hence X = MIN(nums) + M.
        // Then the equation could be sum + M * (N - 1) = (MIN(nums) + M) * N
        // => sum - MIN(nums) * N = M

        int min = Arrays.stream(nums).min().getAsInt();
        int sum = Arrays.stream(nums).sum();
        return sum - min * nums.length;
    }

    private static int dumbSolution(int[] nums) {
        Arrays.sort(nums);
        int result = 0;
        int max = nums[nums.length - 1];

        // We start from the tail to make all the numbers visited to be the same.
        // We use formula stated below to calculate how many moves to make the numbers we visited the same.
        // Then we update the number of moves we already take and the current max so they can be used in the next iteration.
        for (int i = nums.length - 2; i >= 0; i--) {
            if (nums[i] == nums[i + 1]) {
                continue;
            }

            int currentNumAfterMove = nums[i] + result;
            int countOfEqualNumbers = nums.length - 1 - i;
            int movesToMakeRestSame = (max - currentNumAfterMove) * countOfEqualNumbers;

            result += movesToMakeRestSame;
            max = currentNumAfterMove + movesToMakeRestSame;
        }

        return result;
    }
}
