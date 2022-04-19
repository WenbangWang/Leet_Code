package com.wwb.leetcode.other;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class TreeNodeValueTraverseActor implements TreeTraverseActor<List<Integer>> {
    private List<Integer> result;

    public TreeNodeValueTraverseActor() {
        this.result = new ArrayList<>();
    }

    @Override
    public void apply(TreeNode node) {
        result.add(node.val);
    }

    @Override
    public List<Integer> collect() {
        return result;
    }
}
