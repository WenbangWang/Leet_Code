package com.wwb.leetcode.experience.linkedin.roller_coaster_agent;

import java.util.Locale;

/** Immutable result of one simulation run. All metrics are derived in the constructor. */
final class SimulationResult {

    final int lookahead;
    final int totalRuns;   // ceil(dispatches / numTrains) — how many times the train set goes around
    final int totalRiders;
    final int emptySeats;
    final double utilization; // 0.0–1.0

    private final String rideName;
    private final double maxThroughput;

    SimulationResult(Ride ride, int lookahead, int dispatches, int totalRiders) {
        int capacity = ride.trainCapacity();
        this.rideName = ride.name;
        this.lookahead = lookahead;
        this.totalRiders = totalRiders;
        this.totalRuns = (int) Math.ceil((double) dispatches / ride.numTrains);
        this.emptySeats = dispatches * capacity - totalRiders;
        this.utilization = dispatches > 0 ? (double) totalRiders / (dispatches * capacity) : 0;
        this.maxThroughput = ride.maxThroughput();
    }

    void printPart1() {
        System.out.printf("Ride: %s%n", rideName);
        System.out.printf("Total riders: %d%n", totalRiders);
        System.out.printf("Total runs: %d%n", totalRuns);
        System.out.printf("Max possible throughput: %.0f riders/hour%n", maxThroughput);
        System.out.printf("Actual throughput: %.0f riders/hour%n", maxThroughput * utilization);
        System.out.println();
    }

    void printPart2Row() {
        System.out.printf(Locale.US, " %5d    | %4d | %12d | %11d | %7.1f%%%n",
                lookahead, totalRuns, totalRiders, emptySeats, utilization * 100);
    }
}
