package com.wwb.leetcode.medium;

/**
 * Given a binary array, find the maximum number of consecutive 1s in this array if you can flip at most one 0.
 *
 * <pre>
 * Example 1:
 *
 * Input: [1,0,1,1,0]
 * Output: 4
 * Explanation: Flip the first zero will get the the maximum number of consecutive 1s.
 *     After flipping, the maximum number of consecutive 1s is 4.
 * Note:
 *
 * The input array will only contain 0 and 1.
 * The length of input array is a positive integer and will not exceed 10,000
 * </pre>
 * Follow up:
 * <p>
 * What if the input numbers come in one by one as an infinite stream? In other words, you can't store all numbers coming from the stream as it's too large to hold in memory. Could you solve it efficiently?
 */
public class No487 {
    public int findMaxConsecutiveOnes(int[] nums) {
        int start = 0;
        int count = 1;  // number of zeros we can flip
        int maxLen = 0;

        for (int end = 0; end < nums.length; end++) {
            if (nums[end] == 0) {
                count--;  // use a flip
            }

            if (count < 0) {  // window invalid, shrink from left
                if (nums[start] == 0) {
                    count++;  // recover a flip
                }
                start++;
            }

            // update maxLen after ensuring the window is valid
            maxLen = Math.max(maxLen, end - start + 1);
        }

        return maxLen;
    }
}
