package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree and a sum, determine if the tree has a root-to-leaf path such that adding up all the values along the path equals the given sum.
 *
 * For example:
 * Given the below binary tree and sum = 22,
 *       5
 *      / \
 *     4   8
 *    /   / \
 *   11  13  4
 *  /  \      \
 * 7    2      1
 * return true, as there exist a root-to-leaf path 5->4->11->2 which sum is 22.
 */
public class No112 {

    public boolean hasPathSum(TreeNode root, int target) {
        if(root == null) {
            return false;
        }

        target -= root.val;

        if(root.left == null && root.right == null && target == 0) {
            return true;
        }

        return hasPathSum(root.left, target) || hasPathSum(root.right, target);
    }
}
