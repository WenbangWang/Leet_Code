package com.wwb.leetcode.tags.math;

/**
 * Given a column title as appear in an Excel sheet, return its corresponding column number.
 *
 * For example:
 *
 * A -> 1
 * B -> 2
 * C -> 3
 * ...
 * Z -> 26
 * AA -> 27
 * AB -> 28
 */
public class No171 {

    public int titleToNumber(String s) {
        char[] chars = s.toCharArray();
        int length = chars.length;
        int result = 0;

        for(int i = 0; i < length; i++) {
            char c = chars[i];
            int multiple = c - 'A' + 1;
            int exponent = length - i - 1;

            result += multiple * Math.pow(26, exponent);
        }
        return result;
    }
}