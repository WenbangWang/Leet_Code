package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a binary tree, return the zigzag level order traversal of its nodes' values.
 * (ie, from left to right, then right to left for the next level and alternate between).
 *
 * For example:
 * Given binary tree {3,9,20,#,#,15,7},
 *    3
 *   / \
 *  9  20
 *  /  \
 * 15   7
 * return its zigzag level order traversal as:
 * [
 *   [3],
 *   [20,9],
 *   [15,7]
 * ]
 */
public class No103 {

    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        return solution1(root);
    }

    private List<List<Integer>> solution1(TreeNode root) {
        if(root == null) {
            return Collections.emptyList();
        }

        List<List<Integer>> result = new ArrayList<>();
        LinkedList<TreeNode> linkedList = new LinkedList<>();
        int level = 0;

        linkedList.offer(root);

        while(!linkedList.isEmpty()) {
            List<Integer> currentLevel = new ArrayList<>();
            int size = linkedList.size();
            level++;

            for(int i = size - 1; i >= 0; i--) {
                TreeNode node = linkedList.get(i);

                if (level % 2 == 0) {
                    if (node.right != null) {
                        linkedList.offer(node.right);
                    }
                    if (node.left != null) {
                        linkedList.offer(node.left);
                    }
                } else {
                    if (node.left != null) {
                        linkedList.offer(node.left);
                    }
                    if (node.right != null) {
                        linkedList.offer(node.right);
                    }
                }

                currentLevel.add(node.val);
            }

            result.add(currentLevel);

            for(int i = 0; i < size; i++) {
                linkedList.remove();
            }
        }

        return result;
    }

    private List<List<Integer>> solution2(TreeNode root) {
        if(root == null) {
            return Collections.emptyList();
        }

        List<List<Integer>> result = new ArrayList<>();
        LinkedList<TreeNode> linkedList = new LinkedList<>();
        int level = 0;

        linkedList.offer(root);

        while(!linkedList.isEmpty()) {
            List<Integer> currentLevel = new ArrayList<>();
            int size = linkedList.size();

            for (int i = 0; i < size; i++){
                TreeNode node = linkedList.poll();
                currentLevel.add(node.val);

                if (node.left != null) {
                    linkedList.offer(node.left);
                }

                if (node.right != null) {
                    linkedList.offer(node.right);
                }
            }


            if (level % 2 == 1) {
                Collections.reverse(currentLevel);
            }
            level++;
            result.add(currentLevel);
        }

        return result;
    }
}
