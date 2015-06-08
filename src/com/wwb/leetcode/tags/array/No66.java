package com.wwb.leetcode.tags.array;

/**
 * Given a non-negative number represented as an array of digits, plus one to the number.
 *
 * The digits are stored such that the most significant digit is at the head of the list.
 */
public class No66 {

    public int[] plusOne(int[] digits) {
        int carry = 1;

        for(int i = digits.length - 1; i >= 0; i--) {
            int digit = digits[i] + carry;
            carry = digit == 10 ? 1 : 0;
            digits[i] = digit % 10;
        }

        if(carry == 1) {
            int[] newDigits = new int[digits.length + 1];

            newDigits[0] = carry;
            System.arraycopy(digits, 0, newDigits, 1, digits.length);

            return newDigits;
        }

        return digits;
    }
}