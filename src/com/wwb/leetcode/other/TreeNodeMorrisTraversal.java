package com.wwb.leetcode.other;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class TreeNodeMorrisTraversal<T> implements TreeNodeTraversal<T> {
    private Supplier<TreeTraverseActor<T>> traversalActorFactory;

    public TreeNodeMorrisTraversal(Supplier<TreeTraverseActor<T>> traversalActorFactory) {
        this.traversalActorFactory = traversalActorFactory;
    }

    public T inOrder(TreeNode root) {
        TreeTraverseActor<T> actor = traversalActorFactory.get();

        if (root == null) {
            return actor.collect();
        }

        while (root != null) {
            // No left sub-tree, visit the current node
            // then move on to the right sub-tree.
            if (root.left == null) {
                actor.apply(root);

                root = root.right;

                continue;
            }

            TreeNode current = root.left;

            while (current.right != root && current.right != null) {
                current = current.right;
            }

            // Put root to current node's left so we can
            // keep the reference in later iteration.
            if (current.right == null) {
                current.right = root;

                root = root.left;

                continue;
            }

            // At this point, we've already traversed
            // left sub-tree, we should visit the current
            // node (root) and start traverse right
            // sub-tree.
            actor.apply(root);

            root = root.right;
            // Detach the reference we've done before
            // to revert to the original structure.
            current.right = null;
        }

        return actor.collect();
    }

    public T preOrder(TreeNode root) {
        TreeTraverseActor<T> actor = traversalActorFactory.get();

        if (root == null) {
            return actor.collect();
        }

        while (root != null) {
            // No left sub-tree, visit the current node
            // then move on to the right sub-tree.
            if (root.left == null) {
                actor.apply(root);

                root = root.right;

                continue;
            }

            TreeNode current = root.left;

            while (current.right != root && current.right != null) {
                current = current.right;
            }

            // Put root to current node's left so we can
            // keep the reference in later iteration.
            // We should visit the current node then
            // move on to traverse the left sub-tree.
            if (current.right == null) {
                actor.apply(root);

                current.right = root;
                root = root.left;

                continue;
            }

            // At this point, we've already visited
            // root node, traversed left sub-tree
            // we should start traverse right sub-tree.
            root = root.right;
            // Detach the reference we've done before
            // to revert to the original structure.
            current.right = null;
        }

        return actor.collect();
    }

    // This is similar to preOder traversal. The only difference is
    // instead of traversing left sub-tree first, we traverse right sub-tree
    // first.
    // visit current node, traverse right sub-tree, traverse left sub-tree
    // then reverse.
    public T postOrder(TreeNode root) {
        TreeTraverseActor<T> actor = traversalActorFactory.get();

        if (root == null) {
            return actor.collect();
        }

        List<TreeNode> reversedVisitedNodes = new ArrayList<>();

        while (root != null) {
            // No right sub-tree, visit the current node
            // then move on to the left sub-tree.
            if (root.right == null) {
                reversedVisitedNodes.add(root);

                root = root.left;

                continue;
            }

            TreeNode current = root.right;

            while (current.left != root && current.left != null) {
                current = current.left;
            }

            // Put root to current node's right so we can
            // keep the reference in later iteration.
            // We should visit the current node then
            // move on to traverse the right sub-tree.
            if (current.left == null) {
                reversedVisitedNodes.add(root);

                current.left = root;
                root = root.right;

                continue;
            }

            // At this point, we've already visited
            // root node, traversed right sub-tree
            // we should start traverse left sub-tree.
            root = root.left;
            // Detach the reference we've done before
            // to revert to the original structure.
            current.right = null;
        }

        Collections.reverse(reversedVisitedNodes);

        for (TreeNode node: reversedVisitedNodes) {
            actor.apply(node);
        }

        return actor.collect();
    }
}
