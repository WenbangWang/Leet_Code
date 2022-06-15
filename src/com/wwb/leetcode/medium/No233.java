package com.wwb.leetcode.medium;

/**
 * Given an integer n, count the total number of digit 1 appearing in all non-negative integers less than or equal to n.
 *
 * For example:
 * Given n = 13,
 * Return 6, because digit 1 occurred in the following numbers: 1, 10, 11, 12, 13.
 */
public class No233 {

    //traverse each digit of n, if n=3401512
    //for m=100, split n into a=34015 and b=12
    //if a%10==1, #1=a/10*m+(b+1);
    //if a%10==0, #2=a/10*m;
    //if a%10>1; #3=a/10*m+m;
    //In general, #4=(a+8)/10*m+(a % 10 == 1)*(b + 1).
    //For general expression above:
    //if a%10>1, then (a+8)/10=a/10+1, #4=#3
    //if a%10==0, (a+8)/10=a/10, (a % 10 == 1)=false, #4=#2
    //if a%10==1, (a+8)/10=a/10, (a % 10 == 1)=true, #4=#1
    //This is how "(a+8)/10*m+(a % 10 == 1)*(b + 1)" covers all three conditions.
    public int countDigitOne(int n) {
        int count = 0;

        for(long multiplier = 1; multiplier <= n; multiplier *= 10) {
            long mod = n % multiplier;
            long quotient = n / multiplier;
            count += (quotient + 8) / 10 * multiplier + (quotient % 10 == 1 ? mod + 1 : 0);
        }

        return count;
    }
}
