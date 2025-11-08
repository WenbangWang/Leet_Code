package com.wwb.leetcode.medium;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * We are given some website visits: the user with name username[i] visited the website website[i] at time timestamp[i].
 * <p>
 * A 3-sequence is a list of websites of length 3 sorted in ascending order by the time of their visits.  (The websites in a 3-sequence are not necessarily distinct.)
 * <p>
 * Find the 3-sequence visited by the largest number of users. If there is more than one solution, return the lexicographically smallest such 3-sequence.
 * <p>
 * <p>
 * <p>
 * Example 1:
 *
 * <pre>
 * Input: username = ["joe","joe","joe","james","james","james","james","mary","mary","mary"], timestamp = [1,2,3,4,5,6,7,8,9,10], website = ["home","about","career","home","cart","maps","home","home","about","career"]
 * Output: ["home","about","career"]
 * Explanation:
 * The tuples in this example are:
 * ["joe", 1, "home"]
 * ["joe", 2, "about"]
 * ["joe", 3, "career"]
 * ["james", 4, "home"]
 * ["james", 5, "cart"]
 * ["james", 6, "maps"]
 * ["james", 7, "home"]
 * ["mary", 8, "home"]
 * ["mary", 9, "about"]
 * ["mary", 10, "career"]
 * The 3-sequence ("home", "about", "career") was visited at least once by 2 users.
 * The 3-sequence ("home", "cart", "maps") was visited at least once by 1 user.
 * The 3-sequence ("home", "cart", "home") was visited at least once by 1 user.
 * The 3-sequence ("home", "maps", "home") was visited at least once by 1 user.
 * The 3-sequence ("cart", "maps", "home") was visited at least once by 1 user.
 * </pre>
 * <p>
 * <p>
 * Note:
 *
 * <pre>
 * 3 <= N = username.length = timestamp.length = website.length <= 50
 * 1 <= username[i].length <= 10
 * 0 <= timestamp[i] <= 10^9
 * 1 <= website[i].length <= 10
 * Both username[i] and website[i] contain only lowercase characters.
 * It is guaranteed that there is at least one user who visited at least 3 websites.
 * No user visits two websites at the same time.
 * </pre>
 */
public class No1152 {
    // Scenario	Time	Space
    // Typical (users with few visits)	O(n log n)	O(n)
    // Worst (1 user, n visits)	O(n³)	O(n³)
    // Suppose a user has k visits. The number of 3-combinations (in order) = C(k, 3) = k * (k-1) * (k-2) / 6 → O(k³) in the worst case.
    public List<String> mostVisitedPattern(String[] usernames, int[] timestamps, String[] websites) {
        List<Tuple> tuples = IntStream.range(0, usernames.length).mapToObj(i -> new Tuple(
            usernames[i],
            timestamps[i],
            websites[i]
        )).sorted(Comparator.comparingInt(t -> t.timestamp)).toList();
        Map<String, List<String>> usernameToWebsites = tuples.stream().collect(Collectors.groupingBy(
            t -> t.username,
            Collectors.mapping(
                t -> t.website,
                Collectors.toList()
            )
        ));

        Map<Sequence, Set<String>> sequenceToUniqueUsers = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : usernameToWebsites.entrySet()) {
            String username = entry.getKey();
            List<String> sites = entry.getValue();
            Set<Sequence> visited = new HashSet<>();

            for (int i = 0; i < sites.size() - 2; i++) {
                for (int j = 1; j < sites.size() - 1; j++) {
                    for (int k = 2; k < sites.size(); k++) {
                        Sequence seq = new Sequence(sites.get(i), sites.get(j), sites.get(k));

                        if (visited.add(seq)) {
                            sequenceToUniqueUsers.putIfAbsent(seq, new HashSet<>());
                            sequenceToUniqueUsers.get(seq).add(username);
                        }
                    }
                }
            }
        }

        Sequence result = new Sequence("", "", "");
        int max = Integer.MIN_VALUE;

        for (Map.Entry<Sequence, Set<String>> entry : sequenceToUniqueUsers.entrySet()) {
            Sequence sequence = entry.getKey();
            int numberOfUniqueUsers = entry.getValue().size();

            if (max < numberOfUniqueUsers || (max == numberOfUniqueUsers && sequence.toString().compareTo(result.toString()) < 0)) {
                max = numberOfUniqueUsers;
                result = sequence;
            }
        }

        return List.of(result.first, result.second, result.third);
    }

    private static class Tuple {
        String username;
        int timestamp;
        String website;

        Tuple(String username, int timestamp, String website) {
            this.username = username;
            this.timestamp = timestamp;
            this.website = website;
        }
    }

    private static class Sequence {
        String first;
        String second;
        String third;

        Sequence(String first, String second, String third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        public String toString() {
            return first + second + third;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Sequence sequence)) {
                return false;
            }
            return Objects.equals(first, sequence.first) && Objects.equals(
                second,
                sequence.second
            ) && Objects.equals(third, sequence.third);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second, third);
        }
    }
}
