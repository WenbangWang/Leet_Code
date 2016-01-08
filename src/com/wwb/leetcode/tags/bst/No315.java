package com.wwb.leetcode.tags.bst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * You are given an integer array nums and you have to return a new counts array.
 * The counts array has the property where counts[i] is the number of smaller elements to the right of nums[i].
 *
 * Example:
 *
 * Given nums = [5, 2, 6, 1]
 *
 * To the right of 5 there are 2 smaller elements (2 and 1).
 * To the right of 2 there is only 1 smaller element (1).
 * To the right of 6 there is 1 smaller element (1).
 * To the right of 1 there is 0 smaller element.
 * Return the array [2, 1, 1, 0].
 */
public class No315 {

    public List<Integer> countSmaller(int[] nums) {
        if(nums == null || nums.length == 0) {
            return Collections.emptyList();
        }

        int length = nums.length;
        List<Integer> result = new ArrayList<>();
        Node root = new Node(nums[length - 1]);

        result.add(0);

        for(int i = length - 2; i >= 0; i--) {
            result.add(getNumberOfSmallerNumbers(root, nums[i], 0));
        }

        Collections.reverse(result);

        return result;
    }

    private int getNumberOfSmallerNumbers(Node node, int value, int count) {
        if(node.value >= value) {
            node.count++;

            if(node.left == null) {
                node.left = new Node(value);

                return count;
            } else {
                return getNumberOfSmallerNumbers(node.left, value, count);
            }
        } else {
            count += node.count;

            if(node.right == null) {
                node.right = new Node(value);

                return count;
            } else {
                return getNumberOfSmallerNumbers(node.right, value, count);
            }
        }
    }

    private class Node {
        int count;
        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
            this.count = 1;
        }
    }
}
