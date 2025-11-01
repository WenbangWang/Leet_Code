package com.wwb.leetcode.other.anthropic.findmedian;

import java.util.List;

public class Coordinator {
    private final List<Worker> workers;
    private final long totalCount;

    public Coordinator(List<Worker> workers) {
        this.workers = workers;
        // compute total number of elements across all workers
        long sum = 0;
        for (Worker w : workers) {
            sum += w.getTotalCount();
        }
        this.totalCount = sum;
    }

    /**
     * Find the median value using distributed selection.
     * Leverages worker.processPivot() which is O(log n)
     */
    public int findMedian() {
        long k = (totalCount + 1) / 2; // rank of median
        int low = findGlobalMin();
        int high = findGlobalMax();

        while (low <= high) {
            int pivot = choosePivot(low, high);

            // Query all workers for counts relative to pivot
            PivotCountResponse global = new PivotCountResponse(0, 0, 0);
            for (Worker w : workers) {
                global.add(w.processPivot(pivot));
            }

            if (k <= global.less) {
                // median is in left side
                high = pivot - 1;
            } else if (k <= global.less + global.equal) {
                // pivot is median
                return pivot;
            } else {
                // median is in right side
                k -= (global.less + global.equal);
                low = pivot + 1;
            }
        }

        throw new RuntimeException("Median not found");
    }

    /**
     * Choose pivot: simple middle of current range
     */
    private int choosePivot(int low, int high) {
        return low + (high - low) / 2;
    }

    /**
     * Compute minimum key across all workers
     */
    private int findGlobalMin() {
        return workers.stream()
            .mapToInt(Worker::getMinKey)
            .min()
            .orElseThrow();
    }

    /**
     * Compute maximum key across all workers
     */
    private int findGlobalMax() {
        return workers.stream()
            .mapToInt(Worker::getMaxKey)
            .max()
            .orElseThrow();
    }
}
