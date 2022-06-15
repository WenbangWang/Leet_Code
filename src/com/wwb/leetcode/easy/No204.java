package com.wwb.leetcode.easy;

/**
 * Count the number of prime numbers less than a non-negative number, n.
 */
public class No204 {

    public int countPrimes(int n) {
        if (n < 2) {
            return 0;
        }

        boolean[] nonPrime = new boolean[n];
        nonPrime[1] = true;

        int numNonPrimes = 1;
        for (int i = 2; i < Math.sqrt(n); i++) { // O(sqrt(n))
            if (nonPrime[i]) {
                continue;
            }

            // O(log(log(n)))
            for (int j = i * 2; j < n; j += i) {
                if (!nonPrime[j]) {
                    nonPrime[j] = true;
                    numNonPrimes++;
                }
            }
        }
        return (n - 1) - numNonPrimes;
    }
}
