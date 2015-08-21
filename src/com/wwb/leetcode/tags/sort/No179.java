package com.wwb.leetcode.tags.sort;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Given a list of non negative integers, arrange them such that they form the largest number.
 *
 * For example, given [3, 30, 34, 5, 9], the largest formed number is 9534330.
 *
 * Note: The result may be very large, so you need to return a string instead of an integer.
 */
public class No179 {

    public String largestNumber(int[] nums) {
        if(nums == null || nums.length == 0) {
            return "";
        }
        String[] numStrings = new String[nums.length];
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String string1 = o1 + o2;
                String string2 = o2 + o1;

                return string2.compareTo(string1);
            }
        };

        for(int i = 0; i < numStrings.length; i++) {
            numStrings[i] = nums[i] + "";
        }

        Arrays.sort(numStrings, comparator);

        if(numStrings[0].equals("0")) {
            return "0";
        }

        StringBuilder stringBuilder = new StringBuilder();

        for(String num : numStrings) {
            stringBuilder.append(num);
        }

        return stringBuilder.toString();
    }
}