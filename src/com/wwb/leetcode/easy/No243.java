package com.wwb.leetcode.easy;

/**
 * Given an array of strings wordsDict and two different strings that already exist in the array word1 and word2,
 * return the shortest distance between these two words in the list.
 *
 *
 *
 * Example 1:
 *
 * Input: wordsDict = ["practice", "makes", "perfect", "coding", "makes"], word1 = "coding", word2 = "practice"
 * Output: 3
 * Example 2:
 *
 * Input: wordsDict = ["practice", "makes", "perfect", "coding", "makes"], word1 = "makes", word2 = "coding"
 * Output: 1
 *
 *
 * Constraints:
 *
 * 1 <= wordsDict.length <= 3 * 10^4
 * 1 <= wordsDict[i].length <= 10
 * wordsDict[i] consists of lowercase English letters.
 * word1 and word2 are in wordsDict.
 * word1 != word2
 */
public class No243 {
    public int shortestDistance(String[] wordsDict, String word1, String word2) {
        int shortest = Integer.MAX_VALUE;
        int lastWord1Index = -1;
        int lastWord2Index = -1;

        for (int i = 0; i < wordsDict.length; i++) {
            var word = wordsDict[i];

            if (word.equals(word1)) {
                lastWord1Index = i;
            } else if (word.equals(word2)) {
                lastWord2Index = i;
            }

            if (lastWord2Index != -1 && lastWord1Index != -1) {
                shortest = Math.min(shortest, Math.abs(lastWord2Index - lastWord1Index));
            }
        }

        return shortest;
    }
}
