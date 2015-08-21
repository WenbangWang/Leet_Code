package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.ListNode;

/**
 * Write a program to find the node at which the intersection of two singly linked lists begins.
 *
 * For example, the following two linked lists:
 *
 * A:          a1 → a2
 *                    ↘
 *                    c1 → c2 → c3
 *                    ↗
 * B:     b1 → b2 → b3
 * begin to intersect at node c1.
 *
 *
 * Notes:
 *
 * If the two linked lists have no intersection at all, return null.
 * The linked lists must retain their original structure after the function returns.
 * You may assume there are no cycles anywhere in the entire linked structure.
 * Your code should preferably run in O(n) time and use only O(1) memory.
 */
public class No160 {

    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        int lengthA = getLength(headA);
        int lengthB = getLength(headB);

        while(lengthA > lengthB) {
            headA = headA.next;
            lengthA--;
        }

        while(lengthA < lengthB) {
            headB = headB.next;
            lengthB--;
        }

        while(headA != headB) {
            headA = headA.next;
            headB = headB.next;
        }

        return headA;
    }

    private int getLength(ListNode node) {
        ListNode current = node;
        int length = 0;

        while (current != null) {
            current = current.next;
            length++;
        }

        return length;
    }
}