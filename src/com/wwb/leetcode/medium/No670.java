package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * You are given an integer num. You can swap two digits at most once to get the maximum valued number.
 * <p>
 * Return the maximum valued number you can get.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: num = 2736
 * <p>
 * Output: 7236
 * <p>
 * Explanation: Swap the number 2 and the number 7.
 * <p>
 * Example 2:
 * <p>
 * Input: num = 9973
 * <p>
 * Output: 9973
 * <p>
 * Explanation: No swap.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 0 <= num <= 10^8
 */
public class No670 {
    public int maximumSwap(int num) {
        return solution1(num);
    }

    private int solution1(int num) {
        // indexed by 0 - 9.
        List<LinkedList<Integer>> indexes = new ArrayList<>(10);
        init(indexes);

        int runner = num;
        // reversed position from the least significant digit to the most.
        int position = 0;

        while (runner > 0) {
            int mod = runner % 10;
            indexes.get(mod).add(position);

            position++;
            runner /= 10;
        }

        runner = num;

        while (position > 0) {
            // move from the most significant digit to the least
            int currentPow = (int) StrictMath.pow(10, position - 1);
            int digit = runner / currentPow;

            Optional<Pair> positionAndDigitPairToSwapOpt = findLargestDigitToSwapFromLeastSignificant(digit, indexes);

            if (positionAndDigitPairToSwapOpt.isPresent()) {
                int swapPow = (int) StrictMath.pow(10, positionAndDigitPairToSwapOpt.get().position);
                // Set the current digit to the digit to swap
                num -= digit * currentPow;
                num += positionAndDigitPairToSwapOpt.get().digit * currentPow;

                // Set the digit to swap to the current digit
                num -= positionAndDigitPairToSwapOpt.get().digit * swapPow;
                num += digit * swapPow;

                return num;
            }

            indexes.get(digit).removeLast();

            position--;
            runner %= currentPow;
        }

        return num;
    }

    // find the largest digit and the leftmost digit smaller than the largest digit and swap them
    private int solution2(int num) {
        char[] chars = Integer.toString(num).toCharArray();
        int maxIndex = chars.length - 1;
        int leftIndex = 0;
        int rightIndex = 0;

        for (int i = chars.length - 2; i >= 0; i--) {
            if (chars[maxIndex] == chars[i]) {
                continue;
            }

            if (chars[maxIndex] < chars[i]) {
                maxIndex = i;
            } else {
                leftIndex = i;
                rightIndex = maxIndex;
            }
        }

        char temp = chars[leftIndex];
        chars[leftIndex] = chars[rightIndex];
        chars[rightIndex] = temp;

        return Integer.parseInt(new String(chars));
    }

    private void init(List<LinkedList<Integer>> indexes) {
        for (int i = 0; i <= 9; i++) {
            indexes.add(new LinkedList<>());
        }
    }

    private Optional<Pair> findLargestDigitToSwapFromLeastSignificant(
        int lowerBound,
        List<LinkedList<Integer>> indexes
    ) {
        for (int i = indexes.size() - 1; i > lowerBound; i--) {
            if (indexes.get(i).isEmpty()) {
                continue;
            }

            // the lower the position is in the list, the less significant the digit is.
            return Optional.of(new Pair(indexes.get(i).getFirst(), i));
        }

        return Optional.empty();
    }

    private static class Pair {
        int position;
        int digit;

        Pair(int position, int digit) {
            this.position = position;
            this.digit = digit;
        }
    }

}
