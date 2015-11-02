package com.wwb.leetcode.tags.math;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Given n points on a 2D plane, find the maximum number of points that lie on the same straight line.
 */
public class No149 {

    public int maxPoints(Point[] points) {
        if(points == null) {
            return 0;
        }

        if(points.length <= 2) {
            return points.length;
        }

        int result = 0;
        Map<Long, Integer> map = new HashMap<>();
        int length = points.length;

        for(int i = 0; i < length; i++) {
            int overlap = 0;
            int currentMax = 0;
            map.clear();

            for(int j = i + 1; j < length; j++) {
                int x = points[j].x - points[i].x;
                int y = points[j].y - points[i].y;

                if(x == 0 && y == 0) {
                    overlap++;
                    continue;
                }

                int gcd = generateGCD(x, y);

                if(gcd != 0) {
                    x /= gcd;
                    y /= gcd;
                }

                long key = ((long) x << 32) | y;

                if(map.containsKey(key)) {
                    map.put(key, map.get(key) + 1);
                } else {
                    map.put(key, 1);
                }

                currentMax = Math.max(currentMax, map.get(key));
            }

            result = Math.max(result, currentMax + overlap + 1);
        }

        return result;
    }

    private int generateGCD(int a, int b) {
        if(b == 0) {
            return a;
        }

        return generateGCD(b, a % b);
    }
}
