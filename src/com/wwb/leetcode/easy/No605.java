package com.wwb.leetcode.easy;

/**
 * You have a long flowerbed in which some of the plots are planted, and some are not. However, flowers cannot be planted in adjacent plots.
 *
 * Given an integer array flowerbed containing 0's and 1's, where 0 means empty and 1 means not empty, and an integer n, return if n new flowers can be planted in the flowerbed without violating the no-adjacent-flowers rule.
 *
 *
 *
 * Example 1:
 *
 * Input: flowerbed = [1,0,0,0,1], n = 1
 * Output: true
 * Example 2:
 *
 * Input: flowerbed = [1,0,0,0,1], n = 2
 * Output: false
 *
 *
 * Constraints:
 *
 * 1 <= flowerbed.length <= 2 * 10^4
 * flowerbed[i] is 0 or 1.
 * There are no two adjacent flowers in flowerbed.
 * 0 <= n <= flowerbed.length
 */
public class No605 {
    public boolean canPlaceFlowers(int[] flowerbed, int n) {
        if (n == 0) {
            return true;
        }

        int i = 0;

        while (i < flowerbed.length) {
            if (flowerbed[i] == 1) {
                i++;
                continue;
            }

            var consecutiveEmptySlots = countConsecutiveEmptySlots(flowerbed, i);
            var constant = 0;

            // add an additional empty slot when we are at the beginning
            // [0, 0, 1]
            // [0, 0, 0, 1]
            if (i == 0) {
                constant++;
            }

            // add an additional empty slot when we are at the end
            // [1, 0, 0]
            // [1, 0, 0, 0]
            if (i + consecutiveEmptySlots == flowerbed.length) {
                constant++;
            }

            // the above also works when
            // [0, 0, 0]

            // when empty slots are surrounded by flowers (1)
            // 1 -> 0
            // 2 -> 0
            // 3 -> 1
            // 4 -> 1
            // 5 -> 2
            // 6 -> 2
            n -= (consecutiveEmptySlots + constant - 1) / 2;

            if (n <= 0) {
                return true;
            }

            i += consecutiveEmptySlots;
        }

        return false;
    }

    private int countConsecutiveEmptySlots(int[] flowerBed, int i) {
        int result = 0;

        for (; i < flowerBed.length; i++) {
            if (flowerBed[i] == 0) {
                result++;
            } else {
                return result;
            }
        }

        return result;
    }
}
