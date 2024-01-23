package com.wwb.leetcode.utils;

import com.wwb.leetcode.easy.No1128;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class main {

    public static void main(String[] args) {
        var a = new No1128();

        System.out.println(a.numEquivDominoPairs(new int[][]{{1,2},{2,1},{3,4},{5,6}}));

        System.out.println(combination(9, 2, new HashMap<>()));
    }

    private static int factorial(int n) {
        if (n == 0 || n == 1) {
            return 1;
        }

        return n * factorial(n - 1);
    }

    private static int combination(int n, int r, Map<Integer, Integer> factorials) {
        if (!factorials.containsKey(n)) {
            factorials.put(n, factorial(n));
        }

        if (!factorials.containsKey(r)) {
            factorials.put(r, factorial(r));
        }

        if (!factorials.containsKey(n - r)) {
            factorials.put(n - r, factorial(n - r));
        }

        return (int) (1L * factorials.get(n) / (1L * factorials.get(n - r) * factorials.get(r)));
    }

    private static class Task implements Delayed {
        private String name;
        private long startTime;  // milliseconds

        public Task(String name, long delay) {
            this.name = name;
            this.startTime = System.currentTimeMillis() + delay;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.startTime - ((Task) o).startTime);
        }

        @Override
        public String toString() {
            return "task " + name + " at " + startTime;
        }
    }

    private static class TaskProducer implements Runnable {
        private final Random random = new Random();
        private DelayQueue<Task> q;

        public TaskProducer(DelayQueue<Task> q) {
            this.q = q;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int delay = random.nextInt(10000);
                    Task task = new Task(UUID.randomUUID().toString(), delay);
                    System.out.println("Put " + task);
                    q.put(task);
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class TaskConsumer implements Runnable {
        private DelayQueue<Task> q;

        public TaskConsumer(DelayQueue<Task> q) {
            this.q = q;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Task task = q.take();
                    System.out.println("Take " + task + " in " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
