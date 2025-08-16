package com.wwb.leetcode.medium;

/**
 * Koko loves to eat bananas. There are n piles of bananas, the ith pile has piles[i] bananas. The guards have gone and will come back in h hours.
 * <p>
 * Koko can decide her bananas-per-hour eating speed of k. Each hour, she chooses some pile of bananas and eats k bananas from that pile. If the pile has less than k bananas, she eats all of them instead and will not eat any more bananas during this hour.
 * <p>
 * Koko likes to eat slowly but still wants to finish eating all the bananas before the guards return.
 * <p>
 * Return the minimum integer k such that she can eat all the bananas within h hours.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: piles = [3,6,7,11], h = 8
 * Output: 4
 * Example 2:
 *
 * Input: piles = [30,11,23,4,20], h = 5
 * Output: 30
 * Example 3:
 *
 * Input: piles = [30,11,23,4,20], h = 6
 * Output: 23
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * 1 <= piles.length <= 10^4
 * piles.length <= h <= 10^9
 * 1 <= piles[i] <= 10^9
 * </pre>
 */
public class No875 {
    public int minEatingSpeed(int[] piles, int h) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        for (int pile : piles) {
            max = Math.max(max, pile);
            min = Math.min(min, pile);
        }

        int start = min;
        int end = max;

        while (start < end) {
            int speed = (end - start) / 2 + start;
            int hourTaken = 0;

            for (int pile : piles) {
                hourTaken += (pile - 1) / speed + 1;
            }

            if (hourTaken > h) {
                start = speed + 1;
            } else {
                end = speed - 1;
            }
        }

        return start;
    }
}
