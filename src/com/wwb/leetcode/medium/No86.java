package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a linked list and a value x, partition it such that all nodes less than x come before nodes greater than or equal to x.
 *
 * You should preserve the original relative order of the nodes in each of the two partitions.
 *
 * For example,
 * Given 1->4->3->2->5->2 and x = 3,
 * return 1->2->2->4->3->5.
 */
public class No86 {

    public ListNode partition(ListNode head, int x) {
//        return solution1(head, x);
        return solution2(head, x);
    }

    private ListNode solution1(ListNode head, int x) {
        if(head == null) {
            return null;
        }

        ListNode firstHalfHead = null;
        ListNode firstHalfTail = null;
        ListNode secondHalfHead = null;
        ListNode secondHalfTail = null;

        while(head != null) {
            if(head.val < x) {
                if(firstHalfHead == null) {
                    firstHalfHead = new ListNode(head.val);
                    firstHalfTail = firstHalfHead;
                } else {
                    firstHalfTail.next = new ListNode(head.val);
                    firstHalfTail = firstHalfTail.next;
                }
            } else {
                if(secondHalfHead == null) {
                    secondHalfHead = new ListNode(head.val);
                    secondHalfTail = secondHalfHead;
                } else {
                    secondHalfTail.next = new ListNode(head.val);
                    secondHalfTail = secondHalfTail.next;
                }
            }

            head = head.next;
        }

        if(firstHalfTail != null) {
            firstHalfTail.next = secondHalfHead;

            return firstHalfHead;
        }

        return secondHalfHead;
    }

    private ListNode solution2(ListNode head, int x) {
        if(head == null) {
            return null;
        }

        ListNode firstHalf = new ListNode(0);
        ListNode secondHalf = new ListNode(0);
        ListNode firstCurrent = firstHalf;
        ListNode secondCurrent = secondHalf;

        while(head != null) {
            if(head.val < x) {
                firstCurrent.next = head;
                firstCurrent = firstCurrent.next;
            } else {
                secondCurrent.next = head;
                secondCurrent = secondCurrent.next;
            }

            head = head.next;
        }

        secondCurrent.next = null;
        firstCurrent.next = secondHalf.next;

        return firstHalf.next;
    }
}