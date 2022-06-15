package com.wwb.leetcode.medium;

import java.util.HashSet;
import java.util.Set;

/**
 * Given two nodes of a binary tree p and q, return their lowest common ancestor (LCA).
 *
 * Each node will have a reference to its parent node. The definition for Node is below:
 *
 * According to the definition of LCA on Wikipedia:
 * "The lowest common ancestor of two nodes p and q in a tree T is
 * the lowest node that has both p and q as descendants (where we allow a node to be a descendant of itself)."
 *
 * Constraints:
 *
 * The number of nodes in the tree is in the range [2, 10^5].
 * -10^9 <= Node.val <= 10^9
 * All Node.val are unique.
 * p != q
 * p and q exist in the tree.
 */
public class No1650 {
    public Node lowestCommonAncestor(Node p, Node q) {
        return solution1(p, q);
    }

    // O(n) for time and space
    private Node solution1(Node p, Node q) {
        Node current = p;
        Set<Node> visited = new HashSet<>();

        while (current != null) {
            if (isChild(current, q, visited)) {
                return current;
            }

            visited.add(current);
            current = current.parent;
        }

        return null;
    }

    // O(n) time and O(1) space
    // same as detect intersection of two linked lists
    private Node solution2(Node p, Node q) {
        Node p1 = p;
        Node q1 = q;

        while (p1 != q1) {
            p1 = p1.parent == null ? q : p1.parent;
            q1 = q1.parent == null ? p : q1.parent;
        }

        return p1;
    }

    private boolean isChild(Node node, Node child, Set<Node> visited) {
        if (visited.contains(node)) {
            return false;
        }

        if (node == null) {
            return false;
        }

        if (node == child) {
            return true;
        }

        return isChild(node.left, child, visited) || isChild(node.right, child, visited);
    }

    private static class Node {
        public int val;
        public Node left;
        public Node right;
        public Node parent;
    }
}
