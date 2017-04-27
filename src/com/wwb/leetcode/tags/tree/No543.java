package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree, you need to compute the length of the diameter of the tree.
 * The diameter of a binary tree is the length of the longest path between any two nodes in a tree.
 * This path may or may not pass through the root.
 *
 * Example:
 * Given a binary tree
 *     1
 *    / \
 *   2   3
 *  / \
 * 4   5
 * Return 3, which is the length of the path [4,2,1,3] or [5,2,1,3].
 *
 * Note: The length of path between two nodes is represented by the number of edges between them.
 */
public class No543 {
    public int diameterOfBinaryTree(TreeNode root) {
        int[] max = {0};

        this.maxDepth(root, max);

        return max[0];
    }

    private int maxDepth(TreeNode node, int[] max) {
        if(node == null) {
            return 0;
        }

        int left = this.maxDepth(node.left, max);
        int right = this.maxDepth(node.right, max);

        max[0] = Math.max(max[0], left + right);

        return Math.max(left, right) + 1;
    }
}
