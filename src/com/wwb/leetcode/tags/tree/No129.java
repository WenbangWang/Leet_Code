package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree containing digits from 0-9 only, each root-to-leaf path could represent a number.
 *
 * An example is the root-to-leaf path 1->2->3 which represents the number 123.
 *
 * Find the total sum of all root-to-leaf numbers.
 *
 * For example,
 *
 *   1
 *  / \
 * 2   3
 * The root-to-leaf path 1->2 represents the number 12.
 * The root-to-leaf path 1->3 represents the number 13.
 *
 * Return the sum = 12 + 13 = 25.
 */
public class No129 {

    public int sumNumbers(TreeNode root) {
        return sumNumbers(root, 0);
    }

    private int sumNumbers(TreeNode node, int lastSum) {
        if(node == null) {
            return 0;
        }

        int currentSum = 10 * lastSum + node.val;

        if(node.left == null && node.right == null) {
            return 10 * lastSum + node.val;
        }

        return sumNumbers(node.left, currentSum) + sumNumbers(node.right, currentSum);
    }
}
