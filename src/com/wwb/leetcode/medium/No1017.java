package com.wwb.leetcode.medium;

/**
 * Given an integer n, return a binary string representing its representation in base -2.
 *
 * Note that the returned string should not have leading zeros unless the string is "0".
 *
 *
 *
 * Example 1:
 *
 * Input: n = 2
 * Output: "110"
 * Explantion: (-2)2 + (-2)1 = 2
 * Example 2:
 *
 * Input: n = 3
 * Output: "111"
 * Explantion: (-2)2 + (-2)1 + (-2)0 = 3
 * Example 3:
 *
 * Input: n = 4
 * Output: "100"
 * Explantion: (-2)2 = 4
 *
 *
 * Constraints:
 *
 * 0 <= n <= 10^9
 */
public class No1017 {
    public String baseNeg2(int n) {
        StringBuilder res = new StringBuilder();
        while (n != 0) {
            res.append(n & 1);
            n = -(n >> 1);
        }
        return res.isEmpty() ? "0" : res.reverse().toString();
    }
}
