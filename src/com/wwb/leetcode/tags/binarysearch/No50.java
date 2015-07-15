package com.wwb.leetcode.tags.binarysearch;

/**
 * Implement pow(x, n).
 */
public class No50 {

    public double myPow(double x, int n) {
        if(n == 0) {
            return 1;
        }

        double temp = myPow(x, n / 2);
        double result = temp * temp;

        if(n % 2 == 0) {
            return result;
        } else {
            if(n > 0) {
                return x * result;
            } else {
                return result / x;
            }
        }
    }
}