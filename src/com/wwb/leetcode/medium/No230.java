package com.wwb.leetcode.medium;

import com.wwb.leetcode.other.TreeNodeMorrisTraversal;
import com.wwb.leetcode.other.TreeNodeTraversal;
import com.wwb.leetcode.other.TreeTraverseActor;
import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a binary search tree, write a function kthSmallest to find the kth smallest element in it.
 *
 * Note:
 * You may assume k is always valid, 1 ≤ k ≤ BST's total elements.
 *
 * Follow up:
 * What if the BST is modified (insert/delete operations) often and you need to find the kth smallest frequently?
 * How would you optimize the kthSmallest routine?
 */
public class No230 {

    public int kthSmallest(TreeNode root, int k) {
        return solution1(root, k);
    }

    // Binary search
    // O(NlogN) best O(N^2) worst
    private int solution1(TreeNode root, int k) {
        int count = countNodes(root.left);
        if (k <= count) {
            return kthSmallest(root.left, k);
        }

        if (k > count + 1) {
            return kthSmallest(root.right, k - 1 - count); // 1 is counted as current node
        }

        return root.val;
    }

    // Inorder traversal, O(N).
    private int solution2(TreeNode root, int k) {
        TreeNodeTraversal<Integer> traversal = new TreeNodeMorrisTraversal<>(() -> new KthSmallestTraversalActor(k));

        Integer result = traversal.inOrder(root);

        return result == null ? -1 : result;
    }

    private int countNodes(TreeNode n) {
        if (n == null) {
            return 0;
        }

        return 1 + countNodes(n.left) + countNodes(n.right);
    }

    private static class KthSmallestTraversalActor implements TreeTraverseActor<Integer> {
        private int k;
        private TreeNode node;

        KthSmallestTraversalActor(int k) {
            this.k = k;
        }

        @Override
        public void apply(TreeNode node) {
            if (k != 0) {
                k--;
            }

            if (k == 0 && this.node != null) {
                this.node = node;
            }
        }

        @Override
        public Integer collect() {
            return this.node == null ? null : this.node.val;
        }
    }
}
