package com.wwb.leetcode.tags.math;

/**
 * Given a positive integer, return its corresponding column title as appear in an Excel sheet.
 *
 * For example:
 *
 *  1 -> A
 *  2 -> B
 *  3 -> C
 *  ...
 *  26 -> Z
 *  27 -> AA
 *  28 -> AB
 */
public class No168 {

    public String convertToTitle(int n) {
        StringBuilder result = new StringBuilder();

        convertToTitle(n, result);

        return result.toString();
    }

    private void convertToTitle(int n, StringBuilder stringBuilder) {
        if(n == 0) {
            return;
        }

        stringBuilder.append(convertToTitle(--n / 26)).append((char) ('A' + n % 26));
    }
}