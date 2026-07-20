package com.wwb.leetcode.experience.linkedin.roller_coaster_agent;

/**
 * Mutable state for one train being loaded.
 *
 * Tracks the current seat cursor and applies row-boundary alignment:
 * if starting a group at the next row boundary spans fewer rows than
 * the current position, the cursor advances to that boundary first.
 */
final class Train {

    private final int capacity;
    private final int seatsPerRow;
    private int seatPos = 0;
    private int ridersLoaded = 0;

    Train(int capacity, int seatsPerRow) {
        this.capacity = capacity;
        this.seatsPerRow = seatsPerRow;
    }

    int seatsRemaining() {
        return capacity - seatPos;
    }

    int ridersLoaded() {
        return ridersLoaded;
    }

    void load(int groupSize) {
        seatPos = rowAlignedStart(seatPos, groupSize);
        ridersLoaded += groupSize;
        seatPos += groupSize;
    }

    /**
     * Returns the seat position to start loading a group, advancing to the next
     * row boundary only when doing so reduces the number of rows the group spans.
     *
     * Example: 2-wide train, group of 3, current pos=1
     *   At pos 1: seats 1,2,3 → spans rows 0–1 (2 rows, both partial)
     *   At pos 2: seats 2,3,4 → spans rows 1–2 (still 2, but 1 partial) — prefer
     */
    private int rowAlignedStart(int pos, int groupSize) {
        if (pos % seatsPerRow == 0) return pos;

        int nextRowStart = ((pos / seatsPerRow) + 1) * seatsPerRow;
        if (nextRowStart + groupSize > capacity) return pos;

        return rowsSpanned(nextRowStart, groupSize) < rowsSpanned(pos, groupSize)
                ? nextRowStart
                : pos;
    }

    private int rowsSpanned(int pos, int groupSize) {
        return (pos + groupSize - 1) / seatsPerRow - pos / seatsPerRow + 1;
    }
}
