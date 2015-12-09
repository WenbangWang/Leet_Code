package com.wwb.leetcode.tags.dc;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A city's skyline is the outer contour of the silhouette formed by all the buildings in that city
 * when viewed from a distance.
 * Now suppose you are given the locations and height of all the buildings
 * as shown on a cityscape photo (Figure A), write a program to output the skyline
 * formed by these buildings collectively (Figure B).
 *
 *  Buildings  Skyline Contour
 * The geometric information of each building is represented by a triplet of integers [Li, Ri, Hi],
 * where Li and Ri are the x coordinates of the left and right edge of the ith building,
 * respectively, and Hi is its height.
 * It is guaranteed that 0 ≤ Li, Ri ≤ INT_MAX, 0 < Hi ≤ INT_MAX, and Ri - Li > 0.
 * You may assume all buildings are perfect rectangles grounded on an absolutely flat surface at height 0.
 *
 *  For instance, the dimensions of all buildings in Figure A are recorded as:
 * [ [2 9 10], [3 7 15], [5 12 12], [15 20 10], [19 24 8] ] .
 *
 * The output is a list of "key points" (red dots in Figure B) in the format of
 * [ [x1,y1], [x2, y2], [x3, y3], ... ] that uniquely defines a skyline.
 * A key point is the left endpoint of a horizontal line segment.
 * Note that the last key point, where the rightmost building ends,
 * is merely used to mark the termination of the skyline, and always has zero height.
 * Also, the ground in between any two adjacent buildings should be considered part of the skyline contour.
 *
 *  For instance, the skyline in Figure B should be represented as:
 * [ [2 10], [3 15], [7 12], [12 0], [15 10], [20 8], [24, 0] ].
 *
 *  Notes:
 *
 *  The number of buildings in any input list is guaranteed to be in the range [0, 10000].
 * The input list is already sorted in ascending order by the left x position Li.
 * The output list must be sorted by the x position.
 * There must be no consecutive horizontal lines of equal height in the output skyline.
 * For instance, [...[2 3], [4 5], [7 5], [11 5], [12 7]...] is not acceptable;
 * the three lines of height 5 should be merged into one in the final output as such:
 * [...[2 3], [4 5], [12 7], ...]
 */
public class No218 {

    public List<int[]> getSkyline(int[][] buildings) {
        if(buildings == null || buildings.length == 0 || buildings[0] == null || buildings[0].length == 0) {
            return Collections.emptyList();
        }

        return merge(buildings, 0, buildings.length - 1);
    }

    private LinkedList<int[]> merge(int[][] buildings, int start, int end) {
        if(start < end) {
            int mid = start + (end - start) / 2;
            return merge(merge(buildings, start, mid), merge(buildings, mid + 1, end));
        } else {
            LinkedList<int[]> result = new LinkedList<>();
            result.add(new int[] {buildings[start][0], buildings[start][2]});
            result.add(new int[] {buildings[start][1], 0});

            return result;
        }
    }

    private LinkedList<int[]> merge(LinkedList<int[]> firstHalf, LinkedList<int[]> secondHalf) {
        LinkedList<int[]> result = new LinkedList<>();
        int height1 = 0;
        int height2 = 0;
        while(!firstHalf.isEmpty() && !secondHalf.isEmpty()) {
            int x = 0;
            int height = 0;
            int[] firstHalfElement = firstHalf.getFirst();
            int[] secondHalfElement = secondHalf.getFirst();

            if(firstHalfElement[0] < secondHalfElement[0]) {
                x = firstHalfElement[0];
                height1 = firstHalfElement[1];
                height = Math.max(height1, height2);
                firstHalf.removeFirst();
            } else if(secondHalfElement[0] < firstHalfElement[0]) {
                x = secondHalfElement[0];
                height2 = secondHalfElement[1];
                height = Math.max(height1, height2);
                secondHalf.removeFirst();
            } else {
                x = firstHalfElement[0];
                height1 = firstHalfElement[1];
                height2 = secondHalfElement[1];
                height = Math.max(height1, height2);
                firstHalf.removeFirst();
                secondHalf.removeFirst();
            }

            if(result.isEmpty() || height != result.getLast()[1]) {
                result.add(new int[]{x, height});
            }
        }

        result.addAll(firstHalf);
        result.addAll(secondHalf);

        return result;
    }
}
