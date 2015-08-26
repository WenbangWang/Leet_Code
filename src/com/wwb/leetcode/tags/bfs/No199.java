package com.wwb.leetcode.tags.bfs;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a binary tree, imagine yourself standing on the right side of it,
 * return the values of the nodes you can see ordered from top to bottom.
 *
 * For example:
 * Given the following binary tree,
 *   1            <---
 * /   \
 * 2     3         <---
 *  \     \
 *   5     4       <---
 * You should return [1, 3, 4].
 */
public class No199 {

    public List<Integer> rightSideView(TreeNode root) {
        return solution1(root);
    }

    private List<Integer> solution1(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        List<TreeNode> level = new ArrayList<>();

        if(root != null) {
            level.add(root);

            rightSideView(result, level);
        }

        return result;
    }

    private List<Integer> solution2(TreeNode root) {
        List<Integer> result = new ArrayList<>();

        rightSideView(root, result, 0);

        return result;
    }

    private void rightSideView(TreeNode node, List<Integer> result, int currentLevel) {
        if(node == null) {
            return;
        }

        if(result.size() == currentLevel) {
            result.add(node.val);
        }

        rightSideView(node.right, result, currentLevel + 1);
        rightSideView(node.left, result,  currentLevel + 1);
    }

    private void rightSideView(List<Integer> result, List<TreeNode> level) {
        if(level == null || level.isEmpty()) {
            return;
        }

        List<TreeNode> currentLevel = new ArrayList<>();
        result.add(level.get(level.size() - 1).val);

        for(TreeNode current: level) {
            if(current.left != null) {
                currentLevel.add(current.left);
            }
            if(current.right != null) {
                currentLevel.add(current.right);
            }
        }

        rightSideView(result, currentLevel);
    }
}