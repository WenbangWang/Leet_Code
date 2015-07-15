package com.wwb.leetcode.easy;

/**
 * The count-and-say sequence is the sequence of integers beginning as follows:
 * 1, 11, 21, 1211, 111221, ...
 *
 * 1 is read off as "one 1" or 11.
 * 11 is read off as "two 1s" or 21.
 * 21 is read off as "one 2, then one 1" or 1211.
 * Given an integer n, generate the nth sequence.
 *
 * Note: The sequence of integers will be represented as a string.
 */
public class No38 {

    public String countAndSay(int n) {
        if(n < 0) {
            return null;
        }

        if(n == 0) {
            return "";
        }

        if(n == 1) {
            return "1";
        }

        return calculateCountAndSay(countAndSay(n - 1));
    }

    private String calculateCountAndSay(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] charArray = string.toCharArray();
        char last = charArray[0];
        int count = 1;

        for(int i = 1; i < charArray.length; i++) {
            char c = charArray[i];
            if(c == last) {
                count++;
            } else {
                stringBuilder.append(String.valueOf(count)).append(last);
                last = c;
                count = 1;
            }
        }

        stringBuilder.append(String.valueOf(count)).append(last);

        return stringBuilder.toString();
    }
}