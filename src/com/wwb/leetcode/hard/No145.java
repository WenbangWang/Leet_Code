package com.wwb.leetcode.hard;

import com.wwb.leetcode.utils.TreeNode;

import java.util.*;

/**
 * Given a binary tree, return the postorder traversal of its nodes' values.
 *
 * For example:
 * Given binary tree {1,#,2,3},
 * 1
 *  \
 *   2
 *  /
 * 3
 * return [3,2,1].
 *
 * Note: Recursive solution is trivial, could you do it iteratively?
 */
public class No145 {

    public List<Integer> postorderTraversal(TreeNode root) {
        if(root == null) {
            return Collections.emptyList();
        }

        Stack<TreeNode> stack = new Stack<>();
        List<Integer> result = new LinkedList<>();

        stack.push(root);

        while(!stack.isEmpty()) {
            TreeNode node = stack.pop();

            result.add(0, node.val);

            if(node.left != null) {
                stack.push(node.left);
            }

            if(node.right != null) {
                stack.push(node.right);
            }
        }

        return result;
    }
}
