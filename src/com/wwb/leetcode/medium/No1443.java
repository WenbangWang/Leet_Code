package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Given an undirected tree consisting of n vertices numbered from 0 to n-1, which has some apples in their vertices. You spend 1 second to walk over one edge of the tree. Return the minimum time in seconds you have to spend to collect all apples in the tree, starting at vertex 0 and coming back to this vertex.
 * <p>
 * The edges of the undirected tree are given in the array edges, where edges[i] = [ai, bi] means that exists an edge connecting the vertices ai and bi. Additionally, there is a boolean array hasApple, where hasApple[i] = true means that vertex i has an apple; otherwise, it does not have any apple.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * <img src="../doc-files/1443-1.png" />
 *
 * Input: n = 7, edges = [[0,1],[0,2],[1,4],[1,5],[2,3],[2,6]], hasApple = [false,false,true,false,true,true,false]
 * Output: 8
 * Explanation: The figure above represents the given tree where red vertices have an apple. One optimal path to collect all apples is shown by the green arrows.
 * Example 2:
 *
 * <img src="../doc-files/1443-2.png" />
 *
 * Input: n = 7, edges = [[0,1],[0,2],[1,4],[1,5],[2,3],[2,6]], hasApple = [false,false,true,false,false,true,false]
 * Output: 6
 * Explanation: The figure above represents the given tree where red vertices have an apple. One optimal path to collect all apples is shown by the green arrows.
 * Example 3:
 *
 * Input: n = 7, edges = [[0,1],[0,2],[1,4],[1,5],[2,3],[2,6]], hasApple = [false,false,false,false,false,false,false]
 * Output: 0
 *
 *
 * Constraints:
 *
 * 1 <= n <= 10^5
 * edges.length == n - 1
 * edges[i].length == 2
 * 0 <= ai < bi <= n - 1
 * hasApple.length == n
 * </pre>
 */
public class No1443 {
    public int minTime(int n, int[][] edges, List<Boolean> hasApple) {
        Map<Integer, List<Integer>> neighbors = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        for (int i = 0; i < n; i++) {
            neighbors.putIfAbsent(i, new ArrayList<>());
        }

        for (int[] edge : edges) {
            neighbors.get(edge[0]).add(edge[1]);
            neighbors.get(edge[1]).add(edge[0]);
        }

        return dfs(0, 0, neighbors, visited, hasApple);
    }

    private int dfs(int node, int time, Map<Integer, List<Integer>> neighbors, Set<Integer> visited, List<Boolean> hasApple) {
        if (visited.contains(node)) {
            return 0;
        }

        visited.add(node);

        int childrenTime = 0;

        for (int neighbor : neighbors.get(node)) {
            childrenTime += dfs(neighbor, 2, neighbors, visited, hasApple);
        }

        if (childrenTime == 0 && !hasApple.get(node)) {
            return 0;
        }

        return time + childrenTime;
    }
}
