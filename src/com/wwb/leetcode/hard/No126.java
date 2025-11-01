package com.wwb.leetcode.hard;

import java.util.*;

/**
 * Given two words (beginWord and endWord), and a dictionary's word list,
 * find all shortest transformation sequence(s) from beginWord to endWord, such that:
 * <p>
 * Only one letter can be changed at a time
 * Each intermediate word must exist in the word list
 * For example,
 * <pre>
 * Given:
 * beginWord = "hit"
 * endWord = "cog"
 * wordList = ["hot","dot","dog","lot","log"]
 * Return
 * [
 * ["hit","hot","dot","dog","cog"],
 * ["hit","hot","lot","log","cog"]
 * ]
 * Note:
 * All words have the same length.
 * All words contain only lowercase alphabetic characters.
 * </pre>
 */
public class No126 {

    public List<List<String>> findLadders(String beginWord, String endWord, Set<String> wordList) {
        return solution1(beginWord, endWord, wordList);
    }

    private List<List<String>> solution1(String beginWord, String endWord, Set<String> wordList) {
        if (beginWord == null || endWord == null || beginWord.isEmpty() || endWord.isEmpty()
                || wordList == null || wordList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<String>> incomingEdges = new HashMap<>();
        Map<String, Integer> ladders = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        int min = Integer.MAX_VALUE;

        queue.offer(beginWord);

        for (String word : wordList) {
            ladders.put(word, Integer.MAX_VALUE);
        }

        ladders.put(beginWord, 0);
        wordList.add(endWord);

        while (!queue.isEmpty()) {
            String word = queue.poll();
            int currentStep = ladders.get(word) + 1;

            if (currentStep > min) {
                break;
            }

            for (int i = 0; i < word.length(); i++) {
                StringBuilder stringBuilder = new StringBuilder(word);
                for (char c = 'a'; c <= 'z'; c++) {
                    if (c != word.charAt(i)) {
                        stringBuilder.setCharAt(i, c);
                        String newWord = stringBuilder.toString();

                        if (ladders.containsKey(newWord)) {
                            int newStep = ladders.get(newWord);

                            if (currentStep > newStep) {
                                continue;
                            }

                            if (currentStep < newStep) {
                                ladders.put(newWord, currentStep);
                                queue.offer(newWord);
                            }

                            if (!incomingEdges.containsKey(newWord)) {
                                incomingEdges.put(newWord, new LinkedList<>());
                            }

                            incomingEdges.get(newWord).add(word);

                            if (newWord.equals(endWord)) {
                                min = currentStep;
                            }
                        }
                    }
                }
            }
        }

        List<List<String>> result = new LinkedList<>();
        processGraph(beginWord, endWord, new LinkedList<>(), incomingEdges, result);

        return result;
    }

    private void processGraph(String start, String end, List<String> workspace, Map<String, List<String>> incomingEdges, List<List<String>> result) {
        workspace.add(0, end);
        if (start.equals(end)) {
            result.add(new LinkedList<>(workspace));
        } else {
            if (incomingEdges.containsKey(end)) {
                for (String edge : incomingEdges.get(end)) {
                    processGraph(start, edge, workspace, incomingEdges, result);
                }
            }
        }
        workspace.remove(0);
    }


    private List<List<String>> solution2(String start, String end, Set<String> wordList) {
        Map<String, Set<String>> outgoingEdges = new HashMap<>();
        // Distance from start to "key" node.
        Map<String, Integer> distance = new HashMap<>();
        List<List<String>> result = new ArrayList<>();

        populateOutgoingEdgesAndDistance(start, end, wordList, outgoingEdges, distance);
        computeShortedPath(start, end, outgoingEdges, distance, new ArrayList<>(), result);

        return result;
    }

    // Essentially BFS
    private void populateOutgoingEdgesAndDistance(
            String start,
            String end,
            Set<String> wordList,
            Map<String, Set<String>> outgoingEdges,
            Map<String, Integer> distance
    ) {
        Queue<String> queue = new LinkedList<>();

        queue.add(start);
        distance.put(start, 0);

        while (!queue.isEmpty()) {
            int currentQueueSize = queue.size();
            boolean found = false;

            for (int i = 0; i < currentQueueSize; i++) {
                String word = queue.poll();
                int currentDistance = distance.get(word);
                Set<String> neighbors = getNeighbors(word, wordList);
                outgoingEdges.putIfAbsent(word, neighbors);

                for (String neighbor : neighbors) {
                    // haven't visited yet
                    if (!distance.containsKey(neighbor)) {
                        distance.put(neighbor, currentDistance + 1);

                        if (end.equals(neighbor)) {
                            found = true;
                        } else {
                            queue.offer(neighbor);
                        }

                    }
                }
            }

            if (found) {
                break;
            }
        }
    }

    private void computeShortedPath(String start,
                                    String end,
                                    Map<String, Set<String>> outgoingEdges,
                                    Map<String, Integer> distance,
                                    List<String> workspace,
                                    List<List<String>> result) {
        workspace.add(start);

        // Reached destination
        if (start.equals(end)) {
            result.add(new ArrayList<>(workspace));
        } else {
            for (String neighbor : outgoingEdges.getOrDefault(start, Collections.emptySet())) {
                // do not have to visit node not part of the shortest path.
                // the assumption is "distance" only includes node till
                // the "end" node.
                if (distance.get(neighbor) == distance.get(start) + 1) {
                    computeShortedPath(neighbor, end, outgoingEdges, distance, workspace, result);
                }
            }
        }

        workspace.remove(workspace.size() - 1);
    }

    private Set<String> getNeighbors(String word, Set<String> wordList) {
        Set<String> neighbors = new HashSet<>();
        StringBuilder mutableWord = new StringBuilder(word);

        for (int i = 0; i < word.length(); i++) {
            char originalChar = word.charAt(i);
            for (char c = 'a'; c <= 'z'; c++) {
                if (originalChar == c) {
                    continue;
                }

                mutableWord.setCharAt(i, c);
                String mutatedWord = mutableWord.toString();

                if (wordList.contains(mutatedWord)) {
                    neighbors.add(mutatedWord);
                }
            }

            mutableWord.setCharAt(i, originalChar);
        }

        return neighbors;
    }
}
