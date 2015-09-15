package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree, determine if it is height-balanced.
 *
 * For this problem, a height-balanced binary tree is defined as a binary tree
 * in which the depth of the two subtrees of every node never differ by more than 1.
 */
public class No110 {

    public boolean isBalanced(TreeNode root) {
        if(root == null || (root.left == null && root.right == null)) {
            return true;
        }

        return getHeight(root) != -1;
    }

    private int getHeight(TreeNode node) {
        if(node == null) {
            return 0;
        }
        if(node.left == null && node.right == null) {
            return 1;
        }

        int leftHeight = getHeight(node.left);

        if(leftHeight == -1) {
            return -1;
        }

        int rightHeight = getHeight(node.right);

        if(rightHeight == -1) {
            return -1;
        }

        if(Math.abs(leftHeight - rightHeight) > 1) {
            return -1;
        }

        return 1 + Math.max(leftHeight, rightHeight);
    }
}
