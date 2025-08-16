package com.wwb.leetcode.easy;

import java.util.Stack;

/**
 * Given two binary strings, return their sum (also a binary string).
 * <p>
 * <pre>
 * For example,
 * a = "11"
 * b = "1"
 * Return "100".
 * </pre>
 */
public class No67 {

    public String addBinary(String a, String b) {
        int carry = 0;
        Stack<Integer> stack = new Stack<>();
        StringBuilder stringBuilder = new StringBuilder();
        char[] firstCharArray = a.toCharArray();
        char[] secondCharArray = b.toCharArray();
        int aLength = firstCharArray.length;
        int bLength = secondCharArray.length;

        while (aLength - 1 >= 0 && bLength - 1 >= 0) {
            int digit = charToInt(firstCharArray[aLength - 1]) + charToInt(secondCharArray[bLength - 1]) + carry;
            carry = digit / 2;
            stack.push(digit % 2);
            aLength--;
            bLength--;
        }

        while (aLength - 1 >= 0) {
            int digit = charToInt(firstCharArray[aLength - 1]) + carry;
            carry = digit / 2;
            stack.push(digit % 2);
            aLength--;
        }

        while (bLength - 1 >= 0) {
            int digit = charToInt(secondCharArray[bLength - 1]) + carry;
            carry = digit / 2;
            stack.push(digit % 2);
            bLength--;
        }

        if (carry == 1) {
            stack.push(carry);
        }

        while (!stack.isEmpty()) {
            stringBuilder.append(stack.pop());
        }

        return stringBuilder.toString();
    }

    private int charToInt(char c) {
        return c - '0';
    }
}
