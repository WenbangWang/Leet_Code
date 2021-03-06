package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree, flatten it to a linked list in-place.
 *
 * For example,
 * Given
 *
 *     1
 *    / \
 *   2   5
 *  / \   \
 * 3   4   6
 * The flattened tree should look like:
 * 1
 *  \
 *   2
 *    \
 *     3
 *      \
 *       4
 *        \
 *         5
 *          \
 *           6
 */
public class No114 {

    public void flatten(TreeNode root) {
//        solution1(root);
        solution2(root);
    }

    private void solution1(TreeNode root) {
        flatten(root, null);
    }

    private void solution2(TreeNode root) {
        if(root == null || (root.left == null && root.right == null)) {
            return;
        }

        while(root != null) {
            if(root.left == null) {
                root = root.right;
                continue;
            }

            TreeNode left = root.left;

            while(left.right != null) {
                left = left.right;
            }

            left.right = root.right;
            root.right = root.left;
            root.left = null;

            root = root.right;
        }
    }

    private void solution3(TreeNode root) {
        this.flattenn(root);
    }

    private TreeNode flattenn(TreeNode node) {
        if(node == null) {
            return null;
        }

        if(node.left == null && node.right == null) {
            return node;
        }

        if(node.left == null) {
            return flattenn(node.right);
        }

        if(node.right == null) {
            TreeNode lastNodeOnLeft = flattenn(node.left);
            node.right = node.left;
            node.left = null;
            return lastNodeOnLeft;
        }

        TreeNode lastNodeOnLeft = flattenn(node.left);
        TreeNode lastNodeOnRight = flattenn(node.right);

        lastNodeOnLeft.right = node.right;

        node.right = node.left;
        node.left = null;

        return lastNodeOnRight;
    }

    private void flatten(TreeNode node, TreeNode pre) {
        if(node == null) {
            return;
        }

        flatten(node.right, pre);
        flatten(node.left, pre);
        node.right = pre;
        node.left = null;
        pre = node;
    }
}
