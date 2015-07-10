package com.wwb.leetcode.tags.string;

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
        LinkedList<String> result = new LinkedList<>();

        if(digits == null || digits.length() == 0) {
            return result;
        }

        String[] map = new String[] {"0", "1", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        char[] digitArray = digits.toCharArray();
        result.add("");

        for(int i = 0; i < digitArray.length; i++) {
            int index = Character.getNumericValue(digitArray[i]);

            while(result.peek().length() == i) {
                String subResult = result.remove();

                for(char c : map[index].toCharArray()) {
                    result.add(subResult + c);
                }
            }
        }

        return result;
    }
}