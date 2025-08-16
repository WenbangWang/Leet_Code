package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

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
        int pre = lower;

        for (int i = 0; i <= nums.length; i++) {
            int num;
            if (i == nums.length) {
                num = upper;
            } else {
                num = nums[i];
            }
            int lastPre = pre;
            pre = num;

            if (lastPre + 1 == num) {
                continue;
            }

            if (lastPre + 1 == num - 1) {
                result.add(Collections.singletonList(lastPre + 1));
                continue;
            }

            if (lastPre + 1 < num) {
                result.add(IntStream.range(lastPre + 1, num - 1).boxed().toList());
            }
        }

        return result;
    }
}
