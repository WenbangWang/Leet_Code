package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * You need to construct a binary tree from a string consisting of parenthesis and integers.
 * <p>
 * The whole input represents a binary tree. It contains an integer followed by zero, one or two pairs of parenthesis. The integer represents the root’s value and a pair of parenthesis contains a child binary tree with the same structure.
 * <p>
 * You always start to construct the left child node of the parent first if it exists.
 *
 * <pre>
 * Example:
 *
 * Input: "4(2(3)(1))(6(5))"
 * Output: return the tree root node representing the following tree:
 *
 *        4
 *      /   \
 *     2     6
 *    / \   /
 *   3   1 5
 *
 * </pre>
 *
 * <pre>
 * Note:
 *
 * There will only be '(', ')', '-' and '0' ~ '9' in the input string.
 * An empty tree is represented by "" instead of "()".
 * </pre>
 */
public class No536 {
    public TreeNode str2tree(String s) {
        return str2tree(s, new int[]{0});
    }

    private TreeNode str2tree(String s, int[] index) {
        int num = 0;
        int sign = 1;

        if (s.charAt(index[0]) == '-') {
            sign = -1;
            index[0]++;
        }

        while (index[0] < s.length() && Character.isDigit(s.charAt(index[0]))) {
            num *= 10;
            num += (s.charAt(index[0]) - '0');

            index[0]++;
        }

        TreeNode node = new TreeNode(num * sign);

        if (index[0] < s.length() && s.charAt(index[0]) == '(') {
            // skip (
            index[0]++;

            node.left = str2tree(s, index);

            // skip )
            index[0]++;
        }

        if (index[0] < s.length() && s.charAt(index[0]) == '(') {
            // skip (
            index[0]++;

            node.right = str2tree(s, index);

            // skip )
            index[0]++;
        }

        return node;
    }
}
