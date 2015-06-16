package com.wwb.leetcode.tags.string;

/**
 *Implement atoi to convert a string to an integer.
 *
 * Requirements for atoi:
 * The function first discards as many whitespace characters as necessary until the first non-whitespace character is found.
 * Then, starting from this character, takes an optional initial plus or minus sign followed by as many numerical digits as possible,
 * and interprets them as a numerical value.
 *
 * The string can contain additional characters after those that form the integral number,
 * which are ignored and have no effect on the behavior of this function.
 *
 * If the first sequence of non-whitespace characters in str is not a valid integral number,
 * or if no such sequence exists because either str is empty or it contains only whitespace characters,
 * no conversion is performed.
 *
 * If no valid conversion could be performed, a zero value is returned.
 * If the correct value is out of the range of representable values,
 * INT_MAX (2147483647) or INT_MIN (-2147483648) is returned.
 */
public class No8 {

    public int myAtoi(String str) {
        if(str == null || str.length() == 0) {
            return 0;
        }

        int sign = 1;
        int index = 0;
        int total = 0;

        str = str.trim();

        if(str.charAt(index) == '+') {
            sign = 1;
            index++;
        } else if(str.charAt(index) == '-') {
            sign = -1;
            index++;
        }

        while(index < str.length()) {
            char c = str.charAt(index);

            if(c < '0' || c > '9') {
                break;
            }
            int digit = c - '0';

            if(Integer.MAX_VALUE / 10 < total || Integer.MAX_VALUE / 10 == total && Integer.MAX_VALUE % 10 < digit) {
                return sign == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }

            total = total * 10 + digit;
            index++;
        }

        return total * sign;
    }
}