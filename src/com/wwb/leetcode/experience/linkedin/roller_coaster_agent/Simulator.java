package com.wwb.leetcode.experience.linkedin.roller_coaster_agent;

/**
 * Simulates loading all groups onto trains for a given ride and lookahead value.
 *
 * Each train is filled using first-fit with a lookahead window:
 *   scan up to (lookahead + 1) unseated groups from the front of the queue,
 *   seat the first one that fits. Repeat until no group fits on the current train,
 *   then dispatch the next train.
 *
 * Groups already seated via an earlier train's lookahead are skipped transparently
 * and do not count toward the lookahead limit.
 */
final class Simulator {

    private final Ride ride;
    private final int lookahead;

    Simulator(Ride ride, int lookahead) {
        this.ride = ride;
        this.lookahead = lookahead;
    }

    SimulationResult run() {
        boolean[] seated = new boolean[ride.groupSizes.size()];
        int seatedCount = 0;
        int firstUnseated = 0;
        int dispatches = 0;
        int totalRiders = 0;

        while (seatedCount < ride.groupSizes.size()) {
            while (firstUnseated < seated.length && seated[firstUnseated]) firstUnseated++;
            if (firstUnseated >= seated.length) break;

            Train train = new Train(ride.trainCapacity(), ride.seatsPerRow);
            dispatches++;

            while (loadNextGroup(train, seated, firstUnseated)) {
                seatedCount++;
            }

            totalRiders += train.ridersLoaded();
        }

        return new SimulationResult(ride, lookahead, dispatches, totalRiders);
    }

    /** Finds the next fitting group, loads it onto the train, marks it seated. Returns false if none fits. */
    private boolean loadNextGroup(Train train, boolean[] seated, int from) {
        int idx = nextFitting(seated, from, train.seatsRemaining());
        if (idx < 0) return false;
        train.load(ride.groupSizes.get(idx));
        seated[idx] = true;
        return true;
    }

    /** Returns the index of the first unseated group within the lookahead window
     *  that fits in {@code seatsLeft}, or -1 if none found. */
    private int nextFitting(boolean[] seated, int from, int seatsLeft) {
        int considered = 0;
        for (int i = from; i < seated.length; i++) {
            if (seated[i]) continue;
            if (considered > lookahead) break;
            considered++;
            if (ride.groupSizes.get(i) <= seatsLeft) return i;
        }
        return -1;
    }
}
