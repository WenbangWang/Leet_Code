package com.wwb.leetcode.tags.heap;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Median is the middle value in an ordered integer list.
 * If the size of the list is even, there is no middle value.
 * So the median is the mean of the two middle value.
 *
 * Examples:
 * [2,3,4] , the median is 3
 *
 * [2,3], the median is (2 + 3) / 2 = 2.5
 *
 * Design a data structure that supports the following two operations:
 *
 * void addNum(int num) - Add a integer number from the data stream to the data structure.
 * double findMedian() - Return the median of all elements so far.
 * For example:
 *
 * add(1)
 * add(2)
 * findMedian() -> 1.5
 * add(3)
 * findMedian() -> 2
 */
public class No295 {

class MedianFinder {

    private Queue<Integer> minQueue;
    private Queue<Integer> maxQueue;
    private Queue<Integer> temp;
    private Queue<Integer> reference;

    public MedianFinder() {
        this.minQueue = new PriorityQueue<>();
        this.maxQueue = new PriorityQueue<>(11, Collections.reverseOrder());
        this.reference = this.minQueue;
    }
    // Adds a number into the data structure.
    public void addNum(int num) {
        (this.temp = this.maxQueue).offer(num);
        (this.maxQueue = this.minQueue).offer((this.minQueue = this.temp).poll());
    }

    // Returns the median of current data stream
    public double findMedian() {
        return (this.reference.peek() + this.maxQueue.peek()) / 2.0;
    }
}
}
