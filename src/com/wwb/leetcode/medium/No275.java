package com.wwb.leetcode.medium;

/**
 * Follow up for H-Index: What if the citations array is sorted in ascending order?
 * Could you optimize your algorithm?
 */
public class No275 {

    public int hIndex(int[] citations) {
        if(citations == null || citations.length == 0) {
            return 0;
        }

        int length = citations.length;
        int start = 0;
        int end = length - 1;

        while(start <= end) {
            int mid = (end - start) / 2 + start;
            int citation = citations[mid];

            if(citation == length - mid) {
                return citation;
            } else if(citation > length - mid) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }

        return length - start;
    }
}
