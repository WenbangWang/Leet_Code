package com.wwb.leetcode.easy;

import java.util.Stack;

/**
 * Implement the following operations of a queue using stacks.
 *
 * push(x) -- Push element x to the back of queue.
 * pop() -- Removes the element from in front of queue.
 * peek() -- Get the front element.
 * empty() -- Return whether the queue is empty.
 * Notes:
 * You must use only standard operations of a stack --
 * which means only push to top, peek/pop from top, size, and is empty operations are valid.
 * Depending on your language, stack may not be supported natively.
 * You may simulate a stack by using a list or deque (double-ended queue),
 * as long as you use only standard operations of a stack.
 * You may assume that all operations are valid
 * (for example, no pop or peek operations will be called on an empty queue).
 */
public class No232 {

    class MyQueue {
        private Stack<Integer> stack;
        private Stack<Integer> queueStack;

        public MyQueue() {
            this.stack = new Stack<>();
            this.queueStack = new Stack<>();
        }
        // Push element x to the back of queue.
        public void push(int x) {
            this.stack.push(x);
        }

        // Removes the element from in front of queue.
        public void pop() {
            this.fillQueueStack();
            this.queueStack.pop();
        }

        // Get the front element.
        public int peek() {
            this.fillQueueStack();
            return this.queueStack.peek();
        }

        // Return whether the queue is empty.
        public boolean empty() {
            return this.stack.isEmpty() && this.queueStack.isEmpty();
        }

        private void fillQueueStack() {
            if(this.queueStack.isEmpty()) {
                while(!this.stack.isEmpty()) {
                    this.queueStack.push(this.stack.pop());
                }
            }
        }
    }
}