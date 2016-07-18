package com.wwb.leetcode.hard;

/**
 * Given two arrays of length m and n with digits 0-9 representing two numbers.
 * Create the maximum number of length k <= m + n from digits of the two.
 * The relative order of the digits from the same array must be preserved.
 * Return an array of the k digits. You should try to optimize your time and space complexity.
 *
 * Example 1:
 * nums1 = [3, 4, 6, 5]
 * nums2 = [9, 1, 2, 5, 8, 3]
 * k = 5
 * return [9, 8, 6, 5, 3]
 *
 * Example 2:
 * nums1 = [6, 7]
 * nums2 = [6, 0, 4]
 * k = 5
 * return [6, 7, 6, 0, 4]
 *
 * Example 3:
 * nums1 = [3, 9]
 * nums2 = [8, 9]
 * k = 3
 * return [9, 8, 9]
 */
public class No321 {

    public int[] maxNumber(int[] nums1, int[] nums2, int k) {
        int[] result = new int[k];

        for(int i = Math.max(k - nums2.length, 0); i <= Math.min(nums1.length, k); i++) {
            int[] result1 = getMaxSubArray(nums1, i);
            int[] result2 = getMaxSubArray(nums2, k - i);
            int[] tempResult = new int[k];
            int pointer1 = 0;
            int pointer2=  0;
            int pointer = 0;

            while(pointer1 < result1.length || pointer2 < result2.length) {
                tempResult[pointer++] = isGreater(result1, pointer1, result2, pointer2) ? result1[pointer1++] : result2[pointer2++];
            }

            if(!isGreater(result, 0, tempResult, 0)) {
                result = tempResult;
            }
        }

        return result;
    }

    private boolean isGreater(int[] num1, int i, int[] num2, int j) {
        for(; i < num1.length && j < num2.length; i++, j++) {
            if(num1[i] > num2[j]) {
                return true;
            }

            if(num2[j] > num1[i]) {
                return false;
            }
        }

        return i != num1.length;
    }

    private int[] getMaxSubArray(int[] num, int k) {
        int pointer = 0;
        int[] subArray = new int[k];

        for(int i = 0; i < num.length; i++) {
            while(pointer > 0 && num.length - i > k - pointer && subArray[pointer - 1] < num[i]) {
                pointer--;
            }

            if(pointer < k) {
                subArray[pointer++] = num[i];
            }
        }

        return subArray;
    }
}
