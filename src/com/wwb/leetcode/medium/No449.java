package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialization is converting a data structure or object into a sequence of bits so that it can be stored in a file or memory buffer, or transmitted across a network connection link to be reconstructed later in the same or another computer environment.
 *
 * Design an algorithm to serialize and deserialize a binary search tree. There is no restriction on how your serialization/deserialization algorithm should work. You need to ensure that a binary search tree can be serialized to a string, and this string can be deserialized to the original tree structure.
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
            List<Integer> values = preorderDeserialize(data);

            var result = deserialize(values, 0, values.size() - 1, Integer.MAX_VALUE, Integer.MAX_VALUE);

            return result == null ? null : result.node;
        }

        private List<Integer> preorderDeserialize(String data) {
            List<Integer> result = new ArrayList<>();

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

        private TreeNode deserialize(List<Integer> values, int start, int end) {
            if (start > end) {
                return null;
            }

            if (values.get(start) == null) {
                return null;
            }

            TreeNode node = new TreeNode(values.get(start));

            int nextGreaterValueIndex = findNextGreaterValueIndex(start + 1, end, values, values.get(start));

            node.left = deserialize(values, start + 1, nextGreaterValueIndex == -1 ? end : nextGreaterValueIndex - 1);
            node.right = nextGreaterValueIndex == -1 ? null : deserialize(values, nextGreaterValueIndex, end);

            return node;
        }

        private Pair deserialize(List<Integer> values, int start, int end, int min, int max) {
            if (start > end) {
                return null;
            }

            if (values.get(start) == null) {
                return null;
            }

            if (values.get(start) > max) {
                return new Pair(start, null);
            }

            if (values.get(start) < min) {
                return new Pair(start, null);
            }

            TreeNode node = new TreeNode(values.get(start));

            var left = deserialize(values, start + 1, end, min, node.val);

            if (left == null) {
                return new Pair(values.size(), node);
            }

            node.left = left.node;

            var right = deserialize(values, left.nextIndex, end, node.val, max);

            if (right == null) {
                return new Pair(values.size(), node);
            }

            node.right = right.node;

            return new Pair(right.nextIndex, node);
        }

        private int findNextGreaterValueIndex(int start, int end, List<Integer> values, int target) {
            for (int i = start; i <= end; i++) {
                if (values.get(i) != null && values.get(i) > target) {
                    return i;
                }
            }

            return -1;
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

        private class Pair {
            TreeNode node;
            int nextIndex;

            Pair(int nextIndex, TreeNode node) {
                this.nextIndex = nextIndex;
                this.node = node;
            }
        }
    }

// Your Codec object will be instantiated and called as such:
// Codec ser = new Codec();
// Codec deser = new Codec();
// String tree = ser.serialize(root);
// TreeNode ans = deser.deserialize(tree);
// return ans;
}
