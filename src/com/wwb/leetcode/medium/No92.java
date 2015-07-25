package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.ListNode;

/**
 * Reverse a linked list from position m to n. Do it in-place and in one-pass.
 *
 * For example:
 * Given 1->2->3->4->5->NULL, m = 2 and n = 4,
 *
 * return 1->4->3->2->5->NULL.
 *
 * Note:
 * Given m, n satisfy the following condition:
 * 1 ≤ m ≤ n ≤ length of list.
 */
public class No92 {

    public ListNode reverseBetween(ListNode head, int m, int n) {
        if(head == null) {
            return null;
        }

        ListNode start = new ListNode(0);
        ListNode pre = start;
        start.next = head;

        for(int i = 0; i < m - 1; i++) {
            pre = pre.next;
        }

        ListNode current = pre.next;
        ListNode next = current.next;

        for(int i = 0; i < n - m; i++) {
            current.next = next.next;
            next.next = pre.next;
            pre.next = next;
            next = current.next;
        }

        return start.next;
    }
}