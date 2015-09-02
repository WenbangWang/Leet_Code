package com.wwb.leetcode.tags.dc;

import java.util.*;

/**
 * Given a string of numbers and operators,
 * return all possible results from computing all the different possible ways to group numbers and operators.
 * The valid operators are +, - and *.
 *
 *
 * Example 1
 * Input: "2-1-1".
 *
 * ((2-1)-1) = 0
 * (2-(1-1)) = 2
 * Output: [0, 2]
 *
 *
 * Example 2
 * Input: "2*3-4*5"
 *
 * (2*(3-(4*5))) = -34
 * ((2*3)-(4*5)) = -14
 * ((2*(3-4))*5) = -10
 * (2*((3-4)*5)) = -10
 * (((2*3)-4)*5) = 10
 * Output: [-34, -14, -10, -10, 10]
 */
public class No241 {

    public List<Integer> diffWaysToCompute(String input) {
        return solution1(input);
    }

    private List<Integer> solution1(String input) {
        if(input == null || input.length() == 0) {
            return Collections.emptyList();
        }

        List<Integer> result = new ArrayList<>();

        for(int i = 0, length = input.length(); i < length; i++) {
            char c = input.charAt(i);
            if(isOperator(c)) {
                String firstHalf = input.substring(0, i);
                String secondHalf = input.substring(i + 1, length);

                List<Integer> firstHalfResult = solution1(firstHalf);
                List<Integer> secondHalfResult = solution1(secondHalf);

                for(int first : firstHalfResult) {
                    for(int second : secondHalfResult) {
                        result.add(calculate(first, second, c));
                    }
                }
            }
        }

        return result.isEmpty() ? Arrays.asList(Integer.parseInt(input)) : result;
    }

    private List<Integer> solution2(String input) {
        Map<String, List<Integer>> map = new HashMap<>();

        return solution2(input, map);
    }

    private List<Integer> solution2(String input, Map<String, List<Integer>> map) {
        if(map.containsKey(input)) {
            return map.get(input);
        }

        if(input == null || input.length() == 0) {
            return Collections.emptyList();
        }

        List<Integer> result = new ArrayList<>();

        for(int i = 0, length = input.length(); i < length; i++) {
            char c = input.charAt(i);
            if(isOperator(c)) {
                String firstHalf = input.substring(0, i);
                String secondHalf = input.substring(i + 1, length);

                List<Integer> firstHalfResult = solution1(firstHalf);
                List<Integer> secondHalfResult = solution1(secondHalf);

                for(int first : firstHalfResult) {
                    for(int second : secondHalfResult) {
                        result.add(calculate(first, second, c));
                    }
                }
            }
        }

        map.put(input, result.isEmpty() ? Arrays.asList(Integer.parseInt(input)) : result);

        return map.get(input);
    }

    private boolean isOperator(char c) {
        return "+-*".indexOf(c) != -1;
    }

    private int calculate(int first, int second, char c) {
        switch (c) {
            case '+':
                return first + second;
            case '-':
                return first - second;
            case '*':
                return first * second;
            default:
                return Integer.MAX_VALUE;
        }
    }
}