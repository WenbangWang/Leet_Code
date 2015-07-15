package com.wwb.leetcode.tags.backtracking;

import java.util.LinkedList;
import java.util.List;

/**
 * The set [1,2,3,…,n] contains a total of n! unique permutations.
 *
 * By listing and labeling all of the permutations in order,
 * We get the following sequence (ie, for n = 3):
 *
 * "123"
 * "132"
 * "213"
 * "231"
 * "312"
 * "321"
 * Given n and k, return the kth permutation sequence.
 *
 * Note: Given n will be between 1 and 9 inclusive.
 */
public class No60 {

    public String getPermutation(int n, int k) {
        List<String> numbers = getNumbers(n);
        int[] factorial = getFactorialArray(n);
        StringBuilder stringBuilder = new StringBuilder();
        k--;

        for(int i = n; i > 0; i--) {
            int index = k / factorial[i - 1];
            k %= factorial[i - 1];
            stringBuilder.append(numbers.get(index));
            numbers.remove(index);
        }

        return stringBuilder.toString();
    }

    private List<String> getNumbers(int n) {
        List<String> numbers = new LinkedList<>();

        for(int i = 1; i <= n; i++) {
            numbers.add(i + "");
        }

        return numbers;
    }

    private int[] getFactorialArray(int n) {
        int[] factorial = new int[n];
        factorial[0] = 1;

        for(int i = 1; i < n; i++) {
            factorial[i] = i * factorial[i - 1];
        }

        return factorial;
    }
}