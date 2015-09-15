package com.wwb.leetcode.tags.dfs;

import com.wwb.leetcode.utils.ListNode;
import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a singly linked list where elements are sorted in ascending order,
 * convert it to a height balanced BST.
 */
public class No109 {

    public TreeNode sortedListToBST(ListNode head) {
        if(head == null) {
            return null;
        }

        if(head.next == null) {
            return new TreeNode(head.val);
        }

        ListNode pre = new ListNode(0);
        ListNode current = head;
        ListNode runner = current;

        pre.next = current;

        while(runner != null && runner.next != null) {
            pre = current;
            current = current.next;
            runner = runner.next.next;
        }

        pre.next = null;

        TreeNode node = new TreeNode(current.val);

        node.left = sortedListToBST(head);
        node.right = sortedListToBST(current.next);

        return node;
    }
}
