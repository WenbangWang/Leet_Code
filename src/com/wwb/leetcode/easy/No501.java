package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root of a binary search tree (BST) with duplicates, return all the mode(s)
 * (i.e., the most frequently occurred element) in it.
 *
 * If the tree has more than one mode, return them in any order.
 *
 * Assume a BST is defined as follows:
 *
 * The left subtree of a node contains only nodes with keys less than or equal to the node's key.
 * The right subtree of a node contains only nodes with keys greater than or equal to the node's key.
 * Both the left and right subtrees must also be binary search trees.
 *
 *
 * Example 1:
 *
 *
 * Input: root = [1,null,2,2]
 * Output: [2]
 * Example 2:
 *
 * Input: root = [0]
 * Output: [0]
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [1, 10^4].
 * -1^05 <= Node.val <= 10^5
 *
 *
 * Follow up: Could you do that without using any extra space?
 * (Assume that the implicit stack space incurred due to recursion does not count).
 */
public class No501 {
    public int[] findMode(TreeNode root) {
        Mode mode = new Mode();

        inOrder(root, mode);
        mode.modes = new int[mode.modeCount];
        mode.modeCount = 0;
        mode.count = 0;

        inOrder(root, mode);

        return mode.modes;
    }

    private void inOrder(TreeNode node, Mode mode) {
        if (node == null) {
            return;
        }

        inOrder(node.left, mode);
        findMode(node, mode);
        inOrder(node.right, mode);
    }

    private void findMode(TreeNode node, Mode mode) {
        if (node.val != mode.value) {
            mode.value = node.val;
            mode.count = 0;
        }

        mode.count++;

        if (mode.count > mode.maxCount) {
            mode.maxCount = mode.count;
            mode.modeCount = 1;
        } else if (mode.count == mode.maxCount) {
            if (mode.modes != null) {
                mode.modes[mode.modeCount] = mode.value;
            }
            mode.modeCount++;
        }
    }

    private static class Mode {
        int value;
        int count;
        int maxCount;
        int modeCount;
        int[] modes;
    }
}
