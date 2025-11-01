package com.wwb.leetcode.hard;

/**
 * You are given an array arr which consists of only zeros and ones, divide the array into three non-empty parts such that all of these parts represent the same binary value.
 * <p>
 * If it is possible, return any [i, j] with i + 1 < j, such that:
 * <p>
 * arr[0], arr[1], ..., arr[i] is the first part,
 * arr[i + 1], arr[i + 2], ..., arr[j - 1] is the second part, and
 * arr[j], arr[j + 1], ..., arr[arr.length - 1] is the third part.
 * All three parts have equal binary values.
 * If it is not possible, return [-1, -1].
 * <p>
 * Note that the entire part is used when considering what binary value it represents. For example, [1,1,0] represents 6 in decimal, not 3. Also, leading zeros are allowed, so [0,1,1] and [1,1] represent the same value.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: arr = [1,0,1,0,1]
 * Output: [0,3]
 * Example 2:
 *
 * Input: arr = [1,1,0,1,1]
 * Output: [-1,-1]
 * Example 3:
 *
 * Input: arr = [1,1,0,0,1]
 * Output: [0,2]
 *
 *
 * Constraints:
 *
 * 3 <= arr.length <= 3 * 10^4
 * arr[i] is 0 or 1
 * </pre>
 */
public class No927 {
    public int[] threeEqualParts(int[] arr) {
        int numberOfOnes = 0;

        for (int i : arr) {
            if (i == 1) {
                numberOfOnes++;
            }
        }

        if (numberOfOnes == 0) {
            return new int[]{0, arr.length - 1};
        }

        if (numberOfOnes % 3 != 0) {
            return new int[]{-1, -1};
        }

        int index = 0;
        int n = numberOfOnes / 3;

        while (index < arr.length) {
            if (arr[index] == 1) {
                break;
            }

            index++;
        }

        int first = skipLeadingZeros(arr, 0);
        int second = skipLeadingZeros(arr, moveToMatchNumberOfOnes(arr, n, first) + 1);
        int third = skipLeadingZeros(arr, moveToMatchNumberOfOnes(arr, n, second) + 1);

        while (third < arr.length && arr[first] == arr[second] && arr[second] == arr[third]) {
            first++;
            second++;
            third++;
        }

        if (third != arr.length) {
            return new int[]{-1, -1};
        }

        return new int[]{first - 1, second};
    }

    private int skipLeadingZeros(int[] arr, int start) {
        for (int i = start; i < arr.length; i++) {
            if (arr[i] == 1) {
                return i;
            }
        }

        return -1;
    }

    private int moveToMatchNumberOfOnes(int[] arr, int n, int start) {
        for (int i = start; i < arr.length; i++) {
            if (arr[i] == 1) {
                n--;
            }

            if (n == 0) {
                return i;
            }
        }

        return -1;
    }
}
