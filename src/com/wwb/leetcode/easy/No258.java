package com.wwb.leetcode.easy;

/**
 * Given a non-negative integer num, repeatedly add all its digits until the result has only one digit.
 *
 * For example:
 *
 * Given num = 38, the process is like: 3 + 8 = 11, 1 + 1 = 2. Since 2 has only one digit, return it.
 *
 * Follow up:
 * Could you do it without any loop/recursion in O(1) runtime?
 */
public class No258 {

    public int addDigits(int num) {
//        return solution1(num);
        return solution2(num);
    }

    private int solution1(int num) {
        if(num < 10) {
            return num;
        }

        return solution1(num % 10 + solution1(num / 10));
    }

    private int solution2(int num) {
        return num == 0 ? 0 : (num % 9 == 0 ? 9 : num % 9);
    }
}