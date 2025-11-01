package com.wwb.leetcode.hard;

import java.util.*;

/**
 * Median is the middle value in an ordered integer list. If the size of the list is even, there is no middle value. So the median is the mean of the two middle value.
 * <p>
 * Examples:
 * [2,3,4] , the median is 3
 * <p>
 * [2,3], the median is (2 + 3) / 2 = 2.5
 * <p>
 * Given an array nums, there is a sliding window of size k which is moving from the very left of the array to the very right. You can only see the k numbers in the window. Each time the sliding window moves right by one position. Your job is to output the median array for each window in the original array.
 * <p>
 * For example,
 * <p>
 * Given nums = [1,3,-1,-3,5,3,6,7], and k = 3.
 *
 * <div>
 * Window position                Median
 * ---------------               -----
 * [1  3  -1] -3  5  3  6  7       1
 *  1 [3  -1  -3] 5  3  6  7       -1
 *  1  3 [-1  -3  5] 3  6  7       -1
 *  1  3  -1 [-3  5  3] 6  7       3
 *  1  3  -1  -3 [5  3  6] 7       5
 *  1  3  -1  -3  5 [3  6  7]      6
 *  </div>
 * <p>
 * Therefore, return the median sliding window as [1,-1,-1,3,5,6].
 * <p>
 * Note:
 * <p>
 * You may assume k is always valid, ie: 1 ≤ k ≤ input array's size for non-empty array.
 */
public class No480 {
    public double[] medianSlidingWindow(int[] nums, int k) {
        if (nums == null || nums.length < k) {
            return new double[]{};
        }

        double[] result = new double[nums.length - k + 1];
        int index = 0;
        Store store = new TreeMapStore();

        for (int i = 0; i < k; i++) {
            store.add(nums[i]);
        }

        result[index++] = store.findMedian();

        for (int i = k, length = nums.length; i < length; i++) {
            store.remove(nums[i - k]);
            store.add(nums[i]);
            result[index++] = store.findMedian();
        }

        return result;
    }

    private class TreeMapStore implements Store {
        private TreeMap<Integer, Integer> maxHeap;
        private TreeMap<Integer, Integer> minHeap;
        private TreeMap<Integer, Integer> reference;

        TreeMapStore() {
            this.maxHeap = new TreeMap<>(Comparator.reverseOrder());
            this.minHeap = new TreeMap<>();
            this.reference = this.minHeap;
        }

        public void add(int num) {
            this.maxHeap.put(num, this.maxHeap.getOrDefault(num, 0) + 1);

            int key = this.maxHeap.firstKey();
            this.minHeap.put(key, this.minHeap.getOrDefault(key, 0) + 1);
            this.remove(this.maxHeap, key);

            swap();
        }

        public void remove(int num) {
            if (this.minHeap.containsKey(num)) {
                this.remove(this.minHeap, num);

                // don't really need to check whether maxHeap is empty
                // as when minHeap has number, there should be at least two numbers in total (one in each heap).
                int key = this.maxHeap.firstKey();
                this.remove(this.maxHeap, key);
                this.minHeap.put(key, this.minHeap.getOrDefault(key, 0) + 1);
            } else {
                this.remove(this.maxHeap, num);
            }

            swap();
        }

        public double findMedian() {
            return ((double) this.maxHeap.firstKey() + (double) this.reference.firstKey()) / 2.0;
        }

        private void remove(Map<Integer, Integer> heap, int num) {
            int count = heap.get(num) - 1;
            if (count == 0) {
                heap.remove(num);
            } else {
                heap.put(num, count);
            }
        }

        private void swap() {
            TreeMap<Integer, Integer> temp = this.maxHeap;
            this.maxHeap = this.minHeap;
            this.minHeap = temp;
        }
    }

    private class QueueStore implements Store {
        private Queue<Integer> maxHeap;
        private Queue<Integer> minHeap;
        private Queue<Integer> reference;

        QueueStore() {
            this.maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
            this.minHeap = new PriorityQueue<>();
            this.reference = this.minHeap;
        }

        public void add(int num) {
            this.maxHeap.offer(num);
            this.minHeap.offer(this.maxHeap.poll());

            Queue<Integer> temp = this.maxHeap;
            this.maxHeap = this.minHeap;
            this.minHeap = temp;
        }

        public void remove(int num) {
            if (this.minHeap.contains(num)) {
                this.minHeap.remove(num);
                this.minHeap.offer(this.maxHeap.poll());
            } else {
                this.maxHeap.remove(num);
            }

            Queue<Integer> temp = this.maxHeap;
            this.maxHeap = this.minHeap;
            this.minHeap = temp;
        }

        public double findMedian() {
            return ((double) this.maxHeap.peek() + (double) this.reference.peek()) / 2.0;
        }
    }

    private interface Store {
        void add(int num);

        void remove(int num);

        double findMedian();
    }
}
