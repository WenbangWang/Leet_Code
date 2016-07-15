package com.wwb.leetcode.utils;

public abstract class AbstractHeap implements Heap {

    static final int DEFAULT_CAPACITY = 1 << 4;

    int capacity;
    int currentIndex;
    int[] heap;

    public AbstractHeap() {
        this(DEFAULT_CAPACITY);
    }

    public AbstractHeap(int capacity) {
        this.capacity = capacity;
        this.currentIndex = 0;
        this.heap = new int[capacity];
    }

    @Override
    public void add(int value) {
        this.heap[this.currentIndex] = value;

        this.trickleUp(this.currentIndex);
        this.increaseSize();
    }

    @Override
    public int pop() {
        int max = this.peek();

        this.heap[0] = this.heap[--this.currentIndex];
        this.trickleDown(0);

        return max;
    }

    @Override
    public int peek() {
        return this.heap[0];
    }

    @Override
    public boolean isEmpty() {
        return this.currentIndex == 0;
    }

    @Override
    public int size() {
        return this.currentIndex;
    }

    abstract void trickleUp(int index);

    abstract void trickleDown(int index);

    void increaseSize() {
        if(this.currentIndex == this.capacity - 1) {
            this.extendHeap();
        }

        this.currentIndex++;
    }

    void extendHeap() {
        int newCapacity = this.capacity * 2 + 1;
        int[] newHeap = new int[newCapacity];

        System.arraycopy(this.heap, 0, newHeap, 0, this.capacity);

        this.heap = newHeap;
        this.capacity = newCapacity;
    }

    int parentIndex(int index) {
        return (index - 1) / 2;
    }

    int leftChildIndex(int index) {
        return 2 * index + 1;
    }

    int rightChildIndex(int index) {
        return 2 * index + 2;
    }
}
