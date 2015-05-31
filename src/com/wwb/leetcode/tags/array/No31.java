package com.wwb.leetcode.tags.array;

import java.util.Arrays;

/**
 * Implement next permutation, which rearranges numbers into the lexicographically next greater permutation of numbers.
 *
 * If such arrangement is not possible, it must rearrange it as the lowest possible order (ie, sorted in ascending order).
 *
 * The replacement must be in-place, do not allocate extra memory.
 *
 * Here are some examples. Inputs are in the left-hand column and its corresponding outputs are in the right-hand column.
 * 1,2,3 → 1,3,2
 * 3,2,1 → 1,2,3
 * 1,1,5 → 1,5,1
 */
public class No31 {

    public void nextPermutation(int[] num) {
        if(num == null) {
            return;
        }

        int length = num.length;
        int partitionIndex = -1;
        int changeIndex = -1;
        for(int i = length - 2; i >= 0; i--) {
            if(num[i + 1] > num[i]) {
                partitionIndex = i;
                break;
            }
        }

        if(partitionIndex == -1) {
            Arrays.sort(num);
            return;
        }

        for(int i = length - 1; i >= 0; i--) {
            if(num[i] > num[partitionIndex]) {
                changeIndex = i;
                break;
            }
        }

        this.swap(num, partitionIndex, changeIndex);

        int start = partitionIndex + 1;
        int end = length - 1;

        while(start < end) {
            swap(num, start++, end--);
        }
    }

    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}