package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;

/**
 * Given an integer n, return 1 - n in lexicographical order.
 *
 * For example, given 13, return: [1,10,11,12,13,2,3,4,5,6,7,8,9].
 *
 * Please optimize your algorithm to use less time and space. The input size may be as large as 5,000,000.
 */
public class No386 {
    public List<Integer> lexicalOrder(int n) {
        List<Integer> result = new ArrayList<>();
        int current = 1;

        for (int i = 1; i <= n; i++) {
            result.add(current);

            if (current * 10 <= n) {
                current *= 10;
            } else if (current % 10 != 9 && current + 1 <= n) {
                current++;
            } else {
                // when the number is x9...9, move the number
                // back to single digit and make it to x+1
                // which is the next number in lexicographical order
                while ((current / 10) % 10 == 9) {
                    current /= 10;
                }

                // the above condition would fail at x9
                current /= 10;
                current++;
            }
        }
        return result;
    }
}
