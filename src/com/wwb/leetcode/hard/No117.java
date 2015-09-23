package com.wwb.leetcode.hard;

import com.wwb.leetcode.utils.TreeLinkNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Follow up for problem "Populating Next Right Pointers in Each Node".
 *
 * What if the given tree could be any binary tree? Would your previous solution still work?
 *
 * Note:
 *
 * You may only use constant extra space.
 * For example,
 * Given the following binary tree,
 *     1
 *    /  \
 *   2    3
 *  / \    \
 * 4   5    7
 * After calling your function, the tree should look like:
 *     1 -> NULL
 *    /  \
 *   2 -> 3 -> NULL
 *  / \    \
 * 4-> 5 -> 7 -> NULL
 */
public class No117 {

    public void connect(TreeLinkNode root) {
        if(root == null) {
            return;
        }

        Queue<TreeLinkNode> queue = new LinkedList<>();

        queue.offer(root);

        while(!queue.isEmpty()) {
            TreeLinkNode node = queue.poll();
            TreeLinkNode next=  node.next;
            TreeLinkNode nextLevelNode;

            if(node.left != null) {
                queue.offer(node.left);
            }

            if(node.right != null) {
                queue.offer(node.right);
            }

            if(node.left != null && node.right != null) {
                node.left.next = node.right;
                nextLevelNode = node.right;
            } else {
                nextLevelNode = node.left != null ? node.left : node.right;
            }

            while(next != null) {
                if((next.left != null || next.right != null) && nextLevelNode != null) {
                    nextLevelNode.next = next.left != null ? next.left : next.right;
                    break;
                }

                next = next.next;
            }
        }
    }
}
