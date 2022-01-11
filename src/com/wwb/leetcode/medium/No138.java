package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.RandomListNode;

import java.util.HashMap;
import java.util.Map;

/**
 * A linked list is given such that each node contains an additional random pointer which could point to any node in the list or null.
 *
 * Return a deep copy of the list.
 *                   0        1      2      3     4
 * Input: head = [[7,null],[13,0],[11,4],[10,2],[1,0]]
 *                    |        |     |            |
 *                    ---------       -------------
 * Output: [[7,null],[13,0],[11,4],[10,2],[1,0]]
 */
public class No138 {

    public RandomListNode copyRandomList(RandomListNode head) {
        return solution1(head);
        // return solution2(head);
    }

    private RandomListNode solution1(RandomListNode head) {
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

    private RandomListNode solution2(RandomListNode head) {
        RandomListNode current = head;

        if(head == null) {
            return null;
        }

        /* Step 1. create clones of each node and
            insert them next to the original node.
            List [1,2,3] will look like [1,1,2,2,3,3]
        */
        while(current != null){
            /*create node. */
            RandomListNode newNode = new RandomListNode(current.label);

            /*Insert to the next of current */
            newNode.next = current.next;
            current.next = newNode;

            current = newNode.next;
        }

        /* Step 2. Copy the random pointers.
        Cloned nodes random will point to the next of the
        original node.
        */
        current = head;
        while(current != null){
            if(current.random != null){
                /* current.next is cloned node. its random points
                to next of current nodes random. */
                current.next.random = current.random.next;
            }
            current = current.next.next;
        }

        current = head;
        RandomListNode newHead = head.next;
        /* Step 3 : Detach the cloned list from the original list */
        while(current != null){
            RandomListNode node = current.next;
            current.next = current.next.next;
            /* IMPORTANT: Check for the last node. */
            if(current.next != null){
                node.next = current.next.next;
            }
            current = current.next;
        }

        return newHead;
    }
}
