package com.wwb.leetcode.hard;

import java.util.*;

/**
 * Remove the minimum number of invalid parentheses in order to make the input string valid.
 * Return all possible results.
 * <p>
 * Note: The input string may contain letters other than the parentheses ( and ).
 *
 * <pre>
 * Examples:
 * "()())()" -> ["()()()", "(())()"]
 * "(a)())()" -> ["(a)()()", "(a())()"]
 * ")(" -> [""]
 * </pre>
 */
public class No301 {

    public List<String> removeInvalidParentheses(String s) {
//        return solution1(s);
        return solution2(s);
    }

    private List<String> solution1(String s) {
        if (s == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        boolean found = false;

        queue.add(s);
        visited.add(s);

        while (!queue.isEmpty()) {
            String str = queue.poll();

            if (isValid(str)) {
                result.add(str);
                found = true;
            }

            if (found) {
                continue;
            }

            for (int i = 0, length = str.length(); i < length; i++) {
                char c = str.charAt(i);

                if (c != '(' && c != ')') {
                    continue;
                }

                String currentStr = str.substring(0, i) + str.substring(i + 1);

                if (visited.add(currentStr)) {
                    queue.add(currentStr);
                }
            }
        }

        return result;
    }

    private boolean isValid(String s) {
        int count = 0;

        for (char c : s.toCharArray()) {
            if (c == '(') {
                count++;
            }
            if (c == ')' && count-- == 0) {
                return false;
            }
        }
        return count == 0;
    }

    private List<String> solution2(String s) {
        if (s == null) {
            return Collections.emptyList();
        }

        int numberOfExtraLeftParen = 0;
        int numberOfExtraRightParen = 0;
        Set<String> result = new HashSet<>();

        for (char c : s.toCharArray()) {
            if (c == '(') {
                numberOfExtraLeftParen++;
            }
            if (c == ')') {
                if (numberOfExtraLeftParen == 0) {
                    numberOfExtraRightParen++;
                } else {
                    numberOfExtraLeftParen--;
                }
            }
        }

        dfs(s, 0, numberOfExtraLeftParen, numberOfExtraRightParen, 0, result, new StringBuilder());

        return new ArrayList<>(result);
    }

    private void dfs(
        String s,
        int pointer,
        int numberOfExtraLeftParen,
        int numberOfExtraRightParen,
        int openParen,
        Set<String> result,
        StringBuilder stringBuilder
    ) {
        if (pointer == s.length() && numberOfExtraLeftParen == 0 && numberOfExtraRightParen == 0 && openParen == 0) {
            result.add(stringBuilder.toString());
            return;
        }

        if (pointer == s.length() || numberOfExtraLeftParen < 0 || numberOfExtraRightParen < 0 || openParen < 0) {
            return;
        }

        char c = s.charAt(pointer);

        if (c == '(') {
            dfs(s, pointer + 1, numberOfExtraLeftParen - 1, numberOfExtraRightParen, openParen, result, stringBuilder);
            dfs(
                s,
                pointer + 1,
                numberOfExtraLeftParen,
                numberOfExtraRightParen,
                openParen + 1,
                result,
                stringBuilder.append(c)
            );
        } else if (c == ')') {
            dfs(s, pointer + 1, numberOfExtraLeftParen, numberOfExtraRightParen - 1, openParen, result, stringBuilder);
            dfs(
                s,
                pointer + 1,
                numberOfExtraLeftParen,
                numberOfExtraRightParen,
                openParen - 1,
                result,
                stringBuilder.append(c)
            );
        } else {
            dfs(
                s,
                pointer + 1,
                numberOfExtraLeftParen,
                numberOfExtraRightParen,
                openParen,
                result,
                stringBuilder.append(c)
            );
        }

        stringBuilder.setLength(stringBuilder.length() - 1);
    }
}
