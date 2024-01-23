package com.wwb.leetcode.tags.tree;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayDeque;

/**
 * Given the root of a binary tree, return the maximum width of the given tree.
 *
 * The maximum width of a tree is the maximum width among all levels.
 *
 * The width of one level is defined as the length between the end-nodes (the leftmost and rightmost non-null nodes),
 * where the null nodes between the end-nodes that would be present in a complete binary tree extending down to that level are also counted into the length calculation.
 *
 * It is guaranteed that the answer will in the range of a 32-bit signed integer.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: root = [1,3,2,5,3,null,9]
 * Output: 4
 * Explanation: The maximum width exists in the third level with length 4 (5,3,null,9).
 * Example 2:
 *
 *
 * Input: root = [1,3,2,5,null,null,9,6,null,7]
 * Output: 7
 * Explanation: The maximum width exists in the fourth level with length 7 (6,null,null,null,null,null,7).
 * Example 3:
 *
 *
 * Input: root = [1,3,2,5]
 * Output: 2
 * Explanation: The maximum width exists in the second level with length 2 (3,2).
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [1, 3000].
 * -100 <= Node.val <= 100
 */
public class No662 {
    public int widthOfBinaryTree(TreeNode root) {
        ArrayDeque<TreeNodeWithIndex> queue = new ArrayDeque<>();
        int maxWidth = 0;

        queue.offer(new TreeNodeWithIndex(root, 0));

        while (!queue.isEmpty()) {
            int size = queue.size();
            maxWidth = Math.max(maxWidth, queue.peekLast().index - queue.peekFirst().index + 1);

            for (int i = 0; i < size; i++) {
                TreeNodeWithIndex node = queue.poll();

                if (node.node.left != null) {
                    queue.offer(new TreeNodeWithIndex(node.node.left, 2 + node.index));
                }

                if (node.node.right != null) {
                    queue.offer(new TreeNodeWithIndex(node.node.right, 2 + node.index + 1));
                }
            }
        }

        return maxWidth;
    }

    private static class TreeNodeWithIndex {
        TreeNode node;
        int index;

        TreeNodeWithIndex(TreeNode node, int index) {
            this.node = node;
            this.index = index;
        }
    }
}
