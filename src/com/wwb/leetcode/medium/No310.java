package com.wwb.leetcode.medium;

import java.util.*;

/**
 * For a undirected graph with tree characteristics, we can choose any node as the root.
 * The result graph is then a rooted tree. Among all possible rooted trees,
 * those with minimum height are called minimum height trees (MHTs). Given such a graph,
 * write a function to find all the MHTs and return a list of their root labels.
 *
 * Format
 * The graph contains n nodes which are labeled from 0 to n - 1.
 * You will be given the number n and a list of undirected edges (each edge is a pair of labels).
 *
 * You can assume that no duplicate edges will appear in edges.
 * Since all edges are undirected, [0, 1] is the same as [1, 0] and thus will not appear together in edges.
 *
 * Example 1:
 *
 * Given n = 4, edges = [[1, 0], [1, 2], [1, 3]]
 *
 *  0
 *  |
 *  1
 * / \
 * 2   3
 * return [1]
 *
 * Example 2:
 *
 * Given n = 6, edges = [[0, 3], [1, 3], [2, 3], [4, 3], [5, 4]]
 *
 * 0  1  2
 *  \ | /
 *    3
 *    |
 *    4
 *    |
 *    5
 * return [3, 4]
 *
 * Show Hint
 * Note:
 *
 * (1) According to the definition of tree on Wikipedia: “a tree is an undirected graph in which any two vertices are connected by exactly one path. In other words, any connected graph without simple cycles is a tree.”
 *
 * (2) The height of a rooted tree is the number of edges on the longest downward path between the root and a leaf.
 */
public class No310 {

    public List<Integer> findMinHeightTrees(int n, int[][] edges) {
        if(n == 1) {
            return Collections.singletonList(0);
        }

        List<Set<Integer>> graph = new ArrayList<>(n);
        List<Integer> leaves = new ArrayList<>();

        for(int i = 0; i < n; i++) {
            graph.add(new HashSet<>());
        }

        for(int[] edge : edges) {
            graph.get(edge[0]).add(edge[1]);
            graph.get(edge[1]).add(edge[0]);
        }

        for(int i = 0; i < n; i++) {
            if(graph.get(i).size() == 1) {
                leaves.add(i);
            }
        }

        while(n > 2) {
            n -= leaves.size();

            List<Integer> newLeaves = new ArrayList<>();

            for(int leaf : leaves) {
                int newLeaf = graph.get(leaf).iterator().next();
                graph.get(newLeaf).remove(leaf);

                if(graph.get(newLeaf).size() == 1) {
                    newLeaves.add(newLeaf);
                }
            }

            leaves = newLeaves;
        }

        return leaves;
    }
}
