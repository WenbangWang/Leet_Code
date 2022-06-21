package com.wwb.leetcode.utils;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class main {

    public static void main(String[] args) throws InterruptedException {
        DelayQueue<Task> queue = new DelayQueue<>();

        Task task = new Task("slower task", 30000);
        System.out.println("Put " + task);
        queue.offer(task);
//        new Thread(new TaskProducer(queue), "Producer Thread").start();
        new Thread(new TaskConsumer(queue), "Faster Consumer Thread1").start();
//        new Thread(new TaskConsumer(queue), "Slower Consumer Thread2").start();

        Thread.sleep(1000);
        task = new Task("faster task", 1000);
        System.out.println("Put " + task);
        queue.offer(task);

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
            return (int)(this.startTime - ((Task) o).startTime);
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
