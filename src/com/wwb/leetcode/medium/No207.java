package com.wwb.leetcode.medium;

import java.util.*;

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
        return solution3(numCourses, prerequisites);
    }

    private boolean solution1(int numCourses, int[][] prerequisites) {
        List<List<Integer>> graph = new ArrayList<>();
        int[] degree = new int[numCourses];
        int count = 0;
        Queue<Integer> queue = new LinkedList<>();

        for(int i = 0; i < numCourses; i++) {
            graph.add(new ArrayList<>());
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
            graph.add(new ArrayList<>());
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

    private boolean solution3(int numCourses, int[][] prerequisites) {
        int[] inDegrees = new int[numCourses];
        Map<Integer, List<Integer>> courseToDependents = new HashMap<>();

        for(int[] p : prerequisites) {
            inDegrees[p[0]]++;
            courseToDependents.putIfAbsent(p[1], new ArrayList<>());
            courseToDependents.get(p[1]).add(p[0]);
        }
        Queue<Integer> queue = new LinkedList<>();

        for (int i = 0; i < inDegrees.length; i++) {
            if (inDegrees[i] == 0) {
                queue.add(i);
                numCourses--;
            }
        }

        while (!queue.isEmpty()) {
            int course = queue.poll();
            List<Integer> dependents = courseToDependents.getOrDefault(course, Collections.emptyList());

            for (var dependent : dependents) {
                inDegrees[dependent]--;

                if (inDegrees[dependent] == 0) {
                    queue.add(dependent);
                    numCourses--;
                }
            }
        }

        return numCourses == 0;
    }

    private boolean dfs(List<List<Integer>> graph, boolean[] visited, int course) {
        if(visited[course]) {
            return false;
        }

        visited[course] = true;

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
