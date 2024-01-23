package com.wwb.leetcode.tags.string;

import java.util.HashSet;
import java.util.Set;

/**
 * International Morse Code defines a standard encoding where each letter is mapped to a series of dots and dashes,
 * as follows:
 * <p>
 * 'a' maps to ".-",
 * 'b' maps to "-...",
 * 'c' maps to "-.-.", and so on.
 * For convenience, the full table for the 26 letters of the English alphabet is given below:
 * <p>
 * [".-","-...","-.-.","-..",".","..-.","--.","....","..",".---","-.-",".-..","--","-.",
 * "---",".--.","--.-",".-.","...","-","..-","...-",".--","-..-","-.--","--.."]
 * Given an array of strings words where each word can be written as a concatenation of the Morse code of each letter.
 * <p>
 * For example, "cab" can be written as "-.-..--...", which is the concatenation of "-.-.", ".-", and "-...".
 * We will call such a concatenation the transformation of a word.
 * Return the number of different transformations among all words we have.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: words = ["gin","zen","gig","msg"]
 * Output: 2
 * Explanation: The transformation of each word is:
 * "gin" -> "--...-."
 * "zen" -> "--...-."
 * "gig" -> "--...--."
 * "msg" -> "--...--."
 * There are 2 different transformations: "--...-." and "--...--.".
 * Example 2:
 * <p>
 * Input: words = ["a"]
 * Output: 1
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= words.length <= 100
 * 1 <= words[i].length <= 12
 * words[i] consists of lowercase English letters.
 */
public class No804 {
    public int uniqueMorseRepresentations(String[] words) {
        String[] morseCodes = new String[]{
            ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--",
            "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."
        };
        Set<String> encodedWords = new HashSet<>();

        for (String word : words) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < word.length(); i++) {
                sb.append(morseCodes[word.charAt(i) - 'a']);
            }

            encodedWords.add(sb.toString());
        }

        return encodedWords.size();
    }
}
