package com.wwb.leetcode.tags.dfs;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Given a binary tree, return all root-to-leaf paths.
 *
 * For example, given the following binary tree:
 *
 *    1
 *  /   \
 * 2     3
 *  \
 *   5
 * All root-to-leaf paths are:
 *
 * ["1->2->5", "1->3"]
 */
public class No257 {

    public List<String> binaryTreePaths(TreeNode root) {
        return solution1(root);
    }

    private List<String> solution1(TreeNode node) {
        if(node == null) {
            return Collections.emptyList();
        }

        if(node.left == null && node.right == null) {
            return Collections.singletonList(Integer.toString(node.val));
        }

        List<String> leftPaths = solution1(node.left);
        List<String> rightPaths = solution1(node.right);
        List<String> result = new ArrayList<>();

        for(String leftPath : leftPaths) {
            result.add(buildPath(node, leftPath));
        }

        for(String rightPath : rightPaths) {
            result.add(buildPath(node, rightPath));
        }

        return result;
    }

    private List<String> solution2(TreeNode root) {
        List<String> result = new ArrayList<>();

        if(root != null) {
            solution2(root, "", result);
        }

        return result;
    }

    private void solution2(TreeNode node, String path, List<String> result) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(path).append(node.val);

        if(node.left == null && node.right == null) {
            result.add(stringBuilder.toString());
        }

        stringBuilder.append("->");

        if(node.left != null) {
            solution2(node.left, stringBuilder.toString(), result);
        }

        if(node.right != null) {
            solution2(node.right, stringBuilder.toString(), result);
        }
    }

    private String buildPath(TreeNode node, String path) {
        return Integer.toString(node.val) + "->" + path;
    }
}