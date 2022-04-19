package com.wwb.leetcode.hard;

import com.wwb.leetcode.other.TreeNodeMorrisTraversal;
import com.wwb.leetcode.other.TreeNodeTraversal;
import com.wwb.leetcode.other.TreeTraverseActor;
import com.wwb.leetcode.utils.TreeNode;

/**
 * Two elements of a binary search tree (BST) are swapped by mistake.
 * <p>
 * Recover the tree without changing its structure.
 * <p>
 * Note:
 * A solution using O(n) space is pretty straight forward. Could you devise a constant space solution?
 */
public class No99 {

    public void recoverTree(TreeNode root) {
        TreeNode pre = null;
        TreeNode first = null;
        TreeNode second = null;

        while (root != null) {
            if (root.left != null) {
                TreeNode left = root.left;

                while (left.right != null && left.right != root) {
                    left = left.right;
                }

                if (left.right != null) {
                    left.right = null;
                    if (pre != null && pre.val > root.val) {
                        second = root;
                        if (first == null) {
                            first = pre;
                        }
                    }

                    pre = root;

                    root = root.right;
                } else {
                    left.right = root;
                    root = root.left;
                }
            } else {
                if (pre != null && pre.val > root.val) {
                    second = root;
                    if (first == null) {
                        first = pre;
                    }
                }

                pre = root;
                root = root.right;
            }
        }

        if (first != null) {
            int t = first.val;
            first.val = second.val;
            second.val = t;
        }
    }

    // With extra space.
    public void solution1(TreeNode root) {
        if (root == null) {
            return;
        }

        TreeNodeTraversal<PointerHolder> traversal = new TreeNodeMorrisTraversal<>(PointerHolderTraversalActor::new);

        PointerHolder pointerHolder = traversal.inOrder(root);

        int t = pointerHolder.first.val;
        pointerHolder.first.val = pointerHolder.second.val;
        pointerHolder.second.val = t;
    }


    private static class PointerHolderTraversalActor implements TreeTraverseActor<PointerHolder> {
        PointerHolder pointerHolder;

        PointerHolderTraversalActor() {
            this.pointerHolder = new PointerHolder();
        }

        @Override
        public void apply(TreeNode node) {
            if (pointerHolder.pre != null) {
                if (pointerHolder.first == null && pointerHolder.pre.val >= node.val) {
                    pointerHolder.first = pointerHolder.pre;
                }

                if (pointerHolder.first != null && pointerHolder.pre.val >= node.val) {
                    pointerHolder.second = node;
                }
            }

            pointerHolder.pre = node;
        }

        @Override
        public PointerHolder collect() {
            return pointerHolder;
        }
    }

    private static class PointerHolder {
        TreeNode pre = null;
        TreeNode first = null;
        TreeNode second = null;
    }
}
