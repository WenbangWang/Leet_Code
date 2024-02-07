package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.*;

/**
 * Given the root of a binary tree, collect a tree's nodes as if you were doing this:
 *
 * Collect all the leaf nodes.
 * Remove all the leaf nodes.
 * Repeat until the tree is empty.
 *
 *
 * Example 1:
 *
 *
 * Input: root = [1,2,3,4,5]
 * Output: [[4,5,3],[2],[1]]
 * Explanation:
 * [[3,5,4],[2],[1]] and [[3,4,5],[2],[1]] are also considered correct answers
 * since per each level it does not matter the order on which elements are returned.
 * Example 2:
 *
 * Input: root = [1]
 * Output: [[1]]
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [1, 100].
 * -100 <= Node.val <= 100
 */
public class No366 {
    public List<List<Integer>> findLeaves(TreeNode root) {
        return solution1(root);
    }

    private List<List<Integer>> solution1(TreeNode root) {
        if (root == null) {
            return Collections.emptyList();
        }

        List<List<Integer>> result = new ArrayList<>();

        List<Integer> leaves = removeLeaves(root);

        result.add(leaves);

        while (leaves.size() != 1 || leaves.get(0) != root.val) {
            leaves = removeLeaves(root);

            result.add(leaves);
        }

        return result;
    }

    private List<List<Integer>> solution2(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();

        getHeight(root, result);

        return result;
    }

    private int getHeight(TreeNode node, List<List<Integer>> result) {
        if (node == null) {
            return 0;
        }

        // first calculate the height of the left and right children
        int leftHeight = getHeight(node.left, result);
        int rightHeight = getHeight(node.right, result);

        int currHeight = Math.max(leftHeight, rightHeight) + 1;

        // height it 1-based and array index is 0-based
        if (result.size() == currHeight - 1) {
            result.add(new ArrayList<>());
        }

        result.get(currHeight - 1).add(node.val);

        return currHeight;
    }

    private List<Integer> removeLeaves(TreeNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        if (isLeafNode(node)) {
            return Collections.singletonList(node.val);
        }

        List<Integer> result = new ArrayList<>();

        if (isLeafNode(node.left)) {
            TreeNode leaf = node.left;

            node.left = null;

            result.add(leaf.val);
        } else {
            result.addAll(removeLeaves(node.left));
        }

        if (isLeafNode(node.right)) {
            TreeNode leaf = node.right;

            node.right = null;

            result.add(leaf.val);
        } else {
            result.addAll(removeLeaves(node.right));
        }

        return result;
    }

    private boolean isLeafNode(TreeNode node) {
        return node != null && node.left == null && node.right == null;
    }

}
