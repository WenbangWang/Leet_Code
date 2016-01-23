package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given n, generate all structurally unique BST's (binary search trees) that store values 1...n.
 *
 * For example,
 * Given n = 3, your program should return all 5 unique BST's shown below.
 *
 * 1         3     3      2      1
 *  \       /     /      / \      \
 *   3     2     1      1   3      2
 *  /     /       \                 \
 * 2     1         2                 3
 */
public class No95 {

    public List<TreeNode> generateTrees(int n) {
        return solution1(n);
//        return solution2(n);
    }

    private List<TreeNode> solution1(int n) {
        if(n <= 0) {
            return Collections.emptyList();
        }

        List<List<TreeNode>> result = new ArrayList<>(n + 1);
        result.add(new ArrayList<TreeNode>());
        result.get(0).add(null);

        for(int i = 1; i <= n; i++) {
            List<TreeNode> subResult = new ArrayList<>();

            for(int j = 0; j < i; j++) {
                for(TreeNode left : result.get(j)) {
                    for(TreeNode right : result.get(i - j - 1)) {
                        TreeNode node = new TreeNode(j + 1);
                        node.left = left;
                        node.right = copy(right, j + 1);

                        subResult.add(node);
                    }
                }
            }

            result.add(subResult);
        }

        return result.get(n);
    }

    private TreeNode copy(TreeNode node, int offset) {
        if(node == null) {
            return null;
        }

        TreeNode current = new TreeNode(node.val + offset);
        current.left = copy(node.left, offset);
        current.right = copy(node.right, offset);

        return current;
    }

    private List<TreeNode> solution2(int n) {
        if(n <= 0) {
            return Collections.emptyList();
        }

        return generateTrees(1, n);
    }

    private List<TreeNode> generateTrees(int start, int end) {
        if(start > end) {
            return Collections.singletonList(null);
        }

        List<TreeNode> result = new ArrayList<>();

        for(int index = start; index <= end; index++) {
            List<TreeNode> leftTree = generateTrees(start, index - 1);
            List<TreeNode> rightTree = generateTrees(index + 1, end);

            for(TreeNode left : leftTree) {
                for(TreeNode right : rightTree) {
                    TreeNode current = new TreeNode(index);
                    current.left = left;
                    current.right = right;
                    result.add(current);
                }
            }
        }

        return result;
    }
}