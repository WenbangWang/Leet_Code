package com.wwb.leetcode.tags.twopointers;

/**
 * Implement strStr().
 *
 * Returns the index of the first occurrence of needle in haystack, or -1 if needle is not part of haystack.
 */
public class No28 {

    public int strStr(String haystack, String needle) {
//        return solution1(haystack, needle);
        return solution2(haystack, needle);

    }

    private int solution1(String haystack, String needle) {
        char[] haystackArray = haystack.toCharArray();
        char[] needleArray = needle.toCharArray();
        int haystackLength = haystackArray.length;
        int needleLength = needleArray.length;

        if(haystackLength < needleLength) {
            return -1;
        }

        if(needleLength == 0) {
            return 0;
        }

        for(int i = 0; i < haystackLength; i++) {
            int j = 0;

            while(j < needleLength && ((haystackLength - i) >= needleLength)) {
                char haystackChar = haystackArray[i + j];
                char needleChar = needleArray[j];

                if(haystackChar == needleChar) {
                    j++;
                } else {
                    break;
                }
            }

            if(j == needleLength) {
                return i;
            }
        }

        return -1;
    }

    private int solution2(String haystack, String needle) {
        char[] haystackArray = haystack.toCharArray();
        char[] needleArray = needle.toCharArray();
        int haystackLength = haystackArray.length;
        int needleLength = needleArray.length;
        int i = 0;
        int j = 0;

        if(haystackLength < needleLength) {
            return -1;
        }

        if(needleLength == 0) {
            return 0;
        }

        int[] matchTable = generateMatchTable(needle);

        while(i + j < haystackLength) {
            if(needleArray[j] == haystackArray[i + j]) {
                if(j == needleLength - 1) {
                    return i;
                }
                j++;
            } else {
                if(matchTable[j] > -1) {
                    i = i + j - matchTable[j];
                    j = matchTable[j];
                } else {
                    i++;
                    j = 0;
                }
            }
        }

        return -1;
    }

    private int[] generateMatchTable(String string) {
        char[] charArray = string.toCharArray();
        int[] matchTable = new int[charArray.length];
        int position = 2;
        int index = 0;
        matchTable[0] = -1;
        if(matchTable.length > 1) {
            matchTable[1] = 0;
        }

        while(position < matchTable.length) {
            if(charArray[position - 1] == charArray[index]) {
                matchTable[position++] = ++index;
            } else if(index > 0) {
                index = matchTable[index];
            } else {
                matchTable[position++] = 0;
            }
        }

        return matchTable;
    }
}