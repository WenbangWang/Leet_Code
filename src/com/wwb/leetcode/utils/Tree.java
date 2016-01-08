package com.wwb.leetcode.utils;

public class Tree {

    private TreeNode root;

    public Tree(TreeNode root) {
        this.root = root;
    }

    public void inorder() {
        TreeNode current = this.root;

        while(current != null) {
            if(current.left == null) {
                System.out.println(current.val);
                current = current.right;
            } else {
                TreeNode left = current.left;

                //Go to right-most of left sub tree
                while(left.right != null && left.right != current) {
                    left = left.right;
                }

                if(left.right == null) {
                    left.right = current;
                    current = current.left;
                } else {
                    System.out.println(current.val);
                    left.right = null;
                    current = current.right;
                }
            }
        }
    }

    public void preorder() {
        TreeNode current = this.root;

        while(current != null) {
            if(current.left == null) {
                System.out.println(current.val);
                current = current.right;
            } else {
                TreeNode left = current.left;

                while(left.right != null && left.right != current) {
                    left = left.right;
                }

                if(left.right == null) {
                    System.out.println(current.val);
                    left.right = current;
                    current = current.left;
                } else {
                    left.right = null;
                    current = current.right;
                }
            }
        }
    }
}
