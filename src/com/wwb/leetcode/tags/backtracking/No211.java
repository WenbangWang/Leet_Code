package com.wwb.leetcode.tags.backtracking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Design a data structure that supports the following two operations:
 *
 * void addWord(word)
 * bool search(word)
 * search(word) can search a literal word or a regular expression string containing only letters a-z or ".".
 * A "." means it can represent any one letter.
 *
 * For example:
 *
 * addWord("bad")
 * addWord("dad")
 * addWord("mad")
 * search("pad") -> false
 * search("bad") -> true
 * search(".ad") -> true
 * search("b..") -> true
 * Note:
 * You may assume that all words are consist of lowercase letters a-z.
 */
public class No211 {

public class WordDictionary {


    class TrieNode {
        private boolean isWord;
        private TrieNode[] children;
        private char value;
        // Initialize your data structure here.
        public TrieNode(char c) {
            this.isWord = false;
            this.children = new TrieNode[26];
            this.value = c;
        }

        public boolean isWord() {
            return this.isWord;
        }

        public void setWord(boolean isWord) {
            this.isWord = isWord;
        }

        public TrieNode[] getChildren() {
            return this.children;
        }

        public TrieNode getChild(int i) {
            return this.children[i];
        }

        public void setChild(TrieNode c, int i) {
            children[i] = c;
        }

        public char getValue() {
            return this.value;
        }
    }

    private Map<Integer, Set<String>> map;
    private TrieNode root;

    public WordDictionary() {
        this.map = new HashMap<>();
        this.root = new TrieNode(' ');
    }

    public void addWord(String word) {
        TrieNode node = root;

        for(char c : word.toCharArray()) {
            int index = c - 'a';
            TrieNode child = node.getChild(index);

            if(child == null) {
                child = new TrieNode(c);
                node.setChild(child, index);
            }

            node = child;
        }

        node.setWord(true);
    }

    public boolean search(String word) {
        return this.search(word.toCharArray(), 0, this.root);
    }

    private boolean search(char[] charArray, int index, TrieNode parent) {
        if(index == charArray.length) {
            return parent.isWord();
        }

        char c = charArray[index];

        if(c == '.') {
            for(TrieNode child : parent.getChildren()) {
                if(child != null && search(charArray, index + 1, child)) {
                    return true;
                }
            }

            return false;
        }

        TrieNode node = parent.getChild(c - 'a');

        if(node == null) {
            return false;
        }

        return search(charArray, index + 1, node);
    }

    // Adds a word into the data structure.
    public void addWord1(String word) {
        if(word == null) {
            return;
        }
        int length = word.length();
        Set<String> wordSet = this.map.get(length);

        if(wordSet == null) {
            wordSet = new HashSet<>();
            wordSet.add(word);
            this.map.put(length, wordSet);
        } else {
            if(!wordSet.contains(word)) {
                wordSet.add(word);
            }
        }
    }

    // Returns if the word is in the data structure. A word could
    // contain the dot character '.' to represent any one letter.
    public boolean search1(String word) {
        if(word == null) {
            return false;
        }
        int length = word.length();
        Set<String> wordSet = this.map.get(length);

        if(wordSet == null) {
            return false;
        }

        if(wordSet.contains(word)) {
            return true;
        }

        char[] charArray = word.toCharArray();

        for(String existingWord : wordSet) {
            char[] existingWordCharArray = existingWord.toCharArray();
            int count = 0;

            for(int i = 0; i < length; i++) {
                if(charArray[i] == '.' || charArray[i] == existingWordCharArray[i]) {
                    count++;
                }
            }

            if(count == length) {
                return true;
            }
        }

        return false;
    }
}

// Your WordDictionary object will be instantiated and called as such:
// WordDictionary wordDictionary = new WordDictionary();
// wordDictionary.addWord("word");
// wordDictionary.search("pattern");
}