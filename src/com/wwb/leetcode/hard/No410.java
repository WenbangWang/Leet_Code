package com.wwb.leetcode.hard;

/**
 * Given an array which consists of non-negative integers and an integer m, you can split the array into m non-empty continuous subarrays.
 * Write an algorithm to minimize the largest sum among these m subarrays.
 *
 * Note:
 * If n is the length of array, assume the following constraints are satisfied:
 *
 * 1 ≤ n ≤ 1000
 * 1 ≤ m ≤ min(50, n)
 * Examples:
 *
 * Input:
 * nums = [7,2,5,10,8]
 * m = 2
 *
 * Output:
 * 18
 *
 * Explanation:
 * There are four ways to split nums into two subarrays.
 * The best way is to split it into [7,2,5] and [10,8],
 * where the largest sum among the two subarrays is only 18.
 */
public class No410 {
    public int splitArray(int[] nums, int m) {
        long sum = 0;
        int max = 0;
        for(int num : nums) {
            sum += num;
            max = Math.max(max, num);
        }

        if(m == 1) {
            return (int) sum;
        }

        long left = max;
        long right = sum;

        while(left <= right) {
            long mid = left + (right - left) / 2;

            if(binarySearch(mid, nums, m)) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return (int) left;
    }

    private boolean binarySearch(long target, int[] nums, int m) {
        int count = 1;
        long total = 0;

        for(int num : nums) {
            total += num;

            if(total > target) {
                total = num;
                count++;

                if(count > m) {
                    return false;
                }
            }
        }

        return true;
    }
}
