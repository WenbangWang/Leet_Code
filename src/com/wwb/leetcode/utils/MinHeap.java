package com.wwb.leetcode.utils;

public class MinHeap extends Heap {

    @Override
    void trickleUp(int index) {
        int value = this.heap[index];
        int parentIndex = this.parentIndex(index);
        int parent = this.heap[parentIndex];

        while(index > 0 && value < parent) {
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
        int smallerIndex;
        int smallerValue;

        while(index < this.currentIndex / 2) {
            int leftChildIndex = this.leftChildIndex(index);
            int rightChildIndex = this.rightChildIndex(index);

            if(rightChildIndex < this.currentIndex && this.heap[leftChildIndex] < this.heap[rightChildIndex]) {
                smallerIndex = leftChildIndex;
            } else {
                smallerIndex = rightChildIndex;
            }

            smallerValue = this.heap[smallerIndex];

            if(value < smallerValue) {
                break;
            }

            this.heap[index] = smallerValue;
            index = smallerIndex;
        }

        this.heap[index] = value;
    }
}
