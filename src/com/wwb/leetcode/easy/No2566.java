package com.wwb.leetcode.easy;

/**
 * You are given an integer num. You know that Bob will sneakily remap one of the 10 possible digits (0 to 9) to another digit.
 * <p>
 * Return the difference between the maximum and minimum values Bob can make by remapping exactly one digit in num.
 * <p>
 * Notes:
 * <p>
 * When Bob remaps a digit d1 to another digit d2, Bob replaces all occurrences of d1 in num with d2.
 * Bob can remap a digit to itself, in which case num does not change.
 * Bob can remap different digits for obtaining minimum and maximum values respectively.
 * The resulting number after remapping can contain leading zeroes.
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: num = 11891
 * Output: 99009
 * Explanation:
 * To achieve the maximum value, Bob can remap the digit 1 to the digit 9 to yield 99899.
 * To achieve the minimum value, Bob can remap the digit 1 to the digit 0, yielding 890.
 * The difference between these two numbers is 99009.
 * Example 2:
 *
 * Input: num = 90
 * Output: 99
 * Explanation:
 * The maximum value that can be returned by the function is 99 (if 0 is replaced by 9) and the minimum value that can be returned by the function is 0 (if 9 is replaced by 0).
 * Thus, we return 99.
 *
 *
 * Constraints:
 *
 * 1 <= num <= 10^8
 * </pre>
 */
public class No2566 {
    public int minMaxDifference(int num) {
        return solution1(num);
    }

    private int solution1(int num) {
        int mod = 1;
        int runner = num;

        while (runner != 0) {
            runner /= 10;
            mod *= 10;
        }

        mod /= 10;

        runner = num;
        int max = 0;
        int digitToReplace = -1;
        int runningMod = mod;

        while (runningMod != 0) {
            int digit = runner / runningMod;

            if (digit != 9) {
                if (digitToReplace == -1) {
                    digitToReplace = digit;
                }

                if (digitToReplace == digit) {
                    digit = 9;
                }
            }

            max += (digit * runningMod);
            runner %= runningMod;
            runningMod /= 10;
        }

        runner = num;
        runningMod = mod;
        digitToReplace = -1;
        int min = 0;

        while (runningMod != 0) {
            int digit = runner / runningMod;

            if (digitToReplace == -1) {
                digitToReplace = digit;
            }

            if (digitToReplace == digit) {
                digit = 0;
            }

            min += (digit * runningMod);
            runner %= runningMod;
            runningMod /= 10;
        }

        return max - min;
    }

    private int solution2(int num) {
        String str = String.valueOf(num);
        char maxCharToReplace = '9';

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '9') {
                maxCharToReplace = str.charAt(i);
                break;
            }
        }

        String max = str.replace(maxCharToReplace, '9');
        String min = str.replace(str.charAt(0), '0');

        return Integer.parseInt(max) - Integer.parseInt(min);
    }

    // not allow leading zeros
    private int solution3(int num) {
        String str = String.valueOf(num);
        char maxCharToReplace = '9';

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '9') {
                maxCharToReplace = str.charAt(i);
                break;
            }
        }

        String max = str.replace(maxCharToReplace, '9');


        String min = getMin(str);

        return Integer.parseInt(max) - Integer.parseInt(min);
    }

    private String getMin(String str) {
        String min = "";

        if (str.charAt(0) == 1) {
            char minCharToReplace = '0';

            for (int i = 1; i < str.length(); i++) {
                if (str.charAt(i) != '0' && str.charAt(i) != '1') {
                    minCharToReplace = str.charAt(i);
                    break;
                }
            }

            if (minCharToReplace != '0') {
                min = str.replace(minCharToReplace, '0');
            }
        } else {
            min = str.replace(str.charAt(0), '1');
        }
        return min;
    }
}
