package com.wwb.leetcode.easy;

/**
 * Given an array and a value, remove all instances of that value in place and return the new length.
 *
 * The order of elements can be changed. It doesn't matter what you leave beyond the new length.
 */
public class No27 {

    public int removeElement(int[] A, int elem) {
        int count = 0;

        for(int i = 0; i < A.length; i++) {
            if(A[i] != elem) {
                A[count++] = A[i];
            }
        }

        return count;
    }
}