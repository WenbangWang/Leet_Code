package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * There are a total of n courses you have to take, labeled from 0 to n - 1.
 *
 * Some courses may have prerequisites, for example to take course 0 you have to first take course 1,
 * which is expressed as a pair: [0,1]
 *
 * Given the total number of courses and a list of prerequisite pairs, is it possible for you to finish all courses?
 *
 * For example:
 *
 * 2, [[1,0]]
 * There are a total of 2 courses to take. To take course 1 you should have finished course 0. So it is possible.
 *
 * 2, [[1,0],[0,1]]
 * There are a total of 2 courses to take. To take course 1 you should have finished course 0,
 * and to take course 0 you should also have finished course 1. So it is impossible.
 *
 * Note:
 * The input prerequisites is a graph represented by a list of edges, not adjacency matrices.
 */
public class No207 {

    public boolean canFinish(int numCourses, int[][] prerequisites) {
        return solution1(numCourses, prerequisites);
    }

    private boolean solution1(int numCourses, int[][] prerequisites) {
        List<List<Integer>> graph = new ArrayList<>();
        int[] degree = new int[numCourses];
        int count = 0;
        Queue<Integer> queue = new LinkedList<>();

        for(int i = 0; i < numCourses; i++) {
            graph.add(new ArrayList<Integer>());
        }

        for(int i = 0; i < prerequisites.length; i++) {
            int prerequisite = prerequisites[i][1];
            degree[prerequisite]++;
            graph.get(prerequisites[i][0]).add(prerequisite);
        }

        for(int i = 0; i < degree.length; i++) {
            if(degree[i] == 0) {
                queue.add(i);
                count++;
            }
        }

        while(!queue.isEmpty()) {
            int course = queue.poll();
            List<Integer> prerequisiteList = graph.get(course);

            for(int prerequisite : prerequisiteList) {
                degree[prerequisite]--;

                if(degree[prerequisite] == 0) {
                    queue.add(prerequisite);
                    count++;
                }
            }
        }

        return count == numCourses;
    }

    private boolean solution2(int numCourses, int[][] prerequisites) {
        List<List<Integer>> graph = new ArrayList<>();
        boolean[] visited = new boolean[numCourses];

        for(int i = 0; i < numCourses; i++) {
            graph.add(new ArrayList<Integer>());
        }

        for(int i = 0; i < prerequisites.length; i++) {
            graph.get(prerequisites[i][0]).add(prerequisites[i][1]);
        }

        for(int i = 0; i < numCourses; i++) {
            if(!dfs(graph, visited, i)) {
                return false;
            }
        }

        return true;
    }

    private boolean dfs(List<List<Integer>> graph, boolean[] visited, int course) {
        if(visited[course]) {
            return false;
        } else {
            visited[course] = true;
        }

        List<Integer> prerequisiteList = graph.get(course);

        for(int prerequisite: prerequisiteList) {
            if(!dfs(graph, visited, prerequisite)) {
                return false;
            }
        }

        visited[course] = false;

        return true;
    }
}