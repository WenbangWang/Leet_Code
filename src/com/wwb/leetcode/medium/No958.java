package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Given the root of a binary tree, determine if it is a complete binary tree.
 * <p>
 * In a complete binary tree, every level, except possibly the last, is completely filled, and all nodes in the last level are as far left as possible. It can have between 1 and 2h nodes inclusive at the last level h.
 * <p>
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/958_1.png" />
 *
 * Input: root = [1,2,3,4,5,6]
 * Output: true
 * Explanation: Every level before the last is full (ie. levels with node-values {1} and {2, 3}), and all nodes in the last level ({4, 5, 6}) are as far left as possible.
 * </pre>
 * <p>
 * <p>
 *
 * <pre>
 * Example 2:
 *
 * <img src="../doc-files/958_2.png" />
 *
 * Input: root = [1,2,3,4,5,null,7]
 * Output: false
 * Explanation: The node with value 7 isn't as far left as possible.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [1, 100].
 * 1 <= Node.val <= 1000
 *
 * </pre>
 */
public class No958 {
    public boolean isCompleteTree(TreeNode root) {
        return solution1(root);
    }

    // stupid bfs
    private boolean solution1(TreeNode root) {
        int height = getHeight(root);
        Queue<TreeNode> queue = new LinkedList<>();
        int level = 0;

        queue.offer(root);

        while (!queue.isEmpty()) {
            int size = queue.size();

            if (height != level + 1 && (int) StrictMath.pow(2, level) != size) {
                return false;
            }

            boolean isSecondLastRow = level == height;
            boolean foundNull = false;
            level++;

            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();

                if (node.left != null) {
                    queue.offer(node.left);

                    if (isSecondLastRow && foundNull) {
                        return false;
                    }
                } else {
                    foundNull = true;
                }

                if (node.right != null) {
                    queue.offer(node.right);

                    if (isSecondLastRow && foundNull) {
                        return false;
                    }
                } else {
                    foundNull = true;
                }
            }
        }

        return true;
    }

    // bfs
    private boolean solution2(TreeNode root) {
        Queue<TreeNode> queue = new LinkedList<>();

        queue.offer(root);

        while (queue.peek() != null) {
            TreeNode node = queue.poll();

            queue.offer(node.left);
            queue.offer(node.right);
        }

        while (!queue.isEmpty() && queue.peek() == null) {
            queue.poll();
        }

        return queue.isEmpty();
    }

    // dfs
    // What this solution relies on is two properties of a heap.
    // First, a heap is always a complete binary tree.
    // So if a tree can be viewed as a heap, it is complete.
    // Second, inside a heap (which is an array),
    // a parent with index i has two children with index i * 2 and i * 2 + 1,
    // if the parent has two children in the first place.
    // With these two properties known, it is easy to understand this solution.
    // Suppose we have an array which represents a heap of size total.
    // The solution tries to verify whether the tree with total nodes can fit into the array.
    // The tree can fit into the array if and only if all nodes' indices are within the boundary of the array,
    // that is, total.
    // The helper essentially does a DFS which checks
    // whether the index of a node exceeds the boundary of a heap with size total.
    // If idx > total, there must be an empty slot in the array,
    // which means the tree cannot fit into the array so helper returns false.
    // If idx <= total, helper recursively checks children of root.
    private boolean solution3(TreeNode root) {
        int count = countNodes(root);

        return exceedsHeapSize(root, 1, count);
    }

    private boolean exceedsHeapSize(TreeNode node, int index, int n) {
        if (node == null) {
            return true;
        }

        if (index > n) {
            return false;
        }

        return exceedsHeapSize(node.left, 2 * index, n) && exceedsHeapSize(node.right, 2 * index + 1, n);
    }

    private int countNodes(TreeNode node) {
        if (node == null) {
            return 0;
        }

        return countNodes(node.left) + countNodes(node.right) + 1;
    }

    private int getHeight(TreeNode node) {
        if (node == null) {
            return 0;
        }

        return Math.max(getHeight(node.left), getHeight(node.right)) + 1;
    }
}
