package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Two elements of a binary search tree (BST) are swapped by mistake.
 *
 * Recover the tree without changing its structure.
 *
 * Note:
 * A solution using O(n) space is pretty straight forward. Could you devise a constant space solution?
 */
public class No99 {

    public void recoverTree(TreeNode root) {
        TreeNode pre = null;
        TreeNode first = null;
        TreeNode second = null;

        while(root != null) {
            if(root.left != null) {
                TreeNode left  = root.left;

                while(left.right != null && left.right != root) {
                    left = left.right;
                }

                if(left.right != null) {
                    left.right = null;
                    if(pre != null && pre.val > root.val) {
                        second = root;
                        if(first == null) {
                            first = pre;
                        }
                    }

                    pre = root;

                    root = root.right;
                } else {
                    left.right = root;
                    root = root.left;
                }
            } else {
                if(pre != null && pre.val > root.val) {
                    second = root;
                    if(first == null) {
                        first = pre;
                    }
                }

                pre = root;
                root = root.right;
            }
        }

        if(first != null) {
            int t = first.val;
            first.val = second.val;
            second.val = t;
        }
    }
}
