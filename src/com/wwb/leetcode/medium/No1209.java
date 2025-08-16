package com.wwb.leetcode.medium;

import java.util.Deque;
import java.util.LinkedList;

/**
 * You are given a string s and an integer k, a k duplicate removal consists of choosing k adjacent and equal letters from s and removing them, causing the left and the right side of the deleted substring to concatenate together.
 * <p>
 * We repeatedly make k duplicate removals on s until we no longer can.
 * <p>
 * Return the final string after all such duplicate removals have been made. It is guaranteed that the answer is unique.
 * <p>
 *
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: s = "abcd", k = 2
 * Output: "abcd"
 * Explanation: There's nothing to delete.
 * Example 2:
 *
 * Input: s = "deeedbbcccbdaa", k = 3
 * Output: "aa"
 * Explanation:
 * First delete "eee" and "ccc", get "ddbbbdaa"
 * Then delete "bbb", get "dddaa"
 * Finally delete "ddd", get "aa"
 * Example 3:
 *
 * Input: s = "pbbcggttciiippooaais", k = 2
 * Output: "ps"
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 10^5
 * 2 <= k <= 10^4
 * s only contains lowercase English letters.
 * </pre>
 */
public class No1209 {
    public String removeDuplicates(String s, int k) {
        Deque<Node> queue = new LinkedList<>();

        for (char c : s.toCharArray()) {
            if (queue.isEmpty()) {
                queue.offer(new Node(c));
            } else {
                Node last = queue.peekLast();

                if (last.c == c) {
                    if (last.count < k - 1) {
                        queue.offer(new Node(c, last.count + 1));
                    } else {
                        for (int i = 0; i < last.count; i++) {
                            queue.removeLast();
                        }
                    }
                } else {
                    queue.offer(new Node(c));
                }
            }
        }

        StringBuilder result = new StringBuilder(queue.size());

        for (Node node : queue) {
            result.append(node.c);
        }

        return result.toString();
    }

    private static class Node {
        char c;
        int count;

        Node(char c, int count) {
            this.c = c;
            this.count = count;
        }

        Node(char c) {
            this(c, 1);
        }
    }
}
