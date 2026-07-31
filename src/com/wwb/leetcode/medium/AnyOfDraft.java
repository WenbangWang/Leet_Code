package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class AnyOfDraft {

    static <T> T anyOf(List<Supplier<T>> tasks, ExecutorService executor) throws InterruptedException {
        AtomicReference<T> winner = new AtomicReference<>();
        Semaphore done = new Semaphore(0);
        List<Future<?>> futures = new ArrayList<>();

        for (Supplier<T> task : tasks) {
            futures.add(executor.submit(() -> {
                T result = task.get();
                if (winner.compareAndSet(null, result)) {
                    done.release();
                }
            }));
        }

        done.acquire();
        for (Future<?> future : futures) {
            future.cancel(true);
        }
        return winner.get();
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        String result = anyOf(List.of(
                () -> sleepAndReturn(300, "slow"),
                () -> sleepAndReturn(50, "fast"),
                () -> sleepAndReturn(150, "medium")
        ), executor);

        System.out.println("winner: " + result);
        executor.shutdown();
    }

    private static String sleepAndReturn(long millis, String value) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return value;
    }
}
