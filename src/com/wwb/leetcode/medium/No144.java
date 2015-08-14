package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Given a binary tree, return the preorder traversal of its nodes' values.
 *
 * For example:
 * Given binary tree {1,#,2,3},
 * 1
 *  \
 *   2
 *  /
 * 3
 * return [1,2,3].
 *
 * Note: Recursive solution is trivial, could you do it iteratively?
 */
public class No144 {

    public List<Integer> preorderTraversal(TreeNode root) {
        return recursive(root);
    }

    private List<Integer> recursive(TreeNode root) {
        List<Integer> result = new ArrayList<>();

        recursive(root, result);

        return result;
    }

    private void recursive(TreeNode node, List<Integer> result) {
        if(node != null) {
            result.add(node.val);
            recursive(node.left, result);
            recursive(node.right, result);
        }
    }

    private List<Integer> iterative(TreeNode root) {
        Stack<TreeNode> stack = new Stack<>();
        List<Integer> result = new ArrayList<>();
        stack.push(root);

        while(!stack.isEmpty()) {
            TreeNode node = stack.pop();

            if(node != null) {
                result.add(node.val);
                stack.push(node.right);
                stack.push(node.left);
            }
        }

        return result;
    }
}