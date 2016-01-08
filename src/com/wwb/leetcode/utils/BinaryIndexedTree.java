package com.wwb.leetcode.utils;

public class BinaryIndexedTree {

    private int[] tree;

    public BinaryIndexedTree(int[] nums) {
        this.tree = new int[nums.length + 1];

        for(int i = 0; i < nums.length; i++) {
            this.update(i, nums[i]);
        }
    }

    public int getSum(int index) {
        int sum = 0;
        index++;

        while(index > 0) {
            sum += this.tree[index];
            index -= Integer.lowestOneBit(index);
        }

        return sum;
    }

    public void update(int index, int value) {
        index++;

        while(index < this.tree.length) {
            this.tree[index] += value;
            index += Integer.lowestOneBit(index);
        }
    }
}
