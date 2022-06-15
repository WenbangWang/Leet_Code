package com.wwb.leetcode.medium;

/**
 * Given a sorted integer array nums and three integers a, b and c, apply a quadratic function of the form f(x) = ax2 + bx + c to each element nums[i] in the array, and return the array in a sorted order.
 *
 *
 *
 * Example 1:
 *
 * Input: nums = [-4,-2,2,4], a = 1, b = 3, c = 5
 * Output: [3,9,15,33]
 * Example 2:
 *
 * Input: nums = [-4,-2,2,4], a = -1, b = 3, c = 5
 * Output: [-23,-5,1,7]
 *
 *
 * Constraints:
 *
 * 1 <= nums.length <= 200
 * -100 <= nums[i], a, b, c <= 100
 * nums is sorted in ascending order.
 */
public class No360 {
    public int[] sortTransformedArray(int[] nums, int a, int b, int c) {
        int[] result = new int[nums.length];

        int start = 0;
        int end = nums.length - 1;
        int index = a >= 0 ? end : 0;

        while (start <= end) {
            var startValue = quad(nums[start], a, b, c);
            var endValue = quad(nums[end], a, b, c);

            // upward parabola
            if (a >= 0) {
                result[index--] = Math.max(startValue, endValue);

                if (startValue > endValue) {
                    start++;
                } else {
                    end--;
                }
            } else { // downward parabola
                result[index++] = Math.min(startValue, endValue);

                if (startValue < endValue) {
                    start++;
                } else {
                    end--;
                }
            }
        }

        return result;
    }

    private int quad(int x, int a, int b, int c) {
        return a * x * x + b * x + c;
    }
}
