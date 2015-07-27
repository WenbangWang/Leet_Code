package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a binary tree, return the inorder traversal of its nodes' values.
 *
 * For example:
 * Given binary tree {1,#,2,3},
 * 1
 *  \
 *   2
 *  /
 * 3
 * return [1,3,2].
 */
public class No94 {

    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();

        inorderTraversal(root, result);

        return result;
    }

    private void inorderTraversal(TreeNode node, List<Integer> list) {
        if(node == null) {
            return;
        }

        inorderTraversal(node.left, list);
        list.add(node.val);
        inorderTraversal(node.right, list);
    }
}