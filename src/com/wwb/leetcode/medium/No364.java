package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.NestedInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * You are given a nested list of integers nestedList. Each element is either an integer or a list whose elements may also be integers or other lists.
 * <p>
 * The depth of an integer is the number of lists that it is inside of. For example, the nested list [1,[2,2],[[3],2],1] has each integer's value set to its depth. Let maxDepth be the maximum depth of any integer.
 * <p>
 * The weight of an integer is maxDepth - (the depth of the integer) + 1.
 * <p>
 * Return the sum of each integer in nestedList multiplied by its weight.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: nestedList = [[1,1],2,[1,1]]
 * Output: 8
 * Explanation: Four 1's with a weight of 1, one 2 with a weight of 2.
 * 1*1 + 1*1 + 2*2 + 1*1 + 1*1 = 8
 * Example 2:
 * <p>
 * <p>
 * Input: nestedList = [1,[4,[6]]]
 * Output: 17
 * Explanation: One 1 at depth 3, one 4 at depth 2, and one 6 at depth 1.
 * 1*3 + 4*2 + 6*1 = 17
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= nestedList.length <= 50
 * The values of the integers in the nested list is in the range [-100, 100].
 * The maximum depth of any integer is less than or equal to 50.
 */
public class No364 {
    public int depthSumInverse(List<NestedInteger> nestedList) {
        MaxDepth maxDepth = new MaxDepth();
        int depth = 1;
        int result = 0;

        var flattenedList = flattenList(nestedList, maxDepth, depth);

        for (var weightedInteger : flattenedList) {
            result += weightedInteger.getWeight() * weightedInteger.value;
        }

        return result;
    }

    private List<WeightedInteger> flattenList(List<NestedInteger> nestedList, MaxDepth maxDepth, int depth) {
        List<WeightedInteger> result = new ArrayList<>();

        for (NestedInteger nestedInteger : nestedList) {
            if (nestedInteger.isInteger()) {
                result.add(new WeightedInteger(nestedInteger.getInteger(), depth, maxDepth));
            } else {
                result.addAll(flattenList(nestedInteger.getList(), maxDepth, depth + 1));
            }
        }

        maxDepth.value = Math.max(maxDepth.value, depth);

        return result;
    }

    private static class MaxDepth {
        int value = Integer.MIN_VALUE;
    }

    private static class WeightedInteger {
        int value;
        int depth;
        MaxDepth maxDepth;

        WeightedInteger(int value, int depth, MaxDepth maxDepth) {
            this.value = value;
            this.depth = depth;
            this.maxDepth = maxDepth;
        }

        int getWeight() {
            return this.maxDepth.value - depth + 1;
        }
    }
}
