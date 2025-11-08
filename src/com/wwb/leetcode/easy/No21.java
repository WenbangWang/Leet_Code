package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.ListNode;

/**
 * Merge two sorted linked lists and return it as a new list.
 * The new list should be made by splicing together the nodes of the first two lists.
 */
public class No21 {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        return solution1(l1, l2);
    }

    private ListNode solution1(ListNode l1, ListNode l2) {
        ListNode head = new ListNode(0);
        ListNode current = head;

        while (l1 != null || l2 != null) {
            int l1Val = l1 == null ? Integer.MAX_VALUE : l1.val;
            int l2Val = l2 == null ? Integer.MAX_VALUE : l2.val;

            if (l1Val < l2Val) {
                current.next = new ListNode(l1Val);
                l1 = l1.next;
            } else {
                current.next = new ListNode(l2Val);
                l2 = l2.next;
            }

            current = current.next;
        }

        return head.next;
    }

    private ListNode solution2(ListNode l1, ListNode l2) {
        if(l1 == null) {
            return l2;
        }

        if(l2 == null) {
            return l1;
        }

        ListNode head;

        if(l1.val < l2.val) {
            head = new ListNode(l1.val);
            head.next = solution2(l1.next, l2);
        } else {
            head = new ListNode(l2.val);
            head.next = solution2(l1, l2.next);
        }

        return head;
    }

    private ListNode solution3(ListNode l1, ListNode l2) {
        ListNode head = new ListNode(-1);
        ListNode current = head;

        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                current.next = l1;
                l1 = l1.next;
            } else {
                current.next = l2;
                l2 = l2.next;
            }

            current = current.next;
        }

        if (l1 == null) {
            current.next = l2;
        }

        if (l2 == null) {
            current.next = l1;
        }

        return head.next;
    }
}
