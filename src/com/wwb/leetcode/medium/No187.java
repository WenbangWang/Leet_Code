package com.wwb.leetcode.medium;

import java.util.*;

/**
 * All DNA is composed of a series of nucleotides abbreviated as A, C, G, and T,
 * for example: "ACGAATTCCG". When studying DNA, it is sometimes useful to identify repeated sequences within the DNA.
 *
 * Write a function to find all the 10-letter-long sequences (substrings) that occur more than once in a DNA molecule.
 *
 * For example,
 *
 * Given s = "AAAAACCCCCAAAAACCCCCCAAAAAGGGTTT",
 *
 * Return:
 * ["AAAAACCCCC", "CCCCCAAAAA"].
 */
public class No187 {

    public List<String> findRepeatedDnaSequences(String s) {
        List<String> result = new ArrayList<>();
        Set<Integer> once = new HashSet<>();
        Set<Integer> twice = new HashSet<>();
        int length = s.length();
        char[] map = new char[26];

        // 'A' - 'A'
        map[0] = 0; // 00
        map['C' - 'A'] = 1; // 01
        map['G' - 'A'] = 2; // 10
        map['T' - 'A'] = 3; // 11

        for(int i = 0; i < length - 9; i++) {
            int vector = 0;

            for(int j = i; j < i + 10; j++) {
                vector <<= 2;
                vector |= map[s.charAt(j) - 'A'];
            }

            // repeated once AND NOT twice
            if(!once.add(vector) && twice.add(vector)) {
                result.add(s.substring(i, i + 10));
            }
        }

        return result;
    }
}
