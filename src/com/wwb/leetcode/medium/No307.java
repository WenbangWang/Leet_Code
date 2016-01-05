package com.wwb.leetcode.medium;

/**
 * Given an integer array nums, find the sum of the elements between indices i and j (i ≤ j), inclusive.
 *
 * The update(i, val) function modifies nums by updating the element at index i to val.
 * Example:
 * Given nums = [1, 3, 5]

 * sumRange(0, 2) -> 9
 * update(1, 2)
 * sumRange(0, 2) -> 8
 * Note:
 * The array is only modifiable by the update function.
 * You may assume the number of calls to update and sumRange function is distributed evenly.
 */
public class No307 {

    public class NumArray {

        private SegmentTreeNode root;

        public NumArray(int[] nums) {
            this.root = this.buildTree(nums, 0, nums.length - 1);
        }

        void update(int i, int val) {
            this.update(this.root, i, val);
        }

        public int sumRange(int i, int j) {
            return this.sumRange(this.root, i, j);
        }

        private SegmentTreeNode buildTree(int[] nums, int start, int end) {
            if(start > end) {
                return null;
            } else {
                SegmentTreeNode node = new SegmentTreeNode(start, end);

                if(start == end) {
                    node.value = nums[start];
                } else {
                    int mid = start + (end - start) / 2;

                    node.left = this.buildTree(nums, start, mid);
                    node.right = this.buildTree(nums, mid + 1, end);
                    node.value = node.left.value + node.right.value;
                }

                return node;
            }
        }

        private int sumRange(SegmentTreeNode node, int start, int end) {
            if(node.start == start && node.end == end) {
                return node.value;
            } else {
                int mid = node.start + (node.end - node.start) / 2;

                if(end <= mid) {
                    return sumRange(node.left, start, end);
                } else if(start >= mid + 1) {
                    return sumRange(node.right, start, end);
                } else {
                    return sumRange(node.left, start, mid) + sumRange(node.right, mid + 1, end);
                }
            }
        }

        private void update(SegmentTreeNode node, int i, int val) {
            if(node.start == node.end) {
                node.value = val;
            } else {
                int mid = node.start + (node.end - node.start) / 2;

                if(i <= mid) {
                    update(node.left, i, val);
                } else {
                    update(node.right, i, val);
                }

                node.value = node.left.value + node.right.value;
            }
        }

        private class SegmentTreeNode {
            int start;
            int end;
            int value;
            SegmentTreeNode left;
            SegmentTreeNode right;

            SegmentTreeNode(int start, int end) {
                this.start = start;
                this.end = end;
            }
        }
    }
}
