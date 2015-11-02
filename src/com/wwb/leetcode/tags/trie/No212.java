package com.wwb.leetcode.tags.trie;

import java.util.*;

/**
 * Given a 2D board and a list of words from the dictionary, find all words in the board.
 *
 * Each word must be constructed from letters of sequentially adjacent cell,
 * where "adjacent" cells are those horizontally or vertically neighboring.
 * The same letter cell may not be used more than once in a word.
 *
 * For example,
 * Given words = ["oath","pea","eat","rain"] and board =
 *
 * [
 *     ['o','a','a','n'],
 *     ['e','t','a','e'],
 *     ['i','h','k','r'],
 *     ['i','f','l','v']
 * ]
 * Return ["eat","oath"].
 * Note:
 * You may assume that all inputs are consist of lowercase letters a-z.
 */
public class No212 {

    public List<String> findWords(char[][] board, String[] words) {
        if(words == null || words.length == 0) {
            return Collections.emptyList();
        }

        Trie trie = new Trie();
        Set<String> resultSet = new HashSet<>();
        boolean[][] visited = new boolean[board.length][board[0].length];

        for(String word : words) {
            trie.addWord(word);
        }

        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                findWord(board, visited, i, j, trie.root, "", resultSet);
            }
        }

        return new ArrayList<>(resultSet);
    }

    private void findWord(char[][] board, boolean[][] visited, int x, int y, TrieNode node, String string, Set<String> resultSet) {
        if(x < 0 || y < 0 || x >= board.length || y >= board[0].length || visited[x][y]) {
            return;
        }

        string += board[x][y];
        TrieNode child = node.children[board[x][y] - 'a'];

        if(child == null) {
            return;
        }

        if(child.isWord) {
            resultSet.add(string);
        }

        visited[x][y] = true;

        findWord(board, visited, x - 1, y, child, string, resultSet);
        findWord(board, visited, x + 1, y, child, string, resultSet);
        findWord(board, visited, x, y - 1, child, string, resultSet);
        findWord(board, visited, x, y + 1, child, string, resultSet);

        visited[x][y] = false;
    }

    private class Trie {
        TrieNode root;

        Trie() {
            this.root = new TrieNode(' ');
        }

        void addWord(String s) {
            TrieNode node = this.root;

            for(char c : s.toCharArray()) {
                int index = c - 'a';
                TrieNode child = node.children[index];

                if(child == null) {
                    child = new TrieNode(c);
                    node.children[index] = child;
                }

                node = child;
            }

            node.isWord = true;
        }

        boolean search(String s) {
            TrieNode node = this.root;

            for(char c : s.toCharArray()) {
                int index = c - 'a';
                TrieNode child = node.children[index];

                if(child == null) {
                    return false;
                }

                node = child;
            }

            return node.isWord;
        }

        boolean startWith(String s) {
            TrieNode node = this.root;

            for(char c : s.toCharArray()) {
                int index = c - 'a';
                TrieNode child = node.children[index];

                if(child == null) {
                    return false;
                }

                node = child;
            }

            return true;
        }
    }

    private class TrieNode {
        TrieNode[] children;
        boolean isWord;
        char value;
        TrieNode(char c) {
            this.children = new TrieNode[26];
            this.value = c;
        }
    }
}
