package com.wwb.leetcode.medium;

import java.util.Arrays;

/**
 * Given an array of citations (each citation is a non-negative integer) of a researcher,
 * write a function to compute the researcher's h-index.
 *
 * According to the definition of h-index on Wikipedia:
 * "A scientist has index h if h of his/her N papers have at least h citations each,
 * and the other N − h papers have no more than h citations each."
 *
 * For example, given citations = [3, 0, 6, 1, 5],
 * which means the researcher has 5 papers in total and each of them had received 3, 0, 6, 1, 5 citations respectively.
 * Since the researcher has 3 papers with at least 3 citations each and the remaining two with no more than 3 citations each, his h-index is 3.
 *
 * Note: If there are several possible values for h,
 * the maximum one is taken as the h-index.
 */
public class No274 {

    public int hIndex(int[] citations) {
//        return solution1(citations);
        return solution2(citations);
    }

    private int solution1(int[] citations) {
        if(citations == null || citations.length == 0) {
            return 0;
        }

        Arrays.sort(citations);

        for(int paperIndex = 0, length = citations.length; paperIndex < length; paperIndex++) {
            if(citations[paperIndex] >= length - paperIndex) {
                return length - paperIndex;
            }
        }

        return 0;
    }

    private int solution2(int[] citations) {
        if(citations == null || citations.length == 0) {
            return 0;
        }

        int length = citations.length;
        int numberOfPapers = 0;
        // index is number of citations
        // value is number of papers has
        // this number of citations.
        int[] table = new int[length + 1];

        for(int citation : citations) {
            // citation bigger than number of papers
            if(citation > length) {
                table[length]++;
            } else {
                table[citation]++;
            }
        }


        for(int citation = length; citation >= 0; citation--) {
            numberOfPapers += table[citation];

            // number of papers with at least
            // this number of citations.
            if(numberOfPapers >= citation) {
                return citation;
            }
        }

        return 0;
    }
}
