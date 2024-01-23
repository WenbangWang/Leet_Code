package com.wwb.leetcode.tags.array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * There are several squares being dropped onto the X-axis of a 2D plane.
 *
 * You are given a 2D integer array positions where positions[i] = [lefti, sideLengthi]
 * represents the ith square with a side length of sideLengthi
 * that is dropped with its left edge aligned with X-coordinate lefti.
 *
 * Each square is dropped one at a time from a height above any landed squares.
 * It then falls downward (negative Y direction) until it either lands on the top side of another square or on the X-axis.
 * A square brushing the left/right side of another square does not count as landing on it.
 * Once it lands, it freezes in place and cannot be moved.
 *
 * After each square is dropped, you must record the height of the current tallest stack of squares.
 *
 * Return an integer array ans where ans[i] represents the height described above after dropping the ith square.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: positions = [[1,2],[2,3],[6,1]]
 * Output: [2,5,5]
 * Explanation:
 * After the first drop, the tallest stack is square 1 with a height of 2.
 * After the second drop, the tallest stack is squares 1 and 2 with a height of 5.
 * After the third drop, the tallest stack is still squares 1 and 2 with a height of 5.
 * Thus, we return an answer of [2, 5, 5].
 * Example 2:
 *
 * Input: positions = [[100,100],[200,100]]
 * Output: [100,100]
 * Explanation:
 * After the first drop, the tallest stack is square 1 with a height of 100.
 * After the second drop, the tallest stack is either square 1 or square 2, both with heights of 100.
 * Thus, we return an answer of [100, 100].
 * Note that square 2 only brushes the right side of square 1, which does not count as landing on it.
 *
 *
 * Constraints:
 *
 * 1 <= positions.length <= 1000
 * 1 <= lefti <= 10^8
 * 1 <= sideLengthi <= 10^6
 */
public class No699 {
    public List<Integer> fallingSquares(int[][] positions) {
        List<Integer> result = new ArrayList<>();
        Map<Integer, Integer> index = coordinateCompression(positions);
        SegmentTree tree = new SegmentTree(index.size());
        int best = Integer.MIN_VALUE;


        for (int[] position : positions) {
            int start = index.get(position[0]);
            // -1 to avoid case like
            // "A square brushing the left/right side of another square does not count as landing on it"
            int end = index.get(position[0] + position[1] - 1);

            int height = tree.query(start, end) + position[1];
            tree.update(height, start, end);

            best = Math.max(best, height);
            result.add(best);
        }

        return result;
    }

    private Map<Integer, Integer> coordinateCompression(int[][] positions) {
        Set<Integer> coordinates = new TreeSet<>();
        for (int[] position: positions) {
            coordinates.add(position[0]);
            coordinates.add(position[0] + position[1] - 1);
        }

        Map<Integer, Integer> index = new HashMap<>();

        int i = 0;
        for (int coordinate: coordinates) {
            index.put(coordinate, i++);
        }

        return index;
    }

    private static class SegmentTree {
        private Node root;

        SegmentTree(int n) {
            this.root = this.buildTree(0, n - 1);
        }

        int query(int start, int end) {
            return this.query(this.root, start, end);
        }

        void update(int value, int start, int end) {
            this.update(this.root, value, start, end);
        }

        private Node buildTree(int start, int end) {
            if(start > end) {
                return null;
            }

            Node node = new Node(start, end);

            if (start != end) {
                int mid = start + (end - start) / 2;

                node.left = this.buildTree(start, mid);
                node.right = this.buildTree(mid + 1, end);
            }

            return node;
        }


        private int query(Node node, int start, int end) {
            if (node.lazy != 0) {
                execute(node, node.lazy);
                node.lazy = 0;
            }

            if (node.start > end || node.end < start) {
                return 0;
            }

            // since node value is the current max along the path from parent to child
            // so if node's range is within start and end, it means we already found the
            // current max for the range queries (start, end)
            if (node.start >= start && node.end <= end) {
                return node.value;
            }

            return Math.max(this.query(node.left, start, end), this.query(node.right, start, end));
        }

        // update the node value (max) as long as start and end is within node's range
        private void update(Node node, int value, int start, int end) {
            if (node.lazy != 0) {
                execute(node, node.lazy);
                node.lazy = 0;
            }

            if (node.start > end || node.end < start) {
                return;
            }

            if (node.start >= start && node.end <= end) {
                execute(node, value);
                return;
            }

            this.update(node.left, value, start, end);
            this.update(node.right, value, start, end);

            node.value = Math.max(node.left.value, node.right.value);
        }

        private void execute(Node node, int value) {
            node.value = Math.max(node.value, value);

            // not leaf
            if (node.start != node.end) {
                node.left.lazy = Math.max(node.left.lazy, value);
                node.right.lazy = Math.max(node.right.lazy, value);
            }
        }
    }

    private static class Node {
        int start;
        int end;
        int value;
        int lazy;
        Node left;
        Node right;

        Node(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
