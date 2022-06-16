package com.wwb.leetcode.medium;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Given two words (beginWord and endWord), and a dictionary,
 * find the length of shortest transformation sequence from beginWord to endWord, such that:
 *
 * Only one letter can be changed at a time
 * Each intermediate word must exist in the dictionary
 * For example,
 *
 * Given:
 * start = "hit"
 * end = "cog"
 * dict = ["hot","dot","dog","lot","log"]
 * As one shortest transformation is "hit" -> "hot" -> "dot" -> "dog" -> "cog",
 * return its length 5.
 *
 * Note:
 * Return 0 if there is no such transformation sequence.
 * All words have the same length.
 * All words contain only lowercase alphabetic characters.
 */
public class No127 {

    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        if(beginWord == null || endWord == null || wordList == null || beginWord.isEmpty() || endWord.isEmpty()) {
            return 0;
        }

        Queue<String> queue = new LinkedList<>();
        Set<String> wordDict = new HashSet<>(wordList);
        int step = 0;

        queue.add(beginWord);

        while(!queue.isEmpty()) {
            step++;
            var size = queue.size();

            for (int i = 0; i < size; i++) {
                String currentWord = queue.poll();
                char[] currentWordArray = currentWord.toCharArray();
                int currentWordLength = currentWordArray.length;

                if(currentWord.equals(endWord)) {
                    return step;
                }

                for(int j = 0; j < currentWordLength; j++) {
                    for(char c = 'a'; c <= 'z'; c++) {
                        char tempChar = currentWordArray[j];
                        currentWordArray[j] = c;
                        String tempWord = new String(currentWordArray);
                        currentWordArray[j] = tempChar;

                        if(wordDict.contains(tempWord)) {
                            queue.add(tempWord);
                            wordDict.remove(tempWord);
                        }
                    }
                }
            }
        }

        return 0;
    }
}
