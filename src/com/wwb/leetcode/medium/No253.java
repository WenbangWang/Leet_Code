package com.wwb.leetcode.medium;

import com.wwb.leetcode.utils.Interval;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Given an array of meeting time intervals consisting of start and end times [[s1,e1],[s2,e2],...] (si < ei),
 * find the minimum number of conference rooms required.
 *
 * For example,
 * Given [[0, 30],[5, 10],[15, 20]],
 * return 2.
 */
public class No253 {
    public int minMeetingRooms(Interval[] intervals) {
        if(intervals == null || intervals.length == 0) {
            return 0;
        }

        Arrays.sort(intervals, Comparator.comparingInt(value -> value.start));
        Queue<Interval> heap = new PriorityQueue<>(Comparator.comparingInt(value -> value.end));

        heap.offer(intervals[0]);

        for(int i = 1; i < intervals.length; i++) {
            Interval lastMeeting = heap.poll();
            Interval currentMeeting = intervals[i];

            if(currentMeeting.start >= lastMeeting.end) {
                lastMeeting.end = currentMeeting.end;
            } else {
                heap.offer(currentMeeting);
            }

            heap.offer(lastMeeting);
        }

        return heap.size();
    }
}
