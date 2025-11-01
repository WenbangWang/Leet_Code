package com.wwb.leetcode.other.anthropic.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BPETokenizer {
    private final Map<String, Integer> vocab;

    public BPETokenizer(Map<String, Integer> vocab) {
        this.vocab = vocab;
    }

    public List<String> tokenizeWord(String word) {
        word += "</w>";

        //return tokenizeRecursive(word);
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < word.length()) {
            String match = null;
            int maxLen = 0;

            for (int j = word.length(); j > i; j--) {
                String sub = word.substring(i, j);
                if (vocabContainsToken(sub)) {
                    match = sub;
                    maxLen = sub.length();
                    break;
                }
            }

            if (match == null) {
                match = word.substring(i, i + 1);
                maxLen = 1;
            }

            tokens.add(match);
            i += maxLen;
        }
        return tokens;
    }

    public List<String> tokenizeSentence(String sentence) {
        String[] words = sentence.split("\\s+");
        List<String> allTokens = new ArrayList<>(words.length);
        for (String word : words) {
            allTokens.addAll(tokenizeWord(word));
        }
        return allTokens;
    }

    public String detokenize(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (token.endsWith("</w>")) {
                sb.append(token.replace("</w>", ""));
                sb.append(" "); // word boundary
            } else {
                sb.append(token);
            }
        }
        return sb.toString().trim();
    }

    private List<String> tokenizeRecursive(String text) {
        List<String> tokens = new ArrayList<>();
        int n = text.length();

        if (vocabContainsToken(text)) {
            tokens.add(text);
            return tokens;
        }

        // Try greedy longest-match
        for (int end = n; end > 0; end--) {
            String sub = text.substring(0, end);
            if (vocabContainsToken(sub)) {
                tokens.add(sub);
                tokens.addAll(tokenizeRecursive(text.substring(end)));
                return tokens;
            }
        }

        // If no subword found, fallback to single character
        tokens.add(text.substring(0, 1));
        if (n > 1) {
            tokens.addAll(tokenizeRecursive(text.substring(1)));
        }
        return tokens;
    }

    private boolean vocabContainsToken(String token) {
        for (String v : vocab.keySet()) {
            String cleaned = v.replace(" ", "").replace("</w>", "");
            if (cleaned.equals(token)) return true;
        }
        return false;
    }
}
