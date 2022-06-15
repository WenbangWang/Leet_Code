package com.wwb.leetcode.medium;

import java.util.Stack;

/**
 * Convert a non-negative integer to its english words representation.
 * Given input is guaranteed to be less than 2^31 - 1.
 *
 * For example,
 *  123 -> "One Hundred Twenty Three"
 *  12345 -> "Twelve Thousand Three Hundred Forty Five"
 *  1234567 -> "One Million Two Hundred Thirty Four Thousand Five Hundred Sixty Seven"
 */
public class No273 {

    private static final String[] DIGITS = {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
    private static final String[] TENS = {"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    private static final String[] DOUBLE_DIGITS = {"", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
    private static final String HUNDRED = "Hundred";
    private static final String THOUSAND = "Thousand";
    private static final String MILLION = "Million";
    private static final String BILLION = "Billion";
    private static final String TRILLION = "Trillion";
    private static final String[] BIG_DENOMINATIONS = {"", THOUSAND, MILLION, BILLION, TRILLION};

    public String numberToWords(int num) {
        if(num == 0) {
            return DIGITS[0];
        }
        StringBuilder result = new StringBuilder();
        Stack<String> stack = new Stack<>();
        int index = 0;

        while(num > 0) {
            int mod = num % 1000;
            if(mod != 0) {
                stack.push(numberToHundreds(mod) + BIG_DENOMINATIONS[index] + " ");
            }
            num /= 1000;
            index++;
        }

        while(!stack.isEmpty()) {
            result.append(stack.pop());
        }

        return result.toString().trim();
    }

    private String numberToHundreds(int num) {
        if(num == 0) {
            return "";
        } else if(num < 10) {
            return DIGITS[num] + " ";
        } else if(num < 20) {
            return TENS[num % 10] + " ";
        } else if(num < 100) {
            return DOUBLE_DIGITS[num / 10] + " " + numberToHundreds(num % 10);
        } else {
            return DIGITS[num / 100] + " " + HUNDRED + " " + numberToHundreds(num % 100);
        }
    }
}
