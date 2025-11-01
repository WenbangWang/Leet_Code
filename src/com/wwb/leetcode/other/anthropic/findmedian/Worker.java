package com.wwb.leetcode.other.anthropic.findmedian;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Worker {

    private final Map<Integer, Long> localFreq; // sorted key -> count
    private final long[] prefixSum; // cumulative counts
    private final Integer[] keys;   // sorted keys array

    public Worker(Map<Integer, Long> freqMap) {
        // Build sorted TreeMap
        this.localFreq = new TreeMap<>(freqMap);
        int n = localFreq.size();
        this.keys = localFreq.keySet().toArray(new Integer[n]);
        this.prefixSum = new long[n];

        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += localFreq.get(keys[i]);
            prefixSum[i] = sum;
        }
    }

    /**
     * Count elements relative to pivot in O(log n) time
     */
    public PivotCountResponse processPivot(int pivot) {
        int idx = Arrays.binarySearch(keys, pivot);
        long countLess, countEqual, countGreater;

        if (idx >= 0) {
            // pivot exists
            countEqual = localFreq.get(keys[idx]);
            countLess = (idx == 0) ? 0 : prefixSum[idx - 1];
        } else {
            // pivot does not exist
            int insertPos = -idx - 1; // position where pivot would be inserted
            countEqual = 0;
            countLess = (insertPos == 0) ? 0 : prefixSum[insertPos - 1];
        }

        countGreater = prefixSum[prefixSum.length - 1] - countLess - countEqual;

        return new PivotCountResponse(countLess, countEqual, countGreater);
    }

    public long getTotalCount() {
        return prefixSum[prefixSum.length - 1];
    }

    public int getMinKey() {
        return keys[0];
    }

    public int getMaxKey() {
        return keys[keys.length - 1];
    }
}
