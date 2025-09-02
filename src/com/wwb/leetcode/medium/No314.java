package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.*;

/**
 * Given a binary tree, return the vertical order traversal of its nodes' values. (ie, from top to bottom, column by column).
 * <p>
 * If two nodes are in the same row and column, the order should be from left to right.
 * <p>
 * Examples:
 * <p>
 * Given binary tree [3,9,20,null,null,15,7],
 * <pre>
 *   3
 *  /\
 * /  \
 * 9  20
 *    /\
 *   /  \
 *  15   7
 *
 * return its vertical order traversal as:
 * [
 *   [9],
 *   [3,15],
 *   [20],
 *   [7]
 * ]
 * Given binary tree [3,9,8,4,0,1,7],
 *
 *
 *    3
 *   /\
 *  /  \
 *  9   8
 *  /\  /\
 * /  \/  \
 * 4  01   7
 *
 * return its vertical order traversal as:
 * [
 *   [4],
 *   [9],
 *   [3,0,1],
 *   [8],
 *   [7]
 * ]
 * Given binary tree [3,9,8,4,0,1,7,null,null,null,2,5] (0's right child is 2 and 1's left child is 5),
 *      3
 *     /\
 *    /  \
 *    9   8
 *   /\  /\
 *  /  \/  \
 *  4  01   7
 *     /\
 *    /  \
 *   5   2
 * return its vertical order traversal as:
 * [
 *   [4],
 *   [9,5],
 *   [3,0,1],
 *   [8,2],
 *   [7]
 * ]
 * </pre>
 */
public class No314 {
    public List<List<Integer>> verticalOrder(TreeNode root) {
        if (root == null) {
            return Collections.emptyList();
        }

        Queue<TreeNode> nodeQueue = new LinkedList<>();
        Queue<Integer> columnQueue = new LinkedList<>();
        // key is the column index and
        // value is the list of values in this column
        Map<Integer, List<Integer>> map = new TreeMap<>();

        nodeQueue.add(root);
        columnQueue.add(0);

        while (!nodeQueue.isEmpty()) {
            TreeNode node = nodeQueue.poll();
            int column = columnQueue.poll();

            if (!map.containsKey(column)) {
                map.put(column, new ArrayList<>());
            }

            map.get(column).add(node.val);

            if (node.left != null) {
                nodeQueue.add(node.left);
                columnQueue.add(column - 1);
            }

            if (node.right != null) {
                nodeQueue.add(node.right);
                columnQueue.add(column + 1);
            }
        }

        return new ArrayList<>(map.values());
    }
}
