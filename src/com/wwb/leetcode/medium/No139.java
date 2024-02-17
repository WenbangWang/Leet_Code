package com.wwb.leetcode.medium;

import java.util.Set;

/**
 * Given a string s and a dictionary of words dict,
 * determine if s can be segmented into a space-separated sequence of one or more dictionary words.
 *
 * For example, given
 * s = "leetcode",
 * dict = ["leet", "code"].
 *
 * Return true because "leetcode" can be segmented as "leet code".
 */
public class No139 {

    // O(n^3)
    public boolean wordBreak(String s, Set<String> wordDict) {
        if(s == null) {
            return false;
        }
        int length = s.length();
        boolean[] results = new boolean[length + 1];
        results[0] = true;

        for(int i = 1; i <= length; i++) {
            for(int j = i - 1; j >= 0; j--) {
                if(results[j] && wordDict.contains(s.substring(j, i))) {
                    results[i] = true;
                    break;
                }
            }
        }

        return results[length];
    }

    // O(2^n)
    private boolean solution2(String s, Set<String> wordDict) {
        if (s == null) {
            return false;
        }

        if (wordDict.contains(s)) {
            return true;
        }

        for (int i = 1; i < s.length(); i++) {
            String firstHalf = s.substring(0, i);
            String secondHalf = s.substring(i, s.length());

            if (wordDict.contains(firstHalf) && solution2(secondHalf, wordDict)) {
                return true;
            }
        }

        return false;
    }
}
