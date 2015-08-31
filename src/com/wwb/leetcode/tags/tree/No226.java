package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Invert a binary tree.
 *
 *      4
 *    /   \
 *   2     7
 *  / \   / \
 * 1   3 6   9
 *      to
 *      4
 *    /   \
 *   7     2
 *  / \   / \
 * 9   6 3   1
 */
public class No226 {

    public TreeNode invertTree(TreeNode root) {
        invertTree(root);
        return root;
    }

    private void insertTree(TreeNode node) {
        if(node == null || (node.left == null && node.right == null)) {
            return;
        }

        invertTree(node.left);
        invertTree(node.right);

        TreeNode temp = node.left;
        node.left = node.right;
        node.right = temp;
    }
}