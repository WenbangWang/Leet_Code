package com.wwb.leetcode.tags.hashtable;

/**
 * Count the number of prime numbers less than a non-negative number, n.
 */
public class No204 {

    public int countPrimes(int n) {
        if(n == 0 || n == 1) {
            return 0;
        }

        int result = 0;
        boolean[] table = new boolean[n];

        for(int i = 2; i < n; i++) {
            int j = 1;

            if(!table[i]) {
                result++;
            }

            while(i * j < n) {
                table[i * j] = true;
                j++;
            }
        }

        return result;
    }
}