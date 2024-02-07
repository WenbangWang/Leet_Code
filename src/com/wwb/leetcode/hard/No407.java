package com.wwb.leetcode.hard;

import java.util.PriorityQueue;

/**
 * Given an m x n integer matrix heightMap representing the height of each unit cell in a 2D elevation map,
 * return the volume of water it can trap after raining.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: heightMap = [[1,4,3,1,3,2],[3,2,1,3,2,4],[2,3,3,2,3,1]]
 * Output: 4
 * Explanation: After the rain, water is trapped between the blocks.
 * We have two small ponds 1 and 3 units trapped.
 * The total volume of water trapped is 4.
 * Example 2:
 *
 *
 * Input: heightMap = [[3,3,3,3,3],[3,2,2,2,3],[3,2,1,2,3],[3,2,2,2,3],[3,3,3,3,3]]
 * Output: 10
 *
 *
 * Constraints:
 *
 * m == heightMap.length
 * n == heightMap[i].length
 * 1 <= m, n <= 200
 * 0 <= heightMap[i][j] <= 2 * 10^4
 */
public class No407 {
    public int trapRainWater(int[][] heightMap) {
        int m = heightMap.length;
        int n = heightMap[0].length;

        PriorityQueue<Point> pq = new PriorityQueue<>();
        boolean[][] visited = new boolean[m][n];
        int[][] directions = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        int result = 0;

        for (int i = 0; i < m; i++) {
            pq.offer(new Point(i, 0, heightMap[i][0]));
            pq.offer(new Point(i, n - 1, heightMap[i][n - 1]));
            visited[i][0] = visited[i][n - 1] = true;
        }

        for (int i = 1; i < n - 1; i++) {
            pq.offer(new Point(0, i, heightMap[0][i]));
            pq.offer(new Point(m - 1, i, heightMap[m - 1][i]));
            visited[0][i] = visited[m - 1][i] = true;
        }

        while (!pq.isEmpty()) {
            Point p = pq.poll();

            for (int[] direction : directions) {
                int x = p.x + direction[0];
                int y = p.y + direction[1];

                if (x < 0 || x >= m || y < 0 || y >= n || visited[x][y]) {
                    continue;
                }

                result += Math.max(0, p.value - heightMap[x][y]);
                pq.offer(new Point(x, y, Math.max(heightMap[x][y], p.value)));
                visited[x][y] = true;
            }
        }

        return result;
    }

    private static class Point implements Comparable<Point> {
        int x;
        int y;
        int value;

        public Point(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        @Override
        public int compareTo(Point o) {
            return Integer.compare(this.value, o.value);
        }
    }
}
