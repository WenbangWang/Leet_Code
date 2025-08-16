package com.wwb.leetcode.medium;

/**
 * A conveyor belt has packages that must be shipped from one port to another within days days.
 * <p>
 * The ith package on the conveyor belt has a weight of weights[i]. Each day, we load the ship with packages on the conveyor belt (in the order given by weights). We may not load more weight than the maximum weight capacity of the ship.
 * <p>
 * Return the least weight capacity of the ship that will result in all the packages on the conveyor belt being shipped within days days.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: weights = [1,2,3,4,5,6,7,8,9,10], days = 5
 * Output: 15
 * Explanation: A ship capacity of 15 is the minimum to ship all the packages in 5 days like this:
 * 1st day: 1, 2, 3, 4, 5
 * 2nd day: 6, 7
 * 3rd day: 8
 * 4th day: 9
 * 5th day: 10
 *
 * Note that the cargo must be shipped in the order given, so using a ship of capacity 14 and splitting the packages into parts like (2, 3, 4, 5), (1, 6, 7), (8), (9), (10) is not allowed.
 * </pre>
 *
 * <pre>
 * Example 2:
 *
 * Input: weights = [3,2,2,4,1,4], days = 3
 * Output: 6
 * Explanation: A ship capacity of 6 is the minimum to ship all the packages in 3 days like this:
 * 1st day: 3, 2
 * 2nd day: 2, 4
 * 3rd day: 1, 4
 * </pre>
 *
 * <pre>
 * Example 3:
 *
 * Input: weights = [1,2,3,1,1], days = 4
 * Output: 3
 * Explanation:
 * 1st day: 1
 * 2nd day: 2
 * 3rd day: 3
 * 4th day: 1, 1
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * 1 <= days <= weights.length <= 5 * 10^4
 * 1 <= weights[i] <= 500
 * </pre>
 */
public class No1011 {
    public int shipWithinDays(int[] weights, int days) {
        int sum = 0;
        int max = Integer.MIN_VALUE;

        for (int weight : weights) {
            max = Math.max(max, weight);
            sum += weight;
        }

        int start = max;
        int end = sum;

        while (start < end) {
            int capacity = (end - start) / 2 + start;
            int currentDays = 1;
            int load = 0;

            for (int weight : weights) {
                if (load + weight > capacity) {
                    currentDays++;
                    load = 0;
                }
                load += weight;
            }

            if (currentDays > days) {
                start = capacity + 1;
            } else {
                end = capacity;
            }
        }

        return start;
    }
}
