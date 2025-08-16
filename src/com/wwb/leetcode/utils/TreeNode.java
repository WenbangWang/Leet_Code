package com.wwb.leetcode.utils;

import java.util.LinkedList;
import java.util.Queue;

public class TreeNode {

    public int val;
    public TreeNode left;
    public TreeNode right;
    public TreeNode(int x) { val = x; }

    public static TreeNode buildTreeLeetCode(Integer[] values) {
        TreeNode root = null;
        Queue<TreeNode> queue = new LinkedList<>();
        int i = 0;
        TreeNode t = values[i] == null ? null : new TreeNode(values[i]);
        root = t;
        queue.add(root);
        i++;

        while (!queue.isEmpty() && i < values.length) {
            TreeNode node = queue.poll();
            if (node != null) {
                node.left = values[i] == null ? null : new TreeNode(values[i]);
                queue.add(node.left);
                i++;
                if (i >= values.length) {
                    break;
                }
                node.right = values[i] == null ? null : new TreeNode(values[i]);
                queue.add(node.right);
                i++;
            }
        }
        return root;
    }

    public String toString() {
        Queue<TreeNode> q = new LinkedList<>();
        q.add(this);
        LinkedList<String> result = new LinkedList<>();

        while (!q.isEmpty()) {
            TreeNode t = q.poll();
            result.add(t == null ? "null" : String.valueOf(t.val));
            if (t != null) {
                q.add(t.left);
                q.add(t.right);
            }
        }

        // remove trailing nulls
        while (!result.isEmpty() && result.peekLast().equals("null")) {
            result.removeLast();
        }

        return String.join(", ", result);
    }

    public int getMaxDepth() {
        return this.getMaxDepth(this);
    }

    public int size() {
        return this.size(this);
    }

    private int getMaxDepth(TreeNode node) {
        if (node == null) {
            return 0;
        }

        if (node.left == null && node.right == null) {
            return 1;
        }

        return 1 + Math.max(this.getMaxDepth(node.left), this.getMaxDepth(node.right));
    }

    private int size(TreeNode node) {
        if (node == null) {
            return 0;
        }

        if (node.left == null && node.right == null) {
            return 1;
        }

        return 1 + this.size(node.left) + this.size(node.right);
    }
}
