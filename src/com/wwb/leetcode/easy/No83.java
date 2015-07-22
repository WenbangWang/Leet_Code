package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a sorted linked list, delete all duplicates such that each element appear only once.
 *
 * For example,
 * Given 1->1->2, return 1->2.
 * Given 1->1->2->3->3, return 1->2->3.
 */
public class No83 {

    public ListNode deleteDuplicates(ListNode head) {
//        return solution1(head);
        return solution2(head);
    }

    private ListNode solution1(ListNode head) {
        if(head == null) {
            return null;
        }

        ListNode start = new ListNode(0);
        ListNode current = head;

        start.next = head;

        while(current != null) {
            while(current.next != null && current.val == current.next.val) {
                current.next = current.next.next;
            }

            current = current.next;
        }

        return start.next;
    }

    private ListNode solution2(ListNode head) {
        if(head == null) {
            return null;
        }

        while(head.next != null && head.val == head.next.val) {
            head.next = head.next.next;
        }

        head.next = solution2(head.next);

        return head;
    }
}