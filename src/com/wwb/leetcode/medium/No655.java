package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.*;

/**
 * Given the root of a binary tree, construct a 0-indexed m x n string matrix res
 * that represents a formatted layout of the tree.
 * The formatted layout matrix should be constructed using the following rules:
 * <p>
 * The height of the tree is height and the number of rows m should be equal to height + 1.
 * The number of columns n should be equal to 2^(height + 1) - 1.
 * Place the root node in the middle of the top row (more formally, at location res[0][(n-1)/2]).
 * For each node that has been placed in the matrix at position res[r][c],
 * place its left child at res[r+1][c-2height-r-1] and its right child at res[r+1][c+2height-r-1].
 * Continue this process until all the nodes in the tree have been placed.
 * Any empty cells should contain the empty string "".
 * Return the constructed matrix res.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: root = [1,2]
 * Output:
 * [["","1",""],
 * ["2","",""]]
 * Example 2:
 * <p>
 * <p>
 * Input: root = [1,2,3,null,4]
 * Output:
 * [["","","","1","","",""],
 * ["","2","","","","3",""],
 * ["","","4","","","",""]]
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [1, 2^10].
 * -99 <= Node.val <= 99
 * The depth of the tree will be in the range [1, 10].
 */
public class No655 {
    public List<List<String>> printTree(TreeNode root) {
        if (root == null) {
            return Collections.emptyList();
        }

        List<List<String>> result = new ArrayList<>();

        int height = getHeight(root);
        int width = (int) (StrictMath.pow(2, height) - 1);

        for (int i = 0; i < height; i++) {
            result.add(new ArrayList<>(Collections.nCopies(width, "")));
        }

        populateResult(root, result, 0, 0, width);

        return result;
    }

    private void populateResult(TreeNode node, List<List<String>> result, int level, int start, int end) {
        if (level == result.size()) {
            return;
        }

        if (node == null) {
            return;
        }

        var row = result.get(level);
        int mid = start + (end - start) / 2;

        row.set(mid, String.valueOf(node.val));
        populateResult(node.left, result, level + 1, start, mid - 1);
        populateResult(node.right, result, level + 1, mid + 1, end);
    }

    private int getHeight(TreeNode node) {
        if (node == null) {
            return 0;
        }

        var left = getHeight(node.left);
        var right = getHeight(node.right);

        return 1 + Math.max(left, right);
    }
}
