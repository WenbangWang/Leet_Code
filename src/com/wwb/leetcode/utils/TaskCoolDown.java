package com.wwb.leetcode.utils;

import java.util.*;
import java.util.stream.Collectors;

public class TaskCoolDown {
    public static int getTotalTime (int[] tasks, int coolDown) {
       if (tasks == null || tasks.length == 1) {
           return -1;
       }

       if (coolDown == 0) {
           return tasks.length;
       }

       int result = 0;
       Map<Integer, Integer> map = new HashMap<>();

       for (int task : tasks) {
           while (map.containsKey(task) && map.get(task) + coolDown >= result) {
               result++;
           }

           map.put(task, result++);
       }

       return result;
    }

    /**
     * Use "_" to represent wait.
     */
    public static List<String> getTaskSequence (int[] tasks, int coolDown) {
        if (tasks == null || tasks.length == 1) {
            return Collections.emptyList();
        }

        if (coolDown == 0) {
            return Arrays.stream(tasks).mapToObj(String::valueOf).collect(Collectors.toList());
        }

        int timeline = 0;
        Map<Integer, Integer> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (int task : tasks) {
            while (map.containsKey(task) && map.get(task) + coolDown >= timeline) {
                result.add("_");
                timeline++;
            }

            result.add(String.valueOf(task));
            map.put(task, timeline++);
        }

        return result;
    }

    public static int getTotalTimeWithLessSpace (int[] tasks, int coolDown) {
        if (tasks == null || tasks.length == 1) {
            return -1;
        }

        if (coolDown == 0) {
            return tasks.length;
        }

        Set<Integer> set = new LinkedHashSet<>();
        int result = 0;
        int placeholder = -1;

        for (int task : tasks) {
            int next;
            while(set.contains(task) && (next = set.iterator().next()) != task) {
                set.remove(next);
                set.add(placeholder--);
                result++;
            }

            if (!set.isEmpty() && set.iterator().next() == task) {
                result++;
                set.add(placeholder--);
                set.remove(task);
            }

            if (set.size() == coolDown) {
                set.remove(set.iterator().next());
            }

            set.add(task);
            result++;
        }

        return result;
    }
}
