package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;

/**
 * The gray code is a binary numeral system where two successive values differ in only one bit.
 * <p>
 * Given a non-negative integer n representing the total number of bits in the code,
 * print the sequence of gray code. A gray code sequence must begin with 0.
 * <p>
 * For example, given n = 2, return [0,1,3,2]. Its gray code sequence is:
 * <p>
 * 00 - 0
 * 01 - 1
 * 11 - 3
 * 10 - 2
 * Note:
 * For a given n, a gray code sequence is not uniquely defined.
 * <p>
 * For example, [0,2,3,1] is also a valid gray code sequence according to the above definition.
 * <p>
 * For now, the judge is able to judge based on one instance of gray code sequence. Sorry about that.
 */
public class No89 {

    public List<Integer> grayCode(int n) {
        return solution1(n);
    }

    public List<Integer> solution1(int n) {
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < 1 << n; i++) {
            result.add(i >> 1 ^ i);
        }

        return result;
    }

    public List<Integer> solution2(int n) {
        List<Integer> result = new ArrayList<>();

        if (n == 0) {
            result.add(0);

            return result;
        }

        List<Integer> pre = solution2(n - 1);
        int highestBit = (int) StrictMath.pow(2, n - 1);
        result.addAll(pre);

        for (int i = pre.size() - 1; i >= 0; i--) {
            result.add(pre.get(i) + highestBit);
        }

        return result;
    }
}
