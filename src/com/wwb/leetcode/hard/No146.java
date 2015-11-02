package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Design and implement a data structure for Least Recently Used (LRU) cache.
 * It should support the following operations: get and set.

 get(key) - Get the value (will always be positive) of the key if the key exists in the cache, otherwise return -1.
 set(key, value) - Set or insert the value if the key is not already present.
 When the cache reached its capacity, it should invalidate the least recently used item before inserting a new item.
 */
public class No146 {

    public class LRUCache {

        private int capacity;
        private int size;
        private Node head;
        private Node tail;
        private Map<Integer, Node> map;
        public LRUCache(int capacity) {
            this.head = new Node();
            this.tail = new Node();
            this.map = new HashMap<>();

            this.capacity = capacity;
            this.size = 0;
            this.head.next = tail;
            this.tail.pre = head;
        }

        public int get(int key) {
            Node node = this.map.get(key);

            if(node == null) {
                return -1;
            }

            this.moveToHead(node);

            return node.value;
        }

        public void set(int key, int value) {
            Node node = this.map.get(key);

            if(node == null) {
                node = new Node();
                node.key = key;
                node.value = value;

                this.size++;
                this.map.put(key, node);
                this.addHead(node);

                if(this.size > this.capacity) {
                    this.map.remove(this.tail.pre.key);
                    this.removeLast();
                    this.size--;
                }
            } else {
                node.value = value;
                this.moveToHead(node);
            }
        }

        private void remove(Node node) {
            node.pre.next = node.next;
            node.next.pre = node.pre;
        }

        private void removeLast() {
            this.remove(tail.pre);
        }

        private void moveToHead(Node node) {
            this.remove(node);
            this.addHead(node);
        }

        private void addHead(Node node) {
            node.next = this.head.next;
            node.pre = this.head;
            this.head.next.pre = node;
            this.head.next = node;
        }

        private class Node {
            int key;
            int value;
            Node pre;
            Node next;
        }
    }
}
