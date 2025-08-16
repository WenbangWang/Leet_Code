package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root of a binary tree, return the number of nodes where the value of the node is equal to the average of the values in its subtree.
 * <p>
 * Note:
 * <p>
 * The average of n elements is the sum of the n elements divided by n and rounded down to the nearest integer.
 * A subtree of root is a tree consisting of root and all of its descendants.
 *
 *
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/2265_1.png" />
 *
 * Input: root = [4,8,5,0,1,null,6]
 * Output: 5
 * Explanation:
 * For the node with value 4: The average of its subtree is (4 + 8 + 5 + 0 + 1 + 6) / 6 = 24 / 6 = 4.
 * For the node with value 5: The average of its subtree is (5 + 6) / 2 = 11 / 2 = 5.
 * For the node with value 0: The average of its subtree is 0 / 1 = 0.
 * For the node with value 1: The average of its subtree is 1 / 1 = 1.
 * For the node with value 6: The average of its subtree is 6 / 1 = 6.
 * </pre>
 *
 * <pre>
 * Example 2:
 *
 * <img src="../doc-files/2265_2.png" />
 *
 * Input: root = [1]
 * Output: 1
 * Explanation: For the node with value 1: The average of its subtree is 1 / 1 = 1.
 * </pre>
 *
 * <pre>
 * Constraints:
 *
 * The number of nodes in the tree is in the range [1, 1000].
 * 0 <= Node.val <= 1000
 * </pre>
 */
public class No2265 {
    public static void main(String[] args) {
        System.out.println(new No2265().averageOfSubtree(TreeNode.buildTreeLeetCode(new Integer[]{4,8,5,0,1,null,6})));
    }
    public int averageOfSubtree(TreeNode root) {
        return averageOfSubtree(root, new int[1], new int[1]);
    }

    private int averageOfSubtree(TreeNode node, int[] numberOfNodes, int[] sum) {
        if (node == null) {
            return 0;
        }

        int[] leftNumberOfNodes = new int[1];
        int[] leftSum = new int[1];
        int[] rightNumberOfNodes = new int[1];
        int[] rightSum = new int[1];

        int left = averageOfSubtree(node.left, leftNumberOfNodes, leftSum);
        int right = averageOfSubtree(node.right, rightNumberOfNodes, rightSum);

        numberOfNodes[0] = leftNumberOfNodes[0] + rightNumberOfNodes[0] + 1;
        sum[0] = leftSum[0] + rightSum[0] + node.val;

        return left + right + ((sum[0] / numberOfNodes[0]) == node.val ? 1 : 0);
    }
}
