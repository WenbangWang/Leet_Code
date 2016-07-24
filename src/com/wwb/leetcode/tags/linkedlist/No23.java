package com.wwb.leetcode.tags.linkedlist;

import com.wwb.leetcode.utils.ListNode;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class No23 {

    public ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }

        Queue<ListNode> queue = new PriorityQueue<>(lists.length, new Comparator<ListNode>() {
            @Override
            public int compare(ListNode o1, ListNode o2) {
                return Integer.compare(o1.val, o2.val);
            }
        });

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
