package com.wwb.leetcode.tags.linkedlist;

import com.wwb.leetcode.utils.ListNode;

/**
 * You are given two linked lists representing two non-negative numbers.
 * The digits are stored in reverse order and each of their nodes contain a single digit. Add the two numbers and return it as a linked list.
 *
 * Input: (2 -> 4 -> 3) + (5 -> 6 -> 4)
 * Output: 7 -> 0 -> 8
 */
public class No2 {

    /**
     * Definition for singly-linked list.
     * public class ListNode {
     *     int val;
     *     ListNode next;
     *     ListNode(int x) { val = x; }
     * }
     */
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if(l1 == null && l2 != null) {
            return l2;
        } else if(l1 != null && l2 == null) {
            return l1;
        } else if(l1 == null && l2 == null) {
            return null;
        }

        // return solution1(l1, l2);
        return solution2(l1, l2, 0);
    }

    private ListNode solution1(ListNode l1, ListNode l2) {
        ListNode result = new ListNode(-1);
        ListNode current = result;
        int carry = 0;

        while(l1 != null || l2 != null) {
            int sum = carry;

            if(l1 != null) {
                sum += l1.val;
                l1 = l1.next;
            }
            if(l2 != null) {
                sum += l2.val;
                l2 = l2.next;
            }

            int val = sum % 10;
            carry = sum / 10;

            current.next = new ListNode(val);
            current = current.next;
        }

        if(carry == 1) {
            current.next = new ListNode(carry);
        }

        return result.next;
    }

    private ListNode solution2(ListNode l1, ListNode l2, int carry) {
        if(l1 == null) {
            return carry == 0 ? l2 : solution2(new ListNode(1), l2, 0);
        }

        if(l2 == null) {
            return carry == 0 ? l1 : solution2(new ListNode(1), l1, 0);
        }

        int sum = l1.val + l2.val + carry;
        int val = sum % 10;

        ListNode result = new ListNode(val);
        result.next = solution2(l1.next, l2.next, sum / 10);

        return result;
    }
}