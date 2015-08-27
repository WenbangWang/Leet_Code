package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.ListNode;

/**
 * Remove all elements from a linked list of integers that have value val.
 *
 * Example
 * Given: 1 --> 2 --> 6 --> 3 --> 4 --> 5 --> 6, val = 6
 * Return: 1 --> 2 --> 3 --> 4 --> 5
 */
public class No203 {

    public ListNode removeElements(ListNode head, int val) {
        if(head == null) {
            return null;
        }

        ListNode start = new ListNode(0);
        ListNode pre = start;
        ListNode current = head;
        start.next = head;

        while(current != null) {

            if(current.val == val) {
                pre.next = current.next;
            } else {
                pre = pre.next;
            }

            current = current.next;
        }

        return start.next;
    }
}