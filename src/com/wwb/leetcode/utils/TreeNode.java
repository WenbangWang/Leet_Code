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

    public static void printTreeLeetCode(TreeNode root) {
        Queue<TreeNode> q = new LinkedList<>();
        q.add(root);
        StringBuilder sb = new StringBuilder();

        while (!q.isEmpty()) {
            TreeNode t = q.poll();
            sb.append(t == null ? "null" : t.val).append(", ");
            if (t != null) {
                q.add(t.left);
                q.add(t.right);
            }
        }
        System.out.println(sb.toString());
    }
}
