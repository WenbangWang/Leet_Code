package com.wwb.leetcode.medium;

/**
 * Implement int sqrt(int x).
 *
 * Compute and return the square root of x.
 */
public class No69 {

    public int mySqrt(int x) {
        if(x == 0 || x == 1) {
            return x;
        }

        int left = 1;
        int right = x;

        while(left <= right) {
            int mid = left + (right - left) / 2;
            int dividend = x / mid;

            if(dividend == mid) {
                return mid;
            }

            if(dividend < mid) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return right;
    }
}
