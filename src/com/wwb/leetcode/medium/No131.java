package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Given a string s, partition s such that every substring of the partition is a palindrome.
 *
 * Return all possible palindrome partitioning of s.
 *
 * For example, given s = "aab",
 * Return
 *
 * [
 *   ["aa","b"],
 *   ["a","a","b"]
 * ]
 */
public class No131 {

    public List<List<String>> partition(String s) {
        return solution1(s);
    }

    private List<List<String>> solution1(String s) {
        List<List<String>> palindromesList = new ArrayList<>();

        if(s == null || s.length() == 0) {
            return palindromesList;
        }

        if(s.length() == 1) {
            palindromesList.add(Arrays.asList(s));
            return palindromesList;
        }

        if(isPalindrome(s)) {
            palindromesList.add(Arrays.asList(s));
        }

        for(int i = 1, length = s.length(); i < length; i++) {
            String currentString = s.substring(0, i);
            if(isPalindrome(currentString)) {
                List<List<String>> nextPalindromesList = solution1(s.substring(i, length));

                for(List<String> nextPalindromes : nextPalindromesList) {
                    List<String> palindromes = new ArrayList<>();
                    palindromes.add(currentString);
                    for(String nextPalindrome : nextPalindromes) {
                        palindromes.add(nextPalindrome);
                    }
                    palindromesList.add(palindromes);
                }
            }
        }

        return palindromesList;
    }

    private boolean isPalindrome(String s) {
        if(s == null || s.length() == 0) {
            return false;
        }

        char[] charArray = s.toCharArray();

        for(int i = 0, length = charArray.length; i < length / 2; i++) {
            if(charArray[i] != charArray[length - 1 - i]) {
                return false;
            }
        }

        return true;
    }
}