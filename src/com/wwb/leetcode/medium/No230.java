package com.wwb.leetcode.medium;

import com.wwb.leetcode.other.TreeNodeMorrisTraversal;
import com.wwb.leetcode.other.TreeNodeTraversal;
import com.wwb.leetcode.other.TreeTraverseActor;
import com.wwb.leetcode.utils.TreeNode;

import java.util.Stack;

/**
 * Given a binary search tree, write a function kthSmallest to find the kth smallest element in it.
 * <p>
 * Note:
 * You may assume k is always valid, 1 ≤ k ≤ BST's total elements.
 * <p>
 * Follow up:
 * <p>
 * What if the BST is modified (insert/delete operations) often and you need to find the kth smallest frequently?
 * <p>
 * How would you optimize the kthSmallest routine?
 */
public class No230 {

    public int kthSmallest(TreeNode root, int k) {
        return solution1(root, k);
    }

    // Binary search
    // O(N) best O(N^2) worst
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

    private int solution2(TreeNode root, int k) {
        Stack<TreeNode> stack = new Stack<>();

        while (root != null) {
            stack.push(root);
            root = root.left;
        }

        while (k != 0) {
            TreeNode node = stack.pop();
            k--;

            if (k == 0) {
                return node.val;
            }

            TreeNode right = node.right;

            while (right != null) {
                stack.push(right);
                right = right.left;
            }
        }

        return -1;
    }

    // Inorder traversal, O(N).
    private int solution3(TreeNode root, int k) {
        TreeNodeTraversal<Integer> traversal = new TreeNodeMorrisTraversal<>(() -> new KthSmallestTraversalActor(k));

        Integer result = traversal.inOrder(root);

        return result == null ? -1 : result;
    }

    private int solution4(TreeNode root, int k) {
        TreeNodeWithCount rootWithCount = buildBSTWithCount(root);

        return solution4(rootWithCount, k);
    }

    private int solution4(TreeNodeWithCount node, int k) {
        if (k <= node.count) {
            return solution4(node.left, k);
        }

        if (k > node.count + 1) {
            return solution4(node.right, k - node.count - 1);
        }

        return node.val;
    }

    private TreeNodeWithCount buildBSTWithCount(TreeNode node) {
        if (node == null) {
            return null;
        }
        TreeNodeWithCount current = new TreeNodeWithCount(node.val);

        TreeNodeWithCount left = buildBSTWithCount(node.left);
        TreeNodeWithCount right = buildBSTWithCount(node.right);

        if (left != null) {
            current.left = left;
            current.count += left.count;
        }

        if (right != null) {
            current.right = right;
            current.count += right.count;
        }

        current.count++;

        return current;
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

    private static class TreeNodeWithCount {
        int val;
        int count;
        TreeNodeWithCount left;
        TreeNodeWithCount right;

        TreeNodeWithCount(int val) {
            this.val = val;
        }
    }
}
