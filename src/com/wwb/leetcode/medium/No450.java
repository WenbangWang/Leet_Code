package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

/**
 * Given a root node reference of a BST and a key, delete the node with the given key in the BST.
 * Return the root node reference (possibly updated) of the BST.
 *
 * Basically, the deletion can be divided into two stages:
 *
 * Search for a node to remove.
 * If the node is found, delete the node.
 *
 *
 * Example 1:
 *
 *
 * Input: root = [5,3,6,2,4,null,7], key = 3
 * Output: [5,4,6,2,null,null,7]
 * Explanation: Given key to delete is 3. So we find the node with value 3 and delete it.
 * One valid answer is [5,4,6,2,null,null,7], shown in the above BST.
 * Please notice that another valid answer is [5,2,6,null,4,null,7] and it's also accepted.
 *
 * Example 2:
 *
 * Input: root = [5,3,6,2,4,null,7], key = 0
 * Output: [5,3,6,2,4,null,7]
 * Explanation: The tree does not contain a node with value = 0.
 * Example 3:
 *
 * Input: root = [], key = 0
 * Output: []
 *
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [0, 104].
 * -10^5 <= Node.val <= 10^5
 * Each node has a unique value.
 * root is a valid binary search tree.
 * -10^5 <= key <= 10^5
 */
public class No450 {
    public TreeNode deleteNode(TreeNode root, int key) {
        return solution1(root, key);
    }

    private TreeNode solution1(TreeNode root, int key) {
        if (root != null && root.val == key && root.left == null && root.right == null) {
            return null;
        }

        TreeNode result = findAndReplaceNode(root, key, null, null);

        return result == null ? root : result;
    }

    private TreeNode solution2(TreeNode root, int key) {
        if (root == null) {
            return null;
        }

        if (root.val > key) {
            root.left = solution2(root.left, key);
        } else if (root.val < key) {
            root.right = solution2(root.right, key);
        } else {
            if (root.left == null) {
                return root.right;
            }

            if (root.right == null) {
                return root.left;
            }

            TreeNode rightSmallest = root.right;

            while (rightSmallest.left != null) {
                rightSmallest = rightSmallest.left;
            }

            rightSmallest.left = root.left;

            return root.right;
        }

        return root;
    }

    private TreeNode findAndReplaceNode(TreeNode node, int key, TreeNode fromLeft, TreeNode fromRight) {
        if (node == null) {
            return null;
        }

        if (node.val == key) {
            TreeNode detachedNode = detachNextOrderedNode(node);

            if (fromLeft != null) {
                fromLeft.left = detachedNode;
            }

            if (fromRight != null) {
                fromRight.right = detachedNode;
            }

            return detachedNode;
        }

        if (key < node.val) {
            TreeNode left = findAndReplaceNode(node.left, key, node, null);

            if (left != null) {
                return node;
            }

            return null;
        }

        TreeNode right = findAndReplaceNode(node.right, key, null, node);

        if (right != null) {
            return node;
        }

        return null;
    }

    private TreeNode detachNextOrderedNode(TreeNode node) {
        if (node.right == null) {
            return node.left;
        }

        TreeNode current = node.right;
        TreeNode pre = current;

        // find leftmost (smallest) node
        while(current.left != null) {
            pre = current;
            current = current.left;
        }

        // no left
        if (current == node.right) {
            node.right = node.right.right;
        } else {
            pre.left = current.right;
        }

        current.left = node.left;
        current.right = node.right;

        return current;
    }
}
