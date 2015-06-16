package com.wwb.leetcode.easy;

/**
 * Determine whether an integer is a palindrome. Do this without extra space.
 */
public class No9 {

    public boolean isPalindrome(int x) {
        if(x < 0 || x != 0 && x % 10 == 0) {
            return false;
        }

        int reverse = 0;

        while(reverse < x) {
            reverse = reverse * 10 + x % 10;

            x /= 10;
        }

        return (x == reverse || reverse / 10 == x);
    }
}