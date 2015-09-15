package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given an array where elements are sorted in ascending order,
 * convert it to a height balanced BST.
 */
public class No108 {

    public TreeNode sortedArrayToBST(int[] nums) {
        if(nums == null || nums.length == 0) {
            return null;
        }

        return buildTree(nums, 0, nums.length - 1);
    }

    private TreeNode buildTree(int[] nums, int start, int end) {
        if(start > end) {
            return null;
        }

        int mid = (end - start) / 2 + start;
        TreeNode node = new TreeNode(nums[mid]);

        node.left = buildTree(nums, start, mid - 1);
        node.right = buildTree(nums, mid + 1, end);

        return node;
    }
}
