package com.wwb.leetcode.hard;

import com.wwb.leetcode.utils.ListNode;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * You are given an array of k linked-lists lists, each linked-list is sorted in ascending order.
 * <p>
 * Merge all the linked-lists into one sorted linked-list and return it.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: lists = [[1,4,5],[1,3,4],[2,6]]
 * <p>
 * Output: [1,1,2,3,4,4,5,6]
 * <p>
 * Explanation: The linked-lists are:
 * <div>
 * [
 *   1->4->5,
 *   1->3->4,
 *   2->6
 * ]
 * </div>
 * merging them into one sorted list:
 * <p>
 * 1->1->2->3->4->4->5->6
 * <p>
 * Example 2:
 * <p>
 * Input: lists = []
 * <p>
 * Output: []
 * <p>
 * Example 3:
 * <p>
 * <p>
 * Input: lists = [[]]
 * <p>
 * Output: []
 * <p>
 * <p>
 * Constraints:
 * <p>
 * k == lists.length
 * 0 <= k <= 10^4
 * 0 <= lists[i].length <= 500
 * -10^4 <= lists[i][j] <= 10^4
 * lists[i] is sorted in ascending order.
 * The sum of lists[i].length will not exceed 10^4.
 */
public class No23 {

    public ListNode mergeKLists(ListNode[] lists) {
        return solution1(lists);
    }

    private ListNode solution1(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }

        Queue<ListNode> queue = new PriorityQueue<>(lists.length, Comparator.comparingInt(o -> o.val));

        for (ListNode list : lists) {
            if (list != null) {
                queue.add(list);
            }
        }

        ListNode start = new ListNode(0);
        ListNode current = start;

        while (!queue.isEmpty()) {
            ListNode node = queue.poll();

            if (node.next != null) {
                queue.add(node.next);
            }

            current.next = node;
            current = node;
        }

        return start.next;
    }

    private ListNode solution2(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }

        return merge(lists, 0, lists.length - 1);
    }

    private ListNode merge(ListNode[] lists, int start, int end) {
        if (start == end) {
            return lists[start];
        }

        if (start + 1 == end) {
            return merge(lists[start], lists[end]);
        }

        int mid = start + (end - start) / 2;

        return merge(merge(lists, start, mid), merge(lists, mid + 1, end));
    }

    private ListNode merge(ListNode l1, ListNode l2) {
        ListNode start = new ListNode(-1);
        ListNode current = start;

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

        current.next = l1 == null ? l2 : l1;

        return start.next;
    }
}
