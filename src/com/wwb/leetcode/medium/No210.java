package com.wwb.leetcode.medium;

import java.util.*;

/**
 * There are a total of n courses you have to take, labeled from 0 to n - 1.
 *
 * Some courses may have prerequisites, for example to take course 0 you have to first take course 1,
 * which is expressed as a pair: [0,1]
 *
 * Given the total number of courses and a list of prerequisite pairs,
 * return the ordering of courses you should take to finish all courses.
 *
 * There may be multiple correct orders, you just need to return one of them.
 * If it is impossible to finish all courses, return an empty array.
 *
 * For example:
 *
 * 2, [[1,0]]
 * There are a total of 2 courses to take. To take course 1 you should have finished course 0.
 * So the correct course order is [0,1]
 *
 * 4, [[1,0],[2,0],[3,1],[3,2]]
 * There are a total of 4 courses to take. To take course 3 you should have finished both courses 1 and 2.
 * Both courses 1 and 2 should be taken after you finished course 0. So one correct course order is [0,1,2,3].
 * Another correct ordering is[0,2,1,3].
 */
public class No210 {

    public int[] findOrder(int numCourses, int[][] prerequisites) {
        return solution1(numCourses, prerequisites);
    }

    private int[] solution1(int numCourses, int[][] prerequisites) {
        List<List<Integer>> graph = initGraph(numCourses);
        boolean[] alreadyTake = new boolean[numCourses];
        int[] result = new int[numCourses];
        Stack<Integer> stack = new Stack<>();

        mapPrerequisites(graph, prerequisites);

        for(int i = 0; i < numCourses; i++) {
            if(!dfs(graph, stack, alreadyTake, new boolean[numCourses], i)) {
                return new int[0];
            }
        }

        int index = 0;
        while(!stack.isEmpty()) {
            result[index++] = stack.pop();
        }

        return result;
    }

    private int[] solution2(int numCourses, int[][] prerequistes) {
        List<List<Integer>> graph = initGraph(numCourses);
        Queue<Integer> queue = new LinkedList<>();
        int[] incomingLinks = new int[numCourses];
        int[] result = new int[numCourses];
        int visited = 0;

        mapPrerequisites(graph, incomingLinks, prerequistes);

        for(int i = 0; i < numCourses; i++) {
            if(incomingLinks[i] == 0) {
                queue.add(i);
            }
        }

        while(!queue.isEmpty()) {
            int course = queue.poll();
            result[visited++] = course;

            for(int prerequisite : graph.get(course)) {
                incomingLinks[prerequisite]--;

                if(incomingLinks[prerequisite] == 0) {
                    queue.add(prerequisite);
                }
            }
        }

        return visited == numCourses ? result : new int[0];
    }

    private List<List<Integer>> initGraph(int numCourses) {
        List<List<Integer>> graph = new ArrayList<>();

        for(int i = 0; i < numCourses; i++) {
            graph.add(new ArrayList<Integer>());
        }

        return graph;
    }

    private void mapPrerequisites(List<List<Integer>> graph, int[][] prerequisites) {
        for(int[] pair : prerequisites) {
            graph.get(pair[1]).add(pair[0]);
        }
    }

    private void mapPrerequisites(List<List<Integer>> graph, int[] incomingLinks, int[][] prerequisites) {
        for(int[] pair : prerequisites) {
            incomingLinks[pair[0]]++;
            graph.get(pair[1]).add(pair[0]);
        }
    }

    private boolean dfs(List<List<Integer>> graph, Stack<Integer> stack, boolean[] alreadyTake, boolean[] visited, int course) {
        if(alreadyTake[course]) {
            return true;
        }

        if(visited[course]) {
            return false;
        }

        visited[course] = true;

        for(int prerequisite : graph.get(course)) {
            if(!dfs(graph, stack, alreadyTake, visited, prerequisite)) {
                return false;
            }
        }

        alreadyTake[course] = true;
        stack.push(course);

        return true;
    }
}