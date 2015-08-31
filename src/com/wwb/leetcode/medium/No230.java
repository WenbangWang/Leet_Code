package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.Stack;

/**
 * Given a binary search tree, write a function kthSmallest to find the kth smallest element in it.
 *
 * Note:
 * You may assume k is always valid, 1 ≤ k ≤ BST's total elements.
 *
 * Follow up:
 * What if the BST is modified (insert/delete operations) often and you need to find the kth smallest frequently?
 * How would you optimize the kthSmallest routine?
 */
public class No230 {

    public int kthSmallest(TreeNode root, int k) {
        if(root == null) {
            return 0;
        }

        Stack<TreeNode> stack = new Stack<>();
        inorder(root, stack);
        int size = stack.size();

        for(int i = size - k; i < size; i++) {
            stack.pop();
        }

        return stack.peek().val;
    }

    private void inorder(TreeNode node, Stack<TreeNode> stack) {
        if(node == null) {
            return;
        }

        inorder(node.left, stack);
        stack.push(node);
        inorder(node.right, stack);
    }
}