package com.wwb.leetcode.medium;

import java.util.*;

/**
 * There are a total of numCourses courses you have to take, labeled from 0 to numCourses - 1. You are given an array prerequisites where prerequisites[i] = [ai, bi] indicates that you must take course ai first if you want to take course bi.
 *
 * For example, the pair [0, 1] indicates that you have to take course 0 before you can take course 1.
 * Prerequisites can also be indirect. If course a is a prerequisite of course b, and course b is a prerequisite of course c, then course a is a prerequisite of course c.
 *
 * You are also given an array queries where queries[j] = [uj, vj]. For the jth query, you should answer whether course uj is a prerequisite of course vj or not.
 *
 * Return a boolean array answer, where answer[j] is the answer to the jth query.
 *
 *
 *
 * Example 1:
 *
 *
 * Input: numCourses = 2, prerequisites = [[1,0]], queries = [[0,1],[1,0]]
 * Output: [false,true]
 * Explanation: The pair [1, 0] indicates that you have to take course 1 before you can take course 0.
 * Course 0 is not a prerequisite of course 1, but the opposite is true.
 * Example 2:
 *
 * Input: numCourses = 2, prerequisites = [], queries = [[1,0],[0,1]]
 * Output: [false,false]
 * Explanation: There are no prerequisites, and each course is independent.
 * Example 3:
 *
 *
 * Input: numCourses = 3, prerequisites = [[1,2],[1,0],[2,0]], queries = [[1,0],[1,2]]
 * Output: [true,true]
 *
 *
 * Constraints:
 *
 * 2 <= numCourses <= 100
 * 0 <= prerequisites.length <= (numCourses * (numCourses - 1) / 2)
 * prerequisites[i].length == 2
 * 0 <= ai, bi <= n - 1
 * ai != bi
 * All the pairs [ai, bi] are unique.
 * The prerequisites graph has no cycles.
 * 1 <= queries.length <= 10^4
 * 0 <= ui, vi <= n - 1
 * ui != vi
 */
public class No1462 {
    public List<Boolean> checkIfPrerequisite(int numCourses, int[][] prerequisites, int[][] queries) {
        if (prerequisites == null || prerequisites.length == 0) {
            return Collections.nCopies(queries.length, false);
        }

        Map<Integer, Set<Integer>> courseToPrerequisites = new HashMap<>();
        Map<Integer, Set<Integer>> prerequisiteToCourses = new HashMap<>();
        int[] inDegree = new int[numCourses];

        for (int[] p : prerequisites) {
            int prerequisite = p[0];
            int course = p[1];

            inDegree[course]++;

            prerequisiteToCourses.putIfAbsent(prerequisite, new HashSet<>());

            prerequisiteToCourses.get(prerequisite).add(course);
            prerequisiteToCourses.get(prerequisite).addAll(prerequisiteToCourses.getOrDefault(prerequisite, Collections.emptySet()));
        }

        Queue<Integer> queue = new LinkedList<>();

        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        while (!queue.isEmpty()) {
            int prerequisite = queue.poll();

            for (var course : prerequisiteToCourses.getOrDefault(prerequisite, Collections.emptySet())) {
                courseToPrerequisites.putIfAbsent(course, new HashSet<>());
                courseToPrerequisites.get(course).add(prerequisite);
                courseToPrerequisites.get(course).addAll(courseToPrerequisites.getOrDefault(prerequisite, Collections.emptySet()));

                inDegree[course]--;

                if (inDegree[course] == 0) {
                    queue.offer(course);
                }
            }
        }

        List<Boolean> result = new ArrayList<>();

        for (int[] query : queries) {
            int prerequisite = query[0];
            int course = query[1];

            result.add(courseToPrerequisites.getOrDefault(course, Collections.emptySet()).contains(prerequisite));
        }

        return result;
    }

    private int find(int[] parents, int index) {
        if (parents[index] == -1) {
            return index;
        }

//        parents[index] = find(parents, parents[index]);

        return find(parents, parents[index]);
    }
}
