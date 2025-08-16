package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Given the root of a binary tree, each node in the tree has a distinct value.
 *
 * After deleting all nodes with a value in to_delete, we are left with a forest (a disjoint union of trees).
 *
 * Return the roots of the trees in the remaining forest. You may return the result in any order.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/1110.png" />
 *
 * Input: root = [1,2,3,4,5,6,7], to_delete = [3,5]
 * Output: [[1,2,null,4],[6],[7]]
 * </pre>
 *
 * <pre>
 * Example 2:
 *
 * Input: root = [1,2,4,null,3], to_delete = [3]
 * Output: [[1,2,4]]
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * The number of nodes in the given tree is at most 1000.
 * Each node has a distinct value between 1 and 1000.
 * to_delete.length <= 1000
 * to_delete contains distinct values between 1 and 1000.
 * </pre>
 */
public class No1110 {
    public List<TreeNode> delNodes(TreeNode root, int[] to_delete) {
        List<TreeNode> result = new ArrayList<>();

        Set<Integer> set = Arrays.stream(to_delete).boxed().collect(Collectors.toSet());

        if (!set.contains(root.val)) {
            result.add(root);
        }

        dfs(root, set, result);

        return result;
    }

    private TreeNode dfs(TreeNode node, Set<Integer> toDelete, List<TreeNode> result) {
        if (node == null) {
            return null;
        }

        node.left = dfs(node.left, toDelete, result);
        node.right = dfs(node.right, toDelete, result);

        if (toDelete.contains(node.val)) {
            if (node.left != null) {
                result.add(node.left);
            }

            if (node.right != null) {
                result.add(node.right);
            }

            return null;
        }

        return node;
    }
}
