package com.wwb.leetcode.tags.backtracking;

import java.util.*;

/**
 * Given two words (beginWord and endWord), and a dictionary's word list,
 * find all shortest transformation sequence(s) from beginWord to endWord, such that:
 *
 * Only one letter can be changed at a time
 * Each intermediate word must exist in the word list
 * For example,
 *
 * Given:
 * beginWord = "hit"
 * endWord = "cog"
 * wordList = ["hot","dot","dog","lot","log"]
 * Return
 * [
 *     ["hit","hot","dot","dog","cog"],
 *     ["hit","hot","lot","log","cog"]
 * ]
 * Note:
 * All words have the same length.
 * All words contain only lowercase alphabetic characters.
 */
public class No126 {

    public List<List<String>> findLadders(String beginWord, String endWord, Set<String> wordList) {
        if(beginWord == null || endWord == null || beginWord.isEmpty() || endWord.isEmpty()
            || wordList == null || wordList.isEmpty() || !wordList.contains(beginWord)) {
            return Collections.emptyList();
        }

        List<List<String>> result = new LinkedList<>();
        Map<String, List<String>> map = new HashMap<>();
        Map<String, Integer> ladders = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        int length = beginWord.length();
        int min = Integer.MAX_VALUE;

        queue.offer(beginWord);

        for(String word : wordList) {
            ladders.put(word, Integer.MAX_VALUE);
        }

        ladders.put(beginWord, 0);
        wordList.add(endWord);

        while(!queue.isEmpty()) {
            String word = queue.poll();
            int currentStep = ladders.get(word) + 1;

            if(currentStep > min) {
                break;
            }

            for(int i = 0; i < length; i++) {
                StringBuilder stringBuilder = new StringBuilder(word);
                for(char c = 'a'; c <= 'z'; c++) {
                    if(c != word.charAt(i)) {
                        stringBuilder.setCharAt(i, c);
                        String newWord = stringBuilder.toString();

                        if(ladders.containsKey(newWord)) {
                            int newStep = ladders.get(newWord);

                            if(currentStep > newStep) {
                                continue;
                            } else if(currentStep < newStep) {
                                ladders.put(newWord, currentStep);
                                queue.offer(newWord);
                            }

                            if(map.containsKey(newWord)) {
                                map.get(newWord).add(word);
                            } else {
                                map.put(newWord, new LinkedList<>(Arrays.asList(word)));
                            }

                            if(newWord.equals(endWord)) {
                                min = currentStep;
                            }
                        }
                    }
                }
            }
        }

        processGraph(beginWord, endWord, new LinkedList<String>(), map, result);

        return result;
    }

    private void processGraph(String start, String end, List<String> workspace, Map<String, List<String>> graph, List<List<String>> result) {
        workspace.add(0, end);
        if(start.equals(end)) {
            result.add(new LinkedList<>(workspace));
        } else {
            if(graph.containsKey(end)) {
                for(String s : graph.get(end)) {
                    processGraph(start, s, workspace, graph, result);
                }
            }
        }
        workspace.remove(0);
    }
}
