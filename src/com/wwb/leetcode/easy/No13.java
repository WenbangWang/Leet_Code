package com.wwb.leetcode.easy;

/**
 * Given a roman numeral, convert it to an integer.
 * <p>
 * Input is guaranteed to be within the range from 1 to 3999.
 */
public class No13 {

    public int romanToInt(String s) {
        final char THOUSAND = 'M';
        final char FIVE_HUNDRED = 'D';
        final char HUNDRED = 'C';
        final char FIFTY = 'L';
        final char TEN = 'X';
        final char FIVE = 'V';
        final char ONE = 'I';

        int sum = 0;

        for (int length = s.length(), i = length - 1; i >= 0; i--) {
            char c = s.charAt(i);

            switch (c) {
                case ONE -> sum += (sum >= 5 ? -1 : 1);
                case FIVE -> sum += 5;
                case TEN -> sum += 10 * (sum >= 50 ? -1 : 1);
                case FIFTY -> sum += 50;
                case HUNDRED -> sum += 100 * (sum >= 500 ? -1 : 1);
                case FIVE_HUNDRED -> sum += 500;
                case THOUSAND -> sum += 1000;
                default -> sum += 0;
            }
        }

        return sum;
    }
}
