package com.wwb.leetcode.medium;

/**
 * Given two numbers represented as strings, return multiplication of the numbers as a string.
 *
 * Note: The numbers can be arbitrarily large and are non-negative.
 */
public class No43 {

    public String multiply(String num1, String num2) {
        if(!isValidString(num1) || !isValidString(num2)) {
            return null;
        }

        boolean isProductNegative = isProductNegative(num1, num2);
        char num1FirstChar = num1.charAt(0);
        char num2FirstChar = num2.charAt(0);
        int num1Length = num1.length();
        int num2Length = num2.length();
        StringBuilder stringBuilder = new StringBuilder();

        if(isPositiveSign(num1FirstChar) || isNegativeSign(num1FirstChar)) {
            num1 = num1.substring(1, num1Length);
            num1Length -= 1;
        }
        if(isPositiveSign(num2FirstChar) || isNegativeSign(num2FirstChar)) {
            num2 = num2.substring(1, num2Length);
            num2Length -= 1;
        }

        int[] product = new int[num1Length + num2Length];

        for(int i = num1Length - 1; i >= 0; i--) {
            for(int j = num2Length - 1; j >= 0; j--) {
                int index = num1Length + num2Length - i - j - 2;
                product[index] += (num1.charAt(i) - '0') * (num2.charAt(j) - '0');
                product[index + 1] += product[index] / 10;
                product[index] %= 10;
            }
        }

        for(int i = product.length - 1; i > 0; i--) {
            if(stringBuilder.length() != 0 || product[i] != 0) {
                stringBuilder.append(product[i]);
            }
        }

        stringBuilder.append(product[0]);

        if(isProductNegative) {
            return "-" + stringBuilder.toString();
        }

        return stringBuilder.toString();
    }

    private boolean isNegative(String str) {
        if(!isValidString(str)) {
            return false;
        }

        return isNegativeSign(str.charAt(0));
    }


    private boolean isProductNegative(String num1, String num2) {
        boolean num1Negative = isNegative(num1);
        boolean num2Negative = isNegative(num2);

        if(num1Negative && num2Negative) {
            return false;
        } else if(num1Negative || num2Negative) {
            return true;
        }

        return false;
    }

    private boolean isValidString(String str) {
        return str != null && str.length() != 0;
    }

    private boolean isNegativeSign(char c) {
        return c == '-';
    }

    private boolean isPositiveSign(char c) {
        return c == '+';
    }
}