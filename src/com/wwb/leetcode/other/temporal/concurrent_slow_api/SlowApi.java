package com.wwb.leetcode.other.temporal.concurrent_slow_api;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Stand-in for the interview prompt's blocking API: "may take 1-10 seconds to respond". Delay and outcome are
 * randomized per call so the different vollyShot steps in {@link ConcurrentSlowApi} can be traced against the
 * same shape of workload and compared on elapsed time.
 *
 * <p>This mock honors thread interruption (see the catch below) so cancellation is observable when tracing the
 * later steps. A real remote API may not — that gap is the caveat documented on the watchdog cancellation in
 * {@link ConcurrentSlowApi#vollyShot}.
 */
public class SlowApi {
    boolean shot(int index) {
        long delayMillis = ThreadLocalRandom.current().nextLong(1_000, 10_001);
        long start = System.nanoTime();
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // preserve interrupt status for callers checking it
            System.out.printf("  shot(%d) on %s CANCELLED after %dms%n",
                    index, Thread.currentThread().getName(), (System.nanoTime() - start) / 1_000_000);
            return false;
        }
        boolean hit = ThreadLocalRandom.current().nextInt(10) == 0; // ~10% true, mimics a rare successful shot
        System.out.printf("  shot(%d) on %s finished after %dms -> %s%n",
                index, Thread.currentThread().getName(), delayMillis, hit);
        return hit;
    }
}
