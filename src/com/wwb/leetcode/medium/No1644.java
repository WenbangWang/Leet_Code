package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root of a binary tree, return the lowest common ancestor (LCA) of two given nodes, p and q.
 * If either node p or q does not exist in the tree, return null. All values of the nodes in the tree are unique.
 * <p>
 * According to the definition of LCA on Wikipedia: "The lowest common ancestor of two nodes p and q
 * in a binary tree T is the lowest node that has both p and q as descendants
 * (where we allow a node to be a descendant of itself)".
 * A descendant of a node x is a node y that is on the path from node x to some leaf node.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 1
 * Output: 3
 * Explanation: The LCA of nodes 5 and 1 is 3.
 * Example 2:
 * <p>
 * <p>
 * <p>
 * Input: root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 4
 * Output: 5
 * Explanation: The LCA of nodes 5 and 4 is 5. A node can be a descendant of itself according to the definition of LCA.
 * Example 3:
 * <p>
 * <p>
 * <p>
 * Input: root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 10
 * Output: null
 * Explanation: Node 10 does not exist in the tree, so return null.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [1, 104].
 * -10^9 <= Node.val <= 10^9
 * All Node.val are unique.
 * p != q
 * <p>
 * <p>
 * Follow up: Can you find the LCA traversing the tree, without checking nodes existence?
 */
public class No1644 {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        return getAncestor(root, p, q).ancestor;
    }

    private TreeNode solution1(TreeNode root, TreeNode p, TreeNode q) {
        int[] count = new int[1];
        TreeNode node = getAncestor(root, p, q, count);
        return count[0] == 2 ? node : null;
    }

    private TreeNode getAncestor(TreeNode node, TreeNode p, TreeNode q, int[] count) {
        if (node == null) {
            return null;
        }

        TreeNode left = getAncestor(node.left, p, q, count);
        TreeNode right = getAncestor(node.right, p, q, count);

        if (node == p || node == q) {
            count[0]++;

            return node;
        }

        if (left != null && right != null) {
            return node;
        }

        return left == null ? right : left;
    }

    private Result getAncestor(TreeNode node, TreeNode first, TreeNode second) {
        if (node == null) {
            return new Result();
        }

        if (node == first || node == second) {
            var theOther = node == first ? second : first;
            var left = findNode(node.left, theOther);

            // current node is the parent of the other node
            if (left != null) {
                return new Result(node, node, left);
            }

            var right = findNode(node.right, theOther);

            if (right != null) {
                return new Result(node, node, right);
            }

            // didn't find the other node as a child,
            // return to the caller to check on the other side.
            var result = new Result();

            if (node == first) {
                result.first = node;
            } else {
                result.second = node;
            }

            return result;
        }

        var left = getAncestor(node.left, first, second);

        if (left.ancestor != null) {
            return left;
        }

        var right = getAncestor(node.right, first, second);

        if (right.ancestor != null) {
            return right;
        }

        return createAncestorResult(node, left, right);
    }

    private TreeNode findNode(TreeNode node, TreeNode nodeToBeFound) {
        if (node == null) {
            return null;
        }

        if (node == nodeToBeFound) {
            return node;
        }

        var left = findNode(node.left, nodeToBeFound);

        if (left != null) {
            return left;
        }

        return findNode(node.right, nodeToBeFound);
    }

    Result createAncestorResult(TreeNode node, Result left, Result right) {
        var result = new Result();

        result.first = left.first != null ? left.first : right.first;
        result.second = left.second != null ? left.second : right.second;

        // found one node on one side and the other node on the other side
        if (left.first != null && right.second != null || left.second != null && right.first != null) {
            result.ancestor = node;
        }

        return result;
    }

    private static class Result {
        TreeNode ancestor;
        TreeNode first;
        TreeNode second;

        Result() {
            this(null, null, null);
        }

        Result(TreeNode ancestor, TreeNode first, TreeNode second) {
            this.ancestor = ancestor;
            this.first = first;
            this.second = second;
        }
    }
}
