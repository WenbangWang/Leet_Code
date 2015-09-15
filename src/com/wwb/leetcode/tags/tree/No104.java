package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree, find its maximum depth.
 *
 * The maximum depth is the number of nodes along the longest path from the root node down to the farthest leaf node.
 */
public class No104 {

    public int maxDepth(TreeNode root) {
        if(root == null) {
            return 0;
        }

        if(root.left == null && root.right == null) {
            return 1;
        }

        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }
}
