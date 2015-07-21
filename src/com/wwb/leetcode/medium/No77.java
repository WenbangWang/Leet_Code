package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Given two integers n and k, return all possible combinations of k numbers out of 1 ... n.
 *
 * For example,
 * If n = 4 and k = 2, a solution is:
 *
 * [
 *    [2,4],
 *    [3,4],
 *    [2,3],
 *    [1,2],
 *    [1,3],
 *    [1,4],
 * ]
 */
public class No77 {

    public List<List<Integer>> combine(int n, int k) {
//        return solution1(n, k);
        return solution2(n, k);
    }

    private List<List<Integer>> solution1(int n, int k) {
        if(k == n || k == 0) {
            List<Integer> row = new ArrayList<>();

            for(int i = 1; i <= k; i++) {
                row.add(i);
            }

            return new ArrayList<>(Arrays.asList(row));
        }

        List<List<Integer>> result = combine(n - 1, k - 1);
        for(List<Integer> row : result) {
            row.add(n);
        }

        result.addAll(combine(n - 1, k));

        return result;
    }

    private List<List<Integer>> solution2(int n, int k) {
        List<List<List<Integer>>> previous = new ArrayList<>();

        for(int i = 0; i <= n; i++) {
            previous.add(Collections.singletonList(Collections.<Integer>emptyList()));
        }

        for(int i = 1; i <= k; i++) {
            List<List<List<Integer>>> current = new ArrayList<>();
            current.add(Collections.singletonList(allCombine(i)));

            for(int j = i + 1; j <= n; j++) {
                List<List<Integer>> list = new ArrayList<>();
                int size = current.size();

                //c(i, j - 1)
                list.addAll(current.get(size - 1));

                //c(i - 1, j - 1)
                for(List<Integer> item : previous.get(size)) {
                    List<Integer> newItem = new ArrayList<>(item);
                    newItem.add(j);
                    list.add(newItem);
                }

                current.add(list);
            }

            previous = current;
        }

        return previous.isEmpty() ? Collections.<List<Integer>>emptyList() : previous.get(previous.size() - 1);
    }

    private List<Integer> allCombine(int n) {
        List<Integer> result = new ArrayList<>();

        for(int i = 1; i <= n; i++) {
            result.add(i);
        }

        return result;
    }
}