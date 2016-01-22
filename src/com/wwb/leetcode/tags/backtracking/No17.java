package com.wwb.leetcode.tags.backtracking;

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

        result.add("");

        for(int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(digits.charAt(i));

            while(result.peek().length() == i) {
                String subResult = result.remove();

                for(char letter : LETTERS[digit].toCharArray()) {
                    result.add(subResult + letter);
                }
            }
        }

        return result;
    }
}