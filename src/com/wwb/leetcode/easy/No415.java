package com.wwb.leetcode.easy;

/**
 * Given two non-negative integers, num1 and num2 represented as string, return the sum of num1 and num2 as a string.
 * <p>
 * You must solve the problem without using any built-in library for handling large integers (such as BigInteger). You must also not convert the inputs to integers directly.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: num1 = "11", num2 = "123"
 * Output: "134"
 * Example 2:
 * <p>
 * Input: num1 = "456", num2 = "77"
 * Output: "533"
 * Example 3:
 * <p>
 * Input: num1 = "0", num2 = "0"
 * Output: "0"
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= num1.length, num2.length <= 10^4
 * num1 and num2 consist of only digits.
 * num1 and num2 don't have any leading zeros except for the zero itself.
 */
public class No415 {
    public String addStrings(String num1, String num2) {
        return solution1(num1, num2);
    }

    private String solution1(String num1, String num2) {
        // always assume the first string has shorter length
        if (num1.length() > num2.length()) {
            return addStrings(num2, num1);
        }

        int shorterLength = num1.length();
        int longerLength = num2.length();
        int carry = 0;
        char[] common = new char[shorterLength];

        for (int i = shorterLength - 1; i >= 0; i--) {
            int n1 = num1.charAt(i) - '0';
            int n2 = num2.charAt(longerLength - shorterLength + i) - '0';
            int sum = n1 + n2 + carry;

            common[i] = Character.forDigit(sum % 10, 10);
            carry = sum / 10;
        }

        StringBuilder higherDigits = new StringBuilder(num2.substring(0, longerLength - shorterLength));

        if (carry == 1) {
            for (int i = higherDigits.length() - 1; i >= 0; i--) {
                int sum = higherDigits.charAt(i) - '0' + carry;

                higherDigits.setCharAt(i, Character.forDigit(sum % 10, 10));
                carry = sum / 10;
            }

            if (carry == 1) {
                higherDigits.insert(0, '1');
            }
        }

        return higherDigits + new String(common);
    }

    private String solution2(String num1, String num2) {
        int i = num1.length() - 1;
        int j = num2.length() - 1;
        int carry = 0;

        StringBuilder result = new StringBuilder(Math.max(num1.length(), num2.length()));

        while (i >= 0 || j >= 0 || carry != 0) {
            int n1 = i >= 0 ? num1.charAt(i) - '0' : 0;
            int n2 = j >= 0 ? num2.charAt(j) - '0' : 0;

            int sum = n1 + n2 + carry;

            result.append(Character.forDigit(sum % 10, 10));
            carry = sum / 10;
            i--;
            j--;
        }

        return result.reverse().toString();
    }
}
