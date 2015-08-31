package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a complete binary tree, count the number of nodes.
 *
 * In a complete binary tree every level, except possibly the last, is completely filled,
 * and all nodes in the last level are as far left as possible.
 * It can have between 1 and 2h nodes inclusive at the last level h.
 */
public class No222 {

    public int countNodes(TreeNode root) {
        if(root == null) {
            return 0;
        }

        TreeNode left = root;
        TreeNode right = root;
        int height = 0;

        while(right != null) {
            left = left.left;
            right = right.right;
            height++;
        }

        if(left == null) {
            return (1 << height) - 1;
        }

        return 1 + countNodes(root.left) + countNodes(root.right);
    }
}