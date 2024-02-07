package com.wwb.leetcode.hard;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;

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
    private static class AllOne {
        private Map<Integer, KeyNode> keysGroupByCount;
        private Map<String, Integer> countGroupByKey;
        private KeyNode head;
        private KeyNode tail;


        public AllOne() {
            this.head = new KeyNode(-1);
            this.tail = new KeyNode(-1);
            this.keysGroupByCount = new HashMap<>();
            this.countGroupByKey = new HashMap<>();

            this.head.next = this.tail;
            this.tail.pre = this.head;
        }

        public void inc(String key) {
            if (countGroupByKey.containsKey(key)) {
                updateKeyWhenExists(key, 1, node -> node);
            } else {
                updateMapsWithNewCount(key, n -> this.head, null, 1);
            }
        }

        public void dec(String key) {
            if (countGroupByKey.containsKey(key)) {
                updateKeyWhenExists(key, -1, node -> node.pre);
            }
        }

        public String getMaxKey() {
            return this.tail.pre == this.head ? "" : this.tail.pre.keys.iterator().next();
        }

        public String getMinKey() {
            return this.head.next == this.tail ? "" : this.head.next.keys.iterator().next();
        }

        private void updateKeyWhenExists(String key, int delta, Function<KeyNode, KeyNode> newNodeInsertionFunc) {
            var oldNode = keysGroupByCount.get(countGroupByKey.get(key));
            int newCount = oldNode.count + delta;

            updateMapsWithNewCount(key, newNodeInsertionFunc, oldNode, newCount);


            oldNode.keys.remove(key);
            detachOldNodeIfNeeded(oldNode);
        }

        private void insertAfter(KeyNode keyNode, KeyNode newKeyNode) {
            newKeyNode.pre = keyNode;
            newKeyNode.next = keyNode.next;
            keyNode.next.pre = newKeyNode;
            keyNode.next = newKeyNode;
        }

        private void detachOldNodeIfNeeded(KeyNode oldNode) {
            if (oldNode.keys.isEmpty()) {
                oldNode.pre.next = oldNode.next;
                oldNode.next.pre = oldNode.pre;
                oldNode.pre = null;
                oldNode.next = null;

                keysGroupByCount.remove(oldNode.count);
            }
        }

        private void updateMapsWithNewCount(String key, Function<KeyNode, KeyNode> newNodeInsertionFunc, KeyNode keyNode, int newCount) {
            if (newCount == 0) {
                countGroupByKey.remove(key);
            } else {
                keysGroupByCount.computeIfAbsent(newCount, c -> {
                    var newKeyNode = new KeyNode(c);
                    insertAfter(newNodeInsertionFunc.apply(keyNode), newKeyNode);

                    return newKeyNode;
                });

                keysGroupByCount.get(newCount).keys.add(key);
                countGroupByKey.put(key, newCount);
            }
        }


        private static class KeyNode {
            int count;
            KeyNode pre;
            KeyNode next;
            LinkedHashSet<String> keys;

            KeyNode(int count) {
                this.keys = new LinkedHashSet<>();
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
