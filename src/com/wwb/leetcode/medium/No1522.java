package com.wwb.leetcode.medium;


import java.util.ArrayList;
import java.util.List;

/**
 * Given a root of an N-ary tree, you need to compute the length of the diameter of the tree.
 * <p>
 * The diameter of an N-ary tree is the length of the longest path between any two nodes in the tree. This path may or may not pass through the root.
 * <p>
 * (Nary-Tree input serialization is represented in their level order traversal, each group of children is separated by the null value.)
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 *     <img src="../doc-files/1522_1.png" />
 * <p>
 * <p>
 * Input: root = [1,null,3,2,4,null,5,6]
 * <p>
 * Output: 3
 * <p>
 * Explanation: Diameter is shown in red color.
 * <p>
 * Example 2:
 * <p>
 *     <img src="../doc-files/1522_2.png" />
 * <p>
 * <p>
 * Input: root = [1,null,2,null,3,4,null,5,null,6]
 * <p>
 * Output: 4
 * <p>
 * Example 3:
 * <p>
 *     <img src="../doc-files/1522_3.png" />
 * <p>
 * <p>
 * Input: root = [1,null,2,3,4,5,null,null,6,7,null,8,null,9,10,null,null,11,null,12,null,13,null,null,14]
 * <p>
 * Output: 7
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The depth of the n-ary tree is less than or equal to 1000.
 * The total number of nodes is between [0, 10^4].
 */
public class No1522 {
    private int diameter(Node root) {
        return dfs(root).maxDiameter;
    }

    private Data dfs(Node node) {
        if (node.children.isEmpty()) {
            return new Data(0, 0);
        }

        Data result = new Data(Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (Node child : node.children) {
            Data childResult = dfs(child);

            // Don't really need this? See No543
            result.maxDiameter = Math.max(result.maxDiameter, childResult.maxDiameter);

            if (result.maxDepth != Integer.MIN_VALUE) {
                result.maxDiameter = Math.max(result.maxDiameter, result.maxDepth + childResult.maxDepth);
            }
            result.maxDepth = Math.max(result.maxDepth, childResult.maxDepth);
        }

        result.maxDepth++;

        return result;
    }

    private static class Data {
        int maxDepth;
        int maxDiameter;

        Data(int maxDepth, int maxDiameter) {
            this.maxDepth = maxDepth;
            this.maxDiameter = maxDiameter;
        }
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
