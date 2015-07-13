package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given n pairs of parentheses, write a function to generate all combinations of well-formed parentheses.
 *
 * For example, given n = 3, a solution set is:
 *
 * "((()))", "(()())", "(())()", "()(())", "()()()"
 */
public class No22 {

    public List<String> generateParenthesis(int n) {
        return solution2(n);
//        return solution2(n);
    }

    private List<String> solution1(int n) {
        List<List<String>> results = new ArrayList<>();
        results.add(Collections.singletonList(""));

        for(int i = 1; i <= n; i++) {
            List<String> result = new ArrayList<>();

            for(int j = 0; j < i; j++) {
                for(String first : results.get(j)) {
                    for(String second : results.get(i - 1 - j)) {
                        result.add("(" + first + ")" + second);
                    }
                }
            }

            results.add(result);
        }

        return results.get(results.size() - 1);
    }

    private List<String> solution2(int n) {
        List<List<String>> results = new ArrayList<>();
        solution2(n, results);

        return results.get(results.size() - 1);
    }

    private void solution2(int n, List<List<String>> results) {
        if(n == 0) {
            results.add(Collections.singletonList(""));
        } else {
            List<String> result = new ArrayList<>();
            solution2(n - 1, results);
            for(int i = 0; i < n; i++) {
                List<String> head = results.get(i);
                List<String> tail = results.get(n - 1 - i);

                for(String first : head) {
                    for(String second : tail) {
                       result.add( "(" + first + ")" + second);
                    }
                }
            }
            results.add(result);
        }
    }
}