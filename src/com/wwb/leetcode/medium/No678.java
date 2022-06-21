package com.wwb.leetcode.medium;

import java.util.Stack;

/**
 * Given a string s containing only three types of characters: '(', ')' and '*', return true if s is valid.
 *
 * The following rules define a valid string:
 *
 * Any left parenthesis '(' must have a corresponding right parenthesis ')'.
 * Any right parenthesis ')' must have a corresponding left parenthesis '('.
 * Left parenthesis '(' must go before the corresponding right parenthesis ')'.
 * '*' could be treated as a single right parenthesis ')' or a single left parenthesis '(' or an empty string "".
 *
 *
 * Example 1:
 *
 * Input: s = "()"
 * Output: true
 * Example 2:
 *
 * Input: s = "(*)"
 * Output: true
 * Example 3:
 *
 * Input: s = "(*))"
 * Output: true
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 100
 * s[i] is '(', ')' or '*'.
 */
public class No678 {
    public boolean checkValidString(String s) {
        return solution1(s);
    }

    // O(N * 3 ^ N) time and O(N) space.
    private boolean solution1(String s) {
        return checkValidString(s.toCharArray(), 0, new Stack<>());
    }

    private boolean solution2(String s) {
        // open parentheses count in range [minOpenParen, maxOpenParen]
        int minOpenParen = 0;
        int maxOpenParen = 0;

        for (char c : s.toCharArray()) {
            if (c == '(') {
                maxOpenParen++;
                minOpenParen++;
            } else if (c == ')') {
                maxOpenParen--;
                minOpenParen--;
            } else if (c == '*') {
                // if `*` become `(` then openCount++
                maxOpenParen++;
                // if `*` become `)` then openCount--
                minOpenParen--;
                // if `*` become `` then nothing happens
                // So openCount will be in new range [minOpenParen-1, maxOpenParen+1]
            }

            // Currently, don't have enough open parentheses to match close parentheses-> Invalid
            // For example: ())(
            if (maxOpenParen < 0) {
                return false;
            }

            // It's invalid if open parentheses count < 0 that's why minOpenParen can't be negative
            minOpenParen = Math.max(minOpenParen, 0);
        }

        // Return true if can found `openCount == 0` in range [minOpenParen, maxOpenParen]
        return minOpenParen == 0;
    }

    private boolean checkValidString(char[] chars, int index, Stack<Character> stack) {
        for (int i = index; i < chars.length; i++) {
            var c = chars[i];

            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty() || stack.peek() != '(') {
                    return false;
                }

                stack.pop();
            } else if (c == '*') {
                chars[i] = '(';

                if (checkValidString(chars, i, (Stack<Character>) stack.clone())) {
                    return true;
                }

                chars[i] = ')';

                if (checkValidString(chars, i, (Stack<Character>) stack.clone())) {
                    return true;
                }

                // empty string
                return checkValidString(chars, i + 1, (Stack<Character>) stack.clone());
            } else {
                return false;
            }
        }

        return stack.isEmpty();
    }
}
