package com.wwb.leetcode.easy;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * You are given a string s consisting of lowercase English letters. A duplicate removal consists of choosing two adjacent and equal letters and removing them.
 * <p>
 * We repeatedly make duplicate removals on s until we no longer can.
 * <p>
 * Return the final string after all such duplicate removals have been made. It can be proven that the answer is unique.
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: s = "abbaca"
 * Output: "ca"
 * Explanation:
 * For example, in "abbaca" we could remove "bb" since the letters are adjacent and equal, and this is the only possible move.  The result of this move is that the string is "aaca", of which only "aa" is possible, so the final string is "ca".
 * Example 2:
 *
 * Input: s = "azxxzy"
 * Output: "ay"
 * </pre>
 *
 *
 * <pre>
 * Constraints:
 *
 * 1 <= s.length <= 10^5
 * s consists of lowercase English letters.
 * </pre>
 */
public class No1047 {
    public String removeDuplicates(String s) {
        Deque<Character> queue = new ArrayDeque<>();

        for (char c : s.toCharArray()) {
            if (queue.isEmpty()) {
                queue.offer(c);
            } else {
                if (queue.peekLast() == c) {
                    queue.removeLast();
                } else {
                    queue.offer(c);
                }
            }
        }
        StringBuilder result = new StringBuilder(s);

        while (!queue.isEmpty()) {
            result.append(queue.removeFirst());
        }

        return result.toString();
    }
}
