package com.wwb.leetcode.tags.hashtable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Given a string s which consists of lowercase or uppercase letters,
 * return the length of the longest palindrome that can be built with those letters.
 *
 * Letters are case sensitive, for example, "Aa" is not considered a palindrome here.
 *
 *
 *
 * Example 1:
 *
 * Input: s = "abccccdd"
 * Output: 7
 * Explanation: One longest palindrome that can be built is "dccaccd", whose length is 7.
 * Example 2:
 *
 * Input: s = "a"
 * Output: 1
 * Explanation: The longest palindrome that can be built is "a", whose length is 1.
 *
 *
 * Constraints:
 *
 * 1 <= s.length <= 2000
 * s consists of lowercase and/or uppercase English letters only.
 */
public class No409 {
    public int longestPalindrome(String s) {
        return solution1(s);
    }

    private int solution1(String s) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);

        int count = 1;
        char c = chars[0];
        int result = 0;

        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == c) {
                count++;
                continue;
            }

            if (count % 2 == 0) {
                result += count;
            } else {
                if (result % 2 == 0) {
                    result += count;
                } else if (count > 2) {
                    result += (count - 1);
                }
            }
            c = chars[i];
            count = 1;
        }

        if (count % 2 == 0) {
            result += count;
        } else {
            if (result % 2 == 0) {
                result += count;
            } else if (count > 2) {
                result += (count - 1);
            }
        }


        return result;
    }

    private int solution2(String s) {
        Set<Character> set = new HashSet<>();
        for (char c : s.toCharArray()) {
            if (!set.add(c)) {
                set.remove(c);
            }
        }

        int odd = set.size();
        return s.length() - (odd == 0 ? 0 : odd - 1);
    }
}
