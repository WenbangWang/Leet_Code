package com.wwb.leetcode.easy;

/**
 * Given an array arr of positive integers sorted in a strictly increasing order, and an integer k.
 * <p>
 * Return the kth positive integer that is missing from this array.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: arr = [2,3,4,7,11], k = 5
 * <p>
 * Output: 9
 * <p>
 * Explanation: The missing positive integers are [1,5,6,8,9,10,12,13,...]. The 5th missing positive integer is 9.
 * <p>
 * Example 2:
 * <p>
 * Input: arr = [1,2,3,4], k = 2
 * <p>
 * Output: 6
 * <p>
 * Explanation: The missing positive integers are [5,6,7,...]. The 2nd missing positive integer is 6.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= arr.length <= 1000
 * <p>
 * 1 <= arr[i] <= 1000
 * <p>
 * 1 <= k <= 1000
 * <p>
 * arr[i] < arr[j] for 1 <= i < j <= arr.length
 * <p>
 * <p>
 * <p>
 * Follow up:
 * <p>
 * Could you solve this problem in less than O(n) complexity?
 */
public class No1539 {
    // number of missing numbers between [0, i] is arr[i] - i - 1
    public int findKthPositive(int[] arr, int k) {
        return solution2(arr, k);
    }

    // O(N)
    private int solution1(int[] arr, int k) {
        for (int i = 0; i < arr.length; i++) {
            int missingNumbers = arr[i] - i - 1;

            if (missingNumbers >= k) {
                return i + k;
            }
        }

        // missing numbers are out of the arr's range
        // last number in arr + (k - missing numbers before last number)
        // arr[arr.length - 1] + (k - (arr[arr.length - 1] - (arr.length - 1) - 1))
        // arr[arr.length - 1] + (k - (arr[arr.length - 1] - arr.length));
        return k + arr.length;
    }

    // O(logN)
    private int solution2(int[] arr, int k) {
        int start = 0;
        int end = arr.length - 1;

        while (start <= end) {
            int mid = start + (end - start) / 2;

            if (arr[mid] - mid - 1 < k) {
                start  = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        // We break when start == end, so at arr[start], there are at least k missing numbers before arr[start].
        // At index start - 1, we have arr[start-1] - (start-1) - 1 missing numbers
        // so after index start - 1 , we need to find k - (arr[start-1] - (start-1) - 1) missing numbers, i.e. k - arr[start-1] + start missing numbers
        // At index start - 1, our number is arr[start-1]. Add them up, the target number will be arr[start-1] + k - arr[start-1] + start, i.e. k + start;
        return start + k;
    }
}
