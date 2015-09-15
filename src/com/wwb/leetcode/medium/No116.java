package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeLinkNode;

/**
 * Given a binary tree
 * Populate each next pointer to point to its next right node.
 * If there is no next right node, the next pointer should be set to NULL.
 *
 * Initially, all next pointers are set to NULL.
 *
 * Note:
 *
 * You may only use constant extra space.
 * You may assume that it is a perfect binary tree
 * (ie, all leaves are at the same level, and every parent has two children).
 * For example,
 * Given the following perfect binary tree,
 *     1
 *    /  \
 *   2    3
 *  / \  / \
 * 4  5  6  7
 * After calling your function, the tree should look like:
 *     1 -> NULL
 *    /  \
 *   2 -> 3 -> NULL
 *  / \  / \
 * 4->5->6->7 -> NULL
 */
public class No116 {

    public void connect(TreeLinkNode root) {
        TreeLinkNode node = root;

        while(node != null) {
            TreeLinkNode current = node;

            while(current != null) {
                if(current.left != null) {
                    current.left.next = current.right;
                }
                if(current.right != null && current.next != null) {
                    current.right.next = current.next.left;
                }
                current = current.next;
            }

            node = node.left;
        }
    }
}
