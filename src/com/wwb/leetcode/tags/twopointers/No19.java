package com.wwb.leetcode.tags.twopointers;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a linked list, remove the nth node from the end of list and return its head.
 *
 * For example,
 *
 * Given linked list: 1->2->3->4->5, and n = 2.
 *
 * After removing the second node from the end, the linked list becomes 1->2->3->5.
 * Note:
 * Given n will always be valid.
 * Try to do this in one pass.
 */
public class No19 {

    /**
     * Definition for singly-linked list.
     * public class ListNode {
     *     int val;
     *     ListNode next;
     *     ListNode(int x) { val = x; }
     * }
     */
    public ListNode removeNthFromEnd(ListNode head, int n) {
        if(head == null || n < 0) {
            return null;
        }
        ListNode start = new ListNode(0);
        start.next = head;
        ListNode current = start;
        ListNode runner = start;

        for(int i = 0; i <= n; i++) {
            runner = runner.next;
        }

        while(runner != null) {
            current = current.next;
            runner = runner.next;
        }

        current.next = current.next.next;

        return start.next;
    }
}