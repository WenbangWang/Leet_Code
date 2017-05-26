package com.wwb.leetcode.other;

import java.util.LinkedList;
import java.util.Queue;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Convert
 *      1
 *     / \
 *    2   3
 *   /    /\
 *  4    5  6
 *
 * into:
 * 1
 * ｜
 * 2 ——3
 * ｜
 * 4——5——6
 */
public class SalbringTree {
    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.left.left = new TreeNode(4);
        root.right = new TreeNode(3);
        root.right.left = new TreeNode(5);
        root.right.right = new TreeNode(6);

        SalbringTree.convert(root);
    }

    public static void convert (TreeNode root) {
        if (root == null) {
            return;
        }

        Queue<TreeNode> queue = new LinkedList<>();

        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode head = queue.peek();
            TreeNode pre = null;
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                TreeNode current = queue.poll();

                if (current.left != null) {
                    queue.offer(current.left);
                    current.left = null;
                }

                if (current.right != null) {
                    queue.offer(current.right);
                    current.right = null;
                }

                if (pre != null) {
                    pre.right = current;
                }

                pre = current;
            }

            head.left = queue.peek();
        }
    }
}
