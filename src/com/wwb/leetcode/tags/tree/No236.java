package com.wwb.leetcode.tags.tree;

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

    private TreeNode solution1(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null || root == p || root == q) {
            return root;
        }

        TreeNode left = solution1(root.left, p, q);
        TreeNode right = solution1(root.right, p, q);

        if(left != null && right != null) {
            return root;
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

    private Result getAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null) {
            return new Result(null, false);
        }

        if(root == p && root == q) {
            return new Result(root, true);
        }

        Result left = getAncestor(root.left, p, q);
        if(left.isAncestor) {
            return left;
        }
        Result right = getAncestor(root.right, p, q);
        if(right.isAncestor) {
            return right;
        }

        if(left.node != null && right.node != null) {
            return new Result(root, true);
        } else if(root == p || root == q) {
            boolean isAncestor = left.node != null || right.node != null;
            return new Result(root, isAncestor);
        } else {
            return new Result(left.node != null ? left.node : right.node, false);
        }
    }

    private class Result {
        boolean isAncestor;
        TreeNode node;

        Result(TreeNode node, boolean isAncestor) {
            this.node = node;
            this.isAncestor = isAncestor;
        }
    }
}