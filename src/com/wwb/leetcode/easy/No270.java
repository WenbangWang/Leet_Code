package com.wwb.leetcode.easy;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given the root of a binary search tree and a target value, return the value in the BST that is closest to the target.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: root = [4,2,5,1,3], target = 3.714286
 * Output: 4
 * Example 2:
 *
 * Input: root = [1], target = 4.428571
 * Output: 1
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [1, 10^4].
 * 0 <= Node.val <= 10^9
 * -10^9 <= target <= 10^9
 */
public class No270 {
    public int closestValue(TreeNode root, double target) {
        int closestValue = root.val;

        while(root != null) {
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
