package com.wwb.leetcode.experience.linkedin.roller_coaster_agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses the ride input file into {@link Ride} objects.
 *
 * Format per ride:
 *   Ride Name
 *   #Trains #Rows #SeatsPerRow #CircuitSec #ChangeSec
 *   groupSize
 *   groupSize
 *   ...
 * Rides are blank-line separated. The last ride ends with "EOF".
 */
final class RideParser {

    private RideParser() {}

    static List<Ride> parse(String filename) throws IOException {
        Iterator<String> lines = Files.readAllLines(Paths.get(filename)).iterator();
        List<Ride> rides = new ArrayList<>();
        while (lines.hasNext()) {
            String line = lines.next().trim();
            if (line.isEmpty()) continue;
            rides.add(readRide(lines, line));
        }
        return rides;
    }

    private static Ride readRide(Iterator<String> lines, String name) {
        String[] cfg = lines.next().trim().split("\\s+");
        int numTrains      = Integer.parseInt(cfg[0]);
        int numRows        = Integer.parseInt(cfg[1]);
        int seatsPerRow    = Integer.parseInt(cfg[2]);
        int circuitSeconds = Integer.parseInt(cfg[3]);
        int changeSeconds  = Integer.parseInt(cfg[4]);

        List<Integer> groups = new ArrayList<>();
        while (lines.hasNext()) {
            String line = lines.next().trim();
            if (line.equals("EOF") || line.isEmpty()) break;
            groups.add(Integer.parseInt(line));
        }

        return new Ride(name, numTrains, numRows, seatsPerRow,
                        circuitSeconds, changeSeconds, groups);
    }
}
