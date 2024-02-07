package com.wwb.leetcode.hard;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A valid number can be split up into these components (in order):
 * <p>
 * A decimal number or an integer.
 * (Optional) An 'e' or 'E', followed by an integer.
 * A decimal number can be split up into these components (in order):
 * <p>
 * (Optional) A sign character (either '+' or '-').
 * One of the following formats:
 * One or more digits, followed by a dot '.'.
 * One or more digits, followed by a dot '.', followed by one or more digits.
 * A dot '.', followed by one or more digits.
 * An integer can be split up into these components (in order):
 * <p>
 * (Optional) A sign character (either '+' or '-').
 * One or more digits.
 * For example, all the following are valid numbers: ["2", "0089", "-0.1", "+3.14", "4.", "-.9",
 * "2e10", "-90E3", "3e+7", "+6e-1", "53.5e93", "-123.456e789"],
 * while the following are not valid numbers: ["abc", "1a", "1e", "e3", "99e2.5", "--6", "-+3", "95a54e53"].
 *
 * <p>
 * Given a string s, return true if s is a valid number.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: s = "0"
 * Output: true
 * Example 2:
 * <p>
 * Input: s = "e"
 * Output: false
 * Example 3:
 * <p>
 * Input: s = "."
 * Output: false
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= s.length <= 20
 * s consists of only English letters (both uppercase and lowercase), digits (0-9), plus '+', minus '-', or dot '.'.
 */
public class No65 {
    // This is the DFA we have designed above
    private static final List<Map<CharType, Integer>> dfa = List.of(
            Map.of(CharType.DIGIT, 1, CharType.SIGN, 2, CharType.DOT, 3), // 0
            Map.of(CharType.DIGIT, 1, CharType.DOT, 4, CharType.EXPONENT, 5), // 1
            Map.of(CharType.DIGIT, 1, CharType.DOT, 3), // 2
            Map.of(CharType.DIGIT, 4), // 3
            Map.of(CharType.DIGIT, 4, CharType.EXPONENT, 5), // 4
            Map.of(CharType.SIGN, 6, CharType.DIGIT, 7), // 5
            Map.of(CharType.DIGIT, 7), // 6
            Map.of(CharType.DIGIT, 7) // 7
    );

    // These are all of the valid finishing states for our DFA.
    private static final Set<Integer> validFinalStates = Set.of(1, 4, 7);

    public boolean isNumber(String s) {
        int currentState = 0;
        CharType type;

        for (int i = 0; i < s.length(); i++) {
            char curr = s.charAt(i);
            if (Character.isDigit(curr)) {
                type = CharType.DIGIT;
            } else if (curr == '+' || curr == '-') {
                type = CharType.SIGN;
            } else if (curr == 'e' || curr == 'E') {
                type = CharType.EXPONENT;
            } else if (curr == '.') {
                type = CharType.DOT;
            } else {
                return false;
            }

            if (!dfa.get(currentState).containsKey(type)) {
                return false;
            }

            currentState = dfa.get(currentState).get(type);
        }

        return validFinalStates.contains(currentState);
    }

    private enum CharType {
        DIGIT,
        SIGN,
        DOT,
        EXPONENT,
        NONE
    }
}
