package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Find the sum of all left leaves in a given binary tree.
 *
 * Example:
 *
 *    3
 *   / \
 *  9  20
 *    /  \
 *   15   7
 *
 * There are two left leaves in the binary tree, with values 9 and 15 respectively. Return 24.
 */
public class No404 {
    public int sumOfLeftLeaves(TreeNode root) {
        if(root == null) {
            return 0;
        }

        int sum = 0;

        if(root.left != null) {
            if(isLeaf(root.left)) {
                sum += root.left.val;
            } else {
                sum += sumOfLeftLeaves(root.left);
            }
        }

        if(root.right != null) {
            sum += sumOfLeftLeaves(root.right);
        }

        return sum;
    }

    private boolean isLeaf(TreeNode node) {
        return node.left == null && node.right == null;
    }
}
