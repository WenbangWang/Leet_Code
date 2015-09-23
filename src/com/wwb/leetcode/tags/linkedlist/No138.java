package com.wwb.leetcode.tags.linkedlist;

import com.wwb.leetcode.utils.RandomListNode;

import java.util.HashMap;
import java.util.Map;

/**
 * A linked list is given such that each node contains an additional random pointer which could point to any node in the list or null.
 *
 * Return a deep copy of the list.
 */
public class No138 {

    public RandomListNode copyRandomList(RandomListNode head) {
        Map<RandomListNode, RandomListNode> map = new HashMap<>();

        return copyRandomList(head, map);
    }

    private RandomListNode copyRandomList(RandomListNode node,  Map<RandomListNode, RandomListNode> map) {
        if(node == null) {
            return null;
        }

        if(map.containsKey(node)) {
            return map.get(node);
        }

        RandomListNode newNode = new RandomListNode(node.label);
        map.put(node, newNode);
        newNode.next = copyRandomList(node.next, map);
        newNode.random = copyRandomList(node.random, map);

        return newNode;
    }
}
