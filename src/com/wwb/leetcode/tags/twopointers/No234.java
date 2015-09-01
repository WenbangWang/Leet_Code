package com.wwb.leetcode.tags.twopointers;

import com.wwb.leetcode.utils.ListNode;

import java.util.Stack;

/**
 * Given a singly linked list, determine if it is a palindrome.
 *
 * Follow up:
 * Could you do it in O(n) time and O(1) space?
 */
public class No234 {

    public boolean isPalindrome(ListNode head) {
//        return solution1(head);
        return solution2(head);
    }

    private boolean solution1(ListNode head) {
        if(head == null) {
            return true;
        }

        Stack<ListNode> stack = new Stack<>();
        ListNode current = head;
        int size = 0;

        while(current != null) {
            stack.push(current);
            size++;
            current = current.next;
        }

        current = head;

        for(int i = 0; i < size / 2; i++) {
            if(current.val != stack.pop().val) {
                return false;
            }
            current = current.next;
        }

        return true;
    }

    private boolean solution2(ListNode head) {
        if(head == null) {
            return true;
        }

        ListNode current = head;
        ListNode newHead = null;
        ListNode runner = head;

        while(runner != null && runner.next != null) {
            runner = runner.next.next;

            ListNode next = current.next;
            current.next = newHead;
            newHead = current;
            current = next;
        }

        if(runner != null) {
            current = current.next;
        }

        while(newHead != null && current != null) {
            if(newHead.val != current.val) {
                return false;
            }

            newHead = newHead.next;
            current = current.next;
        }

        return true;
    }
}