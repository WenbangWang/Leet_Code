package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.TreeNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Given the root of a binary tree, the value of a target node target, and an integer k, return an array of the values of all nodes that have a distance k from the target node.
 * <p>
 * You can return the answer in any order.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <img src="../doc-files/863.png" />
 * <p>
 * Input: root = [3,5,1,6,2,0,8,null,null,7,4], target = 5, k = 2
 * <p>
 * Output: [7,4,1]
 * <p>
 * Explanation: The nodes that are a distance 2 from the target node (with value 5) have values 7, 4, and 1.
 * <p>
 * Example 2:
 * <p>
 * Input: root = [1], target = 1, k = 3
 * <p>
 * Output: []
 * <p>
 * <p>
 * Constraints:
 * <p>
 * The number of nodes in the tree is in the range [1, 500].
 * <p>
 * 0 <= Node.val <= 500
 * <p>
 * All the values Node.val are unique.
 * <p>
 * target is the value of one of the nodes in the tree.
 * <p>
 * 0 <= k <= 1000
 */
public class No863 {
    public List<Integer> distanceK(TreeNode root, TreeNode target, int k) {
        Map<Integer, List<Integer>> edges = new HashMap<>();
        Queue<TreeNode> queue = new ArrayDeque<>();
        
        queue.offer(root);
        
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            edges.putIfAbsent(node.val, new ArrayList<>());
            connect(queue, edges, node, node.left);
            connect(queue, edges, node, node.right);
        }

        List<Integer> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        result.add(target.val);
        visited.add(target.val);

        for (int i = 0; i < k; i++) {
            List<Integer> currentResult = new ArrayList<>();

            for (int node : result) {
                for (int neighbour : edges.get(node)) {
                    if (visited.add(neighbour)) {
                        currentResult.add(neighbour);
                    }
                }
            }

            result = currentResult;
        }

        return result;
    }

    private void connect(Queue<TreeNode> queue, Map<Integer, List<Integer>> edges, TreeNode node, TreeNode child) {
        if (child != null) {
            queue.offer(child);
            edges.putIfAbsent(child.val, new ArrayList<>());

            edges.get(node.val).add(child.val);
            edges.get(child.val).add(node.val);
        }
    }
}
