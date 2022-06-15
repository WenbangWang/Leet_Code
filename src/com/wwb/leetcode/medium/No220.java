package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Given an array of integers, find out whether there are two distinct indices i and j in the array
 * such that the difference between nums[i] and nums[j] is at most t and the difference between i and j is at most k.
 */
public class No220 {

    public boolean containsNearbyAlmostDuplicate(int[] nums, int k, int t) {
//        return solution1(nums, k, t);
        return solution2(nums, k, t);
    }

    // O(NlogK)
    private boolean solution1(int[] nums, int k, int t) {
        if(nums == null || nums.length < 2 || k < 1 || t < 0) {
            return false;
        }
        TreeSet<Integer> tree = new TreeSet<>();

        for(int i = 0; i < nums.length; i++) {
            int num = nums[i];
            Integer floor = tree.floor(num + t);
            Integer ceiling = tree.ceiling(num - t);

            if((floor != null && floor >= num) || (ceiling != null && ceiling <= num)) {
                return true;
            }

            tree.add(num);

            if(i >= k) {
                tree.remove(nums[i - k]);
            }
        }

        return false;
    }

    // Split nums into t + 1 buckets meaning that
    // each bucket would have numbers whose max difference
    // is t. We also need to check bucket - 1 and bucket + 1
    // for numbers just across the boundary.
    // O(N)
    private boolean solution2(int[] nums, int k, int t) {
        if(nums == null || nums.length < 2 || k < 1 || t < 0) {
            return false;
        }

        Map<Integer, Integer> buckets = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
            int bucketIndex = calculateBucketIndex(nums[i], t);

            // nums[i] in the current bucket
            // meaning that there's already a number
            // whose difference from nums[i] is within t.
            if (buckets.containsKey(bucketIndex)) {
                return true;
            }

            // There's a number in last bucket
            // (smaller than number in current bucket)
            // which could meet the criteria.
            if (buckets.containsKey(bucketIndex - 1)
                    && (long) nums[i] - buckets.get(bucketIndex - 1)  <= t) {
                return true;
            }

            // There's a number in next bucket
            // (bigger than number in current bucket)
            // which could meet the criteria.
            if (buckets.containsKey(bucketIndex + 1)
                    && (long) buckets.get(bucketIndex + 1) - nums[i] <= t) {
                return true;
            }

            if (buckets.size() >= k) {
                buckets.remove(calculateBucketIndex(nums[i - k], t));
            }

            // we won't override bucketIndex
            // since we are checking whether the bucketIndex
            // in the buckets or not at the beginning of the loop
            // and return immediately if found.
            buckets.put(bucketIndex, nums[i]);
        }

        return false;
    }

    private int calculateBucketIndex(int num, int t) {
        // Even when t = MAX INT, the division should
        // still give us an integer rather than a long
        // since any integer / MAX INT + 1 should be 0
        int bucketIndex = (int) (num / ((long) t + 1));

        // When num is negative, we want to offset
        // the bucket index by 1.
        // For example, when -t <=num <= 0,
        // num / t + 1 would be in the bucket of 0
        // but we want them to be in bucket -1.
        if (num < 0) {
            bucketIndex--;
        }

        return bucketIndex;
    }
}
