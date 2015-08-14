package com.wwb.leetcode.tags.linkedlist;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a linked list, return the node where the cycle begins. If there is no cycle, return null.
 *
 * Follow up:
 * Can you solve it without using extra space?
 */
public class No142 {

    public ListNode detectCycle(ListNode head) {
        if(head == null || head.next == null) {
            return null;
        }

        ListNode current = head;
        ListNode runner = current;

        while(runner != null && runner.next != null) {
            current = current.next;
            runner = runner.next.next;

            if(current == runner) {
                break;
            }
        }

        if(runner == null || runner.next == null) {
            return null;
        }

        current = head;

        while(current != runner) {
            current = current.next;
            runner = runner.next;
        }

        return current;
    }
}