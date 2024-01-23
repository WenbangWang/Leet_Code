package com.wwb.leetcode.tags.string;

/**
 * Design a special dictionary that searches the words in it by a prefix and a suffix.
 *
 * Implement the WordFilter class:
 *
 * WordFilter(string[] words) Initializes the object with the words in the dictionary.
 * f(string pref, string suff) Returns the index of the word in the dictionary,
 * which has the prefix pref and the suffix suff. If there is more than one valid index,
 * return the largest of them. If there is no such word in the dictionary, return -1.
 *
 *
 * Example 1:
 *
 * Input
 * ["WordFilter", "f"]
 * [[["apple"]], ["a", "e"]]
 * Output
 * [null, 0]
 * Explanation
 * WordFilter wordFilter = new WordFilter(["apple"]);
 * wordFilter.f("a", "e"); // return 0, because the word at index 0 has prefix = "a" and suffix = "e".
 *
 *
 * Constraints:
 *
 * 1 <= words.length <= 10^4
 * 1 <= words[i].length <= 7
 * 1 <= pref.length, suff.length <= 7
 * words[i], pref and suff consist of lowercase English letters only.
 * At most 10^4 calls will be made to the function f.
 */
public class No745 {
    public static class WordFilter {
        Trie trie;

        public WordFilter(String[] words) {
            this.trie = new Trie();

            for (int i = 0; i < words.length; i++) {
                this.trie.add(words[i], i);
            }
        }

        public int f(String prefix, String suffix) {
            Node node = this.trie.root;
            for (char c: (suffix + '{' + prefix).toCharArray()) {
                if (node.children[c - 'a'] == null) {
                    return -1;
                }
                node = node.children[c - 'a'];
            }
            return node.weight;
        }


        static class Trie {
            Node root;

            Trie() {
                this.root = new Node();
            }

            void add(String word, int index) {
                word += "{";
                for (int i = 0; i < word.length(); i++) {
                    Node node = root;
                    node.weight = index;
                    // add "apple{apple", "pple{apple", "ple{apple", "le{apple", "e{apple", "{apple" into the Trie Tree
                    for (int j = i; j < 2 * word.length() - 1; j++) {
                        int k = word.charAt(j % word.length()) - 'a';
                        if (node.children[k] == null) {
                            node.children[k] = new Node();
                        }
                        node = node.children[k];
                        node.weight = index;
                    }
                }
            }
        }

        static class Node {
            int weight;
            Node[] children;

            Node() {
                this.weight = 0;
                // 'a' - 'z' and '{'. 'z' and '{' are neighbours in ASCII table
                this.children = new Node[27];
            }
        }
    }
    /**
     * Your WordFilter object will be instantiated and called as such:
     * WordFilter obj = new WordFilter(words);
     * int param_1 = obj.f(pref,suff);
     */

}
