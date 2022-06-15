package com.wwb.leetcode.medium;

/**
 * Implement a trie with insert, search, and startsWith methods.
 *
 * Note:
 * You may assume that all inputs are consist of lowercase letters a-z.
 */
public class No208 {

static class TrieNode {
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

public static class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode(' ');
    }

    // Inserts a word into the trie.
    public void insert(String word) {
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

    // Returns if the word is in the trie.
    public boolean search(String word) {
        if(word == null) {
            return false;
        }

        TrieNode node = root;

        for(char c : word.toCharArray()) {
            TrieNode child = node.getChild(c - 'a');

            if(child == null) {
                return false;
            }

            node = child;
        }

        return node.isWord();
    }

    // Returns if there is any word in the trie
    // that starts with the given prefix.
    public boolean startsWith(String prefix) {
        if(prefix == null) {
            return false;
        }

        TrieNode node = root;

        for(char c : prefix.toCharArray()) {
            TrieNode child = node.getChild(c - 'a');

            if(child == null) {
                return false;
            }

            node = child;
        }

        return true;
    }
}

// Your Trie object will be instantiated and called as such:
// Trie trie = new Trie();
// trie.insert("somestring");
// trie.search("key");
}
