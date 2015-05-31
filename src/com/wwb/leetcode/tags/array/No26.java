package com.wwb.leetcode.tags.array;

/**
 * Given a sorted array, remove the duplicates in place such that each element appear only once and return the new length.
 *
 * Do not allocate extra space for another array, you must do this in place with constant memory.
 *
 * For example,
 * Given input array nums = [1,1,2],
 *
 * Your function should return length = 2, with the first two elements of nums being 1 and 2 respectively.
 * It doesn't matter what you leave beyond the new length.
 */
public class No26 {

    public int removeDuplicates(int[] A) {
        int count = 0;

        if(A == null || A.length == 0) {
            return 0;
        }

        for(int i = 1; i < A.length; i++) {
            if(A[i] != A[i - 1]) {
                A[++count] = A[i];
            }
        }

        return count + 1;
    }
}