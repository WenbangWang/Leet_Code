package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a singly linked list L: L0→L1→…→Ln-1→Ln,
 * reorder it to: L0→Ln→L1→Ln-1→L2→Ln-2→…
 *
 * You must do this in-place without altering the nodes' values.
 *
 * For example,
 * Given {1,2,3,4}, reorder it to {1,4,2,3}.
 */
public class No143 {

    public void reorderList(ListNode head) {
        if(head == null || head.next == null) {
            return;
        }

        ListNode current = head;
        ListNode runner = head;

        while(runner.next != null && runner.next.next != null) {
            current = current.next;
            runner = runner.next.next;
        }

        ListNode preMiddle = current;
        ListNode preCurrent = preMiddle.next;

        while(preCurrent.next != null) {
            ListNode node = preCurrent.next;
            preCurrent.next = node.next;
            node.next = preMiddle.next;
            preMiddle.next = node;
        }

        current = head;
        runner = preMiddle.next;

        while(current != preMiddle) {
            preMiddle.next = runner.next;
            runner.next = current.next;
            current.next = runner;
            current = runner.next;
            runner = preMiddle.next;
        }
    }
}