package com.wwb.leetcode.tags.linkedlist;

import com.wwb.leetcode.utils.ListNode;

/**
 * Sort a linked list in O(n log n) time using constant space complexity.
 */
public class No148 {

    public ListNode sortList(ListNode head) {
        if(head == null || head.next == null) {
            return head;
        }

        ListNode current = head;
        ListNode runner = head.next.next;

        while(runner != null && runner.next != null) {
            current = current.next;
            runner = runner.next.next;
        }

        ListNode anotherHead = sortList(current.next);
        current.next = null;

        return merge(sortList(head), anotherHead);
    }

    private ListNode merge(ListNode head1, ListNode head2) {
        ListNode start = new ListNode(Integer.MIN_VALUE);
        ListNode current = start;

        while(head1 != null && head2 != null) {
            if(head1.val > head2.val) {
                current.next = head2;
                head2 = head2.next;
            } else {
                current.next = head1;
                head1 = head1.next;
            }

            current = current.next;
        }

        if(head1 != null) {
            current.next = head1;
        }

        if(head2 != null) {
            current.next = head2;
        }

        return start.next;
    }
}