package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * You are given an integer n, which indicates that there are n courses labeled from 1 to n. You are also given an array relations where relations[i] = [prevCoursei, nextCoursei], representing a prerequisite relationship between course prevCoursei and course nextCoursei: course prevCoursei has to be taken before course nextCoursei.
 * <p>
 * In one semester, you can take any number of courses as long as you have taken all the prerequisites in the previous semester for the courses you are taking.
 * <p>
 * Return the minimum number of semesters needed to take all courses. If there is no way to take all the courses, return -1.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * <p>
 * Input: n = 3, relations = [[1,3],[2,3]]
 * Output: 2
 * Explanation: The figure above represents the given graph.
 * In the first semester, you can take courses 1 and 2.
 * In the second semester, you can take course 3.
 * Example 2:
 * <p>
 * <p>
 * Input: n = 3, relations = [[1,2],[2,3],[3,1]]
 * Output: -1
 * Explanation: No course can be studied because they are prerequisites of each other.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= n <= 5000
 * 1 <= relations.length <= 5000
 * relations[i].length == 2
 * 1 <= prevCoursei, nextCoursei <= n
 * prevCoursei != nextCoursei
 * All the pairs [prevCoursei, nextCoursei] are unique.
 */
public class No1136 {
    public int minimumSemesters(int n, int[][] relations) {
        int[] inDegree = new int[n + 1];
        List<List<Integer>> graph = new ArrayList<>(n + 1);
        for (int i = 0; i < n + 1; ++i) {
            graph.add(new ArrayList<>());
        }
        for (int[] relation : relations) {
            graph.get(relation[0]).add(relation[1]);
            inDegree[relation[1]]++;
        }
        int semester = 0;
        int studiedCount = 0;
        Queue<Integer> queue = new LinkedList<>();
        for (int node = 1; node < n + 1; node++) {
            if (inDegree[node] == 0) {
                queue.offer(node);
            }
        }

        while (!queue.isEmpty()) {
            // start new semester
            semester++;
            var size = queue.size();

            for (int i = 0; i < size; i++) {
                studiedCount++;
                for (int endNode : graph.get(queue.poll())) {
                    inDegree[endNode]--;
                    // if all prerequisite courses learned
                    if (inDegree[endNode] == 0) {
                        queue.offer(endNode);
                    }
                }
            }
        }

        // check if learn all courses
        return studiedCount == n ? semester : -1;
    }
}
