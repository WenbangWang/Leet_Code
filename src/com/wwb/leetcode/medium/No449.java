package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Serialization is converting a data structure or object into a sequence of bits
 * so that it can be stored in a file or memory buffer, or transmitted across a network connection
 * link to be reconstructed later in the same or another computer environment.
 *
 * Design an algorithm to serialize and deserialize a binary search tree.
 * There is no restriction on how your serialization/deserialization algorithm should work.
 * You need to ensure that a binary search tree can be serialized to a string,
 * and this string can be deserialized to the original tree structure.
 *
 * The encoded string should be as compact as possible.
 *
 *
 *
 * Example 1:
 *
 * Input: root = [2,1,3]
 * Output: [2,1,3]
 * Example 2:
 *
 * Input: root = []
 * Output: []
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [0, 10^4].
 * 0 <= Node.val <= 10^4
 * The input tree is guaranteed to be a binary search tree.
 */
public class No449 {
    public class Codec {

        // Encodes a tree to a single string.
        public String serialize(TreeNode root) {
            return preorderSerialize(root).toString();
        }

        // Decodes your encoded data to tree.
        public TreeNode deserialize(String data) {
            Queue<Integer> values = preorderDeserialize(data);

            return deserialize(values,Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        private Queue<Integer> preorderDeserialize(String data) {
            Queue<Integer> result = new ArrayDeque<>();

            for (int i = 0; i < data.length(); i += 2) {
                result.add(byteStringToInt(data.substring(i, i + 2)));
            }

            return result;
        }

        private StringBuilder preorderSerialize(TreeNode node) {
            StringBuilder result = new StringBuilder();

            if (node != null) {
                result.append(intToByteString(node.val));
                result.append(preorderSerialize(node.left));
                result.append(preorderSerialize(node.right));
            }

            return result;
        }

        private TreeNode deserialize(Queue<Integer> values, int min, int max) {
            if (values.isEmpty()) {
                return null;
            }

            int num = values.peek();

            if (num > max || num < min) {
                return null;
            }

            num = values.poll();

            TreeNode node = new TreeNode(num);

            node.left = deserialize(values, min, num);
            node.right = deserialize(values, num, max);

            return node;
        }

        private String intToByteString(int value) {
            char[] bytes = new char[2];
            int mask = (2 << 16) - 1;

            for (int i = 1; i >= 0; i--) {
                bytes[1 - i] = (char) ((value >> (16 * i)) & mask);
            }

            return new String(bytes);
        }

        private int byteStringToInt(String byteString) {
            int result = 0;

            for (int i = 0; i < byteString.length(); i++) {
                result = (result << 16) + (int) byteString.charAt(i);
            }

            return result;
        }
    }

// Your Codec object will be instantiated and called as such:
// Codec ser = new Codec();
// Codec deser = new Codec();
// String tree = ser.serialize(root);
// TreeNode ans = deser.deserialize(tree);
// return ans;
}
