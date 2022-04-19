package com.wwb.leetcode.utils;

import com.wwb.leetcode.hard.No99;

public class main {

    public static void main(String[] args) {
        No99 a = new No99();

        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(3);
        root.left.right = new TreeNode(2);

        a.solution1(root);
    }
}
