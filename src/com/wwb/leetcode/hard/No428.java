package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Serialization is the process of converting a data structure or object into a sequence of bits so that it can be stored in a file or memory buffer, or transmitted across a network connection link to be reconstructed later in the same or another computer environment.
 * </p>
 *
 * <p>
 * Design an algorithm to serialize and deserialize an N-ary tree.
 * An N-ary tree is a rooted tree in which each node has no more than N children.
 * There is no restriction on how your serialization/deserialization algorithm should work.
 * You just need to ensure that an N-ary tree can be serialized to a string
 * and this string can be deserialized to the original tree structure.
 * </p>
 * <p>
 * For example, you may serialize the following 3-ary tree
 * <p>
 *
 * <img src="../doc-files/428.png" />
 * <p>
 * as [1 [3[5 6] 2 4]]. You do not necessarily need to follow this format,
 * so please be creative and come up with different approaches yourself.
 * <p>
 * <p>
 * <p>
 * Note:
 * <p>
 * N is in the range of [1, 1000]
 * Do not use class member/global/static variables to store states.
 * Your serialize and deserialize algorithms should be stateless.
 */
public class No428 {
    private static final int MASK = (1 << 16) - 1;


    private String serialize(Node root) {
        return doSerialize(root).toString();
    }

    private Node deserialize(String data) {
        char[] chars = data.toCharArray();

        return doDeserialize(data.toCharArray(), new int[]{0});
    }


    private StringBuilder doSerialize(Node node) {
        if (node == null) {
            return new StringBuilder(0);
        }

        List<StringBuilder> childResults = new ArrayList<>(node.children.size());
        int childrenSize = 0;

        for (Node child : node.children) {
            StringBuilder childResult = doSerialize(child);
            childrenSize += childResult.length();

            childResults.add(childResult);
        }

        // 2 is the size of the current node value
        StringBuilder result = new StringBuilder(2 + childrenSize);

        result.append(intToChars(node.val));
        childResults.forEach(result::append);

        return result;
    }

    private Node doDeserialize(char[] chars, int[] offsetPtr) {
        Node node = new Node(twoCharsToInt(chars[offsetPtr[0]], chars[++offsetPtr[0]]));
        int childrenSize = twoCharsToInt(chars[++offsetPtr[0]], chars[++offsetPtr[0]]);

        offsetPtr[0]++;

        node.children.addAll(doDeserializeChildren(chars, offsetPtr, offsetPtr[0] + childrenSize));

        return node;
    }

    private List<Node> doDeserializeChildren(char[] chars, int[] offsetPtr, int end) {
        if (offsetPtr[0] == end) {
            return Collections.emptyList();
        }

        List<Node> result = new ArrayList<>();

        while (offsetPtr[0] < end) {
            result.add(doDeserialize(chars, offsetPtr));
        }

        return result;
    }

    private char[] intToChars(int val) {
        char[] chars = new char[2];

        // Higher 16 bits
        chars[0] = (char) (val >> 16);
        // Lower 16 bits
        chars[1] = (char) (val & MASK);

        return chars;
    }

    private int twoCharsToInt(char char1, char char2) {
        int result = 0;

        result += (int) char1;
        result <<= 16;
        result += (int) char2;

        return result;
    }

    private static class Node {
        public int val;
        public List<Node> children;

        Node(int val) {
            this(val, new ArrayList<>());
        }

        Node(int val, List<Node> children) {
            this.val = val;
            this.children = children;
        }
    }
}
