package com.wwb.leetcode.medium;

/**
 * A message containing letters from A-Z is being encoded to numbers using the following mapping:
 * <p>
 * 'A' -> 1
 * 'B' -> 2
 * ...
 * 'Z' -> 26
 * Given an encoded message containing digits, determine the total number of ways to decode it.
 * <p>
 * For example,
 * Given encoded message "12", it could be decoded as "AB" (1 2) or "L" (12).
 * <p>
 * The number of ways decoding "12" is 2.
 */
public class No91 {

    public int numDecodings(String s) {
        int n = s.length();
        if (n == 0) {
            return 0;
        }

        int[] map = new int[n + 1];
        map[n] = 1;
        map[n - 1] = s.charAt(n - 1) == '0' ? 0 : 1;

        for (int i = n - 2; i >= 0; i--) {
            if (s.charAt(i) != '0') {
                map[i] = (Integer.parseInt(s.substring(i, i + 2)) <= 26) ? map[i + 1] + map[i + 2] : map[i + 1];
            }
        }

        return map[0];
    }

    public int solution2(String s) {
        int n = s.length();
        if (n == 0) {
            return 0;
        }

        int[] map = new int[n + 1];
        map[0] = 1;
        map[1] = s.charAt(0) == '0' ? 0 : 1;

        for (int i = 2; i <= n; i++) {
            int current = s.charAt(i - 1) - '0';
            int includePrevious = Integer.parseInt(s.substring(i - 2, i));

            map[i] = (current == 0 ? 0 : map[i - 1])
                    + (includePrevious <= 26  && includePrevious >= 10? map[i - 2] : 0);
        }

        return map[n];
    }
}
