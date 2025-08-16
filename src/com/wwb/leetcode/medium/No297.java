package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Serialization is the process of converting a data structure or object into a sequence of bits
 * so that it can be stored in a file or memory buffer,
 * or transmitted across a network connection link to be reconstructed later in the same or another computer environment.
 * <p>
 * Design an algorithm to serialize and deserialize a binary tree.
 * There is no restriction on how your serialization/deserialization algorithm should work.
 * You just need to ensure that a binary tree can be serialized to a string
 * and this string can be deserialized to the original tree structure.
 *
 * <pre>
 * For example, you may serialize the following tree
 *
 *   1
 *  / \
 * 2   3
 *    / \
 *   4   5
 * as "[1,2,3,null,null,4,5]", just the same as how LeetCode OJ serializes a binary tree.
 * You do not necessarily need to follow this format, so please be creative and come up with different approaches yourself.
 * Note: Do not use class member/global/static variables to store states. Your serialize and deserialize algorithms should be stateless.
 * </pre>
 */
public class No297 {

    public class Codec {
        private static final String SPLITTER = ",";
        private static final String NN = "X";

        // Encodes a tree to a single string.
        public String serialize(TreeNode root) {
            return String.join(SPLITTER, preorderSerialize(root));
        }

        private List<String> preorderSerialize(TreeNode node) {
            if (node == null) {
                return Collections.singletonList(NN);
            }

            List<String> result = new ArrayList<>();

            result.add(String.valueOf(node.val));
            result.addAll(preorderSerialize(node.left));
            result.addAll(preorderSerialize(node.right));

            return result;
        }

        // Decodes your encoded data to tree.
        public TreeNode deserialize(String data) {
            Queue<String> nodes = new LinkedList<>(Arrays.asList(data.split(SPLITTER)));
            return this.buildTree(nodes);
        }

        private TreeNode buildTree(Queue<String> nodes) {
            String val = nodes.poll();
            if (val.equals(NN)) {
                return null;
            }

            TreeNode node = new TreeNode(Integer.parseInt(val));
            node.left = this.buildTree(nodes);
            node.right = this.buildTree(nodes);
            return node;
        }
    }
}
