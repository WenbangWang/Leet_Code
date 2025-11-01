package com.wwb.leetcode.hard;

import com.wwb.leetcode.utils.TreeNode;

import java.util.*;

/**
 * Given the root of a binary search tree, a target value, and an integer k,
 * return the k values in the BST that are closest to the target. You may return the answer in any order.
 * <p>
 * You are guaranteed to have only one unique set of k values in the BST that are closest to the target.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: root = [4,2,5,1,3], target = 3.714286, k = 2
 * Output: [4,3]
 * Example 2:
 * <p>
 * Input: root = [1], target = 0.000000, k = 1
 * Output: [1]
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is n.
 * 1 <= k <= n <= 10^4.
 * 0 <= Node.val <= 10^9
 * -10^9 <= target <= 10^9
 * <p>
 * <p>
 * Follow up: Assume that the BST is balanced. Could you solve it in less than O(n) runtime (where n = total nodes)?
 */
public class No272 {
    public List<Integer> closestKValues(TreeNode root, double target, int k) {
        return solution1(root, target, k);
    }

    // This is guaranteed to be O(N) time and O((logN) space and it mutated the original input.
    private List<Integer> solution1(TreeNode root, double target, int k) {
        List<Integer> result = new ArrayList<>();

        ClosestNode closestNode = new ClosestNode(root, getDelta(root, target));

        flatten(root, target, closestNode);

        result.add(closestNode.node.val);

        var left = closestNode.node.left;
        var right = closestNode.node.right;

        // we already added one node
        k--;

        while (k != 0) {
            // This shouldn't happen
            if (left == null && right == null) {
                return result;
            }

            if (left == null) {
                result.add(right.val);
                right = right.right;
                k--;
                continue;
            }

            if (right == null) {
                result.add(left.val);
                left = left.left;
                k--;
                continue;
            }

            var leftDelta = getDelta(left, target);
            var rightDelta = getDelta(right, target);

            if (leftDelta > rightDelta) {
                result.add(right.val);
                right = right.right;
            } else {
                result.add(left.val);
                left = left.left;
            }

            k--;
        }

        return result;
    }

    private List<Integer> solution2(TreeNode root, double target, int k) {
        List<Integer> result = new ArrayList<>();
        var predecessors = getPredecessors(root, target);
        var successors = getSuccessors(root, target);

        // If the target equals to a value of a node, we should pop that node from one of the two stacks.
        if (!successors.isEmpty() && !predecessors.isEmpty() && successors.peek().val == predecessors.peek().val) {
            getNextPredecessor(predecessors);
        }

        while (k != 0) {
            // This shouldn't happen
            if (predecessors.isEmpty() && successors.isEmpty()) {
                return result;
            }

            if (predecessors.isEmpty()) {
                result.add(getNextSuccessor(successors).val);
                k--;
                continue;
            }

            if (successors.isEmpty()) {
                result.add(getNextPredecessor(predecessors).val);
                k--;
                continue;
            }

            var leftDelta = getDelta(predecessors.peek(), target);
            var rightDelta = getDelta(successors.peek(), target);

            if (leftDelta < rightDelta) {
                result.add(getNextPredecessor(predecessors).val);
            } else {
                result.add(getNextSuccessor(successors).val);
            }
            k--;
        }

        return result;
    }

    private Stack<TreeNode> getPredecessors(TreeNode node, double target) {
        Stack<TreeNode> stack = new Stack<>();

        while (node != null) {
            if (node.val == target) {
                stack.push(node);
                break;
            } else if (node.val < target) {
                stack.push(node);
                node = node.right;
            } else {
                node = node.left;
            }
        }

        return stack;
    }

    private TreeNode getNextPredecessor(Stack<TreeNode> predecessors) {
        var predecessor = predecessors.pop();
        var current = predecessor.left;

        while (current != null) {
            predecessors.push(current);
            // Since we moved to the left subtree outside the loop
            // all nodes on the right side of the left subtree
            // should be smaller than target.
            current = current.right;
        }

        return predecessor;
    }

    private Stack<TreeNode> getSuccessors(TreeNode node, double target) {
        Stack<TreeNode> stack = new Stack<>();

        while (node != null) {
            if (node.val == target) {
                stack.push(node);
                break;
            } else if (node.val > target) {
                stack.push(node);
                node = node.left;
            } else {
                node = node.right;
            }
        }

        return stack;
    }

    private TreeNode getNextSuccessor(Stack<TreeNode> successors) {
        var successor = successors.pop();
        var current = successor.right;

        while (current != null) {
            successors.push(current);
            // Since we moved to the right subtree outside the loop
            // all nodes on the left side of the right subtree
            // should be bigger than target.
            current = current.left;
        }

        return successor;
    }

    private Pair flatten(TreeNode node, double target, ClosestNode closestNode) {
        if (node == null) {
            return null;
        }

        var delta = getDelta(node, target);
        if (delta < closestNode.delta) {
            closestNode.delta = delta;
            closestNode.node = node;
        }

        var pair = new Pair(node);

        var left = flatten(node.left, target, closestNode);
        var right = flatten(node.right, target, closestNode);

        if (left != null) {
            pair.head = left.head;
            node.left = left.tail;
            left.tail.right = node;
        }

        if (right != null) {
            pair.tail = right.tail;
            node.right = right.head;
            right.head.left = node;
        }

        return pair;
    }

    private double getDelta(TreeNode node, double target) {
        return Math.abs(target - node.val);
    }

    private static class Pair {
        TreeNode head;
        TreeNode tail;

        Pair(TreeNode node) {
            this(node, node);
        }

        Pair(TreeNode head, TreeNode tail) {
            this.head = head;
            this.tail = tail;
        }
    }

    private static class ClosestNode {
        TreeNode node;
        double delta;

        ClosestNode(TreeNode node, double delta) {
            this.node = node;
            this.delta = delta;
        }
    }
}
