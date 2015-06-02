package com.wwb.leetcode.tags.binarysearch;

/**
 * Given an array of n positive integers and a positive integer s,
 * find the minimal length of a subarray of which the sum ≥ s. If there isn't one, return 0 instead.
 *
 * For example, given the array [2,3,1,2,4,3] and s = 7,
 * the subarray [4,3] has the minimal length under the problem constraint.
 */
public class No209 {

    public int minSubArrayLen(int s, int[] nums) {
        // return solution1(s, nums);
        return solution2(s, nums);
    }

    private int solution1(int s, int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }

        int sum = nums[0];
        int minCount = Integer.MAX_VALUE;
        int start = 0;
        int end = 0;

        while(end < nums.length && start <= end) {
            if(sum < s) {
                end++;
                if(end < nums.length) {
                    sum += nums[end];
                }
            } else {
                minCount = Math.min(end - start + 1, minCount);
                sum -= nums[start];
                start++;
            }
        }

        return minCount == Integer.MAX_VALUE ? 0 : minCount;
    }

    private int solution2(int s, int[] nums) {
        int[] sums = new int[nums.length + 1];
        for (int i = 1; i < sums.length; i++) {
            sums[i] = sums[i - 1] + nums[i - 1];
        }
        int minCount = Integer.MAX_VALUE;
        for (int i = 0; i < sums.length; i++) {
            int end = binarySearch(i + 1, sums.length - 1, sums[i] + s, sums);
            if (end == sums.length) {
                break;
            }
            if (end - i < minCount) {
                minCount = end - i;
            }
        }
        return minCount == Integer.MAX_VALUE ? 0 : minCount;
    }

    private int binarySearch(int start, int end, int key, int[] sums) {
        while (start <= end) {
            int mid = (start + end) / 2;
            if (sums[mid] >= key) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }
        return start;
    }
}