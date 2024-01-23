package com.wwb.leetcode.easy;

/**
 * We have two special characters:
 *
 * The first character can be represented by one bit 0.
 * The second character can be represented by two bits (10 or 11).
 * Given a binary array bits that ends with 0, return true if the last character must be a one-bit character.
 *
 *
 *
 * Example 1:
 *
 * Input: bits = [1,0,0]
 * Output: true
 * Explanation: The only way to decode it is two-bit character and one-bit character.
 * So the last character is one-bit character.
 * Example 2:
 *
 * Input: bits = [1,1,1,0]
 * Output: false
 * Explanation: The only way to decode it is two-bit character and two-bit character.
 * So the last character is not one-bit character.
 *
 *
 * Constraints:
 *
 * 1 <= bits.length <= 1000
 * bits[i] is either 0 or 1.
 */
public class No717 {
    public boolean isOneBitCharacter(int[] bits) {
        return solution1(bits, bits.length - 2);
    }

    private boolean solution1(int[] bits, int index) {
        if (index < 0) {
            return true;
        }

        if (bits[index] == 1) {
            if (index < 1) {
                return false;
            }

            if (bits[index - 1] != 1) {
                return false;
            }

            return solution1(bits, index - 2);
        }

        return solution1(bits, index - 1) || (index >= 1 && bits[index - 1] == 1 && solution1(bits, index - 2));
    }

    /**
     * if there is only one symbol in array the answer is always true (as last element is 0)
     * if there are two 0s at the end again the answer is true no matter what the rest symbols are( ...1100, ...1000,)
     * if there is 1 right before the last element(...10), the outcome depends on the count of sequential 1, i.e.
     *  a) if there is odd amount of 1(10, ...01110, etc) the answer is false as there is a single 1 without pair
     *  b) if it's even (110, ...011110, etc) the answer is true, as 0 at the end doesn't have anything to pair with
     */
    private boolean solution2(int[] bits) {
        int ones = 0;

        for (int i = bits.length - 2; i >= 0 && bits[i] != 0; i--) {
            ones++;
        }

        return ones % 2 == 0;
    }
}
