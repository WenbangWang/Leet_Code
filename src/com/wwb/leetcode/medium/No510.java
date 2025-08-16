package com.wwb.leetcode.medium;

/**
 * Given a binary search tree and a node in it, find the in-order successor of that node in the BST.
 * <p>
 * The successor of a node p is the node with the smallest key greater than p.val.
 * <p>
 * You will have direct access to the node but not to the root of the tree. Each node will have a reference to its parent node.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/510_1.png" />
 *
 * Input:
 * root = {"$id":"1","left":{"$id":"2","left":null,"parent":{"$ref":"1"},"right":null,"val":1},"parent":null,"right":{"$id":"3","left":null,"parent":{"$ref":"1"},"right":null,"val":3},"val":2}
 * p = 1
 * Output: 2
 * Explanation: 1's in-order successor node is 2. Note that both p and the return value is of Node type.
 * Example 2:
 *
 * <img src="../doc-files/510_2.png" />
 *
 * Input:
 * root = {"$id":"1","left":{"$id":"2","left":{"$id":"3","left":{"$id":"4","left":null,"parent":{"$ref":"3"},"right":null,"val":1},"parent":{"$ref":"2"},"right":null,"val":2},"parent":{"$ref":"1"},"right":{"$id":"5","left":null,"parent":{"$ref":"2"},"right":null,"val":4},"val":3},"parent":null,"right":{"$id":"6","left":null,"parent":{"$ref":"1"},"right":null,"val":6},"val":5}
 * p = 6
 * Output: null
 * Explanation: There is no in-order successor of the current node, so the answer is null.
 * Example 3:
 *
 * <img src="../doc-files/510_3.png" />
 *
 * Input:
 * root = {"$id":"1","left":{"$id":"2","left":{"$id":"3","left":{"$id":"4","left":null,"parent":{"$ref":"3"},"right":null,"val":2},"parent":{"$ref":"2"},"right":{"$id":"5","left":null,"parent":{"$ref":"3"},"right":null,"val":4},"val":3},"parent":{"$ref":"1"},"right":{"$id":"6","left":null,"parent":{"$ref":"2"},"right":{"$id":"7","left":{"$id":"8","left":null,"parent":{"$ref":"7"},"right":null,"val":9},"parent":{"$ref":"6"},"right":null,"val":13},"val":7},"val":6},"parent":null,"right":{"$id":"9","left":{"$id":"10","left":null,"parent":{"$ref":"9"},"right":null,"val":17},"parent":{"$ref":"1"},"right":{"$id":"11","left":null,"parent":{"$ref":"9"},"right":null,"val":20},"val":18},"val":15}
 * p = 15
 * Output: 17
 * Example 4:
 *
 * <img src="../doc-files/510_4.png" />
 *
 * Input:
 * root = {"$id":"1","left":{"$id":"2","left":{"$id":"3","left":{"$id":"4","left":null,"parent":{"$ref":"3"},"right":null,"val":2},"parent":{"$ref":"2"},"right":{"$id":"5","left":null,"parent":{"$ref":"3"},"right":null,"val":4},"val":3},"parent":{"$ref":"1"},"right":{"$id":"6","left":null,"parent":{"$ref":"2"},"right":{"$id":"7","left":{"$id":"8","left":null,"parent":{"$ref":"7"},"right":null,"val":9},"parent":{"$ref":"6"},"right":null,"val":13},"val":7},"val":6},"parent":null,"right":{"$id":"9","left":{"$id":"10","left":null,"parent":{"$ref":"9"},"right":null,"val":17},"parent":{"$ref":"1"},"right":{"$id":"11","left":null,"parent":{"$ref":"9"},"right":null,"val":20},"val":18},"val":15}
 * p = 13
 * Output: 15
 *
 *
 * Note:
 *
 * If the given node has no in-order successor in the tree, return null.
 * It's guaranteed that the values of the tree are unique.
 * Remember that we are using the Node type instead of TreeNode type so their string representation are different.
 * </pre>
 * <p>
 * <p>
 * Follow up:
 * <p>
 * Could you solve it without looking up any of the node's values?
 */
public class No510 {
    public Node inorderSuccessor(Node node) {
        // Case 1: Right subtree exists
        if (node.right != null) {
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }

        // Case 2: No right subtree — go up until you come from a left child
        while (node.parent != null && node == node.parent.right) {
            node = node.parent;
        }
        return node.parent;
    }

    public static class Node {
        public int val;
        public Node left, right, parent;
    }
}
