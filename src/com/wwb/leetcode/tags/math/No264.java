package com.wwb.leetcode.tags.math;

/**
 * Write a program to find the n-th ugly number.
 *
 * Ugly numbers are positive numbers whose prime factors only include 2, 3, 5.
 * For example, 1, 2, 3, 4, 5, 6, 8, 9, 10, 12 is the sequence of the first 10 ugly numbers.
 *
 * Note that 1 is typically treated as an ugly number.
 */
public class No264 {

    public int nthUglyNumber(int n) {
        int[] uglyNumbers = new int[n];
        int indexOfTwo = 0;
        int indexOfThree = 0;
        int indexOfFive = 0;
        int factorOfTwo = 2;
        int factorOfThree = 3;
        int factorOfFive = 5;

        uglyNumbers[0] = 1;

        for(int i = 1; i < n; i++) {
            int minFactor = Math.min(Math.min(factorOfTwo, factorOfThree), factorOfFive);
            uglyNumbers[i] = minFactor;

            if(minFactor == factorOfTwo) {
                factorOfTwo = 2 * uglyNumbers[++indexOfTwo];
            }
            if(minFactor == factorOfThree) {
                factorOfThree = 3 * uglyNumbers[++indexOfThree];
            }
            if(minFactor == factorOfFive) {
                factorOfFive = 5 * uglyNumbers[++indexOfFive];
            }
        }

        return uglyNumbers[n - 1];
    }
}