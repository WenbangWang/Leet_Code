package com.wwb.leetcode.other;

import com.wwb.leetcode.utils.TreeNode;

public interface TreeTraverseActor<T> {
    void apply(TreeNode node);

    T collect();
}
