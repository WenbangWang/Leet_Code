package com.wwb.leetcode.easy;

import java.util.*;

/**
 * Design a max stack data structure that supports the stack operations and supports finding the stack's maximum element.
 *
 * Implement the MaxStack class:
 *
 * MaxStack() Initializes the stack object.
 * void push(int x) Pushes element x onto the stack.
 * int pop() Removes the element on top of the stack and returns it.
 * int top() Gets the element on the top of the stack without removing it.
 * int peekMax() Retrieves the maximum element in the stack without removing it.
 * int popMax() Retrieves the maximum element in the stack and removes it. If there is more than one maximum element, only remove the top-most one.
 *
 *
 * Example 1:
 *
 * Input
 * ["MaxStack", "push", "push", "push", "top", "popMax", "top", "peekMax", "pop", "top"]
 * [[], [5], [1], [5], [], [], [], [], [], []]
 * Output
 * [null, null, null, null, 5, 5, 1, 5, 1, 5]
 *
 * Explanation
 * MaxStack stk = new MaxStack();
 * stk.push(5);   // [5] the top of the stack and the maximum number is 5.
 * stk.push(1);   // [5, 1] the top of the stack is 1, but the maximum is 5.
 * stk.push(5);   // [5, 1, 5] the top of the stack is 5, which is also the maximum, because it is the top most one.
 * stk.top();     // return 5, [5, 1, 5] the stack did not change.
 * stk.popMax();  // return 5, [5, 1] the stack is changed now, and the top is different from the max.
 * stk.top();     // return 1, [5, 1] the stack did not change.
 * stk.peekMax(); // return 5, [5, 1] the stack did not change.
 * stk.pop();     // return 1, [5] the top of the stack and the max element is now 5.
 * stk.top();     // return 5, [5] the stack did not change.
 *
 *
 * Constraints:
 *
 * -10^7 <= x <= 10^7
 * At most 104 calls will be made to push, pop, top, peekMax, and popMax.
 * There will be at least one element in the stack when pop, top, peekMax, or popMax is called.
 */
public class No716 {
    static class MaxStack {
        private Node head;
        private Node tail;
        private TreeMap<Integer, LinkedList<Node>> map;

        public MaxStack() {
            this.head = new Node();
            this.tail = new Node();
            this.map = new TreeMap<>();

            this.head.next = this.tail;
            this.tail.pre = this.head;
        }

        public void push(int x) {
            var node = this.appendNode(x);

            this.map.putIfAbsent(x, new LinkedList<>());
            this.map.get(x).add(node);
        }

        public int pop() {
            var node = this.tail.pre;

            this.removeNode(node);

            return node.value;
        }

        public int top() {
            return this.tail.pre.value;
        }

        public int peekMax() {
            return this.map.lastKey();
        }

        public int popMax() {
            var max = this.map.lastKey();

            var node = this.map.get(max).getLast();

            this.removeNode(node);

            return max;
        }

        private Node appendNode(int value) {
            Node node = new Node();

            node.value = value;

            this.tail.pre.next = node;
            node.next = this.tail;
            node.pre = this.tail.pre;
            this.tail.pre = node;

            return node;
        }

        private void removeNode(Node node) {
            node.pre.next = node.next;
            node.next.pre = node.pre;
            node.pre = null;
            node.next = null;

            this.map.get(node.value).removeLast();

            if (this.map.get(node.value).isEmpty()) {
                this.map.remove(node.value);
            }
        }

        private static class Node {
            Node pre;
            Node next;
            int value;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Node)) return false;
                Node node = (Node) o;
                return value == node.value;
            }

            @Override
            public int hashCode() {
                return Objects.hash(value);
            }
        }
    }

/**
 * Your MaxStack object will be instantiated and called as such:
 * MaxStack obj = new MaxStack();
 * obj.push(x);
 * int param_2 = obj.pop();
 * int param_3 = obj.top();
 * int param_4 = obj.peekMax();
 * int param_5 = obj.popMax();
 */
}
