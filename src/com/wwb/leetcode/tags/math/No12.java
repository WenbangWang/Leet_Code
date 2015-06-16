package com.wwb.leetcode.tags.math;

/**
 * Given an integer, convert it to a roman numeral.
 *
 * Input is guaranteed to be within the range from 1 to 3999.
 */
public class No12 {

    public String intToRoman(int num) {
        final String THOUSAND = "M";
        final String FIVE_HUNDRED = "D";
        final String HUNDRED = "C";
        final String FIFTY = "L";
        final String TEN = "X";
        final String FIVE = "V";
        final String ONE = "I";

        StringBuilder stringBuilder = new StringBuilder();
        int numberOfThousand = num / 1000;
        int numberOfHundred = (num % 1000) / 100;
        int numberOfTen = (num % 100) / 10;
        int numberOfOne = num % 10;

        stringBuilder.append(getRomanNumeral(numberOfThousand, THOUSAND, null, null));
        stringBuilder.append(getRomanNumeral(numberOfHundred, HUNDRED, FIVE_HUNDRED, THOUSAND));
        stringBuilder.append(getRomanNumeral(numberOfTen, TEN, FIFTY, HUNDRED));
        stringBuilder.append(getRomanNumeral(numberOfOne, ONE, FIVE, TEN));

        return stringBuilder.toString();
    }

    private String getRomanNumeral(int count, String single, String fiveTimesSingle, String nextSingle) {
        StringBuilder stringBuilder = new StringBuilder();

        if(count >= 0 && count < 4) {
            appendToStringBuilder(stringBuilder, count, single);
        } else if(count == 4) {
            stringBuilder.append(single);
            stringBuilder.append(fiveTimesSingle);
        } else if(count >= 5 && count < 9) {
            stringBuilder.append(fiveTimesSingle);
            count -= 5;

            appendToStringBuilder(stringBuilder, count, single);
        } else if(count == 9) {
            stringBuilder.append(single);
            stringBuilder.append(nextSingle);
        }

        return stringBuilder.toString();
    }

    private void appendToStringBuilder(StringBuilder stringBuilder, int count, String single) {
        for(int i = 0; i < count; i++) {
            stringBuilder.append(single);
        }
    }
}