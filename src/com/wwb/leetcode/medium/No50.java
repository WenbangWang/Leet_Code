package com.wwb.leetcode.medium;

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
        }

        if(n > 0) {
            return x * result;
        }

        return result / x;
    }
}
