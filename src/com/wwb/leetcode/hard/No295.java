package com.wwb.leetcode.hard;

import java.util.Comparator;
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

    private Queue<Integer> minHeap;
    private Queue<Integer> maxHeap;
    private Queue<Integer> reference;

    public MedianFinder() {
        this.minHeap = new PriorityQueue<>();
        this.maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        this.reference = this.minHeap;
    }
    // Adds a number into the data structure.
    public void addNum(int num) {
        Queue<Integer> temp = this.maxHeap;

        temp.offer(num);
        this.minHeap.offer(temp.poll());
        this.maxHeap = this.minHeap;
        this.minHeap = temp;
    }

    // Returns the median of current data stream
    public double findMedian() {
        return (this.reference.peek() + this.maxHeap.peek()) / 2.0;
    }
}
}
