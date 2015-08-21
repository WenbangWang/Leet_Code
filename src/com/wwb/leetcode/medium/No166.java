package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;

/**
 * Given two integers representing the numerator and denominator of a fraction, return the fraction in string format.
 *
 * If the fractional part is repeating, enclose the repeating part in parentheses.
 *
 * For example,
 *
 * Given numerator = 1, denominator = 2, return "0.5".
 * Given numerator = 2, denominator = 1, return "2".
 * Given numerator = 2, denominator = 3, return "0.(6)".
 */
public class No166 {

    public String fractionToDecimal(int numerator, int denominator) {
        if(numerator == 0) {
            return "0";
        }

        if(denominator == 0) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        long longNumerator = Math.abs((long) numerator);
        long longDenominator = Math.abs((long) denominator);
        Map<Long, Integer> map = new HashMap<>();

        if((numerator > 0) ^ (denominator > 0)) {
            stringBuilder.append("-");
        }

        stringBuilder.append(longNumerator / longDenominator);
        longNumerator %= longDenominator;

        if(longNumerator == 0) {
            return stringBuilder.toString();
        }

        stringBuilder.append(".");

        while(longNumerator != 0) {
            map.put(longNumerator, stringBuilder.length());

            longNumerator *= 10;
            stringBuilder.append(longNumerator / longDenominator);
            longNumerator %= longDenominator;

            Integer index = map.get(longNumerator);

            if(index != null) {
                stringBuilder.insert(index, "(");
                stringBuilder.append(")");
                break;
            }
        }

        return stringBuilder.toString();
    }
}