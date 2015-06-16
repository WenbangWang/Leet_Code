package com.wwb.leetcode.easy;

/**
 * The string "PAYPALISHIRING" is written in a zigzag pattern on a given number of rows like this:
 * (you may want to display this pattern in a fixed font for better legibility)
 *
 * P   A   H   N
 * A P L S I I G
 * Y   I   R
 * And then read line by line: "PAHNAPLSIIGYIR"
 * Write the code that will take a string and make this conversion given a number of rows:
 *
 * string convert(string text, int nRows);
 * convert("PAYPALISHIRING", 3) should return "PAHNAPLSIIGYIR".
 */
public class No6 {

    public String convert(String s, int numRows) {
        if(numRows == 0 || s == null) {
            return null;
        }

        int a = new Integer(new StringBuilder("1").toString());
        char[] charArray = s.toCharArray();
        int length = charArray.length;
        StringBuilder[] stringBuilder = new StringBuilder[numRows];
        int i = 0;

        for(int j = 0; j < numRows; j++) {
            stringBuilder[j] = new StringBuilder();
        }

        while(i < length) {
            //Going down
            for(int j = 0; j < numRows && i < length; j++) {
                stringBuilder[j].append(charArray[i++]);
            }
            //Going up
            for(int j = numRows - 2; j >= 1 && i < length; j--) {
                stringBuilder[j].append(charArray[i++]);
            }
        }

        for(int j = 1; j < numRows; j++) {
            stringBuilder[0].append(stringBuilder[j]);
        }

        return stringBuilder[0].toString();
    }
}