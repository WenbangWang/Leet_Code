package com.wwb.leetcode.medium;

/**
 * Given a string S, find the longest palindromic substring in S.
 * You may assume that the maximum length of S is 1000, and there exists one unique longest palindromic substring.
 */
public class No5 {

    /**
     * So, a palindrome of length 100 (for example), will have a palindrome of
     * <p>
     * length 98 inside it, and one of length 96, ... 50, ... 4, and 2.
     * <p>
     * Because of this, we can move across our string, checking if the current
     * <p>
     * place is a palindrome of a particular length (the longest length palindrome
     * <p>
     * found so far + 1), and if it is, update the longest length, and move forward.
     * <p>
     * In this way, we find our longest palindromes "from the inside out", starting
     * <p>
     * with length x, then x+2, x+4, ...
     * <p>
     * Example:
     * <p>
     * "xxABCDCBAio"
     * <p>
     * 0123456789  < indexes
     * <p>
     * As we scan our string, we initially find a palindrome of length 2 (xx)
     * <p>
     * We always look backwards!
     * <p>
     * When we get to index 2,3,4, we see no length 3+ palindrome ending there.
     * <p>
     * But when we get to index 6, looking back 3 characters, we see "CDC"! So our
     * <p>
     * longest palindrome is now length 3.
     * <p>
     * At index 7, we look back and see no length 4 palindromes, but find one of
     * <p>
     * length 5 ("BCDCB").
     * <p>
     * And finally, by i = 8, we find the full "ABCDCBA"
     */
    public String longestPalindrome(String s) {
        int length = s.length();
        int longestLength = 0;
        int longestIndex = 0;

        for (int currentIndex = 0; currentIndex < length; currentIndex++) {
            if (isPalindrome(s, currentIndex - longestLength, currentIndex)) {
                longestLength++;
                longestIndex = currentIndex;
            } else if (currentIndex - longestLength - 1 >= 0 && isPalindrome(
                s,
                currentIndex - longestLength - 1,
                currentIndex
            ))
            {
                longestLength += 2;
                longestIndex = currentIndex;
            }
        }

        longestIndex++;
        return s.substring(longestIndex - longestLength, longestIndex);
    }

    private boolean isPalindrome(String str, int start, int end) {
        while (start <= end) {
            if (str.charAt(start) != str.charAt(end)) {
                return false;
            }

            start++;
            end--;
        }

        return true;
    }
}
