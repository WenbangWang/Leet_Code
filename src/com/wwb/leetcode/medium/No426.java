package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Convert a BST to a sorted circular doubly-linked list in-place. Think of the left and right pointers as synonymous to the previous and next pointers in a doubly-linked list.
 * <p>
 * Let's take the following BST as an example, it may help you understand the problem better:
 *
 *
 * <img src="../doc-files/426_1.png" />
 * <p>
 * <p>
 * We want to transform this BST into a circular doubly linked list. Each node in a doubly linked list has a predecessor and successor. For a circular doubly linked list, the predecessor of the first element is the last element, and the successor of the last element is the first element.
 * <p>
 * The figure below shows the circular doubly linked list for the BST above. The "head" symbol means the node it points to is the smallest element of the linked list.
 *
 *
 * <img src="../doc-files/426_2.png" />
 * <p>
 * <p>
 * Specifically, we want to do the transformation in place. After the transformation, the left pointer of the tree node should point to its predecessor, and the right pointer should point to its successor. We should return the pointer to the first element of the linked list.
 * <p>
 * The figure below shows the transformed BST. The solid line indicates the successor relationship, while the dashed line means the predecessor relationship.
 *
 * <img src="../doc-files/426_3.png" />
 */
public class No426 {
    public TreeNode treeToDoublyList(TreeNode root) {
        TreeNode[] pre = new TreeNode[1];
        TreeNode[] head = new TreeNode[1];

        inOrder(root, pre, head);

        head[0].left = pre[0];
        pre[0].right = head[0];

        return head[0];
    }

    private void inOrder(TreeNode node, TreeNode[] pre, TreeNode[] head) {
        if (node == null) {
            return;
        }

        inOrder(node.left, pre, head);

        if (pre[0] == null) {
            head[0] = node;
        } else {
            pre[0].right = node;
            node.left = pre[0];
        }

        pre[0] = node;
        inOrder(node.right, pre, head);
    }
}
