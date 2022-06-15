package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;

/**
 * Given an integer array nums, return the number of longest increasing subsequences.
 * <p>
 * Notice that the sequence has to be strictly increasing.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: nums = [1,3,5,4,7]
 * Output: 2
 * Explanation: The two longest increasing subsequences are [1, 3, 4, 7] and [1, 3, 5, 7].
 * Example 2:
 * <p>
 * Input: nums = [2,2,2,2,2]
 * Output: 5
 * Explanation: The length of longest continuous increasing subsequence is 1, and there are 5 subsequences' length is 1, so output 5.
 */
public class No673 {
    public int findNumberOfLIS(int[] nums) {
//        return solution1(nums);
        return solution2(nums);
    }

    // O(N^2)
    private int solution1(int[] nums) {
        if (nums.length == 0 || nums.length == 1) {
            return nums.length;
        }

        LongestIncreasingSubsequenceInfo[] dp = new LongestIncreasingSubsequenceInfo[nums.length];

        LongestIncreasingSubsequenceInfo result = new LongestIncreasingSubsequenceInfo();

        result.count = 0;
        result.length = Integer.MIN_VALUE;

        for (int i = 0; i < nums.length; i++) {
            dp[i] = new LongestIncreasingSubsequenceInfo();

            for (int j = 0; j < i; j++) {
                if (nums[i] > nums[j]) {
                    // If nums[i] could just contribute to
                    // the lis ends with nums[j],
                    // we should add up number of lis
                    // ends with nums[j] to nums[i].
                    if (dp[i].length == dp[j].length + 1) {
                        dp[i].count += dp[j].count;
                    } else if (dp[i].length < dp[j].length + 1) {
                        // If lis ends with nums[i] is not the longest
                        // we should use the result from nums[j]
                        dp[i].length = dp[j].length + 1;
                        dp[i].count = dp[j].count;
                    }
                }
            }

            if (dp[i].length == result.length) {
                result.count += dp[i].count;
            } else if (dp[i].length > result.length) {
                result = dp[i];
            }
        }

        return result.count;
    }

    private static class LongestIncreasingSubsequenceInfo {
        // length of longest increasing subsequence
        // at this point.
        int length = 1;
        // count of lis at this point.
        int count = 1;
    }

    // O(NlogN)
    // patience sort
    private int solution2(int[] nums) {
        if (nums.length == 0 || nums.length == 1) {
            return nums.length;
        }

        LongestIncreasingSubsequence lis = new LongestIncreasingSubsequence();

        for (int num : nums) {
            lis.append(num);
        }

        return lis.getNumberOfLongestIncreasingSubsequences();
    }

    private static class LongestIncreasingSubsequenceNode {
        // the actual value at this position.
        int value;
        // the sum length of lis for all the previous nodes
        // with value greater than or equal to this node.
        int sum;

        LongestIncreasingSubsequenceNode(int value) {
            this(value, 1);
        }

        LongestIncreasingSubsequenceNode(int value, int sum) {
            this.value = value;
            this.sum = sum;
        }

        @Override
        public String toString() {
            return "{" + value + ", " + sum + "}";
        }
    }

    // for an input array [1, 3, 2, 5, 4, 9, 6, 7]
    // the rows should look like
    // (first element is value, second element is sum)
    // [{1, 1}]
    // [{3, 1}, {2, 2}]
    // [{5, 2}, {4, 4}]
    // [{9, 4}, {6, 8}]
    // [{7, 4}]
    private static class LongestIncreasingSubsequence {
        // each row will be in non-ascending order
        // (non-strict descending)
        private List<List<LongestIncreasingSubsequenceNode>> rows;
        // represents the very last node of each row
        // consider the later nodes will always cover
        // the previous nodes in the same row.
        // [3, 2, 1] -> row where 1 is the surface.
        // this "surface" will be in ascending order.
        // the index represents each row.
        private List<LongestIncreasingSubsequenceNode> currentSurface;

        LongestIncreasingSubsequence() {
            this.rows = new ArrayList<>();
            this.currentSurface = new ArrayList<>();
        }

        void append(int value) {
            if (rows.isEmpty()) {
                LongestIncreasingSubsequenceNode node = new LongestIncreasingSubsequenceNode(value);
                List<LongestIncreasingSubsequenceNode> row = new ArrayList<>();
                row.add(node);

                rows.add(row);
                currentSurface.add(node);

                return;
            }

            // at this point, there should be an existing row
            // which the current "num" could be "naturally"
            // fit (not inserted).

            int rowIndex = 0;

            // find which row to append this value

            // if the new value is greater than the very last element of the
            // last row, we should create a new row
            if (value > currentSurface.get(currentSurface.size() - 1).value) {
                rows.add(new ArrayList<>());
                rowIndex = rows.size() - 1;
            } else {
                rowIndex = findRowIndexToAppend(value);
            }

            int sum = 1;

            // rowIndex could be ZERO when the "value" is smaller than
            // all the existing surface values, then this value "should" be
            // positioned as the very last element of the first row
            if (rowIndex > 0) {
                // it is possible that this value could be
                // positioned in the middle of the previous row
                // find which position in this row
                // the value should be inserted at and
                // calculate the "sum" at this position.

                List<LongestIncreasingSubsequenceNode> previousRow = rows.get(rowIndex - 1);
                int position = findPositionToInsert(value, previousRow);

                // the sum can be derived from prefix sum
                // where the sum of each node is an accumulated value
                // of sum from all the previous nodes.

                // this "value" is greater than or equal to the greatest value
                // in the previous row, hence we should use the very last
                // accumulated sum as the sum of this "value"
                if (position == 0) {
                    sum = previousRow.get(previousRow.size() - 1).sum;
                } else {
                    // if this "value" were to be inserted into the previous row
                    // the sum of this position should be the very last sum - the sum from previous position
                    sum = previousRow.get(previousRow.size() - 1).sum - previousRow.get(position - 1).sum;;
                }
            }

            List<LongestIncreasingSubsequenceNode> rowToAppend = rows.get(rowIndex);

            System.out.println(value);
            System.out.println(rowToAppend);

            // if the row to append is not empty, we should add up
            // the previous accumulated sum to the current sum.
            if (!rowToAppend.isEmpty()) {
                sum += rowToAppend.get(rowToAppend.size() - 1).sum;
            }

            LongestIncreasingSubsequenceNode node = new LongestIncreasingSubsequenceNode(value, sum);
            rowToAppend.add(node);

            // replace the surface of the row with the newly created node
            // if we don't need to create a new row.
            if (currentSurface.size() == rows.size()) {
                currentSurface.set(rowIndex, node);
            } else {
                currentSurface.add(node);
            }
        }

        int getNumberOfLongestIncreasingSubsequences() {
            return currentSurface.get(currentSurface.size() - 1).sum;
        }

        // in the collection of last element in each row
        // find the very first element greater than
        // or equal to the input "value".
        // the collection is in ascending order
        private int findRowIndexToAppend(int value) {
            int start = 0;
            int end = currentSurface.size() - 1;

            while (start < end) {
                int mid = (end - start) / 2 + start;

                if (value > currentSurface.get(mid).value) {
                    start = mid + 1;
                } else {
                    end = mid;
                }
            }

            return start;
        }

        // assuming the "row" is in non-ascending order.
        // the return value should be the index to
        // insert this "value"
        private int findPositionToInsert(int value, List<LongestIncreasingSubsequenceNode> row) {
            int start = 0;
            int end = row.size() - 1;

            while(start < end) {
                int mid = (end - start) / 2 + start;

                if (value <= row.get(mid).value) {
                    start = mid + 1;
                } else {
                    end = mid;
                }
            }

            return start;
        }
    }
}
