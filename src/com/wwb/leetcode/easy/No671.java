package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a non-empty special binary tree consisting of nodes with the non-negative value, where each node in this tree has exactly two or zero sub-node. If the node has two sub-nodes, then this node's value is the smaller value among its two sub-nodes. More formally, the property root.val = min(root.left.val, root.right.val) always holds.
 *
 * Given such a binary tree, you need to output the second minimum value in the set made of all the nodes' value in the whole tree.
 *
 * If no such second minimum value exists, output -1 instead.
 *
 *
 *
 *
 *
 * Example 1:
 *
 *
 * Input: root = [2,2,5,null,null,5,7]
 * Output: 5
 * Explanation: The smallest value is 2, the second smallest value is 5.
 * Example 2:
 *
 *
 * Input: root = [2,2,2]
 * Output: -1
 * Explanation: The smallest value is 2, but there isn't any second smallest value.
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [1, 25].
 * 1 <= Node.val <= 2^31 - 1
 * root.val == min(root.left.val, root.right.val) for each internal node of the tree.
 */
public class No671 {
    public int findSecondMinimumValue(TreeNode root) {
        var minimum = root.val;

        var potentialSecondMinimumNode = findSecondMinimumNode(root, minimum);

        if (potentialSecondMinimumNode == null) {
            return -1;
        }

        return potentialSecondMinimumNode.val;
    }

    private TreeNode findSecondMinimumNode(TreeNode node, int minimum) {
        if (node == null) {
            return null;
        }

        if (node.left == null && node.right == null) {
            return node.val == minimum ? null : node;
        }

        var left = findSecondMinimumNode(node.left, minimum);
        var right = findSecondMinimumNode(node.right, minimum);

        if (left == null) {
            return right;
        }

        if (right == null) {
            return left;
        }

        return left.val < right.val ? left : right;
    }
}
