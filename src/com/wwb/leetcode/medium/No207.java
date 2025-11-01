package com.wwb.leetcode.medium;

import java.util.*;

/**
 * There are a total of n courses you have to take, labeled from 0 to n - 1.
 * <p>
 * Some courses may have prerequisites, for example to take course 0 you have to first take course 1,
 * which is expressed as a pair: [0,1]
 * <p>
 * Given the total number of courses and a list of prerequisite pairs, is it possible for you to finish all courses?
 * <p>
 * For example:
 *
 * <pre>
 * 2, [[1,0]]
 * There are a total of 2 courses to take. To take course 1 you should have finished course 0. So it is possible.
 * </pre>
 *
 * <pre>
 * 2, [[1,0],[0,1]]
 * There are a total of 2 courses to take. To take course 1 you should have finished course 0,
 * and to take course 0 you should also have finished course 1. So it is impossible.
 * </pre>
 *
 * <pre>
 * Note:
 * The input prerequisites is a graph represented by a list of edges, not adjacency matrices.
 * </pre>
 */
public class No207 {

    public boolean canFinish(int numCourses, int[][] prerequisites) {
        return solution3(numCourses, prerequisites);
    }

    private boolean solution1(int numCourses, int[][] prerequisites) {
        List<List<Integer>> graph = new ArrayList<>();
        int[] incomingDegree = new int[numCourses];
        int count = 0;
        Queue<Integer> queue = new LinkedList<>();

        for (int i = 0; i < numCourses; i++) {
            graph.add(new ArrayList<>());
        }

        for (int i = 0; i < prerequisites.length; i++) {
            int parent = prerequisites[i][1];
            int child = prerequisites[i][0];

            incomingDegree[parent]++;
            graph.get(child).add(parent);
        }

        for (int i = 0; i < incomingDegree.length; i++) {
            if (incomingDegree[i] == 0) {
                queue.add(i);
                count++;
            }
        }

        while (!queue.isEmpty()) {
            int course = queue.poll();
            List<Integer> parents = graph.get(course);

            for (int parent : parents) {
                incomingDegree[parent]--;

                if (incomingDegree[parent] == 0) {
                    queue.add(parent);
                    count++;
                }
            }
        }

        return count == numCourses;
    }

    private boolean solution2(int numCourses, int[][] prerequisites) {
        List<List<Integer>> graph = new ArrayList<>();
        boolean[] visited = new boolean[numCourses];

        for (int i = 0; i < numCourses; i++) {
            graph.add(new ArrayList<>());
        }

        for (int i = 0; i < prerequisites.length; i++) {
            graph.get(prerequisites[i][0]).add(prerequisites[i][1]);
        }

        for (int i = 0; i < numCourses; i++) {
            if (hasCircle(graph, visited, i)) {
                return false;
            }
        }

        return true;
    }

    // preferred
    private boolean solution3(int numCourses, int[][] prerequisites) {
        int[] inDegrees = new int[numCourses];
        Map<Integer, List<Integer>> courseToDependents = new HashMap<>();

        for (int[] p : prerequisites) {
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

    private boolean hasCircle(List<List<Integer>> graph, boolean[] visited, int course) {
        if (visited[course]) {
            return true;
        }

        visited[course] = true;

        List<Integer> prerequisiteList = graph.get(course);

        for (int prerequisite : prerequisiteList) {
            if (hasCircle(graph, visited, prerequisite)) {
                return true;
            }
        }

        visited[course] = false;

        return false;
    }
}
