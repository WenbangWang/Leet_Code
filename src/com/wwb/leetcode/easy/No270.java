package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root of a binary search tree and a target value, return the value in the BST that is closest to the target.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: root = [4,2,5,1,3], target = 3.714286
 * <p>
 * Output: 4
 * <p>
 * Example 2:
 * <p>
 * Input: root = [1], target = 4.428571
 * <p>
 * Output: 1
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [1, 10^4].
 * <p>
 * 0 <= Node.val <= 10^9
 * <p>
 * -10^9 <= target <= 10^9
 */
public class No270 {
    public int closestValue(TreeNode root, double target) {
        int closestValue = root.val;

        while (root != null) {
            var currentDelta = Math.abs(target - root.val);
            var previousDelta = Math.abs(target - closestValue);

            if (currentDelta < previousDelta) {
                closestValue = root.val;
            }

            root = root.val >= target ? root.left : root.right;
        }

        return closestValue;
    }
}
