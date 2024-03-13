package com.wwb.leetcode.hard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Given an array of words and a length L,
 * format the text such that each line has exactly L characters and is fully (left and right) justified.
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
                int slots = last - index;
                if(currentLength + lastLength + slots > maxWidth) {
                    break;
                }
                currentLength += lastLength;
                last++;
            }

            StringBuilder stringBuilder = new StringBuilder(maxWidth);
            int slots = last - index - 1;

            // last row or a single word consists a row
            if(last == length || slots == 0) {
                stringBuilder.append(Arrays.stream(words, index, last).collect(Collectors.joining(" ")));

                stringBuilder.append(" ".repeat(maxWidth - stringBuilder.length()));
            } else {
                int numberOfSpaces = (maxWidth - currentLength) / slots;
                int numberOfExtraSpaces = (maxWidth - currentLength) % slots;

                for(int i = index; i < last; i++) {
                    stringBuilder.append(words[i]);

                    if(i < last - 1) {
                        boolean shouldAppendOneMoreSpace = (i - index) < numberOfExtraSpaces;

                        stringBuilder.append(" ".repeat(numberOfSpaces + (shouldAppendOneMoreSpace ? 1 : 0)));
                    }
                }
            }

            rows.add(stringBuilder.toString());
            index = last;
        }

        return rows;
    }
}
