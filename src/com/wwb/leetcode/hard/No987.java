package com.wwb.leetcode.hard;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;

/**
 * Given the root of a binary tree, calculate the vertical order traversal of the binary tree.
 * <p>
 * For each node at position (row, col), its left and right children will be at positions (row + 1, col - 1) and (row + 1, col + 1) respectively. The root of the tree is at (0, 0).
 * <p>
 * The vertical order traversal of a binary tree is a list of top-to-bottom orderings for each column index starting from the leftmost column and ending on the rightmost column. There may be multiple nodes in the same row and same column. In such a case, sort these nodes by their values.
 * <p>
 * Return the vertical order traversal of the binary tree.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <img src="../doc-files/987_1.jpg" />
 * <p>
 * Input: root = [3,9,20,null,null,15,7]
 * <p>
 * Output: [[9],[3,15],[20],[7]]
 * <p>
 * Explanation:
 * <p>
 * Column -1: Only node 9 is in this column.
 * <p>
 * Column 0: Nodes 3 and 15 are in this column in that order from top to bottom.
 * <p>
 * Column 1: Only node 20 is in this column.
 * <p>
 * Column 2: Only node 7 is in this column.
 * <p>
 * Example 2:
 * <p>
 * <img src="../doc-files/987_2.jpg" />
 * <p>
 * Input: root = [1,2,3,4,5,6,7]
 * <p>
 * Output: [[4],[2],[1,5,6],[3],[7]]
 * <p>
 * Explanation:
 * <p>
 * Column -2: Only node 4 is in this column.
 * <p>
 * Column -1: Only node 2 is in this column.
 *
 * <div>
 * Column 0: Nodes 1, 5, and 6 are in this column.
 *           1 is at the top, so it comes first.
 *           5 and 6 are at the same position (2, 0), so we order them by their value, 5 before 6.
 * </div>
 * <p>
 * Column 1: Only node 3 is in this column.
 * <p>
 * Column 2: Only node 7 is in this column.
 * <p>
 * <p>
 * Example 3:
 * <p>
 * <img src="../doc-files/987_3.jpg" />
 * <p>
 * Input: root = [1,2,3,4,6,5,7]
 * <p>
 * Output: [[4],[2],[1,5,6],[3],[7]]
 * <p>
 * Explanation:
 * <p>
 * This case is the exact same as example 2, but with nodes 5 and 6 swapped.
 * <p>
 * Note that the solution remains the same since 5 and 6 are in the same location and should be ordered by their values.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [1, 1000].
 * <p>
 * 0 <= Node.val <= 1000
 */
public class No987 {
    public List<List<Integer>> verticalTraversal(TreeNode root) {
        return solution1(root);
    }

    private List<List<Integer>> solution1(TreeNode root) {
        Map<Integer, List<CoordinatedNode>> map = new TreeMap<>();
        List<List<Integer>> result = new ArrayList<>();

        dfs(root, map, 0, 0);

        for (List<CoordinatedNode> vertical : map.values()) {
            Collections.sort(vertical);
            result.add(vertical.stream().map(coordinatedNode -> coordinatedNode.node.val).toList());
        }

        return result;
    }

    private List<List<Integer>> solution2(TreeNode root) {
        if (root == null) {
            return Collections.emptyList();
        }

        Queue<CoordinatedNode> nodeQueue = new LinkedList<>();
        // key is the column index and
        // value is the list of values in this column
        Map<Integer, List<CoordinatedNode>> map = new HashMap<>();
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;

        nodeQueue.add(new CoordinatedNode(root, 0, 0));

        while (!nodeQueue.isEmpty()) {
            CoordinatedNode coordinatedNode = nodeQueue.poll();

            map.putIfAbsent(coordinatedNode.column, new ArrayList<>());
            map.get(coordinatedNode.column).add(coordinatedNode);

            minCol = Math.min(minCol, coordinatedNode.column);
            maxCol = Math.max(maxCol, coordinatedNode.column);

            if (coordinatedNode.node.left != null) {
                nodeQueue.add(new CoordinatedNode(
                    coordinatedNode.node.left,
                    coordinatedNode.row + 1,
                    coordinatedNode.column - 1
                ));
            }

            if (coordinatedNode.node.right != null) {
                nodeQueue.add(new CoordinatedNode(
                    coordinatedNode.node.right,
                    coordinatedNode.row + 1,
                    coordinatedNode.column + 1
                ));
            }
        }

        List<List<Integer>> result = new ArrayList<>();

        for (int i = minCol; i <= maxCol; i++) {
            Collections.sort(map.get(i));
            result.add(map.get(i).stream().map(coordinatedNode -> coordinatedNode.node.val).toList());
        }

        return result;
    }

    private void dfs(TreeNode node, Map<Integer, List<CoordinatedNode>> map, int row, int column) {
        if (node == null) {
            return;
        }

        map.putIfAbsent(column, new ArrayList<>());
        map.get(column).add(new CoordinatedNode(node, row, column));

        dfs(node.left, map, row + 1, column - 1);
        dfs(node.right, map, row + 1, column + 1);
    }

    private static class CoordinatedNode implements Comparable<CoordinatedNode> {
        TreeNode node;
        int row;
        int column;

        CoordinatedNode(TreeNode node, int row, int column) {
            this.node = node;
            this.row = row;
            this.column = column;
        }

        @Override
        public int compareTo(CoordinatedNode o) {
            if (this.row == o.row && this.column == o.column) {
                return Integer.compare(node.val, o.node.val);
            }

            if (this.row == o.row) {
                return Integer.compare(this.column, o.column);
            }

            // this.column == o.column
            return Integer.compare(this.row, o.row);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CoordinatedNode node1)) {
                return false;
            }
            return row == node1.row && column == node1.column && Objects.equals(node, node1.node);
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
        }
    }
}
