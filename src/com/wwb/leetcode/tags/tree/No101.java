package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

import java.util.Stack;

/**
 * Given a binary tree, check whether it is a mirror of itself (ie, symmetric around its center).
 *
 * For example, this binary tree is symmetric:
 *
 *    1
 *   / \
 *   2   2
 *  / \ / \
 * 3  4 4  3
 * But the following is not:
 *    1
 *   / \
 *  2   2
 *   \   \
 *   3    3
 */
public class No101 {

    public boolean isSymmetric(TreeNode root) {
//        return solution1(root);
        return solution2(root);
    }

    private boolean solution1(TreeNode root) {
        return root == null || solution1(root.left, root.right);
    }

    private boolean solution1(TreeNode left, TreeNode right) {
        if(left == null || right == null) {
            return left == right;
        }

        return left.val == right.val && solution1(left.left, right.right) && solution1(left.right, right.left);
    }

    private boolean solution2(TreeNode root) {
        if(root == null || (root.left == null && root.right == null)) {
            return true;
        } else if(root.left == null || root.right == null) {
            return false;
        }

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root.left);
        stack.push(root.right);

        while(!stack.isEmpty()) {
            if(stack.size() % 2 != 0) {
                return false;
            }

            TreeNode right = stack.pop();
            TreeNode left = stack.pop();

            if(left.val != right.val) {
                return false;
            }

            if(left.left != null) {
                if(right.right == null) {
                    return false;
                }
                stack.push(left.left);
                stack.push(right.right);
            } else if(right.right != null) {
                return false;
            }

            if(left.right != null) {
                if(right.left == null) {
                    return false;
                }

                stack.push(left.right);
                stack.push(right.left);
            } else if(right.left != null) {
                return false;
            }
        }

        return true;
    }
}
