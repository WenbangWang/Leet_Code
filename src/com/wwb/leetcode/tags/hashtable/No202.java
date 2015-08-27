package com.wwb.leetcode.tags.hashtable;

import java.util.HashSet;
import java.util.Set;

/**
 * Write an algorithm to determine if a number is "happy".
 *
 * A happy number is a number defined by the following process:
 * Starting with any positive integer, replace the number by the sum of the squares of its digits,
 * and repeat the process until the number equals 1 (where it will stay),
 * or it loops endlessly in a cycle which does not include 1.
 * Those numbers for which this process ends in 1 are happy numbers.
 *
 */
public class No202 {

    public boolean isHappy(int n) {
        return solution1(n);
    }

    private boolean solution1(int n) {
        int x = n;
        int y = n;

        while(x > 1) {
            x = calculate(x);

            if(x == 1) {
                return true;
            }

            y = calculate(calculate(y));

            if(y == 1) {
                return true;
            }

            if(x == y) {
                return false;
            }
        }

        return true;
    }

    private boolean solution2(int n) {
        Set<Integer> set = new HashSet<>();
        set.add(n);

        while(n != 1) {
            n = calculate(n);

            if(set.contains(n)) {
                return false;
            }

            set.add(n);
        }

        return true;
    }

    private int calculate(int n) {
        int result = 0;

        while(n != 0) {
            result += Math.pow(n % 10, 2);
            n /= 10;
        }

        return result;
    }
}