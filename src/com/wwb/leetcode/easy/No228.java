package com.wwb.leetcode.easy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Given a sorted integer array without duplicates, return the summary of its ranges.
 *
 * For example, given [0,1,2,4,5,7], return ["0->2","4->5","7"].
 */
public class No228 {

    public List<String> summaryRanges(int[] nums) {
        if(nums == null || nums.length == 0) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        int start = nums[0];
        int end = nums[0];

        for(int i = 1; i < nums.length; i++) {
            if(nums[i] - 1 > end) {
                putIntoResult(result, start, end);
                start = nums[i];
            }
            end = nums[i];
        }

        putIntoResult(result, start, end);

        return result;
    }

    private String buildRange(int start, int end) {
        return String.format("%s->%s", start, end);
    }

    private void putIntoResult(List<String> result, int start, int end) {
        if(end == start) {
            result.add(Integer.toString(end));
        } else {
            result.add(buildRange(start, end));
        }
    }
}