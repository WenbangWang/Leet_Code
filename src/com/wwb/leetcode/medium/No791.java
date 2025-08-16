package com.wwb.leetcode.medium;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * You are given two strings order and s. All the characters of order are unique and were sorted in some custom order previously.
 * <p>
 * Permute the characters of s so that they match the order that order was sorted. More specifically, if a character x occurs before a character y in order, then x should occur before y in the permuted string.
 * <p>
 * Return any permutation of s that satisfies this property.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: order = "cba", s = "abcd"
 * <p>
 * Output: "cbad"
 * <p>
 * Explanation: "a", "b", "c" appear in order, so the order of "a", "b", "c" should be "c", "b", and "a".
 * <p>
 * Since "d" does not appear in order, it can be at any position in the returned string. "dcba", "cdba", "cbda" are also valid outputs.
 * <p>
 * Example 2:
 * <p>
 * Input: order = "bcafg", s = "abcd"
 * <p>
 * Output: "bcad"
 * <p>
 * Explanation: The characters "b", "c", and "a" from order dictate the order for the characters in s. The character "d" in s does not appear in order, so its position is flexible.
 * <p>
 * Following the order of appearance in order, "b", "c", and "a" from s should be arranged as "b", "c", "a". "d" can be placed at any position since it's not in order. The output "bcad" correctly follows this rule. Other arrangements like "dbca" or "bcda" would also be valid, as long as "b", "c", "a" maintain their order.
 * <p>
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= order.length <= 26
 * 1 <= s.length <= 200
 * order and s consist of lowercase English letters.
 * All the characters of order are unique.
 */
public class No791 {
    public String customSortString(String order, String s) {
        return solution1(order, s);
    }

    private String solution1(String order, String s) {
        Map<Character, Integer> dict = new HashMap<>();

        for (int i = 0; i < order.length(); i++) {
            char c = order.charAt(i);

            dict.put(c, i);
        }

        Character[] unsortedChars = s.chars().mapToObj(c -> (char) c).toArray(Character[]::new);

        Arrays.<Character>sort(unsortedChars, (c1, c2) -> {
            // for characters not exist in the original string, always assume it's the lagest
            if (!dict.containsKey(c1) && !dict.containsKey(c2)) {
                return 0;
            }

            if (!dict.containsKey(c1)) {
                return 1;
            }

            if (!dict.containsKey(c2)) {
                return -1;
            }

            return Integer.compare(dict.get(c1), dict.get(c2));
        });

        StringBuilder result = new StringBuilder(s.length());

        for (Character c : unsortedChars) {
            result.append(c);
        }

        return result.toString();
    }

    private String solution2(String order, String s) {
        Map<Character, Integer> dict = new HashMap<>();
        StringBuilder result = new StringBuilder(s.length());

        for (char c : s.toCharArray()) {
            dict.put(c, dict.getOrDefault(c, 0) + 1);
        }

        for (char c : order.toCharArray()) {
            if (dict.containsKey(c)) {
                result.append(String.valueOf(c).repeat(dict.get(c)));
                dict.remove(c);
            }
        }

        // for chars left in the dict, that means they can be in any place
        for (Map.Entry<Character, Integer> entry : dict.entrySet()) {
            result.append(String.valueOf(entry.getKey()).repeat(entry.getValue()));
        }

        return result.toString();
    }
}
