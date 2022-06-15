package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary tree, find the lowest common ancestor (LCA) of two given nodes in the BST.
 *
 * According to the definition of LCA on Wikipedia:
 * “The lowest common ancestor is defined between two nodes v and w as the lowest node in T
 * that has both v and w as descendants (where we allow a node to be a descendant of itself).”
 *
 *       _______3______
 *      /              \
 *   ___5__          ___1__
 *  /      \        /      \
 *  6      _2       0      8
 *        /  \
 *       7   4
 * For example, the lowest common ancestor (LCA) of nodes 5 and 1 is 3.
 * Another example is LCA of nodes 5 and 4 is 5,
 * since a node can be a descendant of itself according to the LCA definition.
 */
public class No236 {

    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        return solution1(root, p, q);
    }

    private TreeNode solution1(TreeNode node, TreeNode p, TreeNode q) {
        if(node == null || node == p || node == q) {
            return node;
        }

        TreeNode left = solution1(node.left, p, q);
        TreeNode right = solution1(node.right, p, q);

        if(left != null && right != null) {
            return node;
        }

        return left != null ? left : right;
    }

    private TreeNode solution2(TreeNode root, TreeNode p, TreeNode q) {
        Result result = getAncestor(root, p, q);
        if(result.isAncestor) {
            return result.node;
        }

        return null;
    }

    private Result getAncestor(TreeNode node, TreeNode p, TreeNode q) {
        if(node == null) {
            return new Result(null, false);
        }

        if(node == p && node == q) {
            return new Result(node, true);
        }

        Result left = getAncestor(node.left, p, q);
        if(left.isAncestor) {
            return left;
        }
        Result right = getAncestor(node.right, p, q);
        if(right.isAncestor) {
            return right;
        }

        if(left.node != null && right.node != null) {
            return new Result(node, true);
        } else if(node == p || node == q) {
            boolean isAncestor = left.node != null || right.node != null;
            return new Result(node, isAncestor);
        } else {
            return new Result(left.node != null ? left.node : right.node, false);
        }
    }

    private static class Result {
        boolean isAncestor;
        TreeNode node;

        Result(TreeNode node, boolean isAncestor) {
            this.node = node;
            this.isAncestor = isAncestor;
        }
    }
}
