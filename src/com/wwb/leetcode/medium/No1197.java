package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * In an infinite chess board with coordinates from -infinity to +infinity, you have a knight at square [0, 0].
 * <p>
 * A knight has 8 possible moves it can make, as illustrated below.
 * Each move is two squares in a cardinal direction, then one square in an orthogonal direction.
 * <p>
 * <p>
 * Return the minimum number of steps needed to move the knight to the square [x, y]. It is guaranteed the answer exists.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: x = 2, y = 1
 * Output: 1
 * Explanation: [0, 0] → [2, 1]
 * Example 2:
 * <p>
 * Input: x = 5, y = 5
 * Output: 4
 * Explanation: [0, 0] → [2, 1] → [4, 2] → [3, 4] → [5, 5]
 * <p>
 * <p>
 * Constraints:
 * <p>
 * -300 <= x, y <= 300
 * 0 <= |x| + |y| <= 300
 */
public class No1197 {

    public int minKnightMoves(int x, int y) {
        Map<Pair, Integer> map = new HashMap<>();

        return dfs(Math.abs(x), Math.abs(y), map);
    }

    private int dfs(int x, int y, Map<Pair, Integer> map) {
        Pair pair = new Pair(x, y);

        if (map.containsKey(pair)) {
            return map.get(pair);
        }

        if (x + y == 0) {
            return 0;
        }

        if (x + y == 2) {
            return 2;
        }

        int result = Math.min(
                dfs(Math.abs(x - 1), Math.abs(y - 2), map),
                dfs(Math.abs(x - 2), Math.abs(y - 1), map)
        ) + 1;
        map.put(pair, result);

        return result;
    }

    private static class Pair {
        int x;
        int y;

        Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair pair)) return false;

            return x == pair.x &&
                    y == pair.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
