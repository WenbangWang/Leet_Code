package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root node of a binary search tree and two integers low and high,
 * return the sum of values of all nodes with a value in the inclusive range [low, high].
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: root = [10,5,15,3,7,null,18], low = 7, high = 15
 * <p>
 * Output: 32
 * <p>
 * Explanation: Nodes 7, 10, and 15 are in the range [7, 15]. 7 + 10 + 15 = 32.
 * <p>
 * Example 2:
 * <p>
 * <p>
 * Input: root = [10,5,15,3,7,13,18,1,null,6], low = 6, high = 10
 * <p>
 * Output: 23
 * <p>
 * Explanation: Nodes 6, 7, and 10 are in the range [6, 10]. 6 + 7 + 10 = 23.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [1, 2 * 10^4].
 * 1 <= Node.val <= 10^5
 * 1 <= low <= high <= 10^5
 * All Node.val are unique.
 */
public class No938 {
    public int rangeSumBST(TreeNode root, int low, int high) {
        if (root == null) {
            return 0;
        }

        if (root.val < low) {
            return rangeSumBST(root.right, low, high);
        }

        if (root.val > high) {
            return rangeSumBST(root.left, low, high);
        }

        return rangeSumBST(root.left, low, root.val - 1) + rangeSumBST(root.right, root.val + 1, high) + root.val;
    }
}
