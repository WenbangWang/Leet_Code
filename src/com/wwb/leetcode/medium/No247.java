package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A strobogrammatic number is a number that looks the same when rotated 180 degrees (looked at upside down).
 *
 * Find all strobogrammatic numbers that are of length = n.
 *
 * For example, Given n = 2, return ["11","69","88","96"].
 */
public class No247 {

    public List<String> findStrobogrammatic(int n) {
        return solution1(n);
        // return solution2(n, n);
    }

    private List<String> solution1(int n) {
        List<String> result = new ArrayList<>();
        char[] table = {'0', '1', '6', '8', '9'};

        build(n, "", result, table);

        return result;
    }

    private List<String> solution2(int currentLength, int originalLength) {
        if (currentLength == 0) {
            return Collections.singletonList("");
        }

        if (currentLength == 1) {
            return Arrays.asList("0", "1", "8");
        }

        List<String> lastResults = solution2(currentLength - 2, originalLength);
        List<String> currentResults = new ArrayList<>();

        for (String lastResult : lastResults) {
            // A number cannot start from 0
            if (currentLength != originalLength) {
                currentResults.add("0" + lastResult + "0");
            }

            currentResults.add("1" + lastResult + "1");
            currentResults.add("6" + lastResult + "9");
            currentResults.add("8" + lastResult + "8");
            currentResults.add("9" + lastResult + "6");
        }

        return currentResults;
    }

    private void build(int n, String str, List<String> result, char[] table) {
        if(str.length() == n) {
            result.add(str);
            return;
        }

        boolean isLast = n - str.length() == 1;

        for(char c : table) {
            // A number cannot start from 0
            if(str.isEmpty() && c == '0' && n != 1 || (isLast && (c == '6' || c == '9'))) {
                continue;
            }

            StringBuilder stringBuilder = new StringBuilder(str);
            insertIntoMid(isLast, c, stringBuilder);

            build(n, stringBuilder.toString(), result, table);
        }
    }

    private void insertIntoMid(boolean isLast, char c, StringBuilder stringBuilder) {
        int mid = stringBuilder.length() / 2;

        if(c == '6') {
            stringBuilder.insert(mid, "69");
        } else if(c == '9') {
            stringBuilder.insert(mid, "96");
        } else {
            stringBuilder.insert(mid, isLast ? c : "" + c + c);
        }
    }
}
