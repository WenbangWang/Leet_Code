package com.wwb.leetcode.hard;

public class No358 {

    public String rearrangeString(String str, int k) {
        if (str == null || str.isEmpty() || k <= 0) {
            return "";
        }

        int[] counts = new int[26];
        int[] positions = new int[26];
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : str.toCharArray()) {
            counts[c - 'a']++;
        }

        for (int offset = 0, length = str.length(); offset < length; offset++) {
            int maxPosition = findNextMaxPosition(counts, positions, offset);

            if (maxPosition == -1) {
                return "";
            }

            counts[maxPosition]--;
            positions[maxPosition] = maxPosition + offset;

            stringBuilder.append((char) ('a' + maxPosition));
        }

        return stringBuilder.toString();
    }

    private int findNextMaxPosition(int[] counts, int[] positions, int offset) {
        int max = Integer.MIN_VALUE;
        int maxPosition = -1;

        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0 && counts[i] > max && offset >= positions[i]) {
                max = counts[i];
                maxPosition = i;
            }
        }

        return maxPosition;
    }
}
