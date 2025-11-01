package com.wwb.leetcode.medium;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Given a string s of '(' , ')' and lowercase English characters.
 * <p>
 * Your task is to remove the minimum number of parentheses ( '(' or ')', in any positions )
 * so that the resulting parentheses string is valid and return any valid string.
 * <p>
 * Formally, a parentheses string is valid if and only if:
 * <p>
 * It is the empty string, contains only lowercase characters, or
 * It can be written as AB (A concatenated with B), where A and B are valid strings, or
 * It can be written as (A), where A is a valid string.
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: s = "lee(t(c)o)de)"
 * Output: "lee(t(c)o)de"
 * Explanation: "lee(t(co)de)" , "lee(t(c)ode)" would also be accepted.
 * Example 2:
 * <p>
 * Input: s = "a)b(c)d"
 * Output: "ab(c)d"
 * Example 3:
 * <p>
 * Input: s = "))(("
 * Output: ""
 * Explanation: An empty string is also valid.
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= s.length <= 10^5
 * s[i] is either '(' , ')', or lowercase English letter.
 */
public class No1249 {
    public String minRemoveToMakeValid(String s) {
        return solution1(s);
    }

    private String solution1(String s) {
        Stack<Integer> leftParens = new Stack<>();
        Set<Integer> indexesToRemove = new HashSet<>();
        StringBuilder result = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ')') {
                if (leftParens.isEmpty()) {
                    indexesToRemove.add(i);
                    continue;
                }

                leftParens.pop();
                continue;
            }

            if (c == '(') {
                leftParens.add(i);
            }
        }

        indexesToRemove.addAll(leftParens);

        for (int i = 0; i < s.length(); i++) {
            if (indexesToRemove.contains(i)) {
                continue;
            }

            result.append(s.charAt(i));
        }

        return result.toString();
    }

    private String solution2(String s) {
        int left = 0;

        StringBuilder sb = new StringBuilder(s);

        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);

            if (c == ')') {
                if (left == 0) {
                    sb.setCharAt(i, '*');
                    continue;
                }

                left--;
            }

            if (c == '(') {
                left++;
            }
        }

        for (int i = sb.length() - 1; i >= 0; i--) {
            if (left == 0) {
                break;
            }
            char c = sb.charAt(i);

            if (c == '(') {
                sb.setCharAt(i, '*');
                left--;
            }
        }

        StringBuilder result = new StringBuilder(sb.length());

        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);

            if (c == '*') {
                continue;
            }

            result.append(c);
        }

        return result.toString();
    }
}
