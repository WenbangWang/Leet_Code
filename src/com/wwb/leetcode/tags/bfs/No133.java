package com.wwb.leetcode.tags.bfs;

import com.wwb.leetcode.utils.UndirectedGraphNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Clone an undirected graph. Each node in the graph contains a label and a list of its neighbors.
 *
 *
 * OJ's undirected graph serialization:
 * Nodes are labeled uniquely.
 *
 * We use # as a separator for each node, and , as a separator for node label and each neighbor of the node.
 * As an example, consider the serialized graph {0,1,2#1,2#2,2}.
 *
 * The graph has a total of three nodes, and therefore contains three parts as separated by #.
 *
 * First node is labeled as 0. Connect node 0 to both nodes 1 and 2.
 * Second node is labeled as 1. Connect node 1 to node 2.
 * Third node is labeled as 2. Connect node 2 to node 2 (itself), thus forming a self-cycle.
 * Visually, the graph looks like the following:
 *
 *      1
 *     / \
 *    /   \
 *   0 --- 2
 *        / \
 *        \_/
 */
public class No133 {

    public UndirectedGraphNode cloneGraph(UndirectedGraphNode node) {
        Map<Integer, UndirectedGraphNode> map = new HashMap<>();
//        return dfs(node, map);
        return bfs(node, map);
    }

    private UndirectedGraphNode dfs(UndirectedGraphNode node, Map<Integer, UndirectedGraphNode> map) {
        if(node == null) {
            return null;
        }

        if(map.containsKey(node.label)) {
            return map.get(node.label);
        }

        UndirectedGraphNode newNode = new UndirectedGraphNode(node.label);
        map.put(newNode.label, newNode);

        for(UndirectedGraphNode neighbor : node.neighbors) {
            UndirectedGraphNode newNeighbor = dfs(neighbor, map);
            newNode.neighbors.add(newNeighbor);
        }

        return newNode;
    }

    private UndirectedGraphNode bfs(UndirectedGraphNode node, Map<Integer, UndirectedGraphNode> map) {
        if(node == null) {
            return null;
        }

        Queue<UndirectedGraphNode> queue = new LinkedList<>();
        UndirectedGraphNode newNode = new UndirectedGraphNode(node.label);
        map.put(newNode.label, newNode);
        queue.add(node);

        while(!queue.isEmpty()) {
            UndirectedGraphNode currentNode = queue.poll();

            for(UndirectedGraphNode neighbor : currentNode.neighbors) {

                if(!map.containsKey(neighbor.label)) {
                    map.put(neighbor.label, new UndirectedGraphNode(neighbor.label));
                    queue.add(neighbor);
                }

                map.get(currentNode.label).neighbors.add(map.get(neighbor.label));
            }
        }

        return newNode;
    }
}