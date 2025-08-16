package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree containing digits from 0-9 only, each root-to-leaf path could represent a number.
 * <p>
 * An example is the root-to-leaf path 1->2->3 which represents the number 123.
 * <p>
 * Find the total sum of all root-to-leaf numbers.
 * <p>
 * For example,
 *
 * <div>
 *   1
 *  / \
 * 2   3
 * </div>
 * <p>
 * The root-to-leaf path 1->2 represents the number 12.
 * The root-to-leaf path 1->3 represents the number 13.
 * <p>
 * Return the sum = 12 + 13 = 25.
 */
public class No129 {

    public int sumNumbers(TreeNode root) {
        return sumNumbers(root, 0);
    }

    private int sumNumbers(TreeNode node, int lastSum) {
        if (node == null) {
            return 0;
        }

        int currentSum = 10 * lastSum + node.val;

        if (node.left == null && node.right == null) {
            return currentSum;
        }

        return sumNumbers(node.left, currentSum) + sumNumbers(node.right, currentSum);
    }
}
