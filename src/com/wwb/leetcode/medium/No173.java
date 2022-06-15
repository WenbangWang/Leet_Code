package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.Stack;

/**
 * Implement an iterator over a binary search tree (BST). Your iterator will be initialized with the root node of a BST.
 *
 * Calling next() will return the next smallest number in the BST.
 *
 * Note: next() and hasNext() should run in average O(1) time and uses O(h) memory, where h is the height of the tree.
 */
public class No173 {

    public class BSTIterator {

        private Stack<TreeNode> stack;

        public BSTIterator(TreeNode root) {
            this.stack = new Stack<>();
            this.pushLeft(root);
        }

        /** @return whether we have a next smallest number */
        public boolean hasNext() {
            return !this.stack.isEmpty();
        }

        /** @return the next smallest number */
        public int next() {
            TreeNode current = this.stack.pop();

            if(current.right != null) {
                this.pushLeft(current.right);
            }

            return current.val;
        }

        private void pushLeft(TreeNode node) {
            while(node != null) {
                this.stack.push(node);
                if(node.left != null) {
                    node = node.left;
                } else {
                    break;
                }
            }
        }
    }

/**
 * Your BSTIterator will be called like this:
 * BSTIterator i = new BSTIterator(root);
 * while (i.hasNext()) v[f()] = i.next();
 */
}
