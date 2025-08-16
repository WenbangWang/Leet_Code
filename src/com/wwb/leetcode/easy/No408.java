package com.wwb.leetcode.easy;

/**
 * A string can be abbreviated by replacing any number of non-adjacent, non-empty substrings with their lengths. The lengths should not have leading zeros.
 * <p>
 * For example, a string such as "substitution" could be abbreviated as (but not limited to):
 * <p>
 * "s10n" ("s ubstitutio n")
 * <p>
 * "sub4u4" ("sub stit u tion")
 * <p>
 * "12" ("substitution")
 * <p>
 * "su3i1u2on" ("su bst i t u ti on")
 * <p>
 * "substitution" (no substrings replaced)
 * <p>
 * The following are not valid abbreviations:
 * <p>
 * "s55n" ("s ubsti tutio n", the replaced substrings are adjacent)
 * <p>
 * "s010n" (has leading zeros)
 * <p>
 * "s0ubstitution" (replaces an empty substring)
 * <p>
 * Given a string word and an abbreviation abbr, return whether the string matches the given abbreviation.
 * <p>
 * A substring is a contiguous non-empty sequence of characters within a string.
 * <p>
 * <p>
 * <p>
 * Example 1:
 * <p>
 * Input: word = "internationalization", abbr = "i12iz4n"
 * <p>
 * Output: true
 * <p>
 * Explanation: The word "internationalization" can be abbreviated as "i12iz4n" ("i nternational iz atio n").
 * <p>
 * Example 2:
 * <p>
 * Input: word = "apple", abbr = "a2e"
 * <p>
 * Output: false
 * <p>
 * Explanation: The word "apple" cannot be abbreviated as "a2e".
 * <p>
 * <p>
 * Constraints:
 * <p>
 * 1 <= word.length <= 20
 * word consists of only lowercase English letters.
 * 1 <= abbr.length <= 10
 * abbr consists of lowercase English letters and digits.
 * All the integers in abbr will fit in a 32-bit integer.
 */
public class No408 {
    public boolean validWordAbbreviation(String word, String abbreviation) {
        if (abbreviation.length() > word.length()) {
            return false;
        }
        int wordPtr = 0;
        int abbrPtr = 0;

        while (wordPtr < word.length()) {
            char abbrChar = abbreviation.charAt(abbrPtr);
            char wordChar = word.charAt(wordPtr);
            if (Character.isAlphabetic(abbrChar)) {
                if (abbrChar != wordChar) {
                    return false;
                }
                wordPtr++;
                abbrPtr++;
                continue;
            }

            if (Character.isDigit(abbrChar)) {
                if (abbrChar == '0') {
                    return false;
                }

                int start = abbrPtr;
                for (;abbrPtr < abbreviation.length(); abbrPtr++) {
                    if (!Character.isDigit(abbreviation.charAt(abbrPtr))) {
                        break;
                    }
                }
                int wordAbbrLength = Integer.parseInt(abbreviation.substring(start, abbrPtr));

                if (wordPtr + wordAbbrLength >= word.length()) {
                    return false;
                }

                wordPtr += wordAbbrLength;
            }

            return false;
        }

        return abbrPtr == abbreviation.length();
    }
}
