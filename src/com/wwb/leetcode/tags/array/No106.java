package com.wwb.leetcode.tags.array;

import com.wwb.leetcode.utils.TreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Given inorder and postorder traversal of a tree, construct the binary tree.
 *
 * Note:
 * You may assume that duplicates do not exist in the tree.
 */
public class No106 {

    /**
     * Definition for a binary tree node.
     * public class com.wwb.leetcode.utils.TreeNode {
     *     int val;
     *     TreeNode left;
     *     TreeNode right;
     *     TreeNode(int x) { val = x; }
     * }
     */
    public TreeNode buildTree(int[] inorder, int[] postorder) {
        Map<Integer, Integer> inorderMap = new HashMap<>();
        for(int i = 0; i < inorder.length; i++) {
            inorderMap.put(inorder[i], i);
        }

        return buildTree(postorder, inorder, inorderMap, postorder.length - 1, 0, inorder.length - 1);
    }

    private TreeNode buildTree(int[] postorder, int[] inorder, Map<Integer, Integer> inorderMap, int postorderIndex, int start, int end) {
        if(start > end) {
            return null;
        }

        TreeNode node = new TreeNode(postorder[postorderIndex]);
        int inorderIndex = inorderMap.get(postorder[postorderIndex]);
        int rightTreeSize = end - inorderIndex;

        node.left = buildTree(postorder, inorder, inorderMap, postorderIndex - rightTreeSize - 1, start, inorderIndex - 1);
        node.right = buildTree(postorder, inorder, inorderMap, postorderIndex - 1, inorderIndex + 1, end);

        return node;
    }
}