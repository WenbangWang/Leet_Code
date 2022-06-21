package com.wwb.leetcode.easy;

/**
 * Given an integer, write a function to determine if it is a power of three.
 * <p>
 * Follow up:
 * Could you do it without using any loop / recursion?
 */
public class No326 {

    public boolean isPowerOfThree(int n) {
        return n >= 0 && Integer.toString(n, 3).matches("^10*$");
    }
}
