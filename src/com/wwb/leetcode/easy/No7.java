package com.wwb.leetcode.easy;

/**
 * Reverse digits of an integer.
 * <p>
 *
 * <pre>
 * Example1: x = 123, return 321
 * Example2: x = -123, return -321
 * </pre>
 */
public class No7 {

    public int reverse(int x) {
        // return solution1(x);
        return solution2(x);
    }

    private int solution1(int x) {
        boolean isNegative = false;
        String numString = x + "";
        if (x < 0) {
            numString = numString.substring(1);
            isNegative = true;
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (isNegative) {
            stringBuilder.append('-');
        }

        int index = numString.length() - 1;

        for (int i = index; i >= 0; i--) {
            if (numString.charAt(i) != '0') {
                index = i;
                break;
            }
        }

        for (int i = index; i >= 0; i--) {
            char c = numString.charAt(i);

            stringBuilder.append(c);
        }

        try {
            int num = Integer.parseInt(stringBuilder.toString());

            return num;
        } catch (Exception e) {
            return 0;
        }
    }

    private int solution2(int x) {
        int result = 0;

        while (x != 0) {
            int tail = x % 10;
            int currentResult = result * 10 + tail;

            if ((currentResult - tail) / 10 != result) {
                return 0;
            }

            result = currentResult;
            x /= 10;
        }

        return result;
    }
}
