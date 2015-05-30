package com.wwb.leetcode.tags.dfs;

import com.wwb.leetcode.utils.TreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Given preorder and inorder traversal of a tree, construct the binary tree.
 * Note:
 * You may assume that duplicates do not exist in the tree.
 */
public class No105 {

    /**
     * Definition for a binary tree node.
     * public class com.wwb.leetcode.utils.TreeNode {
     *     int val;
     *     TreeNode left;
     *     TreeNode right;
     *     TreeNode(int x) { val = x; }
     * }
     */
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        Map<Integer, Integer> inorderMap = new HashMap<>();
        for(int i = 0; i < inorder.length; i++) {
            inorderMap.put(inorder[i], i);
        }

        return buildTree(preorder, inorder, inorderMap, 0, 0, inorder.length - 1);
    }

    private TreeNode buildTree(int[] preorder, int[] inorder, Map<Integer, Integer> inorderMap, int preorderIndex, int start, int end) {
        if(start > end) {
            return null;
        }

        TreeNode node = new TreeNode(preorder[preorderIndex]);
        int inorderIndex = inorderMap.get(preorder[preorderIndex]);
        int leftTreeSize = inorderIndex - start;

        node.left = buildTree(preorder, inorder, inorderMap, preorderIndex + 1, start, inorderIndex - 1);
        node.right = buildTree(preorder, inorder, inorderMap, preorderIndex + leftTreeSize + 1, inorderIndex + 1, end);

        return node;
    }
}