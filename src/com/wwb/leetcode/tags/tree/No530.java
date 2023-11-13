package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root of a Binary Search Tree (BST), return the minimum absolute difference
 * between the values of any two different nodes in the tree.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: root = [4,2,6,1,3]
 * Output: 1
 * Example 2:
 * <p>
 * <p>
 * Input: root = [1,0,48,null,null,12,49]
 * Output: 1
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [2, 10^4].
 * 0 <= Node.val <= 10^5
 */
public class No530 {
    public int getMinimumDifference(TreeNode root) {
        Integer[] pre = new Integer[]{null};
        int[] min = new int[]{Integer.MAX_VALUE};

        solution(root, pre, min);

        return min[0];
    }

    private void solution(TreeNode node, Integer[] pre, int[] min) {
        if (node == null) {
            return;
        }

        solution(node.left, pre, min);

        if (pre[0] != null) {
            min[0] = Math.min(min[0], node.val - pre[0]);
        }
        pre[0] = node.val;

        solution(node.right, pre, min);
    }

    private int suckSolution(TreeNode root) {
        Result result = suckSolutionRec(root);

        if (result == null) {
            return Integer.MAX_VALUE;
        }

        return result.diff;
    }

    private Result suckSolutionRec(TreeNode node) {
        if (node == null) {
            return null;
        }

        if (node.left == null && node.right == null) {
            return new Result(node.val, node.val, Integer.MAX_VALUE);
        }

        Result left = suckSolutionRec(node.left);
        Result right = suckSolutionRec(node.right);

        if (left == null) {
            return new Result(right.max, node.val, Math.min(
                right.diff,
                Math.abs(node.val - right.min)
            ));
        }

        if (right == null) {
            return new Result(node.val, left.min, Math.min(
                left.diff,
                Math.abs(node.val - left.max)
            ));
        }

        return new Result(right.max, left.min, Math.min(
            Math.min(left.diff, right.diff),
            Math.min(Math.abs(node.val - left.max), Math.abs(node.val - right.min))
        ));
    }

    private static class Result {
        int max;
        int min;
        int diff;

        Result(int max, int min, int diff) {
            this.max = max;
            this.min = min;
            this.diff = diff;
        }
    }
}
