package com.wwb.leetcode.medium;

/**
 * Additive number is a string whose digits can form additive sequence.
 *
 * A valid additive sequence should contain at least three numbers.
 * Except for the first two numbers, each subsequent number in the sequence must be the sum of the preceding two.
 *
 * For example:
 * "112358" is an additive number because the digits can form an additive sequence: 1, 1, 2, 3, 5, 8.
 *
 * 1 + 1 = 2, 1 + 2 = 3, 2 + 3 = 5, 3 + 5 = 8
 * "199100199" is also an additive number, the additive sequence is: 1, 99, 100, 199.
 * 1 + 99 = 100, 99 + 100 = 199
 * Note: Numbers in the additive sequence cannot have leading zeros, so sequence 1, 2, 03 or 1, 02, 3 is invalid.
 *
 * Given a string containing only digits '0'-'9', write a function to determine if it's an additive number.
 *
 * Follow up:
 * How would you handle overflow for very large input integers?
 */
public class No306 {

    public boolean isAdditiveNumber(String num) {
        if(num == null || num.length() < 3) {
            return false;
        }

        int length = num.length();

        for(int first = 1; first <= length / 2; first++) {
            // the number can be just ZERO but cannot be
            // more than 1 digit but start from ZERO.
            if(num.charAt(0) == '0' && first > 1) {
                break;
            }

            // the length of sum of first and second is at least max(first, second)
            // and what's left for the sum is length - first - second
            // so we should stop when max(first, second) is greater than length - first - second
            for(int second = 1; Math.max(first, second) <= length - first - second; second++) {
                if(num.charAt(first) == '0' && second > 1) {
                    break;
                }

                long num1 = Long.parseLong(num.substring(0, first));
                long num2 = Long.parseLong(num.substring(first, first + second));

                if(isValid(num1, num2, num.substring(first + second))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValid(long num1, long num2, String str) {
        if(str.isEmpty()) {
            return true;
        }

        long sum = num1 + num2;

        return str.startsWith(String.valueOf(sum)) && isValid(num2, sum, str.substring(String.valueOf(sum).length()));
    }
}
