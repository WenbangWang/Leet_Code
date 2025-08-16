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

        current.next = null;

        return start.next;
    }
}
