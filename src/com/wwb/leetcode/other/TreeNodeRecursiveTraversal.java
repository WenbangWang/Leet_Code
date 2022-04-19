package com.wwb.leetcode.other;

import com.wwb.leetcode.utils.TreeNode;

import java.util.function.Supplier;

public class TreeNodeRecursiveTraversal<T> implements TreeNodeTraversal<T> {
    private Supplier<TreeTraverseActor<T>> traversalActorFactory;

    public TreeNodeRecursiveTraversal(Supplier<TreeTraverseActor<T>> traversalActorFactory) {
        this.traversalActorFactory = traversalActorFactory;
    }

    @Override
    public T inOrder(TreeNode root) {
        TreeTraverseActor<T> actor = traversalActorFactory.get();

        inOrder(root, actor);

        return actor.collect();
    }

    @Override
    public T preOrder(TreeNode root) {
        TreeTraverseActor<T> actor = traversalActorFactory.get();

        preOrder(root, actor);

        return actor.collect();
    }

    @Override
    public T postOrder(TreeNode root) {
        TreeTraverseActor<T> actor = traversalActorFactory.get();

        postOrder(root, actor);

        return actor.collect();
    }

    private void inOrder(TreeNode node, TreeTraverseActor<T> actor) {
        if (node == null) {
            return;
        }

        inOrder(node.left, actor);
        actor.apply(node);
        inOrder(node.right, actor);
    }

    private void preOrder(TreeNode node, TreeTraverseActor<T> actor) {
        if (node == null) {
            return;
        }

        actor.apply(node);
        preOrder(node.left, actor);
        preOrder(node.right, actor);
    }

    private void postOrder(TreeNode node, TreeTraverseActor<T> actor) {
        if (node == null) {
            return;
        }

        postOrder(node.left, actor);
        postOrder(node.right, actor);
        actor.apply(node);
    }
}
