package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Equations are given in the format A / B = k, where A and B are variables represented as strings,
 * and k is a real number (floating point number). Given some queries, return the answers. If the answer does not exist, return -1.0.
 *
 * Example:
 * Given a / b = 2.0, b / c = 3.0.
 * queries are: a / c = ?, b / a = ?, a / e = ?, a / a = ?, x / x = ? .
 * return [6.0, 0.5, -1.0, 1.0, -1.0 ].
 *
 * The input is: vector<pair<string, string>> equations, vector<double>& values, vector<pair<string, string>> queries , where equations.size() == values.size(), and the values are positive. This represents the equations. Return vector<double>.
 *
 * According to the example above:
 *
 * equations = [ ["a", "b"], ["b", "c"] ],
 * values = [2.0, 3.0],
 * queries = [ ["a", "c"], ["b", "a"], ["a", "e"], ["a", "a"], ["x", "x"] ].
 * The input is always valid. You may assume that evaluating the queries will result in no division by zero and there is no contradiction.
 */
public class No399 {
    public double[] calcEquation(String[][] equations, double[] values, String[][] queries) {
        Map<String, List<String>> pairs = new HashMap<>();
        Map<String, List<Double>> valuesPair = new HashMap<>();
        for (int i = 0; i < equations.length; i++) {
            String[] equation = equations[i];
            if (!pairs.containsKey(equation[0])) {
                pairs.put(equation[0], new ArrayList<>());
                valuesPair.put(equation[0], new ArrayList<>());
            }
            if (!pairs.containsKey(equation[1])) {
                pairs.put(equation[1], new ArrayList<>());
                valuesPair.put(equation[1], new ArrayList<>());
            }
            pairs.get(equation[0]).add(equation[1]);
            pairs.get(equation[1]).add(equation[0]);
            valuesPair.get(equation[0]).add(values[i]);
            valuesPair.get(equation[1]).add(1/values[i]);
        }

        double[] result = new double[queries.length];
        for (int i = 0; i < queries.length; i++) {
            String[] query = queries[i];
            result[i] = dfs(query[0], query[1], pairs, valuesPair, new HashSet<>(), 1.0);
            if (result[i] == 0.0) result[i] = -1.0;
        }
        return result;
    }

    private double dfs(String start, String end, Map<String, List<String>> pairs, Map<String, List<Double>> values, Set<String> visited, double value) {
        if (visited.contains(start) || !pairs.containsKey(start)) {
            return 0.0;
        }
        if (start.equals(end)) return value;
        visited.add(start);

        List<String> strList = pairs.get(start);
        List<Double> valueList = values.get(start);
        double result = 0.0;
        for (int i = 0; i < strList.size(); i++) {
            result = dfs(strList.get(i), end, pairs, values, visited, value*valueList.get(i));
            if (result != 0.0) {
                break;
            }
        }
        visited.remove(start);
        return result;
    }
}
