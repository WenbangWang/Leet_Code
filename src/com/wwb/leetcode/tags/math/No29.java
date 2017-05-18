package com.wwb.leetcode.tags.math;

/**
 * Divide two integers without using multiplication, division and mod operator.
 * If it is overflow, return MAX_INT.
 */
public class No29 {
    public int divide(int dividend, int divisor) {
        if (divisor == 0 || dividend == Integer.MIN_VALUE && divisor == -1) {
            return Integer.MAX_VALUE;
        }

        if (divisor == 1) {
            return dividend;
        }

        int sign = (dividend < 0) ^ (divisor < 0) ? -1 : 1;
        long longDividend = Math.abs((long) dividend);
        long longDivisor = Math.abs((long) divisor);
        int result = 0;

        while (longDividend >= longDivisor) {
            long temp = longDivisor;
            long multiple = 1;

            while (longDividend >= (temp << 1)) {
                temp <<= 1;
                multiple <<=1;
            }

            longDividend -= temp;
            result += multiple;
        }

        return sign == 1 ? result : -result;
    }
}
