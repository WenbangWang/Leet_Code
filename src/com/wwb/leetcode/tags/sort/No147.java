package com.wwb.leetcode.tags.sort;

import com.wwb.leetcode.utils.ListNode;

/**
 * Sort a linked list using insertion sort.
 */
public class No147 {

    public ListNode insertionSortList(ListNode head) {
        if(head == null) {
            return null;
        }

        ListNode start = new ListNode(0);
        ListNode pre = start;
        ListNode current = head;

        while(current != null) {
            ListNode next = current.next;

            while(pre.next != null && pre.next.val < current.val) {
                pre = pre.next;
            }

            current.next = pre.next;
            pre.next = current;
            pre = start;
            current = next;
        }

        return start.next;
    }
}