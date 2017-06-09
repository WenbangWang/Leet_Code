package com.wwb.leetcode.tags.design;

import java.util.Iterator;
import java.util.List;

/**
 * Implement an iterator to flatten a 2d vector.
 *
 * For example,
 * Given 2d vector =
 *
 * [
 * [1,2],
 * [3],
 * [4,5,6]
 * ]
 * By calling next repeatedly until hasNext returns false, the order of elements returned by next should be: [1,2,3,4,5,6].
 *
 * Follow up:
 * As an added challenge, try to code it using only iterators in C++ or iterators in Java.
 */
public class No251 {
    public class Vector2D implements Iterator<Integer> {
        private Iterator<List<Integer>> vectorIterator;
        private Iterator<Integer> currentIterator;

        public Vector2D(List<List<Integer>> vec2d) {
            this.vectorIterator = vec2d.iterator();
        }
    
        @Override
        public Integer next() {
            return this.currentIterator != null ? this.currentIterator.next() : null;
        }

        @Override
        public boolean hasNext() {
            if(this.currentIterator == null || !this.currentIterator.hasNext()) {
                if(!this.vectorIterator.hasNext()) {
                    return false;
                }

                this.currentIterator = this.vectorIterator.next().iterator();
            }

            return this.currentIterator.hasNext() || this.hasNext();
        }
    }

/**
 * Your Vector2D object will be instantiated and called as such:
 * Vector2D i = new Vector2D(vec2d);
 * while (i.hasNext()) v[f()] = i.next();
 */
}
