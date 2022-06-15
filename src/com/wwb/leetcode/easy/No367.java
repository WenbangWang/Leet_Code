package com.wwb.leetcode.easy;

/**
 * Given a positive integer num, write a function which returns True if num is a perfect square else False.
 *
 * Follow up: Do not use any built-in library function such as sqrt.
 *
 *
 *
 * Example 1:
 *
 * Input: num = 16
 * Output: true
 * Example 2:
 *
 * Input: num = 14
 * Output: false
 *
 *
 * Constraints:
 *
 * 1 <= num <= 2^31 - 1
 */
public class No367 {
    public boolean isPerfectSquare(int num) {
        if (num == 0 || num == 1) {
            return true;
        }

        int start = 0;
        int end = num;

        while (start <= end) {
            int mid = start + (end - start) / 2;
            int dividend = num / mid;

            if (dividend == mid) {
                return true;
            } else if (dividend > mid) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        return false;
    }
}
