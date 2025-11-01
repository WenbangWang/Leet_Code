package com.wwb.leetcode.medium;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * You are given a 2D string array responses where each responses[i] is an array of strings representing survey responses from the ith day.
 * <p>
 * Return the most common response across all days after removing duplicate responses within each responses[i]. If there is a tie, return the lexicographically smallest response.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: responses = [["good","ok","good","ok"],["ok","bad","good","ok","ok"],["good"],["bad"]]
 *
 * Output: "good"
 *
 * Explanation:
 *
 * After removing duplicates within each list, responses = [["good", "ok"], ["ok", "bad", "good"], ["good"], ["bad"]].
 * "good" appears 3 times, "ok" appears 2 times, and "bad" appears 2 times.
 * Return "good" because it has the highest frequency.
 * </pre>
 *
 * <pre>
 * Example 2:
 *
 * Input: responses = [["good","ok","good"],["ok","bad"],["bad","notsure"],["great","good"]]
 *
 * Output: "bad"
 *
 * Explanation:
 *
 * After removing duplicates within each list we have responses = [["good", "ok"], ["ok", "bad"], ["bad", "notsure"], ["great", "good"]].
 * "bad", "good", and "ok" each occur 2 times.
 * The output is "bad" because it is the lexicographically smallest amongst the words with the highest frequency.
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * 1 <= responses.length <= 1000
 * 1 <= responses[i].length <= 1000
 * 1 <= responses[i][j].length <= 10
 * responses[i][j] consists of only lowercase English letters
 * </pre>
 */
public class No3527 {
    // O(N * L) where L is the average length of a response.
    // this is the worst case when the count tie every time.
    public String findCommonResponse(List<List<String>> responses) {
        return doFindCommonResponse(responses.stream().map(response -> (Set<String>) new HashSet<>(response)).toList());
    }

    private String doFindCommonResponse(List<Set<String>> responses) {
        Map<String, Integer> responseToCount = new HashMap<>();
        int max = Integer.MIN_VALUE;
        String best = null;

        for (Set<String> response : responses) {
            for (String r : response) {
                int count = responseToCount.getOrDefault(r, 0) + 1;
                responseToCount.put(r, count);

                if (count > max) {
                    max = count;
                    best = r;
                } else if (count == max) {
                    if (best == null || r.compareTo(best) < 0) {
                        best = r;
                    }
                }
            }

        }

        return best;
    }
}
