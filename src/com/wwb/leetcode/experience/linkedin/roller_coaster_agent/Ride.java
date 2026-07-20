package com.wwb.leetcode.experience.linkedin.roller_coaster_agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable configuration for a single roller coaster ride. */
final class Ride {

    final String name;
    final int numTrains;
    final int numRows;
    final int seatsPerRow;
    final int circuitSeconds;
    final int changeSeconds;
    final List<Integer> groupSizes;

    Ride(String name, int numTrains, int numRows, int seatsPerRow,
         int circuitSeconds, int changeSeconds, List<Integer> groupSizes) {
        this.name = name;
        this.numTrains = numTrains;
        this.numRows = numRows;
        this.seatsPerRow = seatsPerRow;
        this.circuitSeconds = circuitSeconds;
        this.changeSeconds = changeSeconds;
        this.groupSizes = Collections.unmodifiableList(new ArrayList<>(groupSizes));
    }

    int trainCapacity() {
        return numRows * seatsPerRow;
    }

    /**
     * Maximum possible riders/hour if every seat on every train is always filled.
     * Integer division on runs/hour: only complete dispatch cycles count.
     */
    double maxThroughput() {
        int cycleSeconds = circuitSeconds + changeSeconds;
        int runsPerHourPerTrain = 3600 / cycleSeconds;
        return (double) runsPerHourPerTrain * numTrains * trainCapacity();
    }
}
