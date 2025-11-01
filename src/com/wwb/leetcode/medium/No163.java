package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

/**
 * Given a sorted integer array nums, where the range of elements are in the inclusive range [lower, upper], return its missing ranges.
 *
 * <pre>
 * Example:
 *
 * Input: nums = [0, 1, 3, 50, 75], lower = 0 and upper = 99,
 * Output: ["2", "4->49", "51->74", "76->99"]
 * </pre>
 */
public class No163 {
    public List<List<Integer>> findMissingRanges(int[] nums, int lower, int upper) {
        List<List<Integer>> result = new ArrayList<>();
        long pre = lower - 1;

        for (int i = 0; i <= nums.length; i++) {
            long num;
            if (i == nums.length) {
                num = upper + 1;
            } else {
                num = nums[i];
            }

            if (pre + 1 < num) {
                result.add(LongStream.range(pre + 1, num - 1).mapToInt(l -> (int) l).boxed().toList());
            }
            pre = num;
        }

        return result;
    }
}
