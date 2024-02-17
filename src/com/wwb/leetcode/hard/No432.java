package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Design a data structure to store the strings' count with the ability to
 * return the strings with minimum and maximum counts.
 *
 * Implement the AllOne class:
 *
 * AllOne() Initializes the object of the data structure.
 * inc(String key) Increments the count of the string key by 1.
 * If key does not exist in the data structure, insert it with count 1.
 *
 * dec(String key) Decrements the count of the string key by 1.
 * If the count of key is 0 after the decrement, remove it from the data structure.
 * It is guaranteed that key exists in the data structure before the decrement.
 *
 * getMaxKey() Returns one of the keys with the maximal count. If no element exists, return an empty string "".
 * getMinKey() Returns one of the keys with the minimum count. If no element exists, return an empty string "".
 * Note that each function must run in O(1) average time complexity.
 *
 *
 *
 * Example 1:
 *
 * Input
 * ["AllOne", "inc", "inc", "getMaxKey", "getMinKey", "inc", "getMaxKey", "getMinKey"]
 * [[], ["hello"], ["hello"], [], [], ["leet"], [], []]
 * Output
 * [null, null, null, "hello", "hello", null, "hello", "leet"]
 *
 * Explanation
 * AllOne allOne = new AllOne();
 * allOne.inc("hello");
 * allOne.inc("hello");
 * allOne.getMaxKey(); // return "hello"
 * allOne.getMinKey(); // return "hello"
 * allOne.inc("leet");
 * allOne.getMaxKey(); // return "hello"
 * allOne.getMinKey(); // return "leet"
 *
 *
 * Constraints:
 *
 * 1 <= key.length <= 10
 * key consists of lowercase English letters.
 * It is guaranteed that for each call to dec, key is existing in the data structure.
 * At most 5 * 10^4 calls will be made to inc, dec, getMaxKey, and getMinKey.
 */
public class No432 {
    public static class AllOne {
        private Map<String, Node> keyToNode;
        private Node head;
        private Node tail;


        public AllOne() {
            this.head = new Node(0);
            this.tail = new Node(0);
            this.keyToNode = new HashMap<>();

            this.head.next = this.tail;
            this.tail.pre = this.head;
        }

        public void inc(String key) {
            Node node = this.keyToNode.getOrDefault(key, this.head);

            node.keys.remove(key);
            Node nextNode;

            if (node.next == this.tail || node.next.count != node.count + 1) {
                nextNode = new Node(node.count + 1);
                nextNode.keys.add(key);

                insertAfter(node, nextNode);
            } else {
                nextNode = node.next;
                nextNode.keys.add(key);
            }

            detachOldNodeIfNeeded(node);
            this.keyToNode.put(key, nextNode);
        }

        public void dec(String key) {
            if (this.keyToNode.containsKey(key)) {
                Node node = this.keyToNode.get(key);

                node.keys.remove(key);

                if (node.count == 1) {
                    this.keyToNode.remove(key);
                } else {
                    Node preNode;
                    if (node.pre == this.head || node.pre.count != node.count - 1) {
                        preNode = new Node(node.count - 1);
                        preNode.keys.add(key);

                        insertBefore(node, preNode);
                    } else {
                        preNode = node.pre;
                        preNode.keys.add(key);
                    }

                    this.keyToNode.put(key, preNode);
                }

                detachOldNodeIfNeeded(node);
            }
        }

        public String getMaxKey() {
            return this.tail.pre == this.head ? "" : this.tail.pre.keys.iterator().next();
        }

        public String getMinKey() {
            return this.head.next == this.tail ? "" : this.head.next.keys.iterator().next();
        }

        private void insertAfter(Node node, Node newNode) {
            newNode.pre = node;
            newNode.next = node.next;
            node.next.pre = newNode;
            node.next = newNode;
        }

        private void insertBefore(Node node, Node newNode) {
            newNode.next = node;
            newNode.pre = node.pre;
            node.pre.next = newNode;
            node.pre = newNode;
        }

        private void detachOldNodeIfNeeded(Node oldNode) {
            if (oldNode != this.head && oldNode.keys.isEmpty()) {
                oldNode.pre.next = oldNode.next;
                oldNode.next.pre = oldNode.pre;
                oldNode.pre = null;
                oldNode.next = null;
            }
        }

        private static class Node {
            int count;
            Node pre;
            Node next;
            Set<String> keys;

            Node(int count) {
                this.keys = new HashSet<>();
                this.count = count;
            }
        }
    }

/**
 * Your AllOne object will be instantiated and called as such:
 * AllOne obj = new AllOne();
 * obj.inc(key);
 * obj.dec(key);
 * String param_3 = obj.getMaxKey();
 * String param_4 = obj.getMinKey();
 */
}
