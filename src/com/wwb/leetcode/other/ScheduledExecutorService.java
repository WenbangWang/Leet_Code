package com.wwb.leetcode.other;

import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorService {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService s = new ScheduledExecutorService();
        TaskProducer producer = new TaskProducer(s, 100);

        s.start();
        s.schedule(producer, 0, TimeUnit.MILLISECONDS);
    }
    private DelayQueue<Task> queue;
    private ExecutorService pool;

    public ScheduledExecutorService() {
        this(Executors.newFixedThreadPool(10));
    }

    public ScheduledExecutorService(ExecutorService pool) {
        this.queue = new DelayQueue<>();
        this.pool = pool;
    }

    /**
     * Creates and executes a one-shot action that becomes enabled after the given delay.
     */
    public void schedule(Runnable command, long delay, TimeUnit unit) {
        this.queue.offer(new Task(command, delay, unit));
    }

    public void start() {
        this.pool.submit(() -> {
           while (true) {
               try {
                   Task t = this.queue.take();

                   System.out.println(t);
                   this.pool.submit(t.command);
               }  catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        });
    }

    private static class Task implements Delayed {
        private Runnable command;
        private long startTime;

        public Task(Runnable command, long delay, TimeUnit unit) {
            this.command = command;
            this.startTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, unit);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.startTime - ((Task) o).startTime);
        }
        @Override
        public String toString() {
            return "task execute at " + startTime;
        }
    }

    private static class TaskProducer implements Runnable {
        private final Random random = new Random();
        private ScheduledExecutorService s;
        private int index;
        private int n;

        public TaskProducer(ScheduledExecutorService s, int n) {
            this.s = s;
            this.n = n;
        }

        @Override
        public void run() {
            try {
                while (this.index != this.n) {
                    int delay = (this.random.nextInt(10) + 1) * 1000 + (this.index * 200) ;
                    String message = "task " + this.index + " will be executed after " + delay + " MILLISECONDS";
                    this.s.schedule(() -> {
                        System.out.println(message);
                    }, delay, TimeUnit.MILLISECONDS);
                    this.index++;
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
