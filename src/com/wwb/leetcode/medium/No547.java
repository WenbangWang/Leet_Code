package com.wwb.leetcode.medium;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * There are n cities. Some of them are connected, while some are not. If city a is connected directly with city b,
 * and city b is connected directly with city c, then city a is connected indirectly with city c.
 *
 * A province is a group of directly or indirectly connected cities and no other cities outside of the group.
 *
 * You are given an n x n matrix isConnected where isConnected[i][j] = 1
 * if the ith city and the jth city are directly connected, and isConnected[i][j] = 0 otherwise.
 *
 * Return the total number of provinces.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: isConnected = [[1,1,0],[1,1,0],[0,0,1]]
 * Output: 2
 * Example 2:
 *
 *
 * Input: isConnected = [[1,0,0],[0,1,0],[0,0,1]]
 * Output: 3
 *
 *
 * Constraints:
 *
 * 1 <= n <= 200
 * n == isConnected.length
 * n == isConnected[i].length
 * isConnected[i][j] is 1 or 0.
 * isConnected[i][i] == 1
 * isConnected[i][j] == isConnected[j][i]
 */
public class No547 {
    public int findCircleNum(int[][] isConnected) {
        return solution1(isConnected);
    }

    private int solution1(int[][] isConnected) {
        int n = isConnected.length;
        int[] parents = new int[n];
        int[] ranks = new int[n];

        Arrays.fill(parents, -1);
        Arrays.fill(ranks, 1);

        for (int city = 0; city < n; city++) {
            int[] connectedCities = isConnected[city];

            for (int connectedCity = 0; connectedCity < n; connectedCity++) {
                if (connectedCities[connectedCity] == 1) {
                    int cityParent = find(parents, city);
                    int connectedCityParent = find(parents, connectedCity);

                    if (cityParent == connectedCityParent) {
                        continue;
                    }

                    if (ranks[cityParent] > ranks[connectedCityParent]) {
                        parents[connectedCityParent] = cityParent;
                        ranks[cityParent] += ranks[connectedCityParent];
                    } else {
                        parents[cityParent] = connectedCityParent;
                        ranks[connectedCityParent] += ranks[cityParent];
                    }
                }
            }
        }

        Set<Integer> roots = new HashSet<>();

        for (int city = 0; city < n; city++) {
            int cityParent = find(parents, city);

            roots.add(cityParent);
        }

        return roots.size();
    }

    private int find(int[] parents, int city) {
        if (parents[city] == -1) {
            return city;
        }

        parents[city] = find(parents, parents[city]);

        return parents[city];
    }

    private int solution2(int[][] isConnected) {
        int n = isConnected.length;
        boolean[] visited = new boolean[n];
        int count = 0;

        for (int city = 0; city < n; city++) {
            if (!visited[city]) {
                dfs(isConnected, visited, city);
                count++;
            }
        }

        return count;
    }

    private void dfs(int[][] isConnected, boolean[] visited, int city) {
        int n = isConnected.length;
        visited[city] = true;

        for (int connectedCity = 0; connectedCity < n; connectedCity++) {
            if (isConnected[city][connectedCity] == 1 && !visited[connectedCity]) {
                dfs(isConnected, visited, connectedCity);
            }
        }
    }

    // BFS
    private int solution3(int[][] isConnected) {
        int n = isConnected.length;
        boolean[] visited = new boolean[n];
        int count = 0;
        Queue<Integer> queue = new LinkedList<>();

        for (int city = 0; city < n; city++) {
            if (!visited[city]) {
                queue.add(city);
                while (!queue.isEmpty()) {
                    int s = queue.poll();
                    visited[s] = true;

                    for (int connectedCity = 0; connectedCity < n; connectedCity++) {
                        if (isConnected[s][connectedCity] == 1 && !visited[connectedCity]) {
                            queue.add(connectedCity);
                        }
                    }
                }
                count++;
            }
        }

        return count;
    }
}
