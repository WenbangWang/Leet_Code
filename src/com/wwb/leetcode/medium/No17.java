package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a digit string, return all possible letter combinations that the number could represent.
 *
 * A mapping of digit to letters (just like on the telephone buttons) is given below.
 *
 * Input:Digit string "23"
 * Output: ["ad", "ae", "af", "bd", "be", "bf", "cd", "ce", "cf"].
 * Note:
 * Although the above answer is in lexicographical order, your answer could be in any order you want.
 */
public class No17 {
    private static final String[] KEYS = {
        "",     // 0
        "",     // 1
        "abc",  // 2
        "def",  // 3
        "ghi",  // 4
        "jkl",  // 5
        "mno",  // 6
        "pqrs", // 7
        "tuv",  // 8
        "wxyz"  // 9
    };

    public List<String> letterCombinations(String digits) {
//        return solution1(digits);
        return solution2(digits);
    }

    private List<String> solution1(String digits) {
        if(digits == null || digits.isEmpty()) {
            return Collections.emptyList();
        }

        final String[] LETTERS = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        int length = digits.length();
        List<List<String>> dp = new ArrayList<>(length + 1);

        for(int i = 0; i <= length; i++) {
            dp.add(new ArrayList<String>());
        }

        dp.get(0).add("");

        for(int i = 1; i <= length; i++) {
            int digit = digits.charAt(i - 1) - '0';

            for(String s : dp.get(i - 1)) {
                for(char letter : LETTERS[digit].toCharArray()) {
                    dp.get(i).add(s + letter);
                }

                if(dp.get(i).isEmpty()) {
                    dp.get(i).add("");
                }
            }
        }

        return dp.get(length);
    }

    private List<String> solution2(String digits) {
        if(digits == null || digits.isEmpty()) {
            return Collections.emptyList();
        }

        final String[] LETTERS = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        int length = digits.length();
        LinkedList<String> result = new LinkedList<>();
        int notCounted = 0;

        result.add("");

        for(int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(digits.charAt(i));

            if(digit == 0 || digit == 1) {
                notCounted++;
                continue;
            }

            while(!result.isEmpty() && result.peek().length() + notCounted == i) {
                String subResult = result.remove();

                for(char letter : LETTERS[digit].toCharArray()) {
                    result.add(subResult + letter);
                }
            }
        }

        return result;
    }

    // time complexity O(m^n) n is the length of digits, m can be as much as 4
    // space complexity O(n) for recursion stack and O(m^n) for result
    private List<String> solution3(String digits) {
        List<String> result = new ArrayList<>();
        if (digits == null || digits.isEmpty()) {
            return result;
        }
        backtrack(digits, 0, new StringBuilder(), result);
        return result;
    }

    private void backtrack(String digits, int index, StringBuilder sb, List<String> result) {
        if (index == digits.length()) {
            result.add(sb.toString());
            return;
        }
        int digit = digits.charAt(index) - '0';
        for (char ch : KEYS[digit].toCharArray()) {
            sb.append(ch);
            backtrack(digits, index + 1, sb, result);
            sb.deleteCharAt(sb.length() - 1); // backtrack
        }
    }
}
