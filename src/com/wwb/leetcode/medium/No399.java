package com.wwb.leetcode.medium;

import java.util.*;

/**
 * Equations are given in the format A / B = k, where A and B are variables represented as strings,
 * and k is a real number (floating point number). Given some queries, return the answers.
 * If the answer does not exist, return -1.0.
 *
 * Example:
 * Given a / b = 2.0, b / c = 3.0.
 * queries are: a / c = ?, b / a = ?, a / e = ?, a / a = ?, x / x = ? .
 * return [6.0, 0.5, -1.0, 1.0, -1.0 ].
 *
 * The input is: vector<pair<string, string>> equations, vector<double>& values, vector<pair<string, string>> queries ,
 * where equations.size() == values.size(), and the values are positive. This represents the equations.
 * Return vector<double>.
 *
 * According to the example above:
 *
 * equations = [ ["a", "b"], ["b", "c"] ],
 * values = [2.0, 3.0],
 * queries = [ ["a", "c"], ["b", "a"], ["a", "e"], ["a", "a"], ["x", "x"] ].
 * The input is always valid.
 * You may assume that evaluating the queries will result in no division by zero and there is no contradiction.
 */
public class No399 {
    public double[] calcEquation(List<List<String>> equations, double[] values, List<List<String>> queries) {
        Map<String, List<String>> dividendToDivisors = new HashMap<>();
        Map<String, List<Double>> dividendToValues = new HashMap<>();
        for (int i = 0; i < equations.size(); i++) {
            var equation = equations.get(i);

            dividendToDivisors.putIfAbsent(equation.get(0), new ArrayList<>());
            dividendToValues.putIfAbsent(equation.get(0), new ArrayList<>());
            dividendToDivisors.putIfAbsent(equation.get(1), new ArrayList<>());
            dividendToValues.putIfAbsent(equation.get(1), new ArrayList<>());

            dividendToDivisors.get(equation.get(0)).add(equation.get(1));
            dividendToDivisors.get(equation.get(1)).add(equation.get(0));
            dividendToValues.get(equation.get(0)).add(values[i]);
            dividendToValues.get(equation.get(1)).add(1/values[i]);
        }

        double[] results = new double[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            var query = queries.get(i);
            results[i] = dfs(query.get(0), query.get(1), dividendToDivisors, dividendToValues, new HashSet<>(), 1.0).orElse(-1.0);
        }
        return results;
    }

    private Optional<Double> dfs(String start, String end, Map<String, List<String>> pairs, Map<String, List<Double>> valuesPair, Set<String> visited, double value) {
        if (visited.contains(start) || !pairs.containsKey(start)) {
            return Optional.empty();
        }
        if (start.equals(end)) {
            return Optional.of(value);
        }
        visited.add(start);

        List<String> divisors = pairs.get(start);
        List<Double> values = valuesPair.get(start);
        Optional<Double> result = Optional.empty();

        for (int i = 0; i < divisors.size(); i++) {
            result = dfs(divisors.get(i), end, pairs, valuesPair, visited, value * values.get(i));
            if (result.isPresent()) {
                break;
            }
        }

        visited.remove(start);

        return result;
    }
}
