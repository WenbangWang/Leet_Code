package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * There is a car with capacity empty seats. The vehicle only drives east (i.e., it cannot turn around and drive west).
 * <p>
 * You are given the integer capacity and an array trips where trips[i] = [numPassengersi, fromi, toi] indicates that the ith trip has numPassengersi passengers and the locations to pick them up and drop them off are fromi and toi respectively. The locations are given as the number of kilometers due east from the car's initial location.
 * <p>
 * Return true if it is possible to pick up and drop off all passengers for all the given trips, or false otherwise.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: trips = [[2,1,5],[3,3,7]], capacity = 4
 * Output: false
 * Example 2:
 *
 * Input: trips = [[2,1,5],[3,3,7]], capacity = 5
 * Output: true
 *
 * </pre>
 *
 * <pre>
 * Constraints:
 *
 * 1 <= trips.length <= 1000
 * trips[i].length == 3
 * 1 <= numPassengersi <= 100
 * 0 <= fromi < toi <= 1000
 * 1 <= capacity <= 10^5
 * </pre>
 */
public class No1094 {
    public boolean carPooling(int[][] trips, int capacity) {
        return solution1(trips, capacity);
    }

    // generalized solution for any number of destinations and trip can end at any places.
    // O(NlogN)
    private boolean solution1(int[][] trips, int capacity) {
        List<Trip> tripList = new ArrayList<>(Arrays.stream(trips).map(trip -> new Trip(trip[0], trip[1], trip[2])).toList());

        tripList.sort(Comparator.comparingInt(trip -> trip.start));

        TreeMap<Integer, List<Trip>> endToTrips = new TreeMap<>();

        for (Trip trip : tripList) {
            Integer previousEnd;
            while ((previousEnd = endToTrips.floorKey(trip.start)) != null) {
                capacity += endToTrips.get(previousEnd).stream().mapToInt(t -> t.passengers).sum();
                endToTrips.remove(previousEnd);
            }

            if (trip.passengers > capacity) {
                return false;
            }

            endToTrips.putIfAbsent(trip.end, new ArrayList<>());
            endToTrips.get(trip.end).add(trip);
            capacity -= trip.passengers;
        }

        return true;
    }

    private boolean solution2(int[][] trips, int capacity) {
        List<Trip> tripList = new ArrayList<>(Arrays.stream(trips).map(trip -> new Trip(trip[0], trip[1], trip[2])).toList());
        Map<Integer, Integer> destToPassengers = new TreeMap<>();

        for (Trip trip : tripList) {
            destToPassengers.put(trip.start, destToPassengers.getOrDefault(trip.start, 0) + trip.passengers);
            destToPassengers.put(trip.end, destToPassengers.getOrDefault(trip.end, 0) - trip.passengers);
        }

        for (int passengers : destToPassengers.values()) {
            capacity -= passengers;

            if (capacity < 0) {
                return false;
            }
        }

        return true;
    }

    private static class Trip {
        int passengers;
        int start;
        int end;

        Trip(int passengers, int start, int end) {
            this.passengers = passengers;
            this.start = start;
            this.end = end;
        }
    }
}
