package com.wwb.leetcode.other.anthropic.tokenizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BPE {
    private Map<String, Integer> vocab = new HashMap<>();

    public BPE(List<String> corpus) {
        buildInitialVocab(corpus);
    }

    private void buildInitialVocab(List<String> corpus) {
        for (String word : corpus) {
            String tokenized = String.join(" ", word.split("")) + " </w>";
            vocab.put(tokenized, vocab.getOrDefault(tokenized, 0) + 1);
        }
    }

    private Map<String, Integer> getPairStats() {
        Map<String, Integer> pairs = new HashMap<>();
        for (Map.Entry<String, Integer> entry : vocab.entrySet()) {
            String[] symbols = entry.getKey().split(" ");
            int freq = entry.getValue();
            for (int i = 0; i < symbols.length - 1; i++) {
                String pair = symbols[i] + " " + symbols[i + 1];
                pairs.put(pair, pairs.getOrDefault(pair, 0) + freq);
            }
        }
        return pairs;
    }

    private void mergePair(String pair) {
        Map<String, Integer> newVocab = new HashMap<>();
        String replacement = pair.replace(" ", "");
        for (Map.Entry<String, Integer> entry : vocab.entrySet()) {
            String newWord = entry.getKey().replace(pair, replacement);
            newVocab.put(newWord, newVocab.getOrDefault(newWord, 0) + entry.getValue());
        }
        vocab = newVocab;
    }

    public void learn(int targetVocabSize) {
        while (true) {
            Set<String> uniqueSymbols = vocab.keySet().stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .collect(Collectors.toSet());
            if (uniqueSymbols.size() >= targetVocabSize) {
                break;
            }

            Map<String, Integer> pairStats = getPairStats();
            if (pairStats.isEmpty()) {
                break;
            }

            String bestPair = Collections.max(
                pairStats.entrySet(),
                Map.Entry.comparingByValue()
            ).getKey();

            mergePair(bestPair);
        }
    }

    public Map<String, Integer> getVocab() {
        return vocab;
    }

    public void printVocab() {
        for (String key : vocab.keySet()) {
            System.out.println(key + " : " + vocab.get(key));
        }
    }
}
