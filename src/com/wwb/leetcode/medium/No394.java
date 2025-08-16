package com.wwb.leetcode.medium;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Given an encoded string, return its decoded string.
 * <p>
 * The encoding rule is: k[encoded_string], where the encoded_string inside the square brackets is being repeated exactly k times. Note that k is guaranteed to be a positive integer.
 * <p>
 * You may assume that the input string is always valid; there are no extra white spaces, square brackets are well-formed, etc. Furthermore, you may assume that the original data does not contain any digits and that digits are only for those repeat numbers, k. For example, there will not be input like 3a or 2[4].
 * <p>
 * The test cases are generated so that the length of the output will never exceed 105.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: s = "3[a]2[bc]"
 * Output: "aaabcbc"
 * Example 2:
 *
 * Input: s = "3[a2[c]]"
 * Output: "accaccacc"
 * Example 3:
 *
 * Input: s = "2[abc]3[cd]ef"
 * Output: "abcabccdcdcdef"
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 30
 * s consists of lowercase English letters, digits, and square brackets '[]'.
 * s is guaranteed to be a valid input.
 * All the integers in s are in the range [1, 300].
 * </pre>
 */
public class No394 {
    public String decodeString(String s) {
        StringBuilder result = new StringBuilder();
        Deque<StringBuilder> queue = new ArrayDeque<>();
        char[] charArray = s.toCharArray();
        int index = 0;
        StringBuilder num = new StringBuilder();

        queue.offer(new StringBuilder());

        while (index < charArray.length) {
            char c = charArray[index];

            if (Character.isDigit(c)) {
                num.append(c);
                index++;
                continue;
            }

            if (c == '[') {
                // skip [
                index++;
                queue.offer(num);
                // use for append the pattern
                queue.offer(new StringBuilder());

                num = new StringBuilder();

                continue;
            }

            if (c == ']') {
                StringBuilder pattern = queue.pollLast();
                StringBuilder patternCount = queue.pollLast();
                queue.peekLast().append(pattern.toString().repeat(Integer.parseInt(patternCount.toString())));
                // skip ]
                index++;
                continue;
            }

            queue.peekLast().append(c);
            index++;
        }

        queue.forEach(result::append);

        return result.toString();
    }
}
