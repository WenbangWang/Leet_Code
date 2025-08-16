package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.ListNode;

/**
 * Given a node from a cyclic linked list which is sorted in ascending order,
 * write a function to insert a value into the list such that it remains a cyclic sorted list.
 * The given node can be a reference to any single node in the list,
 * and may not be necessarily the smallest value in the cyclic list.
 * <p>
 * If there are multiple suitable places for insertion,
 * you may choose any place to insert the new value. After the insertion, the cyclic list should remain sorted.
 * <p>
 * If the list is empty (i.e., given node is null), you should create a new single cyclic list
 * and return the reference to that single node. Otherwise, you should return the original given node.
 * <p>
 * The following example may help you understand the problem better:
 * <p>
 * <img src="../doc-files/708_1.jpg" />
 * 1 ----->
 * |       |
 * |       |
 * 4 <----- 3 <----- head
 * In the figure above, there is a cyclic sorted list of three elements. You are given a reference to the node with value 3, and we need to insert 2 into the list.
 * <p>
 * <img src="../doc-files/708_2.jpg" />
 * <p>
 * The new node should insert between node 1 and node 3. After the insertion, the list should look like this, and we should still return node 3.
 */
public class No708 {
    public ListNode insert(ListNode head, int insertValue) {
        if (head == null) {
            ListNode newNode = new ListNode(insertValue);
            newNode.next = newNode;
            return newNode;
        }
        ListNode pre = null;
        ListNode current = head;

        while (pre != head) {
            if (pre != null) {
                if (pre.val <= insertValue && current.val <= insertValue ||
                    (pre.val > current.val && (insertValue >= pre.val || insertValue <= current.val))) {
                    pre.next = new ListNode(insertValue);
                    pre.next.next = current;
                    break;
                }
            }

            pre = current;
            current = current.next;
        }

        return head;
    }
}
