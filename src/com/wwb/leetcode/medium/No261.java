package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Given n nodes labeled from 0 to n - 1 and a list of undirected edges (each edge is a pair of nodes),
 * write a function to check whether these edges make up a valid tree.
 *
 * For example:
 *
 * Given n = 5 and edges = [[0, 1], [0, 2], [0, 3], [1, 4]], return true.
 *
 * Given n = 5 and edges = [[0, 1], [1, 2], [2, 3], [1, 3], [1, 4]], return false.
 *
 * Note: you can assume that no duplicate edges will appear in edges. Since all edges are undirected,
 * [0, 1] is the same as [1, 0] and thus will not appear together in edges.
 */
public class No261 {
    public boolean validTree(int n, int[][] edges) {
        return unionFind(n, edges);
    }

    private boolean unionFind(int n, int[][] edges) {
        // IMPORTANT!! a valid tree has to have exact N - 1 edges
        // where N is the number of nodes.
        if (edges.length != n - 1) {
            return false;
        }

        // value means parent of the current index
        int[] parent = new int[n];
        // size of each set
        int[] size = new int[n];
        Arrays.fill(parent, -1);
        Arrays.fill(size, 1);

        for(int i = 0; i < edges.length; i++) {
            int x = find(parent, edges[i][0]);
            int y = find(parent, edges[i][1]);

            if (x == y) {
                return false;
            }

            // union
            // We want to ensure the larger set remains the root.
            if (size[x] < size[y]) {
                // Make y the overall root.
                parent[x] = y;
                // The size of the set rooted at y is the sum of the 2.
                size[y] += size[x];
            } else {
                // Make x the overall root.
                parent[y] = x;
                // The size of the set rooted at x is the sum of the 2.
                size[x] += size[y];
            }
        }

        return true;
    }

    private int find(int[] parent, int node) {
        // node is the root
        if (parent[node] == -1) {
            return node;
        }

        // path compression
        // with this the time complexity of this method
        // will be somewhere below O(logV)
        // - V is number of vertices -
        // the amortized time can be as small as constant.
        // without this the time complexity of this method
        // will be O(V) when the graph is a linked list
        // being the worst case.
        parent[node] = find(parent, parent[node]);

        return parent[node];
    }

    private boolean dfs(int n, int[][] edges) {
        if (edges.length != n - 1) {
            return false;
        }

        // Index is the node, value is a list of adjacency nodes.
        List<List<Integer>> adjacencyList = new ArrayList<>();
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            adjacencyList.add(new ArrayList<>());
        }

        for (int i = 0; i < edges.length; i++) {
            int node1 = edges[i][0];
            int node2 = edges[i][1];

            // Add both nodes to the list.
            adjacencyList.get(node1).add(node2);
            adjacencyList.get(node2).add(node1);
        }

        if (hasCycle(adjacencyList, visited, 0, -1)) {
            return false;
        }

        // Check if the graph is disjoint of not.
        // (All nodes should be visited if it is a valid tree)
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                return false;
            }
        }

        return true;
    }

    private boolean hasCycle(List<List<Integer>> adjacencyList, boolean[] visited, int currentNode, int parent) {
        visited[currentNode] = true;

        for (Integer neighbor : adjacencyList.get(currentNode)) {
            // Since parent should always be one of the neighbors
            // of current node hence skip checking it.
            if (neighbor == parent) {
                continue;
            }

            if (visited[neighbor]) {
                return true;
            }

            if (hasCycle(adjacencyList, visited, neighbor, currentNode)) {
                return true;
            }
        }

        return false;
    }
}
