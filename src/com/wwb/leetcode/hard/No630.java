package com.wwb.leetcode.hard;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * There are n different online courses numbered from 1 to n. You are given an array courses where courses[i] = [durationi, lastDayi] indicate that the ith course should be taken continuously for durationi days and must be finished before or on lastDayi.
 *
 * You will start on the 1st day and you cannot take two or more courses simultaneously.
 *
 * Return the maximum number of courses that you can take.
 *
 *
 *
 * Example 1:
 *
 * Input: courses = [[100,200],[200,1300],[1000,1250],[2000,3200]]
 * Output: 3
 * Explanation:
 * There are totally 4 courses, but you can take 3 courses at most:
 * First, take the 1st course, it costs 100 days so you will finish it on the 100th day, and ready to take the next course on the 101st day.
 * Second, take the 3rd course, it costs 1000 days so you will finish it on the 1100th day, and ready to take the next course on the 1101st day.
 * Third, take the 2nd course, it costs 200 days so you will finish it on the 1300th day.
 * The 4th course cannot be taken now, since you will finish it on the 3300th day, which exceeds the closed date.
 * Example 2:
 *
 * Input: courses = [[1,2]]
 * Output: 1
 * Example 3:
 *
 * Input: courses = [[3,2],[4,3]]
 * Output: 0
 *
 *
 * Constraints:
 *
 * 1 <= courses.length <= 10^4
 * 1 <= durationi, lastDayi <= 10^4
 */
public class No630 {
    public int scheduleCourse(int[][] courses) {
        // sort by last day asc
        Arrays.sort(courses, Comparator.comparingInt(a -> a[1]));
        Queue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        int time = 0;

        for (int[] course: courses) {
            // can take the current course
            if (time + course[0] <= course[1]) {
                maxHeap.offer(course[0]);
                time += course[0];
            } else if (!maxHeap.isEmpty() && maxHeap.peek() > course[0]) {
                // pop the max duration course from the heap
                // and take the current course
                // this process is try to minimize the total duration
                // which would maximize the number of course can be taken
                time += course[0] - maxHeap.poll();
                maxHeap.offer(course[0]);
            }
        }
        return maxHeap.size();
    }
}
