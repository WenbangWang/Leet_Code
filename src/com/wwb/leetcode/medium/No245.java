package com.wwb.leetcode.medium;

/**
 * Given an array of strings wordsDict and two strings that already exist in the array word1 and word2,
 * return the shortest distance between these two words in the list.
 *
 * Note that word1 and word2 may be the same. It is guaranteed that they represent two individual words in the list.
 *
 *
 *
 * Example 1:
 *
 * Input: wordsDict = ["practice", "makes", "perfect", "coding", "makes"], word1 = "makes", word2 = "coding"
 * Output: 1
 * Example 2:
 *
 * Input: wordsDict = ["practice", "makes", "perfect", "coding", "makes"], word1 = "makes", word2 = "makes"
 * Output: 3
 *
 *
 * Constraints:
 *
 * 1 <= wordsDict.length <= 10^5
 * 1 <= wordsDict[i].length <= 10
 * wordsDict[i] consists of lowercase English letters.
 * word1 and word2 are in wordsDict.
 */
public class No245 {
    public int shortestWordDistance(String[] wordsDict, String word1, String word2) {
        int shortest = Integer.MAX_VALUE;
        int lastWord1Index = -1;
        int lastWord2Index = -1;
        boolean isSame = word1.equals(word2);

        for (int i = 0; i < wordsDict.length; i++) {
            var word = wordsDict[i];

            if (word.equals(word1)) {
                lastWord2Index = isSame && lastWord1Index != -1 ? lastWord1Index : lastWord2Index;

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
