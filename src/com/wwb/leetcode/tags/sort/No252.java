package com.wwb.leetcode.tags.sort;

import java.util.Arrays;
import java.util.Comparator;

import com.wwb.leetcode.utils.Interval;

/**
 * Given an array of meeting time intervals consisting of start and end times [[s1,e1],[s2,e2],...] (si < ei), determine if a person could attend all meetings.
 *
 * For example,
 * Given [[0, 30],[5, 10],[15, 20]],
 * return false.
 */
public class No252 {
    public boolean canAttendMeetings(Interval[] intervals) {
        if (intervals == null)
            return false;

        // Sort the intervals by start time
        Arrays.sort(intervals, Comparator.comparingInt(value -> value.start));

        for (int i = 1; i < intervals.length; i++)
            if (intervals[i].start < intervals[i - 1].end)
                return false;

        return true;
    }
}
