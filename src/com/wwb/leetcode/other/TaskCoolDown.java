package com.wwb.leetcode.other;

import java.util.*;
import java.util.stream.Collectors;

public class TaskCoolDown {
    public static int getTotalTime(int[] tasks, int coolDown) {
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
    public static List<String> getTaskSequence(int[] tasks, int coolDown) {
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

    public static List<String> getTaskSequenceWithLessSpace(int[] tasks, int coolDown) {
        if (tasks == null || tasks.length == 1) {
            return Collections.emptyList();
        }

        Set<Integer> set = new LinkedHashSet<>();
        List<String> result = new ArrayList<>();
        int count = 0;
        int placeholder = -1;

        for(int i = 0; i < coolDown; i++) {
            set.add(placeholder--);
        }

        for (int task : tasks) {
            if(set.contains(task)) {
                Collection<Integer> toBeRemoved = new ArrayList<>();
                Collection<Integer> toBeAdded = new ArrayList<>();

                for(int candidateTask : set) {
                    toBeRemoved.add(candidateTask);
                    toBeAdded.add(placeholder--);
                    result.add("_");
                    count++;

                    if (candidateTask == task) {
                        break;
                    }
                }

                set.removeAll(toBeRemoved);
                set.addAll(toBeAdded);
            }

            set.remove(set.iterator().next());
            set.add(task);
            result.add(String.valueOf(task));
            count++;
        }

        System.out.println(count);
        return result;
    }

    public static List<String> rearrangeTask(int[] tasks, int coolDown) {
        if (tasks == null || tasks.length == 1) {
            return Collections.emptyList();
        }

        Map<Integer, Integer> taskCountMap = new HashMap<>();
        Queue<Map.Entry<Integer, Integer>> heap = new PriorityQueue<>((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));
        Queue<Map.Entry<Integer, Integer>> tasksToBeProceed = new LinkedList<>();
        List<String> result = new ArrayList<>();
        int numberOfTasks = tasks.length;

        for (int task : tasks) {
            taskCountMap.put(task, taskCountMap.getOrDefault(task, 0) + 1);
        }

        heap.addAll(taskCountMap.entrySet());

        while (!heap.isEmpty()) {
            for (int i = 0; i <= coolDown; i++) {
                Map.Entry<Integer, Integer> taskCount = heap.poll();

                if (taskCount != null) {
                    result.add(String.valueOf(taskCount.getKey()));
                    taskCount.setValue(taskCount.getValue() - 1);
                    if (taskCount.getValue() > 0) {
                        tasksToBeProceed.offer(taskCount);
                    }
                    numberOfTasks--;
                } else if (numberOfTasks > 0) {
                    result.add("_");
                }
            }

            heap.addAll(tasksToBeProceed);
            tasksToBeProceed.clear();
        }

        return result;
    }
}
