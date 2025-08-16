package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree, determine if it is a valid binary search tree (BST).
 * <p>
 * <p>
 * Assume a BST is defined as follows:
 * <p>
 * <p>
 * The left subtree of a node contains only nodes with keys less than the node's key.
 * <p>
 * <p>
 * The right subtree of a node contains only nodes with keys greater than the node's key.
 * <p>
 * <p>
 * Both the left and right subtrees must also be binary search trees.
 */
public class No98 {

    public boolean isValidBST(TreeNode root) {
        return isValidBST(root, null, null);
    }

    private boolean isValidBST(TreeNode node, Integer min, Integer max) {
        if (node == null) {
            return true;
        }

        if ((min != null && node.val <= min) || (max != null && node.val >= max)) {
            return false;
        }

        return isValidBST(node.left, min, node.val) && isValidBST(node.right, node.val, max);
    }
}
