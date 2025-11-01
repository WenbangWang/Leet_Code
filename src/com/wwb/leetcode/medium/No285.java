package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary search tree and a node in it,
 * find the in-order successor of that node in the BST.
 *
 * Note: If the given node has no in-order successor in the tree, return null.
 */
public class No285 {
    public TreeNode inorderSuccessor(TreeNode root, TreeNode p) {
        if (p == null) return null;

        // Case 1: right child exists
        if (p.right != null) {
            return leftmost(p.right);
        }

        // Case 2: no right child -> search from root
        TreeNode successor = null;
        TreeNode curr = root;

        while (curr != null) {
            if (p.val < curr.val) {
                successor = curr;   // possible successor
                curr = curr.left;
            } else if (p.val > curr.val) {
                curr = curr.right;
            } else {
                break; // found node p
            }
        }

        return successor;
    }

    private TreeNode leftmost(TreeNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
}
