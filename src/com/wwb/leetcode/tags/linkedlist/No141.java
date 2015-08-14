package com.wwb.leetcode.tags.linkedlist;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a linked list, determine if it has a cycle in it.
 *
 * Follow up:
 * Can you solve it without using extra space?
 */
public class No141 {

    public boolean hasCycle(ListNode head) {
        if(head == null || head.next == null) {
            return false;
        }

        ListNode current = head;
        ListNode runner = current.next.next;

        while(runner != null && runner.next != null) {
            if(current == runner) {
                return true;
            }

            current = current.next;
            runner = runner.next.next;
        }

        return false;
    }
}