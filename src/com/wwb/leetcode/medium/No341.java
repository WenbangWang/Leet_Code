package com.wwb.leetcode.medium;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Given a nested list of integers, implement an iterator to flatten it.
 * <p>
 * Each element is either an integer, or a list -- whose elements may also be integers or other lists.
 * <p>
 * Example 1:
 * Given the list [[1,1],2,[1,1]],
 * <p>
 * By calling next repeatedly until hasNext returns false,
 * the order of elements returned by next should be: [1,1,2,1,1].
 * <p>
 * Example 2:
 * Given the list [1,[4,[6]]],
 * <p>
 * By calling next repeatedly until hasNext returns false,
 * the order of elements returned by next should be: [1,4,6].
 */
public class No341 {
    // This is the interface that allows for creating nested lists.
    // You should not implement it, or speculate about its implementation
    public interface NestedInteger {
        // @return true if this NestedInteger holds a single integer, rather than a nested list.
        public boolean isInteger();

        // @return the single integer that this NestedInteger holds, if it holds a single integer
        // Return null if this NestedInteger holds a nested list
        public Integer getInteger();

        // @return the nested list that this NestedInteger holds, if it holds a nested list
        // Return null if this NestedInteger holds a single integer
        public List<NestedInteger> getList();
    }

    public static class NestedIterator implements Iterator<Integer> {
    private NestedInteger next;
    private Stack<Iterator<NestedInteger>> stack;

    public NestedIterator(List<NestedInteger> nestedList) {
        stack = new Stack<>();
        stack.push(nestedList.iterator());
    }

    @Override
    public Integer next() {
        return this.next != null ? this.next.getInteger() : null; //Just in case
    }

    @Override
    public boolean hasNext() {
        while (!stack.isEmpty()) {
            if (!stack.peek().hasNext()) {
                stack.pop();
            } else if ((this.next = stack.peek().next()).isInteger()) {
                return true;
            } else {
                stack.push(this.next.getList().iterator());
            }
        }
        return false;
    }
}

/*
 * Your NestedIterator object will be instantiated and called as such:
 * NestedIterator i = new NestedIterator(nestedList);
 * while (i.hasNext()) v[f()] = i.next();
 */
}
