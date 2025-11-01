package com.wwb.leetcode.easy;

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
        StringBuilder result = new StringBuilder();
        int i = a.length() - 1;
        int j = b.length() - 1;

        while (i >= 0 || j >= 0 || carry == 1) {
            int aDigit = i < 0 ? 0 : charToInt(a.charAt(i--));
            int bDigit = j < 0 ? 0 : charToInt(b.charAt(j--));

            int sum = aDigit + bDigit + carry;
            carry = sum / 2;
            result.append(sum % 2);
        }

        return result.reverse().toString();
    }

    private int charToInt(char c) {
        return c - '0';
    }
}
