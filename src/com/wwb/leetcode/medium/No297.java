package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
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
        private static final int MASK = (1 << 16) - 1;

        // Encodes a tree to a single string.
        public String serialize(TreeNode root) {
//            return String.join(SPLITTER, preorderSerialize(root));
            return doSerialize(root).toString();
        }

        private StringBuilder doSerialize(TreeNode node) {
            if (node == null) {
                return new StringBuilder(0);
            }

            NodeType nodeType = NodeType.of(node);
            StringBuilder result = new StringBuilder();

            result.append(intToChars(node.val));
            result.append(intToChars(nodeType.value));
            result.append(doSerialize(node.left));
            result.append(doSerialize(node.right));

            return result;
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
//            Queue<String> nodes = new LinkedList<>(Arrays.asList(data.split(SPLITTER)));
//            return this.buildTree(nodes);

            return doDeserialize(data, new int[1]);
        }

        private TreeNode doDeserialize(String data, int[] offsetPtr) {
            if (offsetPtr[0] == data.length()) {
                return null;
            }

            TreeNode node = new TreeNode(readInt(data, offsetPtr));
            NodeType nodeType = readNodeType(data, offsetPtr);

            switch (nodeType) {
                case LEFT -> node.left = doDeserialize(data, offsetPtr);
                case RIGHT -> node.right = doDeserialize(data, offsetPtr);
                case BOTH -> {
                    node.left = doDeserialize(data, offsetPtr);
                    node.right = doDeserialize(data, offsetPtr);
                }
            }

            return node;
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

        private int readInt(String s, int[] offsetPtr) {
            int result = charsToInt(s.charAt(offsetPtr[0]), s.charAt(offsetPtr[0] + 1));

            offsetPtr[0] += 2;

            return result;
        }

        private NodeType readNodeType(String s, int[] offsetPtr) {
            return NodeType.of(readInt(s, offsetPtr));
        }

        private int charsToInt(char c1, char c2) {
            int result = 0;

            result += (int) c1;
            result <<= 16;
            result += (int) c2;

            return result;
        }

        private char[] intToChars(int v) {
            char[] result = new char[2];

            result[0] = (char) (v >> 16);
            result[1] = (char) (v & MASK);

            return result;
        }

        private enum NodeType {
            NONE(0),
            LEFT(1),
            BOTH(2),
            RIGHT(3);

            int value;

            NodeType(int value) {
                this.value = value;
            }

            static NodeType of(int value) {
                for (NodeType type : NodeType.values()) {
                    if (type.value == value) {
                        return type;
                    }
                }
                throw new IllegalArgumentException("Unknown NodeType: " + value);
            }

            static NodeType of(TreeNode node) {
                if (node.left == null && node.right == null)
                    return NodeType.NONE;

                if (node.left != null && node.right != null) {
                    return NodeType.BOTH;
                }

                return node.left == null ? NodeType.RIGHT : NodeType.LEFT;
            }
        }
    }
}
