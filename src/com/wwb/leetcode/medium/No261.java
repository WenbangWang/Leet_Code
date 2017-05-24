package com.wwb.leetcode.medium;

import java.util.Arrays;

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
        if (edges.length != n - 1) {
            return false;
        }

        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        for(int i = 0; i < edges.length; i++) {
            int x = this.unionFind(parent, edges[i][0]);
            int y = this.unionFind(parent, edges[i][1]);

            if (x == y) {
                return false;
            }

            parent[x] = y;
        }

        return true;
    }

    private int unionFind (int[] nums, int i) {
        if (nums[i] == -1) {
            return i;
        }

        return unionFind(nums, nums[i]);
    }
}
