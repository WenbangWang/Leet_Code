package com.wwb.leetcode.medium;

/**
 * Given a sorted array A of unique numbers, find the K-th missing number starting from the leftmost number of the array.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: A = [4,7,9,10], K = 1
 * Output: 5
 * Explanation:
 * The first missing number is 5.
 * Example 2:
 *
 * Input: A = [4,7,9,10], K = 3
 * Output: 8
 * Explanation:
 * The missing numbers are [5,6,8,...], hence the third missing number is 8.
 * Example 3:
 *
 * Input: A = [1,2,4], K = 3
 * Output: 6
 * Explanation:
 * The missing numbers are [3,5,6,7,...], hence the third missing number is 6.
 *
 *
 * Note:
 *
 * 1 <= A.length <= 50000
 * 1 <= A[i] <= 1e7
 * 1 <= K <= 1e8
 * </pre>
 */
public class No1060 {
    public int missingElement(int[] nums, int k) {
        return solution1(nums, k);
    }

    private int solution1(int[] nums, int k) {
        for (int i = 0; i < nums.length; i++) {
            int missingNumbers = missingNumbers(nums, i);

            if (missingNumbers >= k) {
                // missing numbers at i - 1
                // nums[i - 1] - (i - 1) - nums[0]
                // nums[i - 1] + (k - (nums[i - 1] - (i - 1) - nums[0]))
                // nums[i - 1] + k - nums[i - 1] + i - 1 + nums[0]
                return nums[i - 1] + k - missingNumbers;
            }
        }

        // nums[nums.length - 1] + k - nums[nums.length - 1] + nums.length - 1 + nums[0];
        return nums[nums.length - 1] + k - missingNumbers(nums, nums.length - 1);
    }

    private int solution2(int[] nums, int k) {
        int n = nums.length;

        int lastMissingNumbers = missingNumbers(nums, n - 1);
        if (k >= lastMissingNumbers) {
            return nums[nums.length - 1] + k - lastMissingNumbers;
        }

        int start = 0;
        int end = n - 1;

        while (start <= end) {
            int mid = (end - start) / 2 + start;

            if (missingNumbers(nums, mid) < k) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }

        return nums[start - 1] + k -  missingNumbers(nums, start - 1);
    }

    private int missingNumbers(int[] nums, int i) {
        return nums[i] - i - nums[0];
    }
}
