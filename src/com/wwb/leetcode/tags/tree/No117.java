package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeLinkNode;

/**
 * Follow up for problem "Populating Next Right Pointers in Each Node".
 *
 * What if the given tree could be any binary tree? Would your previous solution still work?
 *
 * Note:
 *
 * You may only use constant extra space.
 * For example,
 * Given the following binary tree,
 *     1
 *    /  \
 *   2    3
 *  / \    \
 * 4   5    7
 * After calling your function, the tree should look like:
 *     1 -> NULL
 *    /  \
 *   2 -> 3 -> NULL
 *  / \    \
 * 4-> 5 -> 7 -> NULL
 */
public class No117 {

    public void connect(TreeLinkNode root) {
//        solution1(root);
        solution2(root);
    }

    private void solution1(TreeLinkNode root) {
        if(root == null) {
            return;
        }

        TreeLinkNode node = root;

        while(node != null) {
            TreeLinkNode current = node;
            TreeLinkNode pre = null;
            node = null;

            while(current != null) {
                if(current.left != null) {
                    if(pre == null) {
                        node = current.left;
                    } else {
                        pre.next = current.left;
                    }

                    pre = current.left;
                }

                if(current.right != null) {
                    if(pre == null) {
                        node = current.right;
                    } else {
                        pre.next = current.right;
                    }

                    pre = current.right;
                }

                current = current.next;
            }
        }
    }

    private void solution2(TreeLinkNode root) {
        TreeLinkNode dummyHead = new TreeLinkNode(0);
        TreeLinkNode pre = dummyHead;

        while(root != null) {
            if(root.left != null) {
                pre.next = root.left;
                pre = root.left;
            }

            if(root.right != null) {
                pre.next = root.right;
                pre = root.right;
            }

            root = root.next;

            if(root == null) {
                pre = dummyHead;
                root = dummyHead.next;
                dummyHead.next = null;
            }
        }
    }
}
