package com.wwb.leetcode.tags.dc;

/**
 * Given an integer array nums, return the number of range sums that lie in [lower, upper] inclusive.
 * Range sum S(i, j) is defined as the sum of the elements in nums between indices i and j (i ≤ j), inclusive.
 * <p>
 * Note:
 * A naive algorithm of O(n2) is trivial. You MUST do better than that.
 * <p>
 * Example:
 * Given nums = [-2, 5, -1], lower = -2, upper = 2,
 * Return 3.
 * The three ranges are : [0, 0], [2, 2], [0, 2] and their respective sums are: -2, -1, 2.
 */
public class No327 {

    public int countRangeSum(int[] nums, int lower, int upper) {
        if (lower > upper) {
            return 0;
        }

        long[] sums = new long[nums.length + 1];

        for (int i = 0; i < nums.length; i++) {
            sums[i + 1] = sums[i] + nums[i];
        }

        return mergeCountRangeSum(sums, 0, nums.length + 1, lower, upper);
    }

    private int mergeCountRangeSum(long[] sums, int start, int end, int lower, int upper) {
        if (end - start <= 1) {
            return 0;
        }

        int mid = (end - start) / 2 + start;
        int count = mergeCountRangeSum(sums, start, mid, lower, upper) + mergeCountRangeSum(sums, mid, end, lower, upper);

        long[] workspace = new long[end - start];
        int j = mid;
        int k = mid;
        int m = mid;

        for (int i = start, pointer = 0; i < mid; i++, pointer++) {
            while (j < end && sums[j] - sums[i] < lower) {
                j++;
            }

            while (k < end && sums[k] - sums[i] <= upper) {
                k++;
            }

            while (m < end && sums[m] < sums[i]) {
                workspace[pointer++] = sums[m++];
            }

            workspace[pointer] = sums[i];
            count += k - j;
        }

        System.arraycopy(workspace, 0, sums, start, m - start);

        return count;
    }

    private int solution2(long[] sums, int lower, int upper) {
        Node root = new Node(sums[0]);
        int result = 0;

        for (int i = 1; i < sums.length; i++) {
            result += this.rangeSum(root, sums[i] - upper, sums[i] - lower);
            this.insert(root, sums[i]);
        }

        return result;
    }

    private Node insert(Node node, long value) {
        if (node == null) {
            return new Node(value);
        } else if (node.value == value) {
            node.count++;
        } else if (node.value > value) {
            node.left = insert(node.left, value);
            node.leftSize++;
        } else {
            node.right = insert(node.right, value);
            node.rightSize++;
        }

        return node;
    }

    private int rangeSum(Node root, long lower, long upper) {
        int total = root.count + root.leftSize + root.rightSize;
        int smaller = this.countSmaller(root, lower);
        int larger = this.countLarger(root, upper);

        return total - smaller - larger;
    }

    private int countSmaller(Node node, long value) {
        if (node == null) {
            return 0;
        } else if (node.value == value) {
            return node.leftSize;
        } else if (node.value > value) {
            return countSmaller(node.left, value);
        } else {
            return node.count + node.leftSize + countSmaller(node.right, value);
        }
    }

    private int countLarger(Node node, long value) {
        if (node == null) {
            return 0;
        } else if (node.value == value) {
            return node.rightSize;
        } else if (node.value > value) {
            return node.count + node.rightSize + countLarger(node.left, value);
        } else {
            return countLarger(node.right, value);
        }
    }

    private class Node {
        long value;
        int count;
        int leftSize;
        int rightSize;
        Node left;
        Node right;

        Node(long value) {
            this.value = value;
            this.count = 1;
        }
    }
}
