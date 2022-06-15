package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root of a binary tree, turn the tree upside down and return the new root.
 *
 * You can turn a binary tree upside down with the following steps:
 *
 * The original left child becomes the new root.
 * The original root becomes the new right child.
 * The original right child becomes the new left child.
 *
 * The mentioned steps are done level by level. It is guaranteed that every right node has a sibling (a left node with the same parent) and has no children.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: root = [1,2,3,4,5]
 * Output: [4,5,2,null,null,3,1]
 * Example 2:
 *
 * Input: root = []
 * Output: []
 * Example 3:
 *
 * Input: root = [1]
 * Output: [1]
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree will be in the range [0, 10].
 * 1 <= Node.val <= 10
 * Every right node in the tree has a sibling (a left node that shares the same parent).
 * Every right node in the tree has no children.
 */
public class No156 {
    public TreeNode upsideDownBinaryTree(TreeNode root) {
        return solution1(root);
    }

    private TreeNode solution1(TreeNode root) {
        if (root == null) {
            return null;
        }

        var result = upsideDownBinaryTreeRec(root);

        return result.root;
    }

    private TreeNode solution2(TreeNode root) {
        TreeNode current = root;
        TreeNode previous = null;
        TreeNode previousRight = null;

        while(current != null) {
            TreeNode left = current.left;

            current.left = previousRight;
            previousRight = current.right;
            current.right = previous;

            previous = current;
            current = left;
        }

        return previous;
    }

    private Nodes upsideDownBinaryTreeRec(TreeNode node) {
        if (hasNoChildren(node)) {
            return new Nodes(node, node);
        }

        var leftNodes = upsideDownBinaryTreeRec(node.left);

        leftNodes.rightMostNode.left = node.right;
        leftNodes.rightMostNode.right = node;

        node.left = null;
        node.right = null;

        return new Nodes(leftNodes.root, node);
    }

    private boolean hasNoChildren(TreeNode node) {
        return node != null && node.left == null && node.right == null;
    }

    private static class Nodes {
        TreeNode root;
        TreeNode rightMostNode;

        Nodes(TreeNode root, TreeNode rightMostNode) {
            this.root = root;
            this.rightMostNode = rightMostNode;
        }
    }
}
