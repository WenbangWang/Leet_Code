package com.wwb.leetcode.tags.bfs;

import java.util.LinkedList;
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

    public int ladderLength(String beginWord, String endWord, Set<String> wordDict) {
        if(beginWord == null || endWord == null || wordDict == null || beginWord.length() == 0 || endWord.length() == 0) {
            return 0;
        }

        LinkedList<String> queue = new LinkedList<>();
        int step = 0;

        queue.add(beginWord);
        wordDict.add(endWord);

        while(!queue.isEmpty()) {
            LinkedList<String> level = new LinkedList<>();
            step++;
            while(!queue.isEmpty()) {
                String currentWord = queue.pollFirst();
                char[] currentWordArray = currentWord.toCharArray();
                int currentWordLength = currentWordArray.length;

                if(currentWord.equals(endWord)) {
                    return step;
                }

                for(int i = 0; i < currentWordLength; i++) {
                    for(char c = 'a'; c <= 'z'; c++) {
                        char tempChar = currentWordArray[i];
                        currentWordArray[i] = c;
                        String tempWord = new String(currentWordArray);
                        currentWordArray[i] = tempChar;

                        if(wordDict.contains(tempWord)) {
                            level.add(tempWord);
                            wordDict.remove(tempWord);
                        }
                    }
                }
            }

            queue = level;
        }

        return 0;
    }
}