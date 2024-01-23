package com.wwb.leetcode.tags.array;

/**
 * Sometimes people repeat letters to represent extra feeling. For example:
 *
 * "hello" -> "heeellooo"
 * "hi" -> "hiiii"
 * In these strings like "heeellooo", we have groups of adjacent letters that are all the same: "h", "eee", "ll", "ooo".
 *
 * You are given a string s and an array of query strings words. A query word is stretchy
 * if it can be made to be equal to s by any number of applications of the following extension operation:
 * choose a group consisting of characters c, and add some number of characters c to the group
 * so that the size of the group is three or more.
 *
 * For example, starting with "hello", we could do an extension on the group "o" to get "hellooo",
 * \but we cannot get "helloo" since the group "oo" has a size less than three.
 * Also, we could do another extension like "ll" -> "lllll" to get "helllllooo". If s = "helllllooo",
 * then the query word "hello" would be stretchy because of these two extension operations:
 * query = "hello" -> "hellooo" -> "helllllooo" = s.
 * Return the number of query strings that are stretchy.
 *
 *
 *
 * Example 1:
 *
 * Input: s = "heeellooo", words = ["hello", "hi", "helo"]
 * Output: 1
 * Explanation:
 * We can extend "e" and "o" in the word "hello" to get "heeellooo".
 * We can't extend "helo" to get "heeellooo" because the group "ll" is not size 3 or more.
 * Example 2:
 *
 * Input: s = "zzzzzyyyyy", words = ["zzyy","zy","zyy"]
 * Output: 3
 *
 *
 * Constraints:
 *
 * 1 <= s.length, words.length <= 100
 * 1 <= words[i].length <= 100
 * s and words[i] consist of lowercase letters.
 */
public class No809 {
    public int expressiveWords(String s, String[] words) {
        int result = 0;

        for (String word : words) {
            if (isExpressive(s, word)) {
                result++;
            }
        }

        return result;
    }

    private boolean isExpressive(String target, String original) {
        if (original.length() >= target.length()) {
            return false;
        }
        int targetIndex = 0;
        int originalIndex = 0;

        while (targetIndex != target.length() && originalIndex != original.length()) {
            char targetC = target.charAt(targetIndex);
            char originalC = original.charAt(originalIndex);

            if (originalC != targetC) {
                return false;
            }

            int targetCount = 0;

            while (targetIndex + 1 != target.length() && targetC == target.charAt(targetIndex + 1)) {
                targetCount++;
                targetIndex++;
            }
            targetCount++;
            targetIndex++;

            int originalCount = 0;
            while (originalIndex + 1 != original.length() && originalC == original.charAt(originalIndex + 1)) {
                originalCount++;
                originalIndex++;
            }
            originalCount++;
            originalIndex++;

            // more char in original than target
            if (originalCount > targetCount ) {
                return false;
            }

            // extended but not extended enough (more than 3)
            if (targetCount > originalCount && targetCount < 3) {
                return false;
            }
        }

        return originalIndex == original.length() && targetIndex == target.length();
    }
}
