package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given a binary tree and a sum, find all root-to-leaf paths where each path's sum equals the given sum.
 *
 * For example:
 * Given the below binary tree and sum = 22,
 *       5
 *      / \
 *     4   8
 *    /   / \
 *   11  13  4
 *  /  \    / \
 * 7    2  5   1
 * return
 * [
 *   [5,4,11,2],
 *   [5,8,4,5]
 * ]
 */
public class No113 {

    public List<List<Integer>> pathSum(TreeNode root, int sum) {
        if(root == null) {
            return Collections.emptyList();
        }

        List<List<Integer>> result = new ArrayList<>();
        List<Integer> currentResult = new ArrayList<>();

        generatePathSum(root, sum, result, currentResult);

        return result;
    }

    private void generatePathSum(TreeNode node, int sum, List<List<Integer>> result, List<Integer> currentResult) {
        if(node == null) {
            return;
        }

        sum -= node.val;
        currentResult.add(node.val);

        if(sum == 0 && node.left == null && node.right == null) {
            result.add(new ArrayList<>(currentResult));
        }

        generatePathSum(node.left, sum, result, currentResult);
        generatePathSum(node.right, sum, result, currentResult);
        currentResult.remove(currentResult.size() - 1);
    }
}
