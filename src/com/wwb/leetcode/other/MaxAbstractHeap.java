package com.wwb.leetcode.other;

public class MaxAbstractHeap extends AbstractHeap implements Heap {

    @Override
    void trickleUp(int index) {
        int value = this.heap[index];
        int parentIndex = this.parentIndex(index);
        int parent = this.heap[parentIndex];

        while(index > 0 && value > parent) {
            this.heap[index] = parent;
            index = parentIndex;
            parentIndex = this.parentIndex(parentIndex);
            parent = this.heap[parentIndex];
        }

        this.heap[index] = value;
    }

    @Override
    void trickleDown(int index) {
        int value = this.heap[index];
        int largerIndex;
        int largerValue;

        while(index < this.currentIndex / 2) {
            int leftChildIndex = this.leftChildIndex(index);
            int rightChildIndex = this.rightChildIndex(index);

            if(rightChildIndex < this.currentIndex && this.heap[leftChildIndex] < this.heap[rightChildIndex]) {
                largerIndex = rightChildIndex;
            } else {
                largerIndex = leftChildIndex;
            }

            largerValue = this.heap[largerIndex];

            if(value > largerValue) {
                break;
            }

            this.heap[index] = largerValue;
            index = largerIndex;
        }

        this.heap[index] = value;
    }
}
