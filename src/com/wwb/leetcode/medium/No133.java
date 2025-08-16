package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.UndirectedGraphNode;

import java.util.*;

/**
 * Clone an undirected graph. Each node in the graph contains a label and a list of its neighbors.
 * <p>
 * <p>
 * OJ's undirected graph serialization:
 * Nodes are labeled uniquely.
 * <p>
 * We use # as a separator for each node, and , as a separator for node label and each neighbor of the node.
 * As an example, consider the serialized graph {0,1,2#1,2#2,2}.
 * <p>
 * The graph has a total of three nodes, and therefore contains three parts as separated by #.
 * <p>
 * First node is labeled as 0. Connect node 0 to both nodes 1 and 2.
 * Second node is labeled as 1. Connect node 1 to node 2.
 * Third node is labeled as 2. Connect node 2 to node 2 (itself), thus forming a self-cycle.
 * Visually, the graph looks like the following:
 *
 * <div>
 *      1
 *     / \
 *    /   \
 *   0 --- 2
 *        / \
 *        \_/
 *
 * </div>
 */
public class No133 {

    public UndirectedGraphNode cloneGraph(UndirectedGraphNode node) {
        Map<Integer, UndirectedGraphNode> visited = new HashMap<>();
//        return dfs(node, visited);
        return bfs(node, visited);
    }

    private UndirectedGraphNode dfs(UndirectedGraphNode node, Map<Integer, UndirectedGraphNode> visited) {
        if (node == null) {
            return null;
        }

        if (visited.containsKey(node.label)) {
            return visited.get(node.label);
        }

        UndirectedGraphNode newNode = new UndirectedGraphNode(node.label);
        visited.put(newNode.label, newNode);

        for (UndirectedGraphNode neighbor : node.neighbors) {
            UndirectedGraphNode newNeighbor = dfs(neighbor, visited);
            newNode.neighbors.add(newNeighbor);
        }

        return newNode;
    }

    private UndirectedGraphNode bfs(UndirectedGraphNode node, Map<Integer, UndirectedGraphNode> visited) {
        if (node == null) {
            return null;
        }

        Queue<UndirectedGraphNode> queue = new LinkedList<>();
        UndirectedGraphNode newNode = new UndirectedGraphNode(node.label);
        visited.put(newNode.label, newNode);
        queue.add(node);

        while (!queue.isEmpty()) {
            UndirectedGraphNode currentNode = queue.poll();

            for (UndirectedGraphNode neighbor : currentNode.neighbors) {

                if (!visited.containsKey(neighbor.label)) {
                    visited.put(neighbor.label, new UndirectedGraphNode(neighbor.label));
                    queue.add(neighbor);
                }

                visited.get(currentNode.label).neighbors.add(visited.get(neighbor.label));
            }
        }

        return newNode;
    }
}
