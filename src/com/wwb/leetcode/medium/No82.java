package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a sorted linked list, delete all nodes that have duplicate numbers,
 * leaving only distinct numbers from the original list.
 *
 * For example,
 * Given 1->2->3->3->4->4->5, return 1->2->5.
 * Given 1->1->1->2->3, return 2->3.
 */
public class No82 {

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
        ListNode previous = start;
        start.next = head;

        while(current != null) {
            while(current.next != null && current.next.val == current.val) {
                current = current.next;
            }

            if(previous.next == current) {
                previous = previous.next;
            } else {
                previous.next = current.next;
            }

            current = current.next;
        }

        return start.next;
    }

    private ListNode solution2(ListNode head) {
        if(head == null) {
            return null;
        }

        if(head.next != null && head.val == head.next.val) {
            while(head.next != null && head.val == head.next.val) {
                head = head.next;
            }

            return solution2(head.next);
        } else {
            head.next = solution2(head.next);
        }

        return head;
    }
}