package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.ListNode;

/**
 * Reverse a singly linked list.
 */
public class No206 {

    public ListNode reverseList(ListNode head) {
        return solution1(head);
    }

    private ListNode solution1(ListNode head) {
        ListNode newHead = null;

        while(head != null) {
            ListNode next = head.next;
            head.next = newHead;
            newHead = head;
            head = next;
        }

        return newHead;
    }

    private ListNode solution2(ListNode head) {
        return solution2(head, null);
    }

    private ListNode solution2(ListNode head, ListNode newHead) {
        if(head == null) {
            return newHead;
        }

        ListNode next = head.next;
        head.next = newHead;

        return solution2(next, head);
    }
}