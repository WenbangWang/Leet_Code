package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given an array of words and a length L, format the text such that each line has exactly L characters and is fully (left and right) justified.
 *
 * You should pack your words in a greedy approach; that is, pack as many words as you can in each line.
 * Pad extra spaces ' ' when necessary so that each line has exactly L characters.
 *
 * Extra spaces between words should be distributed as evenly as possible.
 * If the number of spaces on a line do not divide evenly between words,
 * the empty slots on the left will be assigned more spaces than the slots on the right.
 *
 * For the last line of text, it should be left justified and no extra space is inserted between words.
 *
 * For example,
 * words: ["This", "is", "an", "example", "of", "text", "justification."]
 * L: 16.
 *
 * Return the formatted lines as:
 * [
 *     "This    is    an",
 *     "example  of text",
 *     "justification.  "
 * ]
 * Note: Each word is guaranteed not to exceed L in length.
 */
public class No68 {

    public List<String> fullJustify(String[] words, int maxWidth) {
        if(words == null || words.length == 0) {
            return Collections.emptyList();
        }

        List<String> rows = new ArrayList<>();
        int index = 0;
        int length = words.length;

        while(index < length) {
            int currentLength = words[index].length();
            int last = index + 1;

            while(last < length) {
                int lastLength = words[last].length();
                if(currentLength + lastLength + 1 > maxWidth) {
                    break;
                }
                currentLength += lastLength + 1;
                last++;
            }

            StringBuilder stringBuilder = new StringBuilder();
            int slots = last - index - 1;

            if(last == length || slots == 0) {
                for(int i = index; i < last; i++) {
                    stringBuilder.append(words[i] + " ");
                }

                stringBuilder.deleteCharAt(stringBuilder.length() - 1);

                for(int i = stringBuilder.length(); i < maxWidth; i++) {
                    stringBuilder.append(" ");
                }
            } else {
                int numberOfSpaces = (maxWidth - currentLength) / slots;
                int numberOfExtraSpaces = (maxWidth - currentLength) % slots;

                for(int i = index; i < last; i++) {
                    stringBuilder.append(words[i]);

                    if(i < last - 1) {
                        for(int j = 0; j <= (numberOfSpaces + ((i - index) < numberOfExtraSpaces ? 1 : 0)); j++) {
                            stringBuilder.append(" ");
                        }
                    }
                }
            }

            rows.add(stringBuilder.toString());
            index = last;
        }

        return rows;
    }
}
