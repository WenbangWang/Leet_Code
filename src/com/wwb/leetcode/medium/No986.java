package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;

/**
 * You are given two lists of closed intervals, firstList and secondList, where firstList[i] = [starti, endi] and secondList[j] = [startj, endj]. Each list of intervals is pairwise disjoint and in sorted order.
 * <p>
 * Return the intersection of these two interval lists.
 * <p>
 * A closed interval [a, b] (with a <= b) denotes the set of real numbers x with a <= x <= b.
 * <p>
 * The intersection of two closed intervals is a set of real numbers that are either empty or represented as a closed interval. For example, the intersection of [1, 3] and [2, 4] is [2, 3].
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <img src="../doc-files/986.png" />
 * <p>
 * Input: firstList = [[0,2],[5,10],[13,23],[24,25]], secondList = [[1,5],[8,12],[15,24],[25,26]]
 * <p>
 * Output: [[1,2],[5,5],[8,10],[15,23],[24,24],[25,25]]
 * <p>
 * Example 2:
 * <p>
 * Input: firstList = [[1,3],[5,9]], secondList = []
 * <p>
 * Output: []
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 0 <= firstList.length, secondList.length <= 1000
 * <p>
 * firstList.length + secondList.length >= 1
 * <p>
 * 0 <= starti < endi <= 10^9
 * <p>
 * endi < starti+1
 * <p>
 * 0 <= startj < endj <= 10^9
 * <p>
 * endj < startj+1
 */
public class No986 {
    public int[][] intervalIntersection(int[][] firstList, int[][] secondList) {
        List<int[]> result = new ArrayList<>();

        int first = 0;
        int second = 0;

        while (first < firstList.length && second < secondList.length) {
            int[] firstPair = firstList[first];
            int[] secondPair = secondList[second];

            if (firstPair[1] < secondPair[0]) {
                first++;
                continue;
            }

            if (secondPair[1] < firstPair[0]) {
                second++;
                continue;
            }

            int start = Math.max(firstPair[0], secondPair[0]);
            int end = Math.min(firstPair[1], secondPair[1]);

            result.add(new int[]{start, end});

            if (end == firstPair[1]) {
                first++;
            } else {
                second++;
            }
        }

        return result.toArray(new int[0][2]);
    }
}
