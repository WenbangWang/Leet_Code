package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a linked list, swap every two adjacent nodes and return its head.
 *
 * For example,
 *  * Given 1->2->3->4, you should return the list as 2->1->4->3.
 *
 * Your algorithm should use only constant space.
 * You may not modify the values in the list, only nodes itself can be changed.
 */
public class No24 {

    public ListNode swapPairs(ListNode head) {
//       return solution1(head);
        return solution2(head);
    }

    private ListNode solution1(ListNode head) {
        ListNode start = new ListNode(0);
        start.next = head;

        for(ListNode current = start; current.next != null && current.next.next != null; current = current.next.next) {
            ListNode first = current.next;
            ListNode second = current.next.next;

            first.next = second.next;
            second.next = first;
            current.next = second;
        }

        return start.next;
    }

    private ListNode solution2(ListNode head) {
        if(head == null || head.next == null) {
            return head;
        }

        ListNode first = head;
        ListNode second = head.next;

        first.next = solution1(second.next);
        second.next = first;

        return second;
    }
}
