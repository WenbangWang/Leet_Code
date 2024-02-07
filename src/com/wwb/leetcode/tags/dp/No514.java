package com.wwb.leetcode.tags.dp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In the video game Fallout 4, the quest "Road to Freedom" requires players to reach a metal dial
 * called the "Freedom Trail Ring" and use the dial to spell a specific keyword to open the door.
 *
 * Given a string ring that represents the code engraved on the outer ring and another string key
 * that represents the keyword that needs to be spelled,
 * return the minimum number of steps to spell all the characters in the keyword.
 *
 * Initially, the first character of the ring is aligned at the "12:00" direction.
 * You should spell all the characters in key one by one by rotating ring clockwise or anticlockwise
 * to make each character of the string key aligned at the "12:00" direction and then by pressing the center button.
 *
 * At the stage of rotating the ring to spell the key character key[i]:
 *
 * You can rotate the ring clockwise or anticlockwise by one place, which counts as one step.
 * The final purpose of the rotation is to align one of ring's characters at the "12:00" direction,
 * where this character must equal key[i].
 * If the character key[i] has been aligned at the "12:00" direction, press the center button to spell,
 * which also counts as one step. After the pressing,
 * you could begin to spell the next character in the key (next stage). Otherwise, you have finished all the spelling.
 *
 *
 * Example 1:
 *
 *
 * Input: ring = "godding", key = "gd"
 * Output: 4
 * Explanation:
 * For the first key character 'g', since it is already in place, we just need 1 step to spell this character.
 * For the second key character 'd', we need to rotate the ring "godding" anticlockwise by two steps
 * to make it become "ddinggo".
 * Also, we need 1 more step for spelling.
 * So the final output is 4.
 * Example 2:
 *
 * Input: ring = "godding", key = "godding"
 * Output: 13
 *
 *
 * Constraints:
 *
 * 1 <= ring.length, key.length <= 100
 * ring and key consist of only lower case English letters.
 * It is guaranteed that key could always be spelled by rotating ring.
 */
public class No514 {
    public int findRotateSteps(String ring, String key) {
        Map<Character, List<Integer>> charToIndexes = new HashMap<>();

        for (int i = 0; i < ring.length(); i++) {
            char c = ring.charAt(i);

            charToIndexes.putIfAbsent(c, new ArrayList<>());

            charToIndexes.get(c).add(i);
        }

        return solution1(charToIndexes, key, 0, 0, ring.length());
    }

    // TLE
    private int solution1(Map<Character, List<Integer>> charToIndexes, String key, int index, int position, int length) {
        if (index == key.length()) {
            return 0;
        }

        int result = Integer.MAX_VALUE;

        char c = key.charAt(index);
        List<Integer> indexes = charToIndexes.get(c);

        // There are 4 possible options
        // assume the current character within "key" is "a"
        // "C" represents the current position
        // 1. ...a...C......a... - we have same character on two sides while the one on the left is closer
        // 2. ...a.....C...a... - we have same character on two sides while the one on the right is closer
        // 3. .a..a....C. - we have same character only on left side and we reach the closer one by going right
        // 4. .C....a..a. - we have same character only on right side and we reach the closer one by going left

        for (int i : indexes) {
            int distance;
            // option 1 and 3
            if (i < position) {
                distance = Math.min(
                    position - i,
                    length - position + i
                );
            } else { // option 2 and 4
                distance = Math.min(i - position, length - i + position);
            }

            // "+1" means one more step to "confirm"
            result = Math.min(result, solution1(charToIndexes, key, index + 1, i, length) + distance + 1);
        }

        return result;
    }

    private int solution2(Map<Character, List<Integer>> charToIndexes, String key, int length) {
        // key->position in the ring, value->cost to reach the position
        Map<Integer, Integer> state = new HashMap<>();

        state.put(0, 0);

        for (char c : key.toCharArray()) {
            Map<Integer, Integer> nextState = new HashMap<>();

            for (int i : charToIndexes.get(c)) {
                nextState.putIfAbsent(i, Integer.MAX_VALUE);

                // For every possible previous position and cost
                // calculate how many steps we can reach position "i"
                // and save the "min" into "nextState"
                for (var entry : state.entrySet()) {
                    int distance = Math.abs(i - entry.getKey());
                    int minDistance = Math.min(distance, length - distance);

                    nextState.put(i, Math.min(nextState.get(i), minDistance + entry.getValue()));
                }
            }

            state = nextState;
        }

        // key.length() means for every character we need one more steps to "confirm"
        return state.values().stream().min(Integer::compare).get() + key.length();
    }
}
