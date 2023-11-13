package com.wwb.leetcode.hard;

/**
 * Given two words word1 and word2, find the minimum number of steps required to convert word1 to word2. (each operation is counted as 1 step.)
 *
 * You have the following 3 operations permitted on a word:
 *
 * a) Insert a character
 * b) Delete a character
 * c) Replace a character
 */
public class No72 {

    public int minDistance(String word1, String word2) {
//        return solution1(word1, word2);
        return solution2(word1, word2);
    }

    private int solution1(String word1, String word2) {
        if(word1.equals(word2)) {
            return 0;
        }

        int wordLength1 = word1.length();
        int wordLength2 = word2.length();

        if(wordLength1 == 0 || wordLength2 == 0) {
            return Math.abs(wordLength1 - wordLength2);
        }

        int[][] dp = new int[wordLength1 + 1][wordLength2 + 1];

        for(int i = 0; i <= wordLength1; i++) {
            dp[i][0] = i;
        }

        for(int i = 0; i <= wordLength2; i++) {
            dp[0][i] = i;
        }

        for(int i = 0; i < wordLength1; i++) {
            for(int j = 0; j < wordLength2; j++) {
                if(word1.charAt(i) == word2.charAt(j)) {
                    // no operation is needed
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    // replace word1[i] by word2[j] -> dp[i][j]
                    // delete word1[i] -> dp[i][j + 1]
                    // insert word2[j] into word1[i] -> dp[i + 1][j]
                    dp[i + 1][j + 1] = Math.min(dp[i][j], Math.min(dp[i][j + 1], dp[i + 1][j])) + 1;
                }
            }
        }

        return dp[wordLength1][wordLength2];
    }

    private int solution2(String word1, String word2) {
        if(word1.equals(word2)) {
            return 0;
        }

        int wordLength1 = word1.length();
        int wordLength2 = word2.length();

        if(wordLength1 == 0 || wordLength2 == 0) {
            return Math.abs(wordLength1 - wordLength2);
        }

        int[] dp = new int[wordLength2 + 1];

        for(int i = 0; i <= wordLength2; i++) {
            dp[i] = i;
        }

        for(int i = 1; i <= wordLength1; i++) {
            int previousSteps = dp[0];
            dp[0] = i;

            for(int j = 1; j <= wordLength2; j++) {
                int currentSteps = dp[j];

                if(word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[j] = previousSteps;
                } else {
                    dp[j] = Math.min(previousSteps, Math.min(dp[j], dp[j - 1])) + 1;
                }

                previousSteps = currentSteps;
            }
        }

        return dp[wordLength2];
    }
}
