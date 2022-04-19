package com.wwb.leetcode.other;

import com.wwb.leetcode.utils.TreeNode;

public interface TreeNodeTraversal<T> {

    T inOrder(TreeNode root);

    T preOrder(TreeNode root);

    T postOrder(TreeNode root);
}
