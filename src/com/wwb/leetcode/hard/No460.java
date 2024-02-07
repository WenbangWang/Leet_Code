package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Design and implement a data structure for a Least Frequently Used (LFU) cache.
 *
 * Implement the LFUCache class:
 *
 * LFUCache(int capacity) Initializes the object with the capacity of the data structure.
 * int get(int key) Gets the value of the key if the key exists in the cache. Otherwise, returns -1.
 * void put(int key, int value) Update the value of the key if present, or inserts the key if not already present.
 * When the cache reaches its capacity, it should invalidate and remove the least frequently used
 * key before inserting a new item.
 * For this problem, when there is a tie (i.e., two or more keys with the same frequency),
 * the least recently used key would be invalidated.
 * To determine the least frequently used key, a use counter is maintained for each key in the cache.
 * The key with the smallest use counter is the least frequently used key.
 *
 * When a key is first inserted into the cache, its use counter is set to 1 (due to the put operation).
 * The use counter for a key in the cache is incremented either a get or put operation is called on it.
 *
 * The functions get and put must each run in O(1) average time complexity.
 *
 *
 *
 * Example 1:
 *
 * Input
 * ["LFUCache", "put", "put", "get", "put", "get", "get", "put", "get", "get", "get"]
 * [[2], [1, 1], [2, 2], [1], [3, 3], [2], [3], [4, 4], [1], [3], [4]]
 * Output
 * [null, null, null, 1, null, -1, 3, null, -1, 3, 4]
 *
 * Explanation
 * // cnt(x) = the use counter for key x
 * // cache=[] will show the last used order for tiebreakers (leftmost element is  most recent)
 * LFUCache lfu = new LFUCache(2);
 * lfu.put(1, 1);   // cache=[1,_], cnt(1)=1
 * lfu.put(2, 2);   // cache=[2,1], cnt(2)=1, cnt(1)=1
 * lfu.get(1);      // return 1
 *                  // cache=[1,2], cnt(2)=1, cnt(1)=2
 * lfu.put(3, 3);   // 2 is the LFU key because cnt(2)=1 is the smallest, invalidate 2.
 *                  // cache=[3,1], cnt(3)=1, cnt(1)=2
 * lfu.get(2);      // return -1 (not found)
 * lfu.get(3);      // return 3
 *                  // cache=[3,1], cnt(3)=2, cnt(1)=2
 * lfu.put(4, 4);   // Both 1 and 3 have the same cnt, but 1 is LRU, invalidate 1.
 *                  // cache=[4,3], cnt(4)=1, cnt(3)=2
 * lfu.get(1);      // return -1 (not found)
 * lfu.get(3);      // return 3
 *                  // cache=[3,4], cnt(4)=1, cnt(3)=3
 * lfu.get(4);      // return 4
 *                  // cache=[4,3], cnt(4)=2, cnt(3)=3
 *
 *
 * Constraints:
 *
 * 0 <= capacity <= 10^4
 * 0 <= key <= 10^5
 * 0 <= value <= 10^9
 * At most 2 * 10^5 calls will be made to get and put.
 */
public class No460 {
    class LFUCache {
        private Map<Integer, Integer>  map;
        private Map<Integer, Node> keyToNode;
        private int capacity;
        private Node start;

        public LFUCache(int capacity) {
            this.capacity = capacity;
            this.map = new HashMap<>();
            this.keyToNode = new HashMap<>();
            this.start  = new Node();
        }

        public int get(int key) {
            if (!map.containsKey(key)) {
                return -1;
            }

            bumpCountOrInsert(key);

            return this.map.get(key);
        }

        public void put(int key, int value) {
            if (this.capacity == 0) {
                return;
            }

            if (this.map.size() == this.capacity && !this.map.containsKey(key)) {
                removeLFU();
            }

            this.map.put(key, value);

            bumpCountOrInsert(key);
        }

        private void removeLFU() {
            var lfuNode = this.start.next;

            if (lfuNode != null) {
                var keyToRemove = lfuNode.keys.iterator().next();
                removeKeyFromNode(keyToRemove, lfuNode);

                this.map.remove(keyToRemove);
            }
        }

        private void bumpCountOrInsert(int key) {
            var node = this.keyToNode.getOrDefault(key, this.start);

            Node nextNode;

            if (node.next == null || node.next.count != node.count + 1) {
                nextNode = new Node();
                nextNode.count = node.count + 1;
                nextNode.prev = node;
                nextNode.next = node.next;

                if (node.next != null) {
                    node.next.prev = nextNode;
                }
                node.next = nextNode;
            } else {
                nextNode = node.next;
            }

            nextNode.keys.add(key);
            removeKeyFromNode(key, node);
            this.keyToNode.put(key, nextNode);
        }

        private void removeKeyFromNode(int key, Node node) {
            node.keys.remove(key);

            if (node != this.start && node.keys.isEmpty()) {
                node.prev.next = node.next;

                if (node.next != null) {
                    node.next.prev = node.prev;
                }
            }

            this.keyToNode.remove(key, node);
        }
    }

    private static class Node {
        private int count;
        private LinkedHashSet<Integer> keys = new LinkedHashSet<>();
        private Node next;
        private Node prev;
    }

/**
 * Your LFUCache object will be instantiated and called as such:
 * LFUCache obj = new LFUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */
}
