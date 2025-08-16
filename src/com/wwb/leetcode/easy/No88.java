package com.wwb.leetcode.easy;

/**
 * Given two sorted integer arrays nums1 and nums2, merge nums2 into nums1 as one sorted array.
 * <p>
 * Note:
 * You may assume that nums1 has enough space (size that is greater or equal to m + n) to hold additional elements from nums2.
 * <p>
 * The number of elements initialized in nums1 and nums2 are m and n respectively.
 */
public class No88 {

    public void merge(int A[], int m, int B[], int n) {
        // solution1(A, m, B, n);
        solution2(A, m, B, n);
    }

    private void solution1(int[] A, int m, int[] B, int n) {
        int i = 0;
        int j = 0;
        int[] buffer = new int[m + n];
        int counter = 0;

        while (i < m && j < n) {
            if (A[i] > B[j]) {
                buffer[counter++] = B[j++];
            } else {
                buffer[counter++] = A[i++];
            }
        }

        while (i < m) {
            buffer[counter++] = A[i++];
        }

        while (j < n) {
            buffer[counter++] = B[j++];
        }

        for (int k = 0; k < counter; k++) {
            A[k] = buffer[k];
        }
    }

    private void solution2(int[] A, int m, int[] B, int n) {
        int length = m + n;

        while (n > 0) {
            A[--length] = (m == 0 || A[m - 1] < B[n - 1]) ? B[--n] : A[--m];
        }
    }
}
