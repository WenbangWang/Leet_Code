package com.wwb.leetcode.tags.dfs;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree, find the maximum path sum.
 *
 * For this problem, a path is defined as any sequence of nodes from some starting node to any node in the tree along the parent-child connections. The path does not need to go through the root.
 *
 * For example:
 * Given the below binary tree,
 *
 *   1
 *  / \
 * 2   3
 * Return 6.
 */
public class No124 {

    public int maxPathSum(TreeNode root) {
        if(root == null) {
            return 0;
        }

        int[] max = {Integer.MIN_VALUE};

        maxPathSum(root, max);

        return max[0];
    }

    private int maxPathSum(TreeNode node, int[] max) {
        if(node == null) {
            return 0;
        }

        int left = Math.max(0, maxPathSum(node.left, max));
        int right = Math.max(0, maxPathSum(node.right, max));
        max[0] = Math.max(max[0], left + right + node.val);

        return Math.max(left, right) + node.val;
    }
}
