package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.Map;

/**
 * Given n points on a 2D plane, find the maximum number of points that lie on the same straight line.
 */
public class No149 {

    public int maxPoints(int[][] points ) {
        if(points == null) {
            return 0;
        }

        if(points.length <= 2) {
            return points.length;
        }

        int result = 0;
        int length = points.length;

        for(int i = 0; i < length; i++) {
            int currentMax = 0;
            Map<String, Integer> map = new HashMap<>();

            for(int j = i + 1; j < length; j++) {
                int x = points[j][0] - points[i][0];
                int y = points[j][1] - points[i][1];

                int gcd = generateGCD(x, y);

                if(gcd != 0) {
                    x /= gcd;
                    y /= gcd;
                }

                String key = x + "/" + y;

                map.put(key, map.getOrDefault(key, 0) + 1);

                currentMax = Math.max(currentMax, map.get(key));
            }

            result = Math.max(result, currentMax + 1);
        }

        return result;
    }

    private int generateGCD(int a, int b) {
        if(b == 0) {
            return a;
        }

        return generateGCD(b, a % b);
    }

    // less preferable since double/float is not accurate enough
    // to be used to represent slope
    private int solution2(int[][] points) {
        if(points == null) {
            return 0;
        }

        if(points.length <= 2) {
            return points.length;
        }

        int result = 0;
        int length = points.length;

        for (int i = 0; i < length; i++) {
            int overlap = 0;
            int currentMax = 0;
            int vertical = 0;

            Map<Double, Integer> map = new HashMap<>();

            for (int j = i + 1; j < length; j++) {
                int x = points[j][0] - points[i][0];
                int y = points[j][1] - points[i][1];

                if (x == 0) {
                    if (y == 0) {
                        overlap++;
                    }

                    vertical++;
                    continue;
                }

                // it could produce -0.0 when x is negative and y is zero.
                // by adding a 0.0 would cast it back to 0.0.
                double slope = y * 1.0 / x + 0.0;

                map.put(slope, map.getOrDefault(slope, 0) + 1);

                currentMax = Math.max(currentMax, map.get(slope));
            }

            result = Math.max(result, Math.max(currentMax, vertical) + overlap + 1);
        }

        return result;
    }
}
