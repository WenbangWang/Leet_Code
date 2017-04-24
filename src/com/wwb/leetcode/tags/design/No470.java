package com.wwb.leetcode.tags.design;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class No470 {
    public class LFUCache {
        private int capacity;
        private Node head;
        private Node tail;
        private Map<Integer, Integer> keyValueMap;
        private Map<Integer, Node> keyNodeMap;

        public LFUCache(int capacity) {
            this.capacity = capacity;
            this.head = new Node(0);
            this.tail = new Node(0);
            this.keyNodeMap = new HashMap<>();
            this.keyValueMap = new HashMap<>();

            this.head.next = this.tail;
            this.tail.pre = this.head;
        }

        public int get(int key) {
            if(this.keyValueMap.containsKey(key)) {
                this.increaseFrequency(key);
                return this.keyValueMap.get(key);
            }

            return -1;
        }

        public void put(int key, int value) {
            if(this.capacity == 0) {
                return;
            }

            if(!this.keyValueMap.containsKey(key)) {
                if(this.capacity == this.keyValueMap.size()) {
                    this.removeLeastFrequency();
                }

                this.addToHead(key);
            }

            this.keyValueMap.put(key, value);
            this.increaseFrequency(key);
        }

        private void increaseFrequency(int key) {
            Node node = this.keyNodeMap.get(key);
            node.keys.remove(key);

            if(node.next.count == node.count + 1) {
                node.next.keys.add(key);
            } else {
                Node nextNode = new Node(node.count + 1);
                nextNode.keys.add(key);
                nextNode.next = node.next;
                node.next.pre = nextNode;
                nextNode.pre = node;
                node.next = nextNode;
            }

            this.keyNodeMap.put(key, node.next);

            if(node.keys.isEmpty()) {
                this.removeNode(node);
            }
        }

        private void removeNode(Node node) {
            node.pre.next = node.next;
            node.next.pre = node.pre;
        }

        private void removeLeastFrequency() {
            Node leastFrequencyNode = this.head.next;
            int leastFrequencyKey = leastFrequencyNode.keys.iterator().next();
            leastFrequencyNode.keys.remove(leastFrequencyKey);

            if(leastFrequencyNode.keys.isEmpty()) {
                this.removeNode(leastFrequencyNode);
            }

            this.keyValueMap.remove(leastFrequencyKey);
            this.keyNodeMap.remove(leastFrequencyKey);
        }

        private void addToHead(int key) {
            Node node;

            if(this.head.next.count == 1) {
                (node = this.head.next).keys.add(key);
            } else {
                node = new Node(1);
                node.keys.add(key);

                node.next = this.head.next;
                this.head.next.pre = node;
                node.pre = this.head;
            }

            this.keyNodeMap.put(key, node);
        }

        private class Node {
            int count;
            LinkedHashSet<Integer> keys;
            Node next;
            Node pre;

            Node(int count) {
                this.count = count;
                this.keys = new LinkedHashSet<>();
                this.next = null;
                this.pre = null;
            }
        }
    }
/**
 * Your LFUCache object will be instantiated and called as such:
 * LFUCache obj = new LFUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */
}
