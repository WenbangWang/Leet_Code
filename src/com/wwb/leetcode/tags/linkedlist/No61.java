package com.wwb.leetcode.tags.linkedlist;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a list, rotate the list to the right by k places, where k is non-negative.
 *
 * For example:
 * Given 1->2->3->4->5->NULL and k = 2,
 * return 4->5->1->2->3->NULL.
 */
public class No61 {

    public ListNode rotateRight(ListNode head, int k) {
        if(head == null || head.next == null) {
            return head;
        }

        ListNode start = new ListNode(0);
        ListNode current = start;
        ListNode runner = start;
        int index = 0;
        start.next = head;

        while(runner.next != null) {
            runner = runner.next;
            index++;
        }

        for(int i = index - k % index; i > 0; i--) {
            current = current.next;
        }

        runner.next = start.next;
        start.next = current.next;
        current.next = null;

        return start.next;
    }
}