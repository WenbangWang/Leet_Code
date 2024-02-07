package com.wwb.leetcode.easy;


import java.util.Arrays;

/**
 * You are assigned to put some amount of boxes onto one truck. You are given a 2D array boxTypes,
 * where boxTypes[i] = [numberOfBoxes[i], numberOfUnitsPerBox[i]]:
 *
 * numberOfBoxes[i] is the number of boxes of type i.
 * numberOfUnitsPerBox[i] is the number of units in each box of the type i.
 * You are also given an integer truckSize, which is the maximum number of boxes that can be put on the truck.
 * You can choose any boxes to put on the truck as long as the number of boxes does not exceed truckSize.
 *
 * Return the maximum total number of units that can be put on the truck.
 *
 *
 *
 * Example 1:
 *
 * Input: boxTypes = [[1,3],[2,2],[3,1]], truckSize = 4
 * Output: 8
 * Explanation: There are:
 * - 1 box of the first type that contains 3 units.
 * - 2 boxes of the second type that contain 2 units each.
 * - 3 boxes of the third type that contain 1 unit each.
 * You can take all the boxes of the first and second types, and one box of the third type.
 * The total number of units will be = (1 * 3) + (2 * 2) + (1 * 1) = 8.
 * Example 2:
 *
 * Input: boxTypes = [[5,10],[2,5],[4,7],[3,9]], truckSize = 10
 * Output: 91
 *
 *
 * Constraints:
 *
 * 1 <= boxTypes.length <= 1000
 * 1 <= numberOfBoxes[i], numberOfUnitsPerBox[i] <= 1000
 * 1 <= truckSize <= 10^6
 */
public class No1710 {
    public int maximumUnits(int[][] boxTypes, int truckSize) {
        return solution1(boxTypes, truckSize);
    }

    // O(nlogn) for sorting
    private int solution1(int[][] boxTypes, int truckSize) {
        Arrays.sort(boxTypes, (a, b) -> b[1] - a[1]);

        int unitCount = 0;

        for (int[] boxType : boxTypes) {
            int boxCount = Math.min(truckSize, boxType[0]);
            unitCount += boxCount * boxType[1];
            truckSize -= boxCount;

            if (truckSize == 0)
                break;
        }

        return unitCount;
    }

    // bucket sort. O(n)
    private int solution2(int[][] boxTypes, int truckSize) {
        // given the constraints that the maximum of units per box is 1000
        // hence we create buckets which the index represent the number of units per box
        // and the value represent the count of boxes with such container size.
        int[] buckets = new int[1001];

        for (int[] boxType : boxTypes) {
            int unit = boxType[1];
            int count = boxType[0];

            buckets[unit] += count;
        }

        int unit = 0;

        for (int i = 1000; i >= 0; i--) {
            if (buckets[i] == 0) {
                continue;
            }

            int count = Math.min(truckSize, buckets[i]);
            unit += count * i;
            truckSize -= count;

            if (truckSize == 0) {
                break;
            }
        }

        return unit;
    }
}
